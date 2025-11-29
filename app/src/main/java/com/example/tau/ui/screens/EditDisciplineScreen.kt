package com.example.tau.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.tau.data.DisciplineRequest
import com.example.tau.data.api.RetrofitClient
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import com.example.tau.ui.components.ErrorDialog
import com.example.tau.ui.components.LoadingDialog
import com.example.tau.ui.components.NavTopBar
import kotlinx.coroutines.launch

@Composable
fun EditDisciplineScreen(
    disciplineId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var selectedColorName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showColorDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val colorOptions = listOf(
        "Rosa" to Color(0xFFFFB3D9),
        "Vermelho" to Color(0xFFFF6B6B),
        "Laranja" to Color(0xFFFFD166),
        "Amarelo" to Color(0xFFFFE5B4),
        "Verde" to Color(0xFFA8D8B8),
        "Menta" to Color(0xFF98FF98),
        "Azul" to Color(0xFF74B9FF),
        "Ciano" to Color(0xFF6FD7E6),
        "Roxo" to Color(0xFFDDA0DD),
        "Lavanda" to Color(0xFFE6D7FF),
        "Salmão" to Color(0xFFFFB3BA),
        "Pêssego" to Color(0xFFFFCBB1)
    )

    // Carregar dados da disciplina
    LaunchedEffect(disciplineId) {
        isLoadingData = true
        try {
            val userId = UserDao(context).getUserId()
            if (userId == null) {
                errorMessage = "Sessão expirada. Faça login novamente"
                isLoadingData = false
                return@LaunchedEffect
            }

            val disciplineRepository = com.example.tau.data.repository.DisciplineRepository(context)
            val discipline = disciplineRepository.getDisciplineByLocalId(disciplineId.toLong())
            if (discipline != null) {
                title = discipline.title
                teacher = discipline.teacher
                room = discipline.room
                selectedColorName = discipline.color
            } else {
                errorMessage = "Disciplina não encontrada"
            }
        } catch (e: Exception) {
            errorMessage = "Erro ao carregar disciplina: ${e.message}"
            e.printStackTrace()
        } finally {
            isLoadingData = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NavTopBar(
                title = "Editar Disciplina",
                onBackClick = onBackClick
            ) {
                Row {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Deletar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    val saveIconTint = if (title.isNotBlank()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceContainerHighest
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() && !isLoading) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val userId = UserDao(context).getUserId()
                                        if (userId == null) {
                                            errorMessage = "Sessão expirada. Faça login novamente"
                                            isLoading = false
                                            return@launch
                                        }

                                        val disciplineRepository = com.example.tau.data.repository.DisciplineRepository(context)
                                        disciplineRepository.updateDiscipline(
                                            localId = disciplineId.toLong(),
                                            userId = userId,
                                            name = title,
                                            teacher = teacher.ifBlank { "" },
                                            room = room.ifBlank { "" },
                                            color = selectedColorName.ifBlank { "" }
                                        )
                                        onSaveClick()
                                    } catch (e: Exception) {
                                        errorMessage = "Erro ao atualizar: ${e.message ?: "Verifique sua internet"}"
                                        e.printStackTrace()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = title.isNotBlank() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = Strings.SAVE_BUTTON,
                            tint = saveIconTint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoadingData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("${Strings.DISCIPLINE_TITLE_LABEL} *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text(Strings.DISCIPLINE_TEACHER_LABEL) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text(Strings.DISCIPLINE_ROOM_LABEL) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable(enabled = true, onClick = { showColorDialog = true })
                        .background(Color.Transparent, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (selectedColorName.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.Transparent, RoundedCornerShape(4.dp))
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = Strings.DISCIPLINE_COLOR_LABEL,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        colorOptions.find { it.first == selectedColorName }?.second
                                            ?: Color.Gray,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedColorName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }

        if (showColorDialog) {
            AlertDialog(
                onDismissRequest = { showColorDialog = false },
                title = { Text(Strings.SELECT_COLOR, color = Color.White) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        colorOptions.chunked(3).forEach { colorRow ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                colorRow.forEach { (colorName, colorValue) ->
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .weight(1f)
                                            .clickable {
                                                selectedColorName = colorName
                                                showColorDialog = false
                                            }
                                            .background(colorValue, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (selectedColorName == colorName) "✓" else "",
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showColorDialog = false }) {
                        Text("Fechar", color = Color.White)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar exclusão", color = Color.White) },
                text = {
                    Text(
                        "Tem certeza que deseja excluir esta disciplina? Esta ação não pode ser desfeita.",
                        color = Color.White
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            isLoading = true
                            scope.launch {
                                try {
                                    val disciplineRepository = com.example.tau.data.repository.DisciplineRepository(context)
                                    disciplineRepository.deleteDiscipline(disciplineId.toLong())
                                    onDeleteClick()
                                } catch (e: Exception) {
                                    errorMessage = "Erro ao deletar: ${e.message}"
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Text("Deletar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar", color = Color.White)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (isLoading) {
            LoadingDialog(
                title = "Processando...",
                message = "Aguarde"
            )
        }

        errorMessage?.let { message ->
            ErrorDialog(
                message = message,
                onDismiss = { errorMessage = null }
            )
        }
    }
}


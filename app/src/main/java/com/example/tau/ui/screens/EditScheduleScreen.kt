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
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.tau.data.ScheduleRequest
import com.example.tau.data.api.RetrofitClient
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import com.example.tau.ui.components.ErrorDialog
import com.example.tau.ui.components.LoadingDialog
import com.example.tau.ui.components.NavTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EditScheduleScreen(
    scheduleId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(0) }
    var disciplineId by remember { mutableStateOf(0) }
    var disciplineName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scheduleId) {
        isLoadingData = true
        try {
            val userId = UserDao(context).getUserId()
            if (userId == null) {
                errorMessage = "Sessão expirada. Faça login novamente"
                isLoadingData = false
                return@LaunchedEffect
            }

            val scheduleRepository = com.example.tau.data.repository.ScheduleRepository(context)
            val schedule = scheduleRepository.getScheduleByLocalId(scheduleId.toLong())
            if (schedule != null) {
                startTime = schedule.startTime
                endTime = schedule.endTime
                dayOfWeek = schedule.dayOfWeek
                disciplineId = schedule.disciplineId
                disciplineName = schedule.disciplineName
            } else {
                errorMessage = "Horário não encontrado"
            }
        } catch (e: Exception) {
            errorMessage = "Erro ao carregar horário: ${e.message}"
            e.printStackTrace()
        } finally {
            isLoadingData = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NavTopBar(
                title = "Editar Horário",
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
                    IconButton(
                        onClick = {
                            if (!isLoading) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val userId = UserDao(context).getUserId()
                                        if (userId == null) {
                                            errorMessage = "Sessão expirada. Faça login novamente"
                                            isLoading = false
                                            return@launch
                                        }

                                        val scheduleRepository = com.example.tau.data.repository.ScheduleRepository(context)
                                        val db = com.example.tau.data.local.AppDatabase(context)
                                        val localSchedules = db.getAllSchedules(userId)
                                        val localSchedule = localSchedules.find { it.id.toString() == scheduleId }

                                        if (localSchedule != null) {
                                            scheduleRepository.updateSchedule(
                                                localId = localSchedule.id,
                                                userId = userId,
                                                disciplineLocalId = localSchedule.disciplineId,
                                                dayOfWeek = dayOfWeek,
                                                startTime = startTime,
                                                endTime = endTime
                                            )
                                            onSaveClick()
                                        } else {
                                            errorMessage = "Horário não encontrado"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erro ao atualizar: ${e.message ?: "Verifique sua internet"}"
                                        e.printStackTrace()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = Strings.SAVE_BUTTON,
                            tint = MaterialTheme.colorScheme.onBackground,
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
                CircularProgressIndicator()
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
                    value = disciplineName,
                    onValueChange = {},
                    label = { Text("Disciplina") },
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
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    readOnly = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = getDayName(dayOfWeek),
                    onValueChange = {},
                    label = { Text("Dia da Semana") },
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
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    readOnly = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = formatTimeForDisplay(startTime),
                    onValueChange = {},
                    label = { Text("Hora de Início") },
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
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    readOnly = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = formatTimeForDisplay(endTime),
                    onValueChange = {},
                    label = { Text("Hora de Fim") },
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
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    readOnly = true
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar exclusão", color = Color.White) },
                text = {
                    Text(
                        "Tem certeza que deseja excluir este horário? Esta ação não pode ser desfeita.",
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
                                    val scheduleRepository = com.example.tau.data.repository.ScheduleRepository(context)
                                    scheduleRepository.deleteSchedule(scheduleId.toLong())
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

private fun getDayName(dayNumber: Int): String {
    return when (dayNumber) {
        0 -> "Domingo"
        1 -> "Segunda"
        2 -> "Terça"
        3 -> "Quarta"
        4 -> "Quinta"
        5 -> "Sexta"
        6 -> "Sábado"
        else -> "Dia $dayNumber"
    }
}

private fun formatTimeForDisplay(timeString: String): String {
    val formats = listOf(
        "HH:mm:ss.SSSSSS",
        "HH:mm:ss.SSS",
        "HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )

    for (format in formats) {
        try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            val date = sdf.parse(timeString)
            if (date != null) {
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                return outputFormat.format(date)
            }
        } catch (_: Exception) {
            continue
        }
    }
    return timeString
}

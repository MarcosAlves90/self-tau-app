package com.example.tau.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
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
import com.example.tau.data.Discipline
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import com.example.tau.ui.components.ErrorDialog
import com.example.tau.ui.components.LoadingDialog
import com.example.tau.ui.components.NavTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var completed by remember { mutableStateOf(false) }
    var disciplineName by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedDisciplineId by remember { mutableStateOf("") }
    var disciplines by remember { mutableStateOf<List<Discipline>>(emptyList()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDisciplineDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isLoadingData by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val datePickerState = rememberDatePickerState()

    val formattedDate = remember(selectedDateMillis) {
        selectedDateMillis?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(java.util.Date(it))
        } ?: ""
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text(Strings.DATE_PICKER_CONFIRM_BUTTON, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(Strings.DATE_PICKER_DISMISS_BUTTON, color = Color.White)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(taskId) {
        isLoadingData = true
        try {
            val userId = UserDao(context).getUserId()
            if (userId == null) {
                errorMessage = "Sessão expirada. Faça login novamente"
                return@LaunchedEffect
            }

            val taskRepository = com.example.tau.data.repository.TaskRepository(context)
            val task = taskRepository.getTaskByLocalId(taskId.toLong())

            if (task != null) {
                title = task.title
                description = task.description
                completed = task.completed
                selectedDateMillis = task.expirationDate
                selectedDisciplineId = task.disciplineId.toString()
                disciplineName = task.disciplineName
            } else {
                errorMessage = "Tarefa não encontrada"
            }

            val disciplineRepository = com.example.tau.data.repository.DisciplineRepository(context)
            disciplines = disciplineRepository.getDisciplinesLocal(userId)
        } catch (e: Exception) {
            errorMessage = "Erro ao carregar tarefa: ${e.message}"
        } finally {
            isLoadingData = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NavTopBar(
                title = "Editar Tarefa",
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

                    val canSave = title.isNotBlank() &&
                            selectedDisciplineId.isNotBlank() &&
                            !isLoading
                    val saveIconTint =
                        if (canSave) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.surfaceContainerHighest

                    IconButton(
                        onClick = {
                            if (!canSave) return@IconButton
                            isLoading = true
                            scope.launch {
                                try {
                                    val userId = UserDao(context).getUserId()
                                    if (userId == null) {
                                        errorMessage = "Sessão expirada. Faça login novamente"
                                        isLoading = false
                                        return@launch
                                    }

                                    val dateString =
                                        buildExpirationDateStringWithMillis(selectedDateMillis)

                                    val taskRepository =
                                        com.example.tau.data.repository.TaskRepository(context)
                                    val db = com.example.tau.data.local.AppDatabase(context)
                                    val localTask =
                                        db.getAllTasks(userId).find { it.id.toString() == taskId }

                                    if (localTask != null) {
                                        taskRepository.updateTask(
                                            localId = localTask.id,
                                            userId = userId,
                                            title = title,
                                            description = description.ifBlank { "" },
                                            status = completed,
                                            disciplineLocalId = selectedDisciplineId.toLong(),
                                            expirationDate = dateString
                                        )
                                        onSaveClick()
                                    } else {
                                        errorMessage = "Tarefa não encontrada"
                                    }
                                } catch (e: Exception) {
                                    errorMessage =
                                        "Erro ao atualizar: ${e.message ?: "Verifique sua internet"}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = canSave
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("${Strings.TASK_TITLE_LABEL} *") },
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
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(Strings.TASK_DESCRIPTION_LABEL) },
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
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = completed,
                        onCheckedChange = { completed = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Concluído",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    TextField(
                        value = formattedDate,
                        onValueChange = {},
                        label = { Text(Strings.TASK_EXPIRATION_DATE_LABEL) },
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
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                        readOnly = true
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { showDisciplineDialog = true }
                        .background(Color.Transparent, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val selectedDisciplineName =
                        disciplines.find { it.id == selectedDisciplineId }?.title
                            ?: disciplineName
                    if (selectedDisciplineName.isEmpty()) {
                        Text(
                            text = "Disciplina *",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = selectedDisciplineName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar exclusão", color = Color.White) },
                text = {
                    Text(
                        "Tem certeza que deseja excluir esta tarefa? Esta ação não pode ser desfeita.",
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
                                    val taskRepository =
                                        com.example.tau.data.repository.TaskRepository(context)
                                    taskRepository.deleteTask(taskId.toLong())
                                    onDeleteClick()
                                } catch (e: Exception) {
                                    errorMessage = "Erro ao deletar: ${e.message}"
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

        if (showDisciplineDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDisciplineDialog = false },
                confirmButton = {},
                title = { Text("Selecione a disciplina") },
                text = {
                    Column {
                        disciplines.forEach { discipline ->
                            Text(
                                text = discipline.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedDisciplineId = discipline.id
                                        showDisciplineDialog = false
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
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

private fun buildExpirationDateStringWithMillis(expirationMillis: Long?): String {
    val dateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val date = expirationMillis?.let { java.util.Date(it) } ?: java.util.Date()
    return dateFormat.format(date)
}

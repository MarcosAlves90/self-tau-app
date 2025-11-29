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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tau.data.Discipline
import com.example.tau.ui.Strings
import com.example.tau.ui.components.NavTopBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.tau.data.local.UserDao
import kotlinx.coroutines.launch
import com.example.tau.ui.components.LoadingDialog
import com.example.tau.ui.components.ErrorDialog

private object ScheduleScreenDimens {
    val contentPadding = 16.dp
    val cornerRadius = 24.dp
    val dividerPadding = 8.dp
    val textFieldHeight = 48.dp
    val iconSize = 28.dp
    val dividerColor = Color.DarkGray
}

private data class ScheduleFormState(
    val selectedDisciplineId: String = "",
    val selectedDay: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val showDisciplineDialog: Boolean = false,
    val showDayDialog: Boolean = false,
    val showStartTimeDialog: Boolean = false,
    val showEndTimeDialog: Boolean = false
) {
    fun isValid(): Boolean =
        selectedDisciplineId.isNotBlank() &&
        selectedDay.isNotBlank() &&
        startTime.isNotBlank() &&
        endTime.isNotBlank()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    disciplines: List<Discipline> = emptyList()
) {
    var formState by remember { mutableStateOf(ScheduleFormState()) }
    var loadedDisciplines by remember { mutableStateOf<List<Discipline>>(disciplines) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (disciplines.isEmpty()) {
            val userId = UserDao(context).getUserId()
            if (userId != null) {
                val disciplineRepository = com.example.tau.data.repository.DisciplineRepository(context)
                loadedDisciplines = disciplineRepository.getDisciplinesLocal(userId)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CreateScheduleTopBar(
                isFormValid = formState.isValid(),
                onBackClick = onBackClick,
                onSaveClick = {
                    if (formState.isValid() && !isLoading) {
                        isLoading = true
                        scope.launch {
                            try {
                                val userId = UserDao(context).getUserId()
                                if (userId == null) {
                                    errorMessage = "Sessão expirada. Faça login novamente"
                                    isLoading = false
                                    return@launch
                                }

                                val dayMap = mapOf(
                                    "Domingo" to 0,
                                    "Segunda" to 1,
                                    "Terça" to 2,
                                    "Quarta" to 3,
                                    "Quinta" to 4,
                                    "Sexta" to 5
                                )

                                val scheduleRepository = com.example.tau.data.repository.ScheduleRepository(context)
                                scheduleRepository.createSchedule(
                                    userId = userId,
                                    disciplineLocalId = formState.selectedDisciplineId.toLong(),
                                    dayOfWeek = dayMap[formState.selectedDay] ?: 0,
                                    startTime = "${formState.startTime}:00.000Z",
                                    endTime = "${formState.endTime}:00.000Z"
                                )

                                onSaveClick()
                            } catch (e: Exception) {
                                errorMessage = "Erro de conexão: ${e.message ?: "Verifique sua internet"}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        CreateScheduleForm(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            formState = formState,
            onFormStateChange = { formState = it },
            disciplines = loadedDisciplines
        )
    }

    if (isLoading) {
        LoadingDialog(
            title = "Criando horário...",
            message = "Aguarde enquanto salvamos os dados"
        )
    }

    if (errorMessage != null) {
        ErrorDialog(
            title = "Erro ao Criar Horário",
            message = errorMessage ?: "Erro desconhecido",
            onDismiss = { errorMessage = null },
            buttonText = "Tentar Novamente"
        )
    }
}


@Composable
private fun CreateScheduleTopBar(
    isFormValid: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    NavTopBar(
        title = Strings.PAGE_TITLE_CREATE_SCHEDULE,
        onBackClick = onBackClick
    ) {
        val saveIconTint = if (isFormValid) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }

        IconButton(
            onClick = onSaveClick,
            enabled = isFormValid
        ) {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = Strings.SAVE_BUTTON,
                tint = saveIconTint,
                modifier = Modifier.size(ScheduleScreenDimens.iconSize)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateScheduleForm(
    modifier: Modifier = Modifier,
    formState: ScheduleFormState,
    onFormStateChange: (ScheduleFormState) -> Unit,
    disciplines: List<Discipline>
) {
    val daysOfWeek = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta")

    val selectedDisciplineName = disciplines
        .find { it.id == formState.selectedDisciplineId }?.title ?: ""

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(ScheduleScreenDimens.contentPadding)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DisciplineSelector(
            selectedDisciplineName = selectedDisciplineName,
            onClick = { onFormStateChange(formState.copy(showDisciplineDialog = true)) }
        )
        VerticalDividerWithSpacing()

        DaySelector(
            selectedDay = formState.selectedDay,
            onClick = { onFormStateChange(formState.copy(showDayDialog = true)) }
        )
        VerticalDividerWithSpacing()

        TimeSelector(
            label = "${Strings.SCHEDULE_START_TIME_LABEL} *",
            value = formState.startTime,
            onClick = { onFormStateChange(formState.copy(showStartTimeDialog = true)) }
        )
        VerticalDividerWithSpacing()

        TimeSelector(
            label = "${Strings.SCHEDULE_END_TIME_LABEL} *",
            value = formState.endTime,
            onClick = { onFormStateChange(formState.copy(showEndTimeDialog = true)) }
        )
    }

    if (formState.showDisciplineDialog) {
        DisciplineDialog(
            disciplines = disciplines,
            onDismiss = { onFormStateChange(formState.copy(showDisciplineDialog = false)) },
            onSelect = { disciplineId ->
                onFormStateChange(formState.copy(
                    selectedDisciplineId = disciplineId,
                    showDisciplineDialog = false
                ))
            }
        )
    }

    if (formState.showDayDialog) {
        DayDialog(
            days = daysOfWeek,
            onDismiss = { onFormStateChange(formState.copy(showDayDialog = false)) },
            onConfirm = { day ->
                onFormStateChange(formState.copy(
                    selectedDay = day,
                    showDayDialog = false
                ))
            }
        )
    }

    if (formState.showStartTimeDialog) {
        TimeInputDialog(
            initialTime = formState.startTime,
            onDismiss = { onFormStateChange(formState.copy(showStartTimeDialog = false)) },
            onConfirm = { time ->
                onFormStateChange(formState.copy(
                    startTime = time,
                    showStartTimeDialog = false
                ))
            }
        )
    }

    if (formState.showEndTimeDialog) {
        TimeInputDialog(
            initialTime = formState.endTime,
            onDismiss = { onFormStateChange(formState.copy(showEndTimeDialog = false)) },
            onConfirm = { time ->
                onFormStateChange(formState.copy(
                    endTime = time,
                    showEndTimeDialog = false
                ))
            }
        )
    }
}

@Composable
private fun DisciplineSelector(
    selectedDisciplineName: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ScheduleScreenDimens.textFieldHeight)
            .clickable(enabled = true, onClick = onClick)
            .background(Color.Transparent, RoundedCornerShape(ScheduleScreenDimens.cornerRadius))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (selectedDisciplineName.isEmpty()) {
            Text(
                text = "${Strings.SCHEDULE_DISCIPLINE_LABEL} *",
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

@Composable
private fun DaySelector(
    selectedDay: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ScheduleScreenDimens.textFieldHeight)
            .clickable(enabled = true, onClick = onClick)
            .background(Color.Transparent, RoundedCornerShape(ScheduleScreenDimens.cornerRadius))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (selectedDay.isEmpty()) {
            Text(
                text = "${Strings.SCHEDULE_DAY_LABEL} *",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = selectedDay,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun TimeSelector(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ScheduleScreenDimens.textFieldHeight)
            .clickable(enabled = true, onClick = onClick)
            .background(Color.Transparent, RoundedCornerShape(ScheduleScreenDimens.cornerRadius))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplineDialog(
    disciplines: List<Discipline>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.SCHEDULE_DISCIPLINE_LABEL, color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                disciplines.forEach { discipline ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(discipline.id)
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = discipline.title,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.DATE_PICKER_DISMISS_BUTTON, color = Color.White)
            }
        },
        confirmButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDialog(
    days: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.SCHEDULE_DAY_LABEL, color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                days.forEach { day ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onConfirm(day)
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = day,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.DATE_PICKER_DISMISS_BUTTON, color = Color.White)
            }
        },
        confirmButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeInputDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = if (initialTime.isNotEmpty()) initialTime.split(":") else listOf("00", "00")
    var hour by remember { mutableIntStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 0) }
    var minute by remember { mutableIntStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val formattedHour = hour.toString().padStart(2, '0')
                    val formattedMinute = minute.toString().padStart(2, '0')
                    onConfirm("$formattedHour:$formattedMinute")
                }
            ) {
                Text(Strings.DATE_PICKER_CONFIRM_BUTTON, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.DATE_PICKER_DISMISS_BUTTON, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { hour = (hour + 1) % 24 }
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Aumentar hora")
                        }
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.DarkGray, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hour.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { hour = (hour - 1 + 24) % 24 }
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Diminuir hora")
                        }
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { minute = (minute + 1) % 60 }
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Aumentar minuto")
                        }
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.DarkGray, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = minute.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.White
                            )
                        }
                        IconButton(
                            onClick = { minute = (minute - 1 + 60) % 60 }
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Diminuir minuto")
                        }
                    }
                }
            }
        }
    )
}


@Composable
private fun VerticalDividerWithSpacing() {
    Spacer(modifier = Modifier.height(ScheduleScreenDimens.dividerPadding))
    HorizontalDivider(color = ScheduleScreenDimens.dividerColor)
    Spacer(modifier = Modifier.height(ScheduleScreenDimens.dividerPadding))
}

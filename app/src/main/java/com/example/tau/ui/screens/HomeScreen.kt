package com.example.tau.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tau.data.Discipline
import com.example.tau.data.Schedule
import com.example.tau.data.Task
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import com.example.tau.ui.components.PageTopBar
import com.example.tau.ui.utils.ColorMapper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(modifier: Modifier = Modifier, onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val taskRepository = remember { com.example.tau.data.repository.TaskRepository(context) }
    val scheduleRepository = remember { com.example.tau.data.repository.ScheduleRepository(context) }
    val disciplineRepository = remember { com.example.tau.data.repository.DisciplineRepository(context) }

    var upcomingTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var todaysSchedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var disciplines by remember { mutableStateOf<List<Discipline>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val userId = UserDao(context).getUserId() ?: return@LaunchedEffect

            val allTasks = taskRepository.getTasksLocal(userId).filter { !it.completed }
            upcomingTasks = allTasks.sortedBy { it.expirationDate ?: Long.MAX_VALUE }.take(3)

            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
            todaysSchedules = scheduleRepository.getSchedulesLocal(userId).filter { it.dayOfWeek == today }

            disciplines = disciplineRepository.getDisciplinesLocal(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PageTopBar(title = Strings.PAGE_TITLE_HOME, onMenuClick = onMenuClick) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Próximas Tarefas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (upcomingTasks.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma tarefa pendente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(upcomingTasks) { task ->
                    TaskSummaryItem(task = task)
                }
            }

            item {
                Text(
                    text = "Horários de Hoje",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (todaysSchedules.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum horário para hoje",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(todaysSchedules) { schedule ->
                    ScheduleSummaryItem(schedule = schedule)
                }
            }

            item {
                Text(
                    text = "Disciplinas (${disciplines.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (disciplines.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma disciplina cadastrada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(disciplines.take(3)) { discipline ->
                    DisciplineSummaryItem(discipline = discipline)
                }
                if (disciplines.size > 3) {
                    item {
                        Text(
                            text = "+ ${disciplines.size - 3} mais...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskSummaryItem(task: Task, modifier: Modifier = Modifier) {
    val backgroundColor = ColorMapper.colorNameToColor(task.disciplineColor)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.disciplineName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                task.expirationDate?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text(
                        text = "Expira em: ${sdf.format(Date(it))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleSummaryItem(schedule: Schedule, modifier: Modifier = Modifier) {
    val backgroundColor = ColorMapper.colorNameToColor(schedule.disciplineColor)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.disciplineName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${schedule.startTime} - ${schedule.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun DisciplineSummaryItem(discipline: Discipline, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorMapper.colorNameToColor(discipline.color))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = discipline.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Professor: ${discipline.teacher}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
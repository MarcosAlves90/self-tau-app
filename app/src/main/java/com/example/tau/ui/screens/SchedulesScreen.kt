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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.example.tau.data.Schedule
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import com.example.tau.ui.components.PageTopBar
import com.example.tau.ui.utils.ColorMapper

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

@Composable
fun SchedulesScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onCreateScheduleClick: () -> Unit,
    onScheduleClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scheduleRepository = remember { com.example.tau.data.repository.ScheduleRepository(context) }
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val userId = UserDao(context).getUserId() ?: return@LaunchedEffect
            schedules = scheduleRepository.getSchedulesLocal(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PageTopBar(title = Strings.PAGE_TITLE_SCHEDULES, onMenuClick = onMenuClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateScheduleClick) {
                Icon(Icons.Filled.Add, contentDescription = Strings.CREATE_SCHEDULE_BUTTON_DESC)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(schedules) { schedule ->
                ScheduleItem(
                    schedule = schedule,
                    onClick = { onScheduleClick(schedule.id) }
                )
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: Schedule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val backgroundColor = ColorMapper.colorNameToColor(schedule.disciplineColor)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.disciplineName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dia: ${getDayName(schedule.dayOfWeek)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Horário: ${schedule.startTime} - ${schedule.endTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
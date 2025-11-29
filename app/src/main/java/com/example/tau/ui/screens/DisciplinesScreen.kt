package com.example.tau.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
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
import com.example.tau.data.Discipline
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import com.example.tau.ui.components.PageTopBar
import com.example.tau.ui.utils.ColorMapper

@Composable
fun DisciplinesScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onCreateDisciplineClick: () -> Unit,
    onDisciplineClick: (String) -> Unit
) {
    val context = LocalContext.current
    val disciplineRepository = remember { com.example.tau.data.repository.DisciplineRepository(context) }
    var disciplines by remember { mutableStateOf<List<Discipline>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val userId = UserDao(context).getUserId() ?: return@LaunchedEffect
            disciplines = disciplineRepository.getDisciplinesLocal(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PageTopBar(title = Strings.PAGE_TITLE_DISCIPLINES, onMenuClick = onMenuClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateDisciplineClick) {
                Icon(Icons.Filled.Add, contentDescription = Strings.CREATE_DISCIPLINE_BUTTON_DESC)
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
            items(disciplines) { discipline ->
                DisciplineItem(
                    discipline = discipline,
                    onClick = { onDisciplineClick(discipline.id) }
                )
            }
        }
    }
}

@Composable
fun DisciplineItem(
    discipline: Discipline,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorMapper.colorNameToColor(discipline.color))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Disciplina",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = discipline.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Professor: ${discipline.teacher}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sala: ${discipline.room}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}


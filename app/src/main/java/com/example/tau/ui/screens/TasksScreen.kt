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
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tau.data.Task
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import kotlinx.coroutines.launch
import com.example.tau.ui.components.PageTopBar
import com.example.tau.ui.utils.ColorMapper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val taskRepository = remember { com.example.tau.data.repository.TaskRepository(context) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val userId = UserDao(context).getUserId()
            Log.d("TasksScreen", "UserId: $userId")

            if (userId == null) {
                Log.e("TasksScreen", "UserId is null")
                return@LaunchedEffect
            }

            tasks = taskRepository.getTasksLocal(userId)
            Log.d("TasksScreen", "Tasks loaded from local: ${tasks.size} items")
        } catch (e: Exception) {
            Log.e("TasksScreen", "Exception loading tasks", e)
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { PageTopBar(title = Strings.PAGE_TITLE_TASKS, onMenuClick = onMenuClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTaskClick) {
                Icon(Icons.Filled.Add, contentDescription = Strings.CREATE_TASK_BUTTON_DESC)
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
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onTaskUpdate = { updatedTask ->
                        tasks = tasks.map {
                            if (it.id == updatedTask.id) updatedTask else it
                        }
                    },
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}


@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    onTaskUpdate: (Task) -> Unit,
    onClick: () -> Unit = {}
) {
    val backgroundColor = ColorMapper.colorNameToColor(task.disciplineColor)
    val context = LocalContext.current
    val taskRepository = remember { com.example.tau.data.repository.TaskRepository(context) }
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }

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
            Checkbox(
                checked = task.completed,
                enabled = !isUpdating,
                onCheckedChange = { newStatus ->
                    isUpdating = true
                    scope.launch {
                        try {
                            val updatedTask = task.copy(completed = newStatus)
                            onTaskUpdate(updatedTask)

                            val userId = UserDao(context).getUserId()
                            if (userId != null) {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                val dateString = task.expirationDate?.let {
                                    dateFormat.format(Date(it))
                                } ?: dateFormat.format(Date())

                                val db = com.example.tau.data.local.AppDatabase(context)
                                val localTasks = db.getAllTasks(userId)
                                val localTask = localTasks.find { it.id.toString() == task.id }

                                if (localTask != null) {
                                    taskRepository.updateTask(
                                        localId = localTask.id,
                                        userId = userId,
                                        title = task.title,
                                        description = task.description,
                                        status = newStatus,
                                        disciplineLocalId = localTask.disciplineId,
                                        expirationDate = dateString
                                    )
                                    Log.d("TaskItem", "Task ${task.id} updated successfully")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TaskItem", "Exception updating task", e)
                        } finally {
                            isUpdating = false
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.disciplineName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
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



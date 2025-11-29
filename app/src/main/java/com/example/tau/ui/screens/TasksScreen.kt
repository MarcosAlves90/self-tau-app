package com.example.tau.ui.screens

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tau.data.Task
import com.example.tau.data.local.UserDao
import com.example.tau.ui.Strings
import com.example.tau.ui.components.PageTopBar
import com.example.tau.ui.utils.ColorMapper
import kotlinx.coroutines.launch
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
        tasks = loadTasks(context, taskRepository)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PageTopBar(
                title = Strings.PAGE_TITLE_TASKS,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTaskClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = Strings.CREATE_TASK_BUTTON_DESC
                )
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
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskUpdate = { updatedTask ->
                        val mutable = tasks.toMutableList()
                        val index = mutable.indexOfFirst { it.id == updatedTask.id }
                        if (index != -1) {
                            mutable[index] = updatedTask
                            tasks = mutable.toList()
                        }
                    },
                    onClick = { onTaskClick(task.id) }
                )
            }
        }
    }
}

private fun loadTasks(
    context: Context,
    taskRepository: com.example.tau.data.repository.TaskRepository
): List<Task> {
    return try {
        val userId = UserDao(context).getUserId()
        Log.d("TasksScreen", "UserId: $userId")

        if (userId == null) {
            Log.e("TasksScreen", "UserId is null")
            emptyList()
        } else {
            val userTasks = taskRepository.getTasksLocal(userId)
            Log.d("TasksScreen", "Tasks loaded from local: ${userTasks.size} items")
            userTasks
        }
    } catch (e: Exception) {
        Log.e("TasksScreen", "Exception loading tasks", e)
        e.printStackTrace()
        emptyList()
    }
}

@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    onTaskUpdate: (Task) -> Unit,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val taskRepository = remember { com.example.tau.data.repository.TaskRepository(context) }
    val scope = rememberCoroutineScope()

    val backgroundColor = ColorMapper.colorNameToColor(task.disciplineColor)
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
                    scope.launch {
                        handleTaskStatusChange(
                            context = context,
                            taskRepository = taskRepository,
                            task = task,
                            newStatus = newStatus,
                            onUpdatingChange = { updating -> isUpdating = updating },
                            onTaskUpdate = onTaskUpdate
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Column(modifier = Modifier.weight(1f)) {
                TaskInfo(task = task)
            }
        }
    }
}

@Composable
private fun TaskInfo(task: Task) {
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

    task.expirationDate?.let { expirationMillis ->
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatExpirationDate(expirationMillis),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

private suspend fun handleTaskStatusChange(
    context: Context,
    taskRepository: com.example.tau.data.repository.TaskRepository,
    task: Task,
    newStatus: Boolean,
    onUpdatingChange: (Boolean) -> Unit,
    onTaskUpdate: (Task) -> Unit
) {
    onUpdatingChange(true)

    try {
        val updatedTask = task.copy(completed = newStatus)
        onTaskUpdate(updatedTask)

        val userId = UserDao(context).getUserId() ?: return
        val expirationDateString = buildExpirationDateString(task.expirationDate)

        val db = com.example.tau.data.local.AppDatabase(context)
        val localTask = db.getAllTasks(userId).find { it.id.toString() == task.id }

        if (localTask != null) {
            taskRepository.updateTask(
                localId = localTask.id,
                userId = userId,
                title = task.title,
                description = task.description,
                status = newStatus,
                disciplineLocalId = localTask.disciplineId,
                expirationDate = expirationDateString
            )
            Log.d("TaskItem", "Task ${task.id} updated successfully")
        } else {
            Log.e("TaskItem", "Local task not found for id=${task.id}")
        }
    } catch (e: Exception) {
        Log.e("TaskItem", "Exception updating task", e)
    } finally {
        onUpdatingChange(false)
    }
}

private fun buildExpirationDateString(expirationMillis: Long?): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = expirationMillis?.let { Date(it) } ?: Date()
    return dateFormat.format(date)
}

private fun formatExpirationDate(expirationMillis: Long): String {
    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return "Expira em: ${displayFormat.format(Date(expirationMillis))}"
}

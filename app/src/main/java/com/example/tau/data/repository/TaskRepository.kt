package com.example.tau.data.repository

import android.content.Context
import android.util.Log
import com.example.tau.data.Task
import com.example.tau.data.TaskRequest
import com.example.tau.data.api.RetrofitClient
import com.example.tau.data.local.AppDatabase
import com.example.tau.data.local.LocalTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class TaskRepository(context: Context) {
    private val db = AppDatabase(context)
    private val TAG = "TaskRepository"

    private fun parseDate(dateString: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                return sdf.parse(dateString)?.time
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    private fun createTaskRequest(
        userId: Int,
        title: String,
        description: String,
        status: Boolean,
        remoteDisciplineId: Int,
        expirationDate: String
    ): TaskRequest {
        return TaskRequest(
            usuario_id = userId,
            titulo = title,
            descricao = description,
            status = status,
            disciplina_id = remoteDisciplineId,
            data_validade = expirationDate
        )
    }

    suspend fun syncTasks(userId: Int): List<Task> = withContext(Dispatchers.IO) {
        getTasksLocal(userId)
    }

    fun getTasksLocal(userId: Int): List<Task> {
        val localTasks = db.getAllTasks(userId)
        val disciplineMap = db.getAllDisciplinesMap(userId)
        return localTasks.map { task ->
            val discipline = disciplineMap[task.disciplineId]
            Task(
                id = task.id.toString(),
                title = task.title,
                description = task.description,
                expirationDate = parseDate(task.expirationDate),
                completed = task.status,
                disciplineId = discipline?.remoteId ?: 0,
                disciplineColor = discipline?.color ?: "#6200EE",
                disciplineName = discipline?.name ?: "Sem disciplina"
            )
        }
    }

    suspend fun createTask(userId: Int, title: String, description: String, status: Boolean, disciplineLocalId: Long, expirationDate: String): Long {
        val discipline = db.getDisciplineByLocalId(disciplineLocalId)
        if (discipline?.remoteId == null) {
            throw IllegalStateException("Cannot create task: associated discipline is not synced with remote database. Please sync disciplines first.")
        }

        val localId = db.insertTask(
            remoteId = null,
            userId = userId,
            title = title,
            description = description,
            status = status,
            disciplineId = disciplineLocalId,
            expirationDate = expirationDate,
            synced = false
        )

        syncCreateTask(localId, userId, title, description, status, disciplineLocalId, expirationDate)

        return localId
    }

    private suspend fun syncCreateTask(localId: Long, userId: Int, title: String, description: String, status: Boolean, disciplineLocalId: Long, expirationDate: String) {
        try {
            withContext(Dispatchers.IO) {
                val discipline = db.getDisciplineByLocalId(disciplineLocalId)
                discipline?.remoteId?.let { remoteDisciplineId ->
                    val request = createTaskRequest(userId, title, description, status, remoteDisciplineId, expirationDate)
                    val response = RetrofitClient.apiService.createTask(request)
                    if (response.isSuccessful) {
                        val remoteId = response.body()?.id
                        db.updateTask(
                            localId = localId,
                            remoteId = remoteId,
                            title = title,
                            description = description,
                            status = status,
                            disciplineId = disciplineLocalId,
                            expirationDate = expirationDate,
                            synced = true
                        )
                    } else {
                        Log.e(TAG, "Failed to create task remotely: ${response.message()}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing task creation", e)
        }
    }

    suspend fun updateTask(localId: Long, userId: Int, title: String, description: String, status: Boolean, disciplineLocalId: Long, expirationDate: String) {
        val task = db.getTaskByLocalId(localId)

        db.updateTask(
            localId = localId,
            remoteId = task?.remoteId,
            title = title,
            description = description,
            status = status,
            disciplineId = disciplineLocalId,
            expirationDate = expirationDate,
            synced = false
        )

        syncUpdateTask(localId, task, userId, title, description, status, disciplineLocalId, expirationDate)
    }

    private suspend fun syncUpdateTask(localId: Long, task: LocalTask?, userId: Int, title: String, description: String, status: Boolean, disciplineLocalId: Long, expirationDate: String) {
        withContext(Dispatchers.IO) {
            try {
                val discipline = db.getDisciplineByLocalId(disciplineLocalId)
                if (task?.remoteId != null) {
                    // Update existing remote task
                    discipline?.remoteId?.let { remoteDisciplineId ->
                        val request = createTaskRequest(userId, title, description, status, remoteDisciplineId, expirationDate)
                        val response = RetrofitClient.apiService.updateTask(task.remoteId, request)
                        if (response.isSuccessful) {
                            db.updateTask(localId, task.remoteId, title, description, status, disciplineLocalId, expirationDate, synced = true)
                        } else {
                            Log.e(TAG, "Failed to update task remotely: ${response.message()}")
                        }
                    }
                } else {
                    // Task not synced yet, create it remotely
                    discipline?.remoteId?.let { remoteDisciplineId ->
                        val request = createTaskRequest(userId, title, description, status, remoteDisciplineId, expirationDate)
                        val response = RetrofitClient.apiService.createTask(request)
                        if (response.isSuccessful) {
                            val remoteId = response.body()?.id
                            db.updateTask(localId, remoteId, title, description, status, disciplineLocalId, expirationDate, synced = true)
                        } else {
                            Log.e(TAG, "Failed to create task remotely on update: ${response.message()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing task update", e)
            }
        }
    }

    suspend fun deleteTask(localId: Long) {
        val task = db.getTaskByLocalId(localId)

        db.deleteTask(localId)

        syncDeleteTask(task)
    }

    private suspend fun syncDeleteTask(task: LocalTask?) {
        withContext(Dispatchers.IO) {
            try {
                task?.remoteId?.let { remoteId ->
                    val response = RetrofitClient.apiService.deleteTask(remoteId)
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to delete task remotely: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing task deletion", e)
            }
        }
    }

    fun getTaskByLocalId(localId: Long): Task? {
        val task = db.getTaskByLocalId(localId)
        val localDisciplines = db.getAllDisciplines(task?.userId ?: 0)

        return task?.let {
            val discipline = localDisciplines.find { it.id == task.disciplineId }
            Task(
                id = it.id.toString(),
                title = it.title,
                description = it.description,
                expirationDate = parseDate(it.expirationDate),
                completed = it.status,
                disciplineId = discipline?.remoteId ?: 0,
                disciplineColor = discipline?.color ?: "#6200EE",
                disciplineName = discipline?.name ?: "Sem disciplina"
            )
        }
    }

    suspend fun syncTasksFromRemote(userId: Int) = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getTasks(usuarioId = userId)
            if (response.isSuccessful) {
                response.body()?.forEach { remote ->
                    val localTasks = db.getAllTasks(userId)
                    val existing = localTasks.find { it.remoteId == remote.id }
                    val localDisciplines = db.getAllDisciplines(userId)
                    val discipline = localDisciplines.find { it.remoteId == remote.disciplina_id }
                    val localDisciplineId = discipline?.id ?: 0L
                    if (existing != null) {
                        db.updateTask(
                            localId = existing.id,
                            remoteId = remote.id,
                            title = remote.titulo,
                            description = remote.descricao,
                            status = remote.status,
                            disciplineId = localDisciplineId,
                            expirationDate = remote.data_validade,
                            synced = true
                        )
                    } else {
                        db.insertTask(
                            remoteId = remote.id,
                            userId = userId,
                            title = remote.titulo,
                            description = remote.descricao,
                            status = remote.status,
                            disciplineId = localDisciplineId,
                            expirationDate = remote.data_validade,
                            synced = true
                        )
                    }
                }
            } else {
                Log.e(TAG, "Failed to sync tasks from remote: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tasks from remote", e)
        }
    }

    suspend fun syncTasksToRemote(userId: Int) = withContext(Dispatchers.IO) {
        try {
            val unsyncedTasks = db.getAllTasks(userId).filter { !it.synced }
            unsyncedTasks.forEach { task ->
                val discipline = db.getDisciplineByLocalId(task.disciplineId)
                discipline?.remoteId?.let { remoteDisciplineId ->
                    val request = createTaskRequest(
                        userId,
                        task.title,
                        task.description,
                        task.status,
                        remoteDisciplineId,
                        task.expirationDate
                    )

                    if (task.remoteId != null) {
                        val response = RetrofitClient.apiService.updateTask(task.remoteId, request)
                        if (response.isSuccessful) {
                            db.updateTask(
                                localId = task.id,
                                remoteId = task.remoteId,
                                title = task.title,
                                description = task.description,
                                status = task.status,
                                disciplineId = task.disciplineId,
                                expirationDate = task.expirationDate,
                                synced = true
                            )
                        } else {
                            Log.e(TAG, "Failed to sync update task to remote: ${response.message()}")
                        }
                    } else {
                        val response = RetrofitClient.apiService.createTask(request)
                        if (response.isSuccessful) {
                            val remoteId = response.body()?.id ?: 0
                            db.updateTask(
                                localId = task.id,
                                remoteId = remoteId,
                                title = task.title,
                                description = task.description,
                                status = task.status,
                                disciplineId = task.disciplineId,
                                expirationDate = task.expirationDate,
                                synced = true
                            )
                        } else {
                            Log.e(TAG, "Failed to sync create task to remote: ${response.message()}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tasks to remote", e)
        }
    }
}

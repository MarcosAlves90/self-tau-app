package com.example.tau.data.repository

import android.content.Context
import com.example.tau.data.Schedule
import com.example.tau.data.ScheduleRequest
import com.example.tau.data.api.RetrofitClient
import com.example.tau.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleRepository(context: Context) {
    private val db = AppDatabase(context)

    private fun parseTime(timeString: String): String {
        return try {
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
            timeString
        } catch (_: Exception) {
            timeString
        }
    }

    suspend fun syncSchedules(userId: Int): List<Schedule> = withContext(Dispatchers.IO) {
        getSchedulesLocal(userId)
    }

    suspend fun syncSchedulesFromRemote(userId: Int) = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getSchedules(usuarioId = userId)
            if (response.isSuccessful) {
                response.body()?.forEach { remote ->
                    val localSchedules = db.getAllSchedules(userId)
                    val existing = localSchedules.find { it.remoteId == remote.id }
                    val localDisciplines = db.getAllDisciplines(userId)
                    val discipline = localDisciplines.find { it.remoteId == remote.disciplina_id }
                    val localDisciplineId = discipline?.id ?: 0L
                    if (existing != null) {
                        db.updateSchedule(
                            localId = existing.id,
                            remoteId = remote.id,
                            disciplineId = localDisciplineId,
                            dayOfWeek = remote.dia_semana,
                            startTime = remote.hora_comeco,
                            endTime = remote.hora_fim,
                            synced = true
                        )
                    } else {
                        db.insertSchedule(
                            remoteId = remote.id,
                            userId = userId,
                            disciplineId = localDisciplineId,
                            dayOfWeek = remote.dia_semana,
                            startTime = remote.hora_comeco,
                            endTime = remote.hora_fim,
                            synced = true
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSchedulesLocal(userId: Int): List<Schedule> {
        val localSchedules = db.getAllSchedules(userId)
        val disciplineMap = db.getAllDisciplinesMap(userId)
        return localSchedules.map { schedule ->
            val discipline = disciplineMap[schedule.disciplineId]
            Schedule(
                id = schedule.id.toString(),
                disciplineId = discipline?.remoteId ?: 0,
                dayOfWeek = schedule.dayOfWeek,
                startTime = parseTime(schedule.startTime),
                endTime = parseTime(schedule.endTime),
                disciplineColor = discipline?.color ?: "#6200EE",
                disciplineName = discipline?.name ?: "Sem disciplina"
            )
        }
    }

    suspend fun createSchedule(userId: Int, disciplineLocalId: Long, dayOfWeek: Int, startTime: String, endTime: String): Long {
        val localId = db.insertSchedule(
            remoteId = null,
            userId = userId,
            disciplineId = disciplineLocalId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            synced = false
        )

        try {
            withContext(Dispatchers.IO) {
                val discipline = db.getDisciplineByLocalId(disciplineLocalId)
                discipline?.remoteId?.let { remoteDisciplineId ->
                    val request = ScheduleRequest(
                        usuario_id = userId,
                        disciplina_id = remoteDisciplineId,
                        hora_comeco = startTime,
                        hora_fim = endTime,
                        dia_semana = dayOfWeek
                    )
                    val response = RetrofitClient.apiService.createSchedule(request)
                    if (response.isSuccessful) {
                        db.updateSchedule(
                            localId = localId,
                            remoteId = null,
                            disciplineId = disciplineLocalId,
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            synced = true
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return localId
    }

    fun createScheduleAsync(userId: Int, disciplineLocalId: Long, dayOfWeek: Int, startTime: String, endTime: String): Long {
        val localId = db.insertSchedule(
            remoteId = null,
            userId = userId,
            disciplineId = disciplineLocalId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            synced = false
        )
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val discipline = db.getDisciplineByLocalId(disciplineLocalId)
                discipline?.remoteId?.let { remoteDisciplineId ->
                    val request = ScheduleRequest(
                        usuario_id = userId,
                        disciplina_id = remoteDisciplineId,
                        hora_comeco = startTime,
                        hora_fim = endTime,
                        dia_semana = dayOfWeek
                    )
                    RetrofitClient.apiService.createSchedule(request)
                }
            } catch (_: Exception) {}
        }
        return localId
    }

    suspend fun updateSchedule(localId: Long, userId: Int, disciplineLocalId: Long, dayOfWeek: Int, startTime: String, endTime: String) {
        val schedule = db.getScheduleByLocalId(localId)

        db.updateSchedule(
            localId = localId,
            remoteId = schedule?.remoteId,
            disciplineId = disciplineLocalId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            synced = false
        )

        withContext(Dispatchers.IO) {
            try {
                val discipline = db.getDisciplineByLocalId(disciplineLocalId)
                schedule?.remoteId?.let { remoteId ->
                    discipline?.remoteId?.let { remoteDisciplineId ->
                        val request = ScheduleRequest(
                            usuario_id = userId,
                            disciplina_id = remoteDisciplineId,
                            hora_comeco = startTime,
                            hora_fim = endTime,
                            dia_semana = dayOfWeek
                        )
                        val response = RetrofitClient.apiService.updateSchedule(remoteId, request)
                        if (response.isSuccessful) {
                            db.updateSchedule(localId, remoteId, disciplineLocalId, dayOfWeek, startTime, endTime, synced = true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateScheduleAsync(localId: Long, userId: Int, disciplineLocalId: Long, dayOfWeek: Int, startTime: String, endTime: String) {
        db.updateSchedule(
            localId = localId,
            remoteId = null,
            disciplineId = disciplineLocalId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            synced = false
        )
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val discipline = db.getDisciplineByLocalId(disciplineLocalId)
                discipline?.remoteId?.let { remoteDisciplineId ->
                    val request = ScheduleRequest(
                        usuario_id = userId,
                        disciplina_id = remoteDisciplineId,
                        hora_comeco = startTime,
                        hora_fim = endTime,
                        dia_semana = dayOfWeek
                    )
                    RetrofitClient.apiService.updateSchedule(localId.toInt(), request)
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteSchedule(localId: Long) {
        val schedule = db.getScheduleByLocalId(localId)

        db.deleteSchedule(localId)

        withContext(Dispatchers.IO) {
            try {
                schedule?.remoteId?.let { remoteId ->
                    RetrofitClient.apiService.deleteSchedule(remoteId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteScheduleAsync(localId: Long) {
        db.deleteSchedule(localId)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.deleteSchedule(localId.toInt())
            } catch (_: Exception) {}
        }
    }

    fun getScheduleByLocalId(localId: Long): Schedule? {
        val schedule = db.getScheduleByLocalId(localId)
        val localDisciplines = db.getAllDisciplines(schedule?.userId ?: 0)

        return schedule?.let {
            val discipline = localDisciplines.find { it.id == schedule.disciplineId }
            Schedule(
                id = it.id.toString(),
                disciplineId = discipline?.remoteId ?: 0,
                dayOfWeek = it.dayOfWeek,
                startTime = parseTime(it.startTime),
                endTime = parseTime(it.endTime),
                disciplineColor = discipline?.color ?: "#6200EE",
                disciplineName = discipline?.name ?: "Sem disciplina"
            )
        }
    }
}
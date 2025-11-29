package com.example.tau.data.repository

import android.content.Context
import com.example.tau.data.Discipline
import com.example.tau.data.DisciplineRequest
import com.example.tau.data.DisciplineResponse
import com.example.tau.data.api.RetrofitClient
import com.example.tau.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DisciplineRepository(context: Context) {
    private val db = AppDatabase(context)

    suspend fun syncDisciplines(userId: Int): List<Discipline> = withContext(Dispatchers.IO) {
        getDisciplinesLocal(userId)
    }

    fun getDisciplinesLocal(userId: Int): List<Discipline> {
        return db.getAllDisciplines(userId).map {
            Discipline(
                id = it.id.toString(),
                title = it.name,
                teacher = it.teacher,
                room = it.room,
                color = it.color
            )
        }
    }

    suspend fun createDiscipline(userId: Int, name: String, teacher: String, room: String, color: String): Long {
        val localId = db.insertDiscipline(
            remoteId = null,
            userId = userId,
            name = name,
            teacher = teacher,
            room = room,
            color = color,
            synced = false
        )

        withContext(Dispatchers.IO) {
            try {
                val request = DisciplineRequest(
                    usuario_id = userId,
                    nome = name,
                    professor = teacher,
                    sala = room,
                    cores = color
                )
                val response = RetrofitClient.apiService.createDiscipline(request)
                if (response.isSuccessful) {
                    val remoteId = response.body()?.id
                    db.updateDiscipline(
                        localId = localId,
                        remoteId = remoteId,
                        name = name,
                        teacher = teacher,
                        room = room,
                        color = color,
                        synced = true
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return localId
    }

    fun createDisciplineAsync(userId: Int, name: String, teacher: String, room: String, color: String): Long {
        val localId = db.insertDiscipline(
            remoteId = null,
            userId = userId,
            name = name,
            teacher = teacher,
            room = room,
            color = color,
            synced = false
        )
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val request = DisciplineRequest(
                    usuario_id = userId,
                    nome = name,
                    professor = teacher,
                    sala = room,
                    cores = color
                )
                val response = RetrofitClient.apiService.createDiscipline(request)
                if (response.isSuccessful) {
                    val remoteId = response.body()?.id
                    db.updateDiscipline(
                        localId = localId,
                        remoteId = remoteId,
                        name = name,
                        teacher = teacher,
                        room = room,
                        color = color,
                        synced = true
                    )
                }
            } catch (_: Exception) {}
        }
        return localId
    }

    suspend fun updateDiscipline(localId: Long, userId: Int, name: String, teacher: String, room: String, color: String) {
        val discipline = db.getDisciplineByLocalId(localId)

        db.updateDiscipline(
            localId = localId,
            remoteId = discipline?.remoteId,
            name = name,
            teacher = teacher,
            room = room,
            color = color,
            synced = false
        )

        withContext(Dispatchers.IO) {
            try {
                discipline?.remoteId?.let { remoteId ->
                    val request = DisciplineRequest(
                        usuario_id = userId,
                        nome = name,
                        professor = teacher,
                        sala = room,
                        cores = color
                    )
                    val response = RetrofitClient.apiService.updateDiscipline(remoteId, request)
                    if (response.isSuccessful) {
                        db.updateDiscipline(localId, remoteId, name, teacher, room, color, synced = true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateDisciplineAsync(localId: Long, userId: Int, name: String, teacher: String, room: String, color: String) {
        db.updateDiscipline(
            localId = localId,
            remoteId = null,
            name = name,
            teacher = teacher,
            room = room,
            color = color,
            synced = false
        )
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val request = DisciplineRequest(
                    usuario_id = userId,
                    nome = name,
                    professor = teacher,
                    sala = room,
                    cores = color
                )
                RetrofitClient.apiService.updateDiscipline(localId.toInt(), request)
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteDiscipline(localId: Long) {
        val discipline = db.getDisciplineByLocalId(localId)

        db.deleteDiscipline(localId)

        withContext(Dispatchers.IO) {
            try {
                discipline?.remoteId?.let { remoteId ->
                    RetrofitClient.apiService.deleteDiscipline(remoteId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteDisciplineAsync(localId: Long) {
        db.deleteDiscipline(localId)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                RetrofitClient.apiService.deleteDiscipline(localId.toInt())
            } catch (_: Exception) {}
        }
    }

    fun getDisciplineByLocalId(localId: Long): Discipline? {
        val local = db.getDisciplineByLocalId(localId)
        return local?.let {
            Discipline(
                id = it.id.toString(),
                title = it.name,
                teacher = it.teacher,
                room = it.room,
                color = it.color
            )
        }
    }

    suspend fun syncDisciplinesFromRemote(userId: Int) = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getDisciplines(userId)
            if (response.isSuccessful) {
                response.body()?.forEach { remote ->
                    val localDisciplines = db.getAllDisciplines(userId)
                    val existing = localDisciplines.find { it.remoteId == remote.id }
                    if (existing != null) {
                        db.updateDiscipline(
                            localId = existing.id,
                            remoteId = remote.id,
                            name = remote.nome,
                            teacher = remote.professor,
                            room = remote.sala,
                            color = remote.cores,
                            synced = true
                        )
                    } else {
                        val nameMatch = localDisciplines.find { it.remoteId == null && it.name == remote.nome }
                        if (nameMatch != null) {
                            db.updateDiscipline(
                                localId = nameMatch.id,
                                remoteId = remote.id,
                                name = remote.nome,
                                teacher = remote.professor,
                                room = remote.sala,
                                color = remote.cores,
                                synced = true
                            )
                        } else {
                            db.insertDiscipline(
                                remoteId = remote.id,
                                userId = userId,
                                name = remote.nome,
                                teacher = remote.professor,
                                room = remote.sala,
                                color = remote.cores,
                                synced = true
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.example.tau.data

data class Discipline(
    val id: String,
    val title: String,
    val teacher: String,
    val room: String,
    val color: String
)

data class DisciplineRequest(
    val usuario_id: Int,
    val nome: String,
    val professor: String,
    val sala: String,
    val cores: String
)

data class DisciplineResponse(
    val id: Int,
    val usuario_id: Int,
    val nome: String,
    val professor: String,
    val sala: String,
    val cores: String
)

data class TaskRequest(
    val usuario_id: Int,
    val titulo: String,
    val descricao: String,
    val status: Boolean,
    val disciplina_id: Int,
    val data_validade: String
)

data class TaskResponse(
    val id: Int,
    val usuario_id: Int,
    val titulo: String,
    val descricao: String,
    val status: Boolean,
    val disciplina_id: Int,
    val data_validade: String
)

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val expirationDate: Long?,
    val completed: Boolean,
    val disciplineId: Int = 0,
    val disciplineColor: String = "#6200EE",
    val disciplineName: String = ""
)

data class Schedule(
    val id: String,
    val disciplineId: Int,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val disciplineColor: String = "#6200EE",
    val disciplineName: String = ""
)

data class ScheduleRequest(
    val usuario_id: Int,
    val disciplina_id: Int,
    val hora_comeco: String,
    val hora_fim: String,
    val dia_semana: Int
)

data class ScheduleResponse(
    val id: Int,
    val usuario_id: Int,
    val disciplina_id: Int,
    val hora_comeco: String,
    val hora_fim: String,
    val dia_semana: Int
)

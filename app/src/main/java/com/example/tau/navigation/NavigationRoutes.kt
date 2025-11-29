package com.example.tau.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {

    object SignUp : Screen("signup", "Cadastro", Icons.Filled.Person)
    object Login : Screen("login", "Login", Icons.Filled.Lock)
    object Home : Screen("home", "Início", Icons.Filled.Home)
    object About : Screen("about", "Sobre", Icons.Filled.Info)
    object Tasks : Screen("tasks", "Tarefas", Icons.AutoMirrored.Filled.List)
    object CreateTask : Screen("create_task", "Criar Tarefa", Icons.Filled.Add)
    object EditTask : Screen("edit_task/{taskId}", "Editar Tarefa", Icons.Filled.Edit) {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }
    object Disciplines : Screen("disciplines", "Disciplinas", Icons.Filled.Edit)
    object CreateDiscipline : Screen("create_discipline", "Criar Disciplina", Icons.Filled.Add)
    object EditDiscipline : Screen("edit_discipline/{disciplineId}", "Editar Disciplina", Icons.Filled.Edit) {
        fun createRoute(disciplineId: String) = "edit_discipline/$disciplineId"
    }
    object Settings : Screen("settings", "Configurações", Icons.Filled.Settings)
    object Welcome : Screen("welcome", "Bem-vindo", Icons.Filled.Home)
    object Schedules : Screen("schedules", "Horários", Icons.Filled.DateRange)
    object CreateSchedule : Screen("create_schedule", "Criar Horário", Icons.Filled.Add)
    object EditSchedule : Screen("edit_schedule/{scheduleId}", "Editar Horário", Icons.Filled.Edit) {
        fun createRoute(scheduleId: String) = "edit_schedule/$scheduleId"
    }

    companion object {
        fun getAllScreens() = listOf(Home, Tasks, Disciplines, Schedules, About, Settings)
    }
}

package com.example.tau.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.tau.navigation.Screen
import com.example.tau.ui.screens.AboutScreen
import com.example.tau.ui.screens.CreateTaskScreen
import com.example.tau.ui.screens.EditTaskScreen
import com.example.tau.ui.screens.HomeScreen
import com.example.tau.ui.screens.LoginScreen
import com.example.tau.ui.screens.SettingsScreen
import com.example.tau.ui.screens.SignUpScreen
import com.example.tau.ui.screens.TasksScreen
import com.example.tau.ui.screens.WelcomeScreen
import com.example.tau.ui.screens.DisciplinesScreen
import com.example.tau.ui.screens.CreateDisciplineScreen
import com.example.tau.ui.screens.EditDisciplineScreen
import com.example.tau.ui.screens.CreateScheduleScreen
import com.example.tau.ui.screens.EditScheduleScreen
import com.example.tau.ui.screens.SchedulesScreen
import com.example.tau.data.local.UserDao

private fun NavGraphBuilder.composableNoTransition(
    route: String,
    content: @Composable () -> Unit
) {
    composable(
        route = route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        content()
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLoggedIn = UserDao(context).isLoggedIn()
    val startDestination = if (isLoggedIn) Screen.Tasks.route else Screen.Welcome.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composableNoTransition(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) }
            )
        }
        composableNoTransition(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composableNoTransition(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Tasks.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composableNoTransition(Screen.Home.route) {
            HomeScreen(onMenuClick = onMenuClick)
        }
        composableNoTransition(Screen.About.route) {
            AboutScreen(onMenuClick = onMenuClick)
        }
        composableNoTransition(Screen.Tasks.route) {
            TasksScreen(
                onMenuClick = onMenuClick,
                onCreateTaskClick = { navController.navigate(Screen.CreateTask.route) },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                }
            )
        }
        composableNoTransition(Screen.CreateTask.route) {
            CreateTaskScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditTask.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            EditTaskScreen(
                taskId = taskId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() },
                onDeleteClick = { navController.popBackStack() }
            )
        }
        composableNoTransition(Screen.Settings.route) {
            SettingsScreen(
                onMenuClick = onMenuClick,
                onLogout = {
                    UserDao(context).clearSession()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composableNoTransition(Screen.Disciplines.route) {
            DisciplinesScreen(
                onMenuClick = onMenuClick,
                onCreateDisciplineClick = { navController.navigate(Screen.CreateDiscipline.route) },
                onDisciplineClick = { disciplineId ->
                    navController.navigate(Screen.EditDiscipline.createRoute(disciplineId))
                }
            )
        }
        composableNoTransition(Screen.CreateDiscipline.route) {
            CreateDisciplineScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditDiscipline.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None }, popExitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val disciplineId = backStackEntry.arguments?.getString("disciplineId") ?: ""
            EditDisciplineScreen(
                disciplineId = disciplineId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() },
                onDeleteClick = { navController.popBackStack() }
            )
        }
        composableNoTransition(Screen.CreateSchedule.route) {
            CreateScheduleScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }
        composableNoTransition(Screen.Schedules.route) {
            SchedulesScreen(
                onMenuClick = onMenuClick,
                onCreateScheduleClick = { navController.navigate(Screen.CreateSchedule.route) },
                onScheduleClick = { scheduleId ->
                    navController.navigate(Screen.EditSchedule.createRoute(scheduleId))
                }
            )
        }
        composable(
            route = Screen.EditSchedule.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: ""
            EditScheduleScreen(
                scheduleId = scheduleId,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() },
                onDeleteClick = { navController.popBackStack() }
            )
        }
    }
}

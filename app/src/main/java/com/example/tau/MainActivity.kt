package com.example.tau

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tau.ui.components.Sidebar
import com.example.tau.ui.navigation.AppNavHost
import com.example.tau.ui.theme.TauTheme

private val AUTH_ROUTES = listOf("signup", "login")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TauTheme {
                TauApp()
            }
        }
    }
}

@Composable
private fun TauApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    var isSidebarOpen by remember { mutableStateOf(false) }

    val isAuthenticationScreen = currentRoute in AUTH_ROUTES

    Box(modifier = Modifier.fillMaxSize()) {
        AppNavHost(
            navController = navController,
            onMenuClick = { isSidebarOpen = true },
            modifier = Modifier.fillMaxSize()
        )

        if (!isAuthenticationScreen) {
            Sidebar(
                isOpen = isSidebarOpen,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                onClose = { isSidebarOpen = false }
            )
        }
    }
}
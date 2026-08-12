package com.example.calltranscriber.ui.screens

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calltranscriber.ui.screens.home.HomeScreen
import com.example.calltranscriber.ui.screens.history.HistoryScreen
import com.example.calltranscriber.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "Home")
    data object History : Screen("history", "History")
    data object Settings : Screen("settings", "Settings")
    data object Approval : Screen("approval/{recordId}", "Approval")
    data object Result : Screen("result/{recordId}", "Result")
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination

    val bottomItems = listOf(Screen.Home, Screen.History, Screen.Settings)
    val showBottomBar = bottomItems.any {
        currentDestination?.hierarchy?.any { destination -> destination.route == it.route } == true
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.weight(1f),
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) { HomeScreen(onNavigateApproval = { recordId ->
                    navController.navigate("approval/$recordId")
                }) }
                composable(Screen.History.route) { HistoryScreen(onNavigateApproval = { recordId ->
                    navController.navigate("approval/$recordId")
                }) }
                composable(Screen.Settings.route) { SettingsScreen() }
                composable(Screen.Approval.route) {
                    val recordId = it.arguments?.getString("recordId")?.toLongOrNull() ?: 0L
                    ApprovalScreen(
                        recordId = recordId,
                        onDone = { navController.popBackStack() },
                        onViewResult = { navController.navigate("result/$recordId") }
                    )
                }
                composable(Screen.Result.route) {
                    val recordId = it.arguments?.getString("recordId")?.toLongOrNull() ?: 0L
                    ResultScreen(recordId = recordId, onBack = { navController.popBackStack() })
                }
            }

            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val icon = when (screen) {
                                    Screen.Home -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                                    Screen.History -> if (selected) Icons.Filled.History else Icons.Outlined.History
                                    else -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                                }
                                androidx.compose.material3.Icon(icon, contentDescription = screen.label)
                            },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    }
}

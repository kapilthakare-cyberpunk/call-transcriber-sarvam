package com.example.calltranscriber.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.calltranscriber.ui.screens.approval.ApprovalScreen
import com.example.calltranscriber.ui.screens.history.HistoryScreen
import com.example.calltranscriber.ui.screens.home.HomeScreen
import com.example.calltranscriber.ui.screens.result.ResultScreen
import com.example.calltranscriber.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple("home", "Home", Icons.Default.Home),
                    Triple("history", "History", Icons.Default.History),
                    Triple("settings", "Settings", Icons.Default.Settings)
                )
                items.forEach { (route, label, icon) ->
                    val selected = currentDestination?.hierarchy?.any { it.route == route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("history") { HistoryScreen() }
            composable("settings") { SettingsScreen() }
            composable("approval") { ApprovalScreen() }
            composable("result") { ResultScreen() }
        }
    }
}

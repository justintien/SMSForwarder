package com.jiapan.smsfowarder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jiapan.smsfowarder.ui.history.HistoryScreen
import com.jiapan.smsfowarder.ui.settings.SettingsScreen

private sealed class Dest(val route: String, val label: String, val icon: ImageVector) {
    data object History : Dest("history", "記錄", Icons.AutoMirrored.Filled.Message)
    data object Settings : Dest("settings", "設定", Icons.Filled.Settings)
}

private val destinations = listOf(Dest.History, Dest.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsForwarderApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        topBar = {
            val title = destinations.firstOrNull { dest ->
                currentDestination?.hierarchy?.any { it.route == dest.route } == true
            }?.label ?: "SMS Forwarder"
            TopAppBar(title = { Text(title) })
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { dest ->
                    val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Dest.History.route,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Dest.History.route) { HistoryScreen() }
            composable(Dest.Settings.route) { SettingsScreen() }
        }
    }
}

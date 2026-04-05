package com.holidaymessenger.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.holidaymessenger.ui.birthdays.BirthdayScreen
import com.holidaymessenger.ui.contacts.ContactPickerScreen
import com.holidaymessenger.ui.history.HistoryScreen
import com.holidaymessenger.ui.holidays.HolidayScreen
import com.holidaymessenger.ui.home.HomeScreen
import com.holidaymessenger.ui.recurring.RecurringEditScreen
import com.holidaymessenger.ui.recurring.RecurringScreen
import com.holidaymessenger.ui.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Holidays : Screen("holidays", "Holidays", Icons.Filled.Celebration)
    data object Birthdays : Screen("birthdays", "Birthdays", Icons.Filled.Cake)
    data object Recurring : Screen("recurring", "Recurring", Icons.Filled.Repeat)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Holidays,
    Screen.Birthdays,
    Screen.Recurring,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToHistory = { navController.navigate("history") }
                )
            }
            composable(Screen.Holidays.route) {
                HolidayScreen(
                    onPickContacts = { holidayId ->
                        navController.navigate("contacts/holiday/$holidayId")
                    }
                )
            }
            composable(Screen.Birthdays.route) {
                BirthdayScreen()
            }
            composable(Screen.Recurring.route) {
                RecurringScreen(
                    onAddNew = { navController.navigate("recurring/edit") },
                    onEdit = { id -> navController.navigate("recurring/edit/$id") }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable("history") {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable("contacts/holiday/{holidayId}") { backStackEntry ->
                val holidayId = backStackEntry.arguments?.getString("holidayId")?.toLongOrNull() ?: 0
                ContactPickerScreen(
                    holidayId = holidayId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("recurring/edit") {
                RecurringEditScreen(
                    scheduledMessageId = null,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("recurring/edit/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                RecurringEditScreen(
                    scheduledMessageId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

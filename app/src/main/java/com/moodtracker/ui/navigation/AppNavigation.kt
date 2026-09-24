package com.moodtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moodtracker.ui.screens.HomeScreen
import com.moodtracker.ui.screens.RecordScreen
import com.moodtracker.ui.screens.SettingsScreen
import com.moodtracker.ui.screens.StatsScreen
import com.moodtracker.viewmodel.MoodViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val iconSelected: ImageVector) {
    data object Record : Screen("record", "记录", Icons.Outlined.EditNote, Icons.Filled.EditNote)
    data object History : Screen("history", "历史", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    data object Stats : Screen("stats", "统计", Icons.Outlined.BarChart, Icons.Filled.BarChart)
    data object Settings : Screen("settings", "设置", Icons.Outlined.EditNote, Icons.Filled.EditNote)
}

private val bottomNav = listOf(Screen.Record, Screen.History, Screen.Stats)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val viewModel: MoodViewModel = viewModel(
        factory = MoodViewModel.factory(context.applicationContext as android.app.Application)
    )

    val showBottomBar = currentDestination?.route in bottomNav.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNav.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.iconSelected else screen.icon,
                                    contentDescription = screen.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Record.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Record.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.History.route) {
                RecordScreen(viewModel = viewModel)
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

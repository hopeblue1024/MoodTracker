package com.moodtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moodtracker.MoodTrackerApp
import com.moodtracker.ui.screens.CalendarScreen
import com.moodtracker.ui.screens.HomeScreen
import com.moodtracker.ui.screens.RecordScreen
import com.moodtracker.ui.screens.SettingsScreen
import com.moodtracker.ui.screens.StatsScreen
import com.moodtracker.viewmodel.MoodViewModel
import com.moodtracker.viewmodel.MoodViewModelFactory

/** 底部导航项数据 */
private data class NavItem(val route: String, val label: String, val icon: ImageVector)

/**
 * 应用主导航 — 底部 4 Tab (首页/记录/日历/统计) + 设置页
 */
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as MoodTrackerApp
    val viewModel: MoodViewModel = viewModel(
        factory = MoodViewModelFactory(app.database, app.preferences, app)
    )

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        NavItem("home", "首页", Icons.Default.Home),
        NavItem("record", "记录", Icons.Default.Add),
        NavItem("calendar", "日历", Icons.Default.DateRange),
        NavItem("stats", "统计", Icons.Default.BarChart),
    )

    // 设置页不显示底部导航
    val showBottomBar = currentRoute != "settings"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route ||
                            (item.route == "record" && currentRoute?.startsWith("edit/") == true)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onAddRecord = { navController.navigate("record") },
                    onEditRecord = { id -> navController.navigate("edit/$id") },
                    onSettings = { navController.navigate("settings") }
                )
            }
            composable("record") {
                RecordScreen(
                    viewModel = viewModel,
                    recordId = -1L,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit/{recordId}",
                arguments = listOf(navArgument("recordId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: -1L
                RecordScreen(
                    viewModel = viewModel,
                    recordId = recordId,
                    onDone = { navController.popBackStack() }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    viewModel = viewModel,
                    onEditRecord = { id -> navController.navigate("edit/$id") }
                )
            }
            composable("stats") {
                StatsScreen(viewModel = viewModel)
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

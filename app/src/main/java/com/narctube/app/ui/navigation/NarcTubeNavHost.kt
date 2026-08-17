package com.narctube.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.narctube.app.R
import com.narctube.app.ui.downloads.DownloadsScreen
import com.narctube.app.ui.home.HomeScreen
import com.narctube.app.ui.settings.SettingsScreen

private sealed class Destination(val route: String, val labelRes: Int) {
    data object Home : Destination("home", R.string.nav_home)
    data object Downloads : Destination("downloads", R.string.nav_downloads)
    data object Settings : Destination("settings", R.string.nav_settings)
}

private val bottomBarDestinations = listOf(Destination.Home, Destination.Downloads, Destination.Settings)

@Composable
fun NarcTubeNavHost(sharedUrl: String? = null) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBar {
                bottomBarDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val icon = when (destination) {
                                Destination.Home -> Icons.Filled.Home
                                Destination.Downloads -> Icons.Filled.Download
                                Destination.Settings -> Icons.Filled.Settings
                            }
                            Icon(icon, contentDescription = stringResource(destination.labelRes))
                        },
                        label = { Text(stringResource(destination.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Destination.Home.route) { HomeScreen(sharedUrl = sharedUrl) }
            composable(Destination.Downloads.route) { DownloadsScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}

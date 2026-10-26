package br.edu.ifrs.genki.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.edu.ifrs.genki.R
import br.edu.ifrs.genki.ui.home.HomeScreen
import br.edu.ifrs.genki.ui.medications.MedicationsScreen
import br.edu.ifrs.genki.ui.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object MedicationsRoute
@Serializable object SettingsRoute

private enum class TopLevelTab(val route: Any, @param:StringRes val label: Int, val icon: ImageVector) {
    Home(HomeRoute, R.string.tab_home, Icons.Filled.Home),
    Medications(MedicationsRoute, R.string.tab_medications, Icons.AutoMirrored.Filled.List),
    Settings(SettingsRoute, R.string.tab_settings, Icons.Filled.Settings),
}

@Composable
fun GenkiApp() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                TopLevelTab.entries.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.label)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<HomeRoute> { HomeScreen() }
            composable<MedicationsRoute> { MedicationsScreen() }
            composable<SettingsRoute> { SettingsScreen() }
        }
    }
}

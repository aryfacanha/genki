package br.edu.ifrs.genki.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import br.edu.ifrs.genki.ui.theme.GenkiNavInactive
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object MedicationsRoute
@Serializable object SettingsRoute

// Ícone de comprimidos da aba Medicamentos, copiado do protótipo (`IconPills` em `src/shared.tsx`).
private val PillsIcon: ImageVector = ImageVector.Builder(
    name = "Pills",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(
        "M4.22 11.29l6.36-6.36a4.243 4.243 0 016 6L10.22 17.3a4.243 4.243 0 01-6-6z" +
            "m10.25 7.45l1.06-1.06a2.12 2.12 0 000-3l-1.06-1.06-4.24 4.24 1.06 1.06c.83.83 2.17.83 3-.18z"
    ),
    fill = SolidColor(Color.Black),
).build()

private enum class TopLevelTab(val route: Any, @param:StringRes val label: Int, val icon: ImageVector) {
    Home(HomeRoute, R.string.tab_home, Icons.Filled.Home),
    Medications(MedicationsRoute, R.string.tab_medications, PillsIcon),
    Settings(SettingsRoute, R.string.tab_settings, Icons.Filled.Settings),
}

@Composable
fun GenkiApp() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Como no protótipo (`BottomNav`): fundo da superfície com linha fina acima, sem elevação;
            // indicador verde-claro atrás do ícone ativo e rótulo de 16 sp sempre visível.
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
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
                            label = {
                                Text(
                                    stringResource(tab.label),
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = GenkiNavInactive,
                                unselectedTextColor = GenkiNavInactive,
                            ),
                        )
                    }
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

package br.edu.ifrs.genki.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.edu.ifrs.genki.R
import br.edu.ifrs.genki.reminders.Notifications
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.details.DetailScreen
import br.edu.ifrs.genki.ui.form.MedicationFormScreen
import br.edu.ifrs.genki.ui.home.HomeScreen
import br.edu.ifrs.genki.ui.medications.MedicationsScreen
import br.edu.ifrs.genki.ui.settings.SettingsScreen
import br.edu.ifrs.genki.ui.theme.GenkiNavInactive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object MedicationsRoute
@Serializable object SettingsRoute
@Serializable data class DetailRoute(val id: Long)
@Serializable object AddMedicationRoute
@Serializable data class EditMedicationRoute(val id: Long)

private enum class TopLevelTab(val route: Any, @param:StringRes val label: Int, val icon: ImageVector) {
    Home(HomeRoute, R.string.tab_home, Icons.Filled.Home),
    Medications(MedicationsRoute, R.string.tab_medications, PillsIcon),
    Settings(SettingsRoute, R.string.tab_settings, Icons.Filled.Settings),
}

@Composable
fun GenkiApp() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
    // Ao salvar um medicamento sem a permissão de notificações, explica e pede (ESPEC §6, Android 13+).
    val context = LocalContext.current
    var explainNotifications by remember { mutableStateOf(false) }
    val requestNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    // Barra inferior só nas três abas; detalhes e formulários ocupam a tela inteira.
    val onTopLevel = TopLevelTab.entries.any { tab -> currentDestination?.hasRoute(tab.route::class) == true }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            // Na Hoje e na lista, o aviso fica acima do botão "Adicionar medicamento", como no protótipo.
            val aboveAddButton = currentDestination?.hasRoute(MedicationsRoute::class) == true ||
                currentDestination?.hasRoute(HomeRoute::class) == true
            SnackbarHost(snackbarHostState, Modifier.padding(bottom = if (aboveAddButton) 80.dp else 0.dp))
        },
        bottomBar = {
            if (!onTopLevel) return@Scaffold
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
                                // Com fonte ampliada (1,3× no emulador) "Medicamentos" era cortado; o rótulo
                                // encolhe só o necessário para caber, sem passar de 16 sp na escala normal.
                                Text(
                                    stringResource(tab.label),
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    autoSize = TextAutoSize.StepBased(
                                        minFontSize = 12.sp,
                                        maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
                                    ),
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
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
        ) {
            composable<HomeRoute> { HomeScreen(onAdd = { navController.navigate(AddMedicationRoute) }) }
            composable<MedicationsRoute> {
                MedicationsScreen(
                    onOpen = { id -> navController.navigate(DetailRoute(id)) },
                    onAdd = { navController.navigate(AddMedicationRoute) },
                )
            }
            composable<SettingsRoute> { SettingsScreen() }
            composable<DetailRoute> { entry ->
                val id = entry.toRoute<DetailRoute>().id
                DetailScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(EditMedicationRoute(id)) },
                    onDeleted = { navController.popBackStack() },
                    showMessage = showMessage,
                )
            }
            composable<AddMedicationRoute> {
                MedicationFormScreen(
                    editId = null,
                    onClose = { navController.popBackStack() },
                    onSaved = { message ->
                        showMessage(message)
                        navController.popBackStack()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.canNotify(context)) {
                            explainNotifications = true
                        }
                    },
                )
            }
            composable<EditMedicationRoute> { entry ->
                MedicationFormScreen(
                    editId = entry.toRoute<EditMedicationRoute>().id,
                    onClose = { navController.popBackStack() },
                    onSaved = { message -> showMessage(message); navController.popBackStack() },
                )
            }
        }
    }

    if (explainNotifications) {
        AlertDialog(
            onDismissRequest = { explainNotifications = false },
            title = { Text("Permitir notificações?") },
            text = { Text("Para avisar nos horários dos medicamentos, o Genki precisa mostrar notificações.") },
            confirmButton = {
                TextButton(onClick = {
                    explainNotifications = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("Permitir") }
            },
            dismissButton = { TextButton(onClick = { explainNotifications = false }) { Text("Agora não") } },
        )
    }
}

package br.edu.ifrs.genki.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.R
import br.edu.ifrs.genki.camera.ui.CameraScreen
import br.edu.ifrs.genki.reminders.Notifications
import br.edu.ifrs.genki.ui.intro.IntroScreen
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.add.AddChoiceScreen
import br.edu.ifrs.genki.ui.details.DetailScreen
import br.edu.ifrs.genki.ui.form.FormMode
import br.edu.ifrs.genki.ui.form.MedicationFormScreen
import br.edu.ifrs.genki.ui.home.HomeScreen
import br.edu.ifrs.genki.ui.medications.MedicationsScreen
import br.edu.ifrs.genki.ui.settings.SettingsScreen
import br.edu.ifrs.genki.ui.theme.GenkiNavInactive
import br.edu.ifrs.genki.ui.tutorial.LocalTutorial
import br.edu.ifrs.genki.ui.tutorial.TutorialController
import br.edu.ifrs.genki.ui.tutorial.TutorialOverlay
import br.edu.ifrs.genki.ui.tutorial.TutorialScreen
import br.edu.ifrs.genki.ui.tutorial.TutorialTarget
import br.edu.ifrs.genki.ui.tutorial.tutorialTarget
import br.edu.ifrs.genki.voice.ui.VoiceScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable object IntroRoute
@Serializable object HomeRoute
@Serializable object MedicationsRoute
@Serializable object SettingsRoute
@Serializable data class DetailRoute(val id: Long, val fromHome: Boolean = false)
@Serializable object AddChoiceRoute
/** [fromSession] = começa com o rascunho da câmera. */
@Serializable data class VoiceRoute(val fromSession: Boolean = false)
@Serializable object CameraRoute
/** [fromSession] = formulário começa com o rascunho da voz ("Prefiro digitar"). */
@Serializable data class AddMedicationRoute(val fromSession: Boolean = false)
@Serializable data class EditMedicationRoute(val id: Long)
/** "Corrigir" um rascunho na revisão da voz. */
@Serializable data class FixDraftRoute(val localId: String)

private enum class TopLevelTab(val route: Any, @param:StringRes val label: Int, val icon: ImageVector) {
    Home(HomeRoute, R.string.tab_home, Icons.Filled.Home),
    Medications(MedicationsRoute, R.string.tab_medications, PillsIcon),
    Settings(SettingsRoute, R.string.tab_settings, Icons.Filled.Settings),
}

@Composable
fun GenkiApp(openHomeRequests: Int = 0) {
    val navController = rememberNavController()
    // Toque na notificação do lembrete: vai para a Hoje (cartão "Agora"), exceto no meio de um cadastro ou edição,
    // que não pode ser descartado sem perguntar.
    LaunchedEffect(openHomeRequests) {
        if (openHomeRequests == 0) return@LaunchedEffect
        val current = navController.currentDestination
        val inForm = listOf(AddChoiceRoute::class, VoiceRoute::class, CameraRoute::class, AddMedicationRoute::class,
            EditMedicationRoute::class, FixDraftRoute::class).any { current?.hasRoute(it) == true }
        if (!inForm) {
            navController.navigate(HomeRoute) {
                popUpTo<HomeRoute>()
                launchSingleTop = true
            }
        }
    }
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
    // "Resolver em Ajustes" da Hoje: Ajustes abre já em Permissões.
    var scrollToPermissions by remember { mutableStateOf(false) }
    val requestNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    // Depois de cadastrar (formulário ou voz): aviso, volta e, sem permissão, explica as notificações.
    val onAdded: (String) -> Unit = { message ->
        showMessage(message)
        navController.popBackStack()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.canNotify(context)) {
            explainNotifications = true
        }
    }
    val start: Any = remember {
        if ((context.applicationContext as GenkiApplication).container.settings.introDone) HomeRoute else IntroRoute
    }
    // Barra inferior só nas três abas; detalhes e formulários ocupam a tela inteira.
    val onTopLevel = TopLevelTab.entries.any { tab -> currentDestination?.hasRoute(tab.route::class) == true }

    // Tutorial guiado (DEC-044): vai sozinho à "Adicionar medicamento" nos passos de lá e volta para a Hoje.
    val tutorial = remember { TutorialController() }
    var tutorialOpenedAdd by remember { mutableStateOf(false) }
    LaunchedEffect(tutorial.step) {
        val onAdd = navController.currentDestination?.hasRoute(AddChoiceRoute::class) == true
        if (tutorial.current?.screen == TutorialScreen.ADD) {
            if (!onAdd) {
                navController.navigate(AddChoiceRoute) { launchSingleTop = true }
                tutorialOpenedAdd = true
            }
        } else if (tutorialOpenedAdd) {
            if (onAdd) navController.popBackStack()
            tutorialOpenedAdd = false
        }
    }
    val startTutorial = {
        navController.navigate(HomeRoute) {
            popUpTo<HomeRoute>()
            launchSingleTop = true
        }
        tutorial.start()
    }

    CompositionLocalProvider(LocalTutorial provides tutorial) { Box(Modifier.fillMaxSize()) {
    Scaffold(
        // Durante o tutorial, o leitor de tela fica só no balão.
        modifier = Modifier.fillMaxSize().then(if (tutorial.step != null || tutorial.offering) Modifier.clearAndSetSemantics { } else Modifier),
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
                            modifier = when (tab) {
                                TopLevelTab.Medications -> Modifier.tutorialTarget(TutorialTarget.TAB_MEDICATIONS)
                                TopLevelTab.Settings -> Modifier.tutorialTarget(TutorialTarget.TAB_SETTINGS)
                                TopLevelTab.Home -> Modifier
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    // A Hoje é a raiz das abas, também quando o app começou pela introdução (RV-4041-04).
                                    popUpTo<HomeRoute> { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = {
                                // Com fonte ampliada "Medicamentos" era cortado; o rótulo encolhe só o necessário
                                // para caber (8 sp na escala 2,0× ainda dá 16 dp na tela), sem passar de 16 sp na escala normal.
                                Text(
                                    stringResource(tab.label),
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    autoSize = TextAutoSize.StepBased(
                                        minFontSize = 8.sp,
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
            // Primeira abertura: introdução (DEC-040); depois, sempre a Hoje.
            startDestination = start,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
            // Troca de tela sem esmaecer (pedido do autor, 27/10).
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onAdd = { navController.navigate(AddChoiceRoute) },
                    onOpen = { id -> navController.navigate(DetailRoute(id, fromHome = true)) },
                    // Como o toque na aba Ajustes, já rolando até Permissões (bateria do autor, 30/10).
                    onOpenSettings = {
                        scrollToPermissions = true
                        navController.navigate(SettingsRoute) {
                            popUpTo<HomeRoute> { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable<MedicationsRoute> {
                MedicationsScreen(
                    onOpen = { id -> navController.navigate(DetailRoute(id)) },
                    onAdd = { navController.navigate(AddChoiceRoute) },
                )
            }
            // A escolha sai da pilha: voltar do formulário ou da voz leva à aba de onde veio.
            composable<AddChoiceRoute> {
                AddChoiceScreen(
                    onBack = { navController.popBackStack() },
                    onVoice = { navController.navigate(VoiceRoute()) { popUpTo<AddChoiceRoute> { inclusive = true } } },
                    onType = { navController.navigate(AddMedicationRoute()) { popUpTo<AddChoiceRoute> { inclusive = true } } },
                    onCamera = { navController.navigate(CameraRoute) { popUpTo<AddChoiceRoute> { inclusive = true } } },
                )
            }
            // Da câmera segue para a voz ou o formulário já com nome e concentração (RF22).
            composable<CameraRoute> {
                CameraScreen(
                    onClose = { navController.popBackStack() },
                    onVoice = { navController.navigate(VoiceRoute(fromSession = true)) { popUpTo<CameraRoute> { inclusive = true } } },
                    onType = { navController.navigate(AddMedicationRoute(fromSession = true)) { popUpTo<CameraRoute> { inclusive = true } } },
                )
            }
            composable<VoiceRoute> { entry ->
                VoiceScreen(
                    fromSession = entry.toRoute<VoiceRoute>().fromSession,
                    onClose = { navController.popBackStack() },
                    onType = { navController.navigate(AddMedicationRoute(fromSession = true)) { popUpTo<VoiceRoute> { inclusive = true } } },
                    onFix = { localId -> navController.navigate(FixDraftRoute(localId)) },
                    onSaved = onAdded,
                )
            }
            composable<FixDraftRoute> { entry ->
                MedicationFormScreen(
                    mode = FormMode.FixDraft(entry.toRoute<FixDraftRoute>().localId),
                    onClose = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable<IntroRoute> {
                IntroScreen(onDone = {
                    navController.navigate(HomeRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                    // Pergunta antes, na tela e em voz alta, se a pessoa quer o tutorial.
                    tutorial.offer()
                })
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    onTutorial = startTutorial,
                    scrollToPermissions = scrollToPermissions,
                    onScrolled = { scrollToPermissions = false },
                )
            }
            composable<DetailRoute> { entry ->
                val route = entry.toRoute<DetailRoute>()
                val id = route.id
                DetailScreen(
                    id = id,
                    backLabel = if (route.fromHome) "Voltar para Hoje" else "Voltar para Medicamentos",
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(EditMedicationRoute(id)) },
                    onDeleted = { navController.popBackStack() },
                    showMessage = showMessage,
                )
            }
            composable<AddMedicationRoute> { entry ->
                MedicationFormScreen(
                    mode = FormMode.New(fromSession = entry.toRoute<AddMedicationRoute>().fromSession),
                    onClose = { navController.popBackStack() },
                    onSaved = onAdded,
                )
            }
            composable<EditMedicationRoute> { entry ->
                MedicationFormScreen(
                    mode = FormMode.Edit(entry.toRoute<EditMedicationRoute>().id),
                    onClose = { navController.popBackStack() },
                    onSaved = { message -> showMessage(message); navController.popBackStack() },
                )
            }
        }
    }

    TutorialOverlay(tutorial)
    } }

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

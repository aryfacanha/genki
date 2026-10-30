package br.edu.ifrs.genki.voice.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.domain.DuplicateNames
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Frequency
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.TreatmentType
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.form.DoseInput
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.ChoiceGrid
import br.edu.ifrs.genki.ui.components.FieldError
import br.edu.ifrs.genki.ui.components.GenkiCard
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.InfoRow
import br.edu.ifrs.genki.ui.components.MicIcon
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.ui.components.SectionTitle
import br.edu.ifrs.genki.ui.components.TimeDialog
import br.edu.ifrs.genki.ui.components.genkiFieldColors
import br.edu.ifrs.genki.voice.NameCommands
import br.edu.ifrs.genki.ui.components.VoiceMuteButton
import br.edu.ifrs.genki.voice.Speaker
import br.edu.ifrs.genki.voice.SpeechInput
import br.edu.ifrs.genki.voice.VoiceQuestions
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

/**
 * Cadastrar por voz (`UsarVozMedicamento.tsx`, ESPEC §7): início com o microfone, perguntas do que falta
 * (texto + fala, respostas por voz ou por toque) e revisão com "Corrigir" por toque antes de salvar.
 */
@Composable
fun VoiceScreen(
    fromSession: Boolean,
    onClose: () -> Unit,
    onType: () -> Unit,
    onFix: (String) -> Unit,
    onSaved: (String) -> Unit,
    viewModel: VoiceViewModel = viewModel(factory = VoiceViewModel.factory(fromSession)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val speaker = remember { Speaker(context) }
    val input = remember {
        SpeechInput(context, object : SpeechInput.Events {
            override fun listening(active: Boolean) = viewModel.listening(active)
            override fun partial(text: String) = viewModel.partial(text)
            override fun result(text: String, latencyMs: Long?) = viewModel.heard(text, latencyMs)
            override fun error(code: Int) = viewModel.recognitionError(code)
            override fun pausing(active: Boolean) = viewModel.pausing(active)
        })
    }
    DisposableEffect(Unit) {
        onDispose {
            input.destroy()
            speaker.shutdown()
        }
    }
    LaunchedEffect(state.savedMessage) { state.savedMessage?.let(onSaved) }

    val startListening = {
        speaker.stop()
        if (input.isAvailable()) input.start() else viewModel.unavailable()
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startListening() else viewModel.micDenied()
    }
    // Depois de falar a pergunta, o microfone abre sozinho (DEC-040: sem tocar a cada pergunta). Só com a permissão já
    // dada, com a tela à vista e numa pergunta; se ninguém falar, o reconhecedor para e espera o toque.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // Pode ser desligado na introdução ou em Ajustes.
    val settings = remember { (context.applicationContext as GenkiApplication).container.settings }
    val autoListen = remember { settings.autoListen }
    // Assistente de voz (DEC-049): sem ele, as perguntas ficam só na tela e o microfone abre logo depois delas.
    val voiceOn = remember { settings.voiceAssistant }
    // Silenciar só nesta tela, sem desligar o assistente.
    var muted by rememberSaveable { mutableStateOf(false) }
    // Pergunta sendo lida agora (silenciar no meio dela abre o microfone como se a leitura tivesse terminado).
    var speakingId by remember { mutableIntStateOf(-1) }
    val current by rememberUpdatedState(state)
    val afterPrompt = { prompt: Prompt ->
        speakingId = -1
        val ready = prompt.listen && autoListen && current.phase in setOf(VoicePhase.ASKING, VoicePhase.NAMES) && !current.listening &&
            !current.correctionPending && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (ready && current.prompt?.id == prompt.id && input.isAvailable()) input.start()
    }
    LaunchedEffect(state.prompt) {
        val prompt = state.prompt ?: return@LaunchedEffect
        if (voiceOn && !muted) {
            speakingId = prompt.id
            speaker.speak(prompt.text) { afterPrompt(prompt) }
        } else {
            afterPrompt(prompt)
        }
    }
    val toggleMute = {
        muted = !muted
        val prompt = state.prompt
        if (muted) {
            speaker.stop()
            if (prompt != null && speakingId == prompt.id) afterPrompt(prompt)
        } else if (prompt != null && !state.listening) {
            // Voltou a voz: relê a pergunta da tela.
            speaker.speak(prompt.text)
        }
    }
    val listenNow = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            askMic.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val onMic = { if (state.listening) input.stop() else listenNow() }
    val preferTyping = {
        speaker.stop()
        if (state.listening) input.stop()
        if (viewModel.preferTyping()) onType()
    }

    var askDiscard by rememberSaveable { mutableStateOf(false) }
    val handleBack = {
        when {
            // Trocando o nome, Voltar desiste da troca mesmo ouvindo: descarta a fala, não a entrega (RV-INF-04).
            state.renaming != null -> { speaker.stop(); input.cancel(); viewModel.cancelRename() }
            state.listening -> input.stop()
            // Voltar no meio da correção por voz desfaz só a correção, não o cadastro.
            state.correctionPending -> viewModel.undoCorrection()
            state.correcting -> viewModel.cancelCorrection()
            // Com a pergunta de sair aberta, nada de falar nem de ouvir sozinho (RV-4041-01).
            state.drafts.isNotEmpty() -> { speaker.stop(); input.cancel(); askDiscard = true }
            else -> { viewModel.discard(); onClose() }
        }
    }
    BackHandler(onBack = handleBack)
    // "Cancelar" no topo sai do cadastro de qualquer ponto, como no formulário (teste informal, 29/10).
    val handleCancel = {
        speaker.stop()
        input.cancel()
        if (state.drafts.isNotEmpty()) askDiscard = true else { viewModel.discard(); onClose() }
    }

    // A leitura em voz alta segue o volume de mídia do Android. Quando fica abaixo de 30 %, um aviso aparece no meio
    // da tela e some sozinho em alguns segundos ou ao aumentar o volume (teste do autor no A31, 27/10).
    val audio = remember { context.getSystemService(AudioManager::class.java) }
    var volumeNotice by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        var wasLow = false
        while (true) {
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val low = audio.getStreamVolume(AudioManager.STREAM_MUSIC) * 10 < max * 3
            if (low && !wasLow) volumeNotice = true
            if (!low) volumeNotice = false
            wasLow = low
            delay(500)
        }
    }
    LaunchedEffect(volumeNotice) {
        if (volumeNotice) { delay(4_000); volumeNotice = false }
    }

    Box(Modifier.fillMaxSize()) {
        // O teclado empurra a tela, sem cobrir o campo da resposta digitada (teste do autor no A31, 27/10).
        Column(Modifier.fillMaxSize().imePadding()) {
            GenkiTopBar(
                when (state.phase) {
                    VoicePhase.REVIEW -> "Confira antes de salvar"
                    VoicePhase.NAMES -> "Confira os remédios"
                    else -> "Cadastrar por voz"
                },
                onBack = handleBack,
                actionLabel = "Cancelar",
                onAction = handleCancel,
                extra = if (voiceOn) ({ VoiceMuteButton(muted, toggleMute) }) else null,
            )
            when (state.phase) {
                VoicePhase.INTRO -> Intro(state, onMic, preferTyping)
                VoicePhase.NAMES -> Names(
                    state, onMic, preferTyping,
                    // Toque em qualquer ação: para de falar e de ouvir, para não misturar com a fala seguinte.
                    onRename = { speaker.stop(); input.cancel(); viewModel.beginRename(it) },
                    onTyped = { id, text -> speaker.stop(); input.cancel(); viewModel.typedName(id, text) },
                    onCancelRename = { speaker.stop(); input.cancel(); viewModel.cancelRename() },
                    onSplit = { speaker.stop(); input.cancel(); viewModel.splitName(it) },
                    onRemove = { speaker.stop(); input.cancel(); viewModel.removeName(it) },
                    onConfirm = { speaker.stop(); input.cancel(); viewModel.confirmNames() },
                )
                VoicePhase.ASKING -> Asking(
                    state, onMic, preferTyping,
                    // Resposta por toque enquanto ouvia sozinho: a escuta para, para não responder a pergunta seguinte.
                    onAnswer = { speaker.stop(); input.cancel(); viewModel.tapped(it) },
                    onCorrect = { input.cancel(); viewModel.beginCorrection(); listenNow() },
                    onKeep = viewModel::keepCorrection,
                    onUndo = viewModel::undoCorrection,
                    onDose = { speaker.stop(); input.cancel(); viewModel.chooseDose(it) },
                    onRemove = { speaker.stop(); input.cancel(); viewModel.remove(it) },
                    // Abriu o relógio, a quantidade, os dias ou o campo: para de falar e de ouvir sozinho.
                    onQuiet = { speaker.stop(); input.cancel() },
                )
                VoicePhase.REVIEW -> Review(
                    state, onMic, onFix, viewModel::remove, viewModel::save,
                    onKeep = viewModel::keepCorrection, onUndo = viewModel::undoCorrection,
                )
            }
        }
        AnimatedVisibility(
            visible = volumeNotice,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 800)),
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 6.dp,
            ) {
                Text(
                    "O volume está baixo. Aumente para ouvir as perguntas.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }

    if (state.duplicates.isNotEmpty()) {
        val names = DuplicateNames.list(state.duplicates)
        AlertDialog(
            onDismissRequest = viewModel::dismissDuplicates,
            title = { Text(if (state.duplicates.size == 1) "$names já está cadastrado" else "$names já estão cadastrados") },
            text = { Text("Dois cadastros iguais geram lembretes repetidos. Se for outro horário do mesmo remédio, você pode adicionar o horário no cadastro que já existe.") },
            confirmButton = { TextButton(onClick = viewModel::dismissDuplicates) { Text("Voltar e conferir") } },
            dismissButton = { TextButton(onClick = viewModel::confirmDuplicates) {
                    Text(if (state.duplicatesOnNames) "Continuar mesmo assim" else "Cadastrar mesmo assim")
                } },
        )
    }

    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            title = { Text("Descartar cadastro?") },
            text = { Text("As informações que você falou ainda não foram salvas e serão perdidas.") },
            confirmButton = { TextButton(onClick = { askDiscard = false }) { Text("Continuar cadastro") } },
            dismissButton = {
                TextButton(onClick = { askDiscard = false; speaker.stop(); viewModel.discard(); onClose() }) {
                    Text("Descartar", color = MaterialTheme.colorScheme.error)
                }
            },
        )
    }
}

@Composable
private fun ColumnScope.Intro(state: VoiceUiState, onMic: () -> Unit, onType: () -> Unit) {
    Column(
        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Conte quais medicamentos você usa, quanto toma e em quais horários.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Por exemplo: “${VoiceViewModel.EXAMPLE}”.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Você pode falar tudo de uma vez ou aos poucos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        MicButton(state.listening, size = 88, onClick = onMic)
        Spacer(Modifier.height(20.dp))
        Listening(state, idle = "Toque no microfone para falar")
        Spacer(Modifier.height(16.dp))
        Text(
            "Você poderá revisar tudo antes de salvar.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        TypeLink(onType)
    }
}

/**
 * "Confira os remédios" (DEC-046): a lista de nomes entendidos antes das doses e horários. Cada nome pode ser trocado
 * (toque e diga ou digite; "A, B" separa em dois), separado em um por palavra ou tirado no X; por voz valem
 * "remover X", "trocar X por Y" e mais nomes. "Continuar" ou "pronto" segue para as perguntas.
 */
@Composable
private fun ColumnScope.Names(
    state: VoiceUiState,
    onMic: () -> Unit,
    onType: () -> Unit,
    onRename: (String) -> Unit,
    onTyped: (String, String) -> Unit,
    onCancelRename: () -> Unit,
    onSplit: (String) -> Unit,
    onRemove: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp)) {
        SectionTitle(if (state.drafts.size == 1) "Entendi este remédio" else "Entendi estes ${state.drafts.size} remédios")
        Text(
            "Confira os nomes. Toque no nome para corrigir. Se estiver tudo certo, toque em Continuar.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.drafts.forEach { draft ->
                NameCard(
                    draft,
                    renaming = state.renaming == draft.localId,
                    onRename = { onRename(draft.localId) },
                    onTyped = { onTyped(draft.localId, it) },
                    onCancelRename = onCancelRename,
                    onSplit = { onSplit(draft.localId) },
                    onRemove = { onRemove(draft.localId) },
                )
            }
        }
        state.heard?.let {
            Text("Você disse: “$it”", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        }
        state.message?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Text(
            "Por voz, diga outro remédio para incluir, ou por exemplo: “remover Metoprolol”, “trocar Simvastatina por Sinvastatina”.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
    BottomActions {
        PrimaryButton("Continuar", onClick = onConfirm)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MicButton(state.listening, size = 64, onClick = onMic)
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Listening(
                    state,
                    idle = if (state.renaming != null) "Toque no microfone e diga o nome certo" else "Toque no microfone para corrigir ou incluir",
                    center = false,
                )
            }
        }
        // Como nas perguntas (sugestão da revisão do Codex, 29/10).
        TypeLink(onType, Modifier.align(Alignment.CenterHorizontally))
    }
}

/** Um nome da conferência: tocar troca, X tira, "Separar" divide dois remédios entendidos como um. */
@Composable
private fun NameCard(
    draft: MedicationDraft,
    renaming: Boolean,
    onRename: () -> Unit,
    onTyped: (String) -> Unit,
    onCancelRename: () -> Unit,
    onSplit: () -> Unit,
    onRemove: () -> Unit,
) {
    val name = draft.name ?: "Remédio sem nome"
    GenkiCard {
        Column(Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onRename,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f).semantics { contentDescription = "Corrigir o nome $name" },
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Tirar $name", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (NameCommands.canSplit(draft.name) && !renaming) {
                TextButton(onClick = onSplit, modifier = Modifier.padding(start = 4.dp).heightIn(min = 48.dp)) {
                    Text(
                        "São ${NameCommands.split(name).size} remédios? Separar",
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
            if (renaming) {
                var typed by rememberSaveable(draft.localId) { mutableStateOf(draft.name.orEmpty()) }
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        "Diga o nome certo no microfone ou digite aqui. Para separar, use vírgula: Rivaroxabana, Metoprolol.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    OutlinedTextField(
                        colors = genkiFieldColors(),
                        value = typed,
                        onValueChange = { typed = it },
                        label = { Text("Nome do remédio") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onTyped(typed) }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton("Cancelar", onClick = onCancelRename, modifier = Modifier.weight(1f))
                        PrimaryButton("Salvar nome", onClick = { onTyped(typed) }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.Asking(
    state: VoiceUiState,
    onMic: () -> Unit,
    onType: () -> Unit,
    onAnswer: (String) -> Unit,
    onCorrect: () -> Unit,
    onKeep: () -> Unit,
    onUndo: () -> Unit,
    onDose: (String) -> Unit,
    onRemove: (String) -> Unit,
    onQuiet: () -> Unit,
) {
    val question = state.question ?: return
    // Por pergunta: respondida por botão ou fala, o campo, os dias ou o relógio abertos não passam para a seguinte (A31-01).
    var typing by rememberSaveable(question.draftId, question.field) { mutableStateOf(false) }
    var typed by rememberSaveable(question.draftId, question.field) { mutableStateOf("") }
    // Horários pelo relógio, em vez de digitar (pedido do autor, 28/10).
    var picking by rememberSaveable(question.draftId, question.field) { mutableStateOf(false) }
    // "Corrigir por voz" fecha o relógio; voltar ou Desfazer reabre a pergunta sem ele (RV-DVC-05).
    LaunchedEffect(state.correcting) { if (state.correcting) picking = false }
    // Com o teclado aberto, rola até o fim para o campo e o "Enviar" ficarem à vista.
    val scroll = rememberScrollState()
    val imeVisible = WindowInsets.isImeVisible
    // O teclado sobe animado e o fim da tela muda várias vezes; acompanha até ele parar.
    LaunchedEffect(typing) { if (typing) snapshotFlow { scroll.maxValue }.collect { scroll.scrollTo(it) } }
    Column(Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 20.dp, vertical = 20.dp)) {
        SectionTitle(if (state.correctionPending) "Confira a correção" else "Entendi até agora")
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.drafts.forEach { SoFarCard(it, current = it.localId == question.draftId, onAsk = onQuiet, onRemove = { onRemove(it.localId) }) }
        }
        state.heard?.let {
            Text(
                "Você disse: “$it”",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        when {
            state.correctionPending -> Text(
                "Se estiver certo, toque em Manter correção. Para voltar como estava, toque em Desfazer.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
            state.correcting -> {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Diga a correção",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Text(
                    "Exemplo: o nome é metoprolol; ou: na verdade é às 9",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            else -> {
                // Correção fora da pergunta: o nome entendido errado, por exemplo (pedido do autor, 27/10).
                TextButton(onClick = onCorrect, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Algo errado? Corrigir por voz", textDecoration = TextDecoration.Underline)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    question.text,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Text(question.example, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                if (question.quickAnswers.isNotEmpty()) {
                    ChoiceGrid(
                        question.quickAnswers,
                        label = { it.label },
                        onClick = { onAnswer(it.text) },
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        // Resposta digitada (como no protótipo): datas e o que não está nos botões; passa pelo mesmo interpretador.
        if (!state.correctionPending) {
            if (typing && question.field == Field.DAYS && !state.correcting) {
                DayPicker(onDone = { onAnswer(it); typing = false })
            } else if (typing && question.field == Field.DOSE && !state.correcting) {
                DosePicker(onDone = { onDose(it); typing = false })
            } else if (picking && !state.correcting) {
                TimesPicker(question.everyHours, onDone = { onAnswer(it); picking = false })
            } else if (typing) {
                // Abre já com o teclado no campo.
                val focus = remember { FocusRequester() }
                LaunchedEffect(Unit) { focus.requestFocus() }
                OutlinedTextField(
                    colors = genkiFieldColors(),
                    value = typed,
                    onValueChange = { typed = it },
                    label = { Text(if (state.correcting) "Digite a correção" else "Digite a resposta") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (typed.isNotBlank()) { onAnswer(typed); typed = ""; typing = false }
                    }),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).focusRequester(focus),
                )
                PrimaryButton(
                    "Enviar",
                    onClick = { if (typed.isNotBlank()) { onAnswer(typed); typed = ""; typing = false } },
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                if (question.field == Field.TIMES && !state.correcting) {
                    TextButton(onClick = { onQuiet(); picking = true }, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                        Text("Escolher no relógio", textDecoration = TextDecoration.Underline)
                    }
                }
                TextButton(onClick = { onQuiet(); typing = true }, modifier = Modifier.padding(top = 8.dp).heightIn(min = 48.dp)) {
                    Text(
                        when {
                            state.correcting -> "Digitar a resposta"
                            question.field == Field.DAYS -> "Escolher os dias"
                            question.field == Field.DOSE -> "Escolher a quantidade"
                            else -> "Digitar a resposta"
                        },
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
        }
    }
    if (state.correctionPending) {
        BottomActions {
            PrimaryButton("Manter correção", onClick = onKeep)
            SecondaryButton("Desfazer", onClick = onUndo)
        }
        return
    }
    // Digitando (teclado aberto), o microfone sai de baixo para o campo e o "Enviar" caberem acima do teclado.
    if (typing && imeVisible) return
    BottomActions {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MicButton(state.listening, size = 64, onClick = onMic)
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Listening(
                    state,
                    idle = if (state.correcting) "Toque no microfone e diga a correção" else "Toque no microfone para responder",
                    center = false,
                )
            }
        }
        TypeLink(onType, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun ColumnScope.Review(
    state: VoiceUiState,
    onMic: () -> Unit,
    onFix: (String) -> Unit,
    onRemove: (String) -> Unit,
    onSave: () -> Unit,
    onKeep: () -> Unit,
    onUndo: () -> Unit,
) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (state.correctionPending) {
            Text(
                "Confira a correção",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            state.heard?.let { Text("Você disse: “$it”", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(
                "Se estiver certo, toque em Manter correção. Para voltar como estava, toque em Desfazer.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
        } else {
            Text(
                "Para mudar algo, toque em Corrigir ou fale de novo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.drafts.forEach { draft ->
                ReviewCard(
                    draft,
                    onFix = { onFix(draft.localId) },
                    onRemove = if (state.drafts.size > 1) ({ onRemove(draft.localId) }) else null,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Listening(state, idle = "", center = false)
    }
    if (state.correctionPending) {
        BottomActions {
            PrimaryButton("Manter correção", onClick = onKeep)
            SecondaryButton("Desfazer", onClick = onUndo)
        }
        return
    }
    BottomActions {
        PrimaryButton(if (state.drafts.size > 1) "Confirmar e salvar todos" else "Confirmar e salvar", onClick = onSave)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            MicButton(state.listening, size = 48, onClick = onMic)
            Spacer(Modifier.size(8.dp))
            Text(if (state.listening) "Ouvindo… toque para parar" else "Corrigir por voz")
        }
    }
}

/** Microfone redondo verde; ouvindo, mostra o estado e para ao tocar de novo. */
@Composable
private fun MicButton(listening: Boolean, size: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (listening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 4.dp,
        modifier = Modifier.size(size.dp).semantics {
            contentDescription = if (listening) "Parar de ouvir" else "Ativar microfone"
        },
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(MicIcon, contentDescription = null, modifier = Modifier.size((size * 0.43f).dp))
        }
    }
}

/** "Ouvindo…" com o texto parcial, ou a instrução; mensagem de erro abaixo. */
@Composable
private fun Listening(state: VoiceUiState, idle: String, center: Boolean = true) {
    val align = if (center) TextAlign.Center else TextAlign.Start
    Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        if (state.listening) {
            Text(
                // Entre trechos da mesma resposta (bateria do autor, 30/10: a fala ainda cortava).
                if (state.pausing) "Pode continuar falando. Para terminar, toque no microfone." else "Ouvindo… toque para parar",
                fontWeight = FontWeight.SemiBold, textAlign = align, modifier = Modifier.fillMaxWidth(),
            )
            if (state.partial.isNotEmpty()) {
                Text("“${state.partial}”", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = align, modifier = Modifier.fillMaxWidth())
            }
        } else if (idle.isNotEmpty()) {
            Text(idle, fontWeight = FontWeight.SemiBold, textAlign = align, modifier = Modifier.fillMaxWidth())
        }
        FieldError(state.message)
    }
}

@Composable
private fun TypeLink(onType: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onType, modifier = modifier.heightIn(min = 48.dp)) {
        Text("Prefiro digitar", textDecoration = TextDecoration.Underline)
    }
}

/** Resumo do que já foi entendido de um medicamento; o da pergunta atual fica destacado. */
@Composable
private fun SoFarCard(draft: MedicationDraft, current: Boolean, onAsk: () -> Unit, onRemove: () -> Unit) {
    val name = draft.name ?: "Medicamento sem nome"
    var confirm by rememberSaveable(draft.localId) { mutableStateOf(false) }
    GenkiCard {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(top = 8.dp),
                )
                // X discreto para tirar um medicamento entendido por engano ("Use" virou "Uso"; bateria do autor, 28/10).
                // Com "Tirar …?" aberto, a pergunta para de ser falada e o microfone não abre sozinho (RV-4041-01).
                IconButton(onClick = { onAsk(); confirm = true }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Tirar $name",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val lines = listOfNotNull(
                draft.concentration,
                draft.dose,
                draft.days?.let(MedicationText::days),
                draft.times.takeIf { it.isNotEmpty() }?.let(MedicationText::times),
                draft.treatment?.let(MedicationText::treatment),
            )
            if (lines.isNotEmpty()) {
                Text(lines.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Tirar $name?") },
            text = { Text("O que foi entendido sobre ele não será cadastrado.") },
            confirmButton = {
                TextButton(onClick = { confirm = false; onRemove() }) { Text("Tirar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } },
        )
    }
}

/**
 * Quantidade na pergunta da dose com o mesmo menu e − número + do formulário (DEC-039), em vez de digitar (bateria do
 * autor no A31, 28/10).
 */
@Composable
private fun DosePicker(onDone: (String) -> Unit) {
    var dose by rememberSaveable { mutableStateOf("") }
    Column(Modifier.padding(top = 16.dp)) {
        DoseInput(dose = dose, error = null, onChange = { dose = it }, title = false)
        if (dose.isNotBlank()) {
            PrimaryButton("Confirmar quantidade", onClick = { onDone(dose.trim()) }, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

/** Início e fim como o validador vai gravar: "por N dias" vira data final (DEC-003, RF08). */
private fun period(draft: MedicationDraft): String? {
    val start = draft.startDate ?: LocalDate.now()
    val end = draft.durationDays?.let { start.plusDays(it - 1L) } ?: draft.endDate ?: return null
    return MedicationText.period(start, end)
}

@Composable
private fun ReviewCard(draft: MedicationDraft, onFix: () -> Unit, onRemove: (() -> Unit)?) {
    val missing = "Falta informar"
    GenkiCard {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle(draft.name ?: "Medicamento sem nome", Modifier.weight(1f))
                TextButton(
                    onClick = onFix,
                    modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Corrigir ${draft.name.orEmpty()}" },
                ) { Text("Corrigir") }
            }
            draft.concentration?.let { InfoRow("Concentração", it) }
            InfoRow("Quanto toma", draft.dose ?: missing)
            InfoRow("Dias", draft.days?.let(MedicationText::days) ?: missing)
            InfoRow("Horários", draft.times.takeIf { it.isNotEmpty() }?.let(MedicationText::times) ?: missing)
            val treatment = draft.treatment
            InfoRow(
                "Duração",
                treatment?.let(MedicationText::treatment) ?: missing,
                secondary = if (treatment == TreatmentType.TEMPORARY) period(draft) ?: missing else null,
                help = if (treatment == TreatmentType.UNDEFINED) MedicationText.UNKNOWN_DURATION_HELP else null,
            )
            if (onRemove != null) {
                TextButton(onClick = onRemove, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text("Remover da lista", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * Horários escolhidos no relógio na pergunta dos horários (pedido do autor, 28/10): o relógio abre logo; cada
 * horário escolhido vira uma caixa marcada (tocar tira). Vira a frase "às 08:00 e às 20:00" e passa pelo mesmo
 * interpretador das outras respostas. Na pergunta do primeiro horário ([everyHours]), o primeiro escolhido gera os
 * horários do dia (RV-DVC-03) e cada um pode ser trocado ali mesmo, sem esperar a revisão (E12, DEC-040); com todos
 * os horários do dia na resposta, o interpretador os mantém como estão.
 */
@Composable
private fun TimesPicker(everyHours: Int?, onDone: (String) -> Unit) {
    var chosen by rememberSaveable { mutableStateOf(listOf<LocalTime>()) }
    var open by rememberSaveable { mutableStateOf(true) }
    // Horário do dia sendo trocado (pergunta do primeiro horário).
    var editing by rememberSaveable { mutableStateOf<LocalTime?>(null) }
    if (everyHours != null) {
        if (chosen.isNotEmpty()) {
            Text(
                "Horários do dia: ${MedicationText.times(chosen.sorted())}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
            Text(
                "Se algum não servir, toque nele para mudar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            ChoiceGrid(
                chosen.sorted(),
                label = MedicationText::time,
                onClick = { time -> editing = time },
                maxColumns = 4,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    } else if (chosen.isNotEmpty()) {
        Text(
            "Toque num horário para tirar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
        ChoiceGrid(
            chosen.sorted(),
            label = MedicationText::time,
            selected = { true },
            onClick = { time -> chosen = chosen - time },
            maxColumns = 4,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
    SecondaryButton(
        when {
            chosen.isEmpty() -> "Escolher horário"
            everyHours != null -> "Escolher outro primeiro horário"
            else -> "Adicionar outro horário"
        },
        onClick = { open = true },
        modifier = Modifier.padding(top = 16.dp),
    )
    if (chosen.isNotEmpty()) {
        PrimaryButton(
            if (chosen.size > 1) "Confirmar horários" else "Confirmar horário",
            onClick = { onDone(VoiceQuestions.timesAnswer(chosen)) },
            modifier = Modifier.padding(top = 8.dp),
        )
    }
    if (open) {
        TimeDialog(
            initial = if (everyHours != null) chosen.minOrNull() ?: LocalTime.of(8, 0) else VoiceQuestions.nextClockTime(chosen),
            taken = if (everyHours != null) emptySet() else chosen.toSet(),
            onDismiss = { open = false },
            onConfirm = { chosen = if (everyHours != null) Frequency.fill(it, everyHours) else chosen + it; open = false },
        )
    }
    editing?.let { old ->
        TimeDialog(
            initial = old,
            taken = (chosen - old).toSet(),
            onDismiss = { editing = null },
            onConfirm = { chosen = chosen - old + it; editing = null },
        )
    }
}

/**
 * Dias escolhidos por toque na pergunta dos dias, em vez de digitar (teste do autor no A31, 27/10). Vira a frase
 * "segunda, quarta e sexta" e passa pelo mesmo interpretador das outras respostas.
 */
@Composable
private fun DayPicker(onDone: (String) -> Unit) {
    var chosen by rememberSaveable { mutableStateOf(listOf<DayOfWeek>()) }
    // Marcado com ✓, não só pela cor.
    ChoiceGrid(
        MedicationText.WEEK,
        label = { MedicationText.dayName(it).replaceFirstChar { c -> c.uppercase() } },
        selected = { it in chosen },
        onClick = { day -> chosen = if (day in chosen) chosen - day else chosen + day },
        maxColumns = 4,
        modifier = Modifier.padding(top = 16.dp),
    )
    if (chosen.isNotEmpty()) {
        PrimaryButton(
            "Confirmar dias",
            onClick = {
                val names = MedicationText.WEEK.filter { it in chosen }.map(MedicationText::dayName)
                onDone(if (names.size == 7) "todos os dias" else names.dropLast(1).joinToString(", ").let {
                    if (it.isEmpty()) names.last() else "$it e ${names.last()}"
                })
            },
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

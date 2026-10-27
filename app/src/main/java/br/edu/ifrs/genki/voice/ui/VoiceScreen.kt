package br.edu.ifrs.genki.voice.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.TreatmentType
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.FieldError
import br.edu.ifrs.genki.ui.components.GenkiCard
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.InfoRow
import br.edu.ifrs.genki.ui.components.MicIcon
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SectionTitle
import br.edu.ifrs.genki.voice.SpeechInput
import br.edu.ifrs.genki.voice.Speaker

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
        })
    }
    DisposableEffect(Unit) {
        onDispose {
            input.destroy()
            speaker.shutdown()
        }
    }
    LaunchedEffect(state.prompt) { state.prompt?.let { speaker.speak(it.text) } }
    LaunchedEffect(state.savedMessage) { state.savedMessage?.let(onSaved) }

    val startListening = {
        speaker.stop()
        if (input.isAvailable()) input.start() else viewModel.unavailable()
    }
    val askMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startListening() else viewModel.micDenied()
    }
    val onMic = {
        when {
            state.listening -> input.stop()
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ->
                startListening()
            else -> askMic.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val preferTyping = {
        speaker.stop()
        if (state.listening) input.stop()
        if (viewModel.preferTyping()) onType()
    }

    var askDiscard by rememberSaveable { mutableStateOf(false) }
    val handleBack = {
        when {
            state.listening -> input.stop()
            state.drafts.isNotEmpty() -> askDiscard = true
            else -> { viewModel.discard(); onClose() }
        }
    }
    BackHandler(onBack = handleBack)

    Column(Modifier.fillMaxSize()) {
        GenkiTopBar(
            if (state.phase == VoicePhase.REVIEW) "Confira antes de salvar" else "Cadastrar por voz",
            onBack = handleBack,
        )
        when (state.phase) {
            VoicePhase.INTRO -> Intro(state, onMic, preferTyping)
            VoicePhase.ASKING -> Asking(state, onMic, preferTyping, onQuickAnswer = { speaker.stop(); viewModel.tapped(it) })
            VoicePhase.REVIEW -> Review(state, onMic, onFix, viewModel::remove, viewModel::save)
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.Asking(state: VoiceUiState, onMic: () -> Unit, onType: () -> Unit, onQuickAnswer: (String) -> Unit) {
    val question = state.question ?: return
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp)) {
        SectionTitle("Entendi até agora")
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.drafts.forEach { SoFarCard(it, current = it.localId == question.draftId) }
        }
        state.heard?.let {
            Text(
                "Você disse: “$it”",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            question.text,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Text(question.example, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        if (question.quickAnswers.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                question.quickAnswers.forEach { answer ->
                    AssistChip(
                        onClick = { onQuickAnswer(answer) },
                        label = { Text(answer) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }
    }
    BottomActions {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MicButton(state.listening, size = 64, onClick = onMic)
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) { Listening(state, idle = "Toque no microfone para responder", center = false) }
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
) {
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp)) {
        Text(
            "Para mudar algo, toque em Corrigir ou fale de novo.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )
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
            Text("Ouvindo… toque para parar", fontWeight = FontWeight.SemiBold, textAlign = align, modifier = Modifier.fillMaxWidth())
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
private fun SoFarCard(draft: MedicationDraft, current: Boolean) {
    GenkiCard {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                draft.name ?: "Medicamento sem nome",
                style = MaterialTheme.typography.titleMedium,
                color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            val lines = listOfNotNull(
                draft.concentration,
                draft.dose,
                draft.days?.let(MedicationText::days),
                draft.times.takeIf { it.isNotEmpty() }?.let(MedicationText::times),
                draft.treatment?.let(MedicationText::treatment),
            )
            if (lines.isNotEmpty()) {
                Text(lines.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
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
                secondary = when {
                    treatment != TreatmentType.TEMPORARY -> null
                    draft.durationDays != null -> "Por ${draft.durationDays} dias a partir de hoje"
                    draft.endDate != null -> "Até ${MedicationText.date(draft.endDate)}"
                    else -> missing
                },
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

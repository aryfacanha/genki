package br.edu.ifrs.genki.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.data.MedicationRepository
import br.edu.ifrs.genki.data.Settings
import br.edu.ifrs.genki.domain.ActiveAlert
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.Occurrence
import br.edu.ifrs.genki.domain.OccurrenceCalculator
import br.edu.ifrs.genki.reminders.ReminderReceiver
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.reminders.AlertStore
import br.edu.ifrs.genki.reminders.Notifications
import br.edu.ifrs.genki.reminders.ReminderScheduler
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.EmptyMessage
import br.edu.ifrs.genki.ui.components.GenkiCard
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.components.AddMedicationButton
import br.edu.ifrs.genki.ui.genkiContainer
import br.edu.ifrs.genki.ui.theme.GenkiCardSubtext
import br.edu.ifrs.genki.ui.theme.GenkiOnIconBackground
import br.edu.ifrs.genki.ui.theme.GenkiOnPrimaryContainer
import br.edu.ifrs.genki.ui.theme.GenkiPrimaryContainer
import br.edu.ifrs.genki.ui.tutorial.TutorialTarget
import br.edu.ifrs.genki.ui.tutorial.tutorialTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class HomeState(
    val hasMedications: Boolean,
    val date: LocalDate,
    val today: List<Occurrence>,
    /** Minuto atual: na lista, os horários até ele já passaram. */
    val minute: LocalTime,
    /** Medicamentos do primeiro horário de hoje depois do minuto atual, fora os avisos pendentes; vazio = já passaram. */
    val next: List<Occurrence>,
    /** Medicamentos do primeiro horário de outro dia, para quando os de hoje acabaram (`null` = nenhum). */
    val upcoming: Pair<LocalDate, List<Occurrence>>? = null,
    /** Avisos tocando ou adiados, com o medicamento (DEC-029). */
    val alerts: List<Pair<ActiveAlert, Medication>> = emptyList(),
    val intervalMinutes: Int = 20,
    /** Repetição sem resposta ligada em Ajustes: o cartão "Agora" avisa que o Genki vai avisar de novo (P7). */
    val repeatEnabled: Boolean = true,
    /** Cadastrados que não aparecem hoje por estarem desligados ou terminados (`null` = nenhum). */
    val hidden: String? = null,
)

class HomeViewModel(
    repository: MedicationRepository,
    alertStore: AlertStore,
    settings: Settings,
    private val scheduler: ReminderScheduler,
) : ViewModel() {
    /** Relógio do aparelho a cada minuto, para a Hoje andar sozinha. */
    private val minutes = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(now)
            delay(ChronoUnit.MILLIS.between(now, now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)) + 50)
        }
    }

    val state: StateFlow<HomeState?> = combine(
        repository.observeAll(), minutes, alertStore.alerts, settings.reminders,
    ) { medications, now, alerts, reminderSettings ->
        val today = OccurrenceCalculator.today(medications, now.toLocalDate())
        val minute = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
        // Aviso de medicamento apagado não aparece (o cancelamento já limpa; isto cobre a corrida).
        val withMedication = alerts.mapNotNull { alert ->
            medications.firstOrNull { it.id == alert.medicationId }?.let { alert to it }
        }
        HomeState(
            hasMedications = medications.isNotEmpty(),
            date = now.toLocalDate(),
            today = today,
            minute = minute,
            next = ActiveAlert.nextGroup(today, alerts, minute),
            upcoming = OccurrenceCalculator.firstGroupAfter(medications, now.toLocalDate(), ZoneId.systemDefault()),
            alerts = withMedication,
            intervalMinutes = reminderSettings.intervalMinutes,
            repeatEnabled = reminderSettings.repeatEnabled,
            hidden = MedicationText.hiddenFromToday(
                disabled = medications.filter { !it.remindersEnabled }.map { it.name },
                ended = medications.filter { it.remindersEnabled && OccurrenceCalculator.ended(it, now.toLocalDate()) }.map { it.name },
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** "Entendi": nada é registrado (DEC-004). */
    fun acknowledge(timeId: Long) = scheduler.acknowledge(timeId)

    fun snooze(timeId: Long) = scheduler.snooze(timeId)

    /** Cartão único com vários avisos: "Entendi"/"Adiar" valem para todos os do cartão. */
    fun acknowledgeAll(timeIds: List<Long>) = timeIds.forEach(::acknowledge)

    fun snoozeAll(timeIds: List<Long>) = timeIds.forEach(::snooze)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = genkiContainer()
                HomeViewModel(container.medicationRepository, container.alertStore, container.settings, container.reminderScheduler)
            }
        }
    }
}

/**
 * Hoje (RF09), como `GenkiHome.tsx`: avisos pendentes com "Entendi" e "Adiar" (DEC-029), próximo lembrete e
 * horários do dia. A lista não mostra "Visto às", porque reconhecer não registra nada (DEC-004, DEC-028).
 */
@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Permissões mudam fora do app; relê ao voltar para a tela, como em Ajustes.
    val context = LocalContext.current
    var resumes by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { resumes++ }
    val notificationsAllowed = remember(resumes) { Notifications.canNotify(context) }
    val exactAllowed = remember(resumes) {
        (context.applicationContext as GenkiApplication).container.reminderScheduler.canScheduleExact()
    }
    // "Entendi" não registra nada (DEC-004); o cartão some, então a Hoje diz o que aconteceu (P6).
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(notice) {
        if (notice != null) {
            delay(NOTICE_MILLIS)
            notice = null
        }
    }
    val acknowledge: (Long) -> Unit = { viewModel.acknowledge(it); notice = ACK_NOTICE }
    val acknowledgeAll: (List<Long>) -> Unit = { viewModel.acknowledgeAll(it); notice = ACK_NOTICE }
    Column(Modifier.fillMaxSize()) {
        val s = state
        // O aviso sem medicamentos fica no centro da área toda acima do botão, como em Medicamentos (mesma altura).
        Box(Modifier.weight(1f)) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            ) {
                Column(Modifier.padding(top = 14.dp, bottom = 10.dp)) {
                    Text("Genki", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
                    Text(
                        MedicationText.longDate(s?.date ?: LocalDate.now()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                notice?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
                    ) { Text(it, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) }
                }
                if (s?.hasMedications == true && (!notificationsAllowed || !exactAllowed)) {
                    PermissionWarning(notificationsAllowed, onOpenSettings)
                }
                when {
                    s == null || !s.hasMedications -> Unit
                    // Avisos pendentes vêm antes de "Nenhum lembrete para hoje": um adiado pode passar da meia-noite
                    // depois do último dia do tratamento (RV-M2B-01).
                    s.today.isEmpty() && s.alerts.isEmpty() -> {
                        Text(
                            "Nenhum lembrete para hoje.",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                        )
                        s.upcoming?.let { UpcomingCard(it, s.date, onOpen) }
                    }
                    else -> {
                        // Vários avisos ao mesmo tempo: um cartão só, com "Ver um por um" (pedido do autor, 27/10).
                        val (ringing, snoozed) = s.alerts.partition { it.first.snoozedUntil == null }
                        listOf(ringing, snoozed).filter { it.isNotEmpty() }.forEach { group ->
                            AlertGroup(
                                group = group,
                                intervalMinutes = s.intervalMinutes,
                                repeatEnabled = s.repeatEnabled,
                                onAcknowledge = acknowledge,
                                onSnooze = viewModel::snooze,
                                onAcknowledgeAll = acknowledgeAll,
                                onSnoozeAll = viewModel::snoozeAll,
                            )
                        }
                        if (s.alerts.isEmpty()) {
                            NextCard(s.next, s.upcoming, s.date, onOpen)
                        } else {
                            val after = s.next.takeIf { it.isNotEmpty() }?.let(::groupLine)
                                ?: s.upcoming?.let { (date, group) ->
                                    "${MedicationText.upcomingDay(date, s.date).lowercase()}, ${groupLine(group)}"
                                }
                            after?.let {
                                Text(
                                    "Depois: $it",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 12.dp),
                                )
                            }
                        }
                        if (s.today.isEmpty()) {
                            Text("Nenhum outro lembrete para hoje.", modifier = Modifier.padding(top = 16.dp))
                        } else {
                            Text(
                                "Horários de hoje",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp).semantics { heading() },
                            )
                            // Linha "Agora" entre os horários que já passaram e os que faltam; não diz se foi tomado (DEC-004).
                            val firstAhead = s.today.indexOfFirst { it.time > s.minute }.let { if (it == -1) s.today.size else it }
                            // O horário que está no cartão "Agora" ou adiado não é "já passou": o cartão já o mostra (P8).
                            val inCard = { o: Occurrence -> s.alerts.any { (alert, _) -> alert.matches(o) } }
                            val pastCount = s.today.take(firstAhead).count { !inCard(it) }
                            // Os que já passaram ficam recolhidos: com muitos remédios, os próximos só apareciam depois de
                            // várias rolagens (pré-revisão heurística, 30/10, N1).
                            var showPast by rememberSaveable { mutableStateOf(false) }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (pastCount > 0) {
                                    TextButton(onClick = { showPast = !showPast }, modifier = Modifier.heightIn(min = 48.dp)) {
                                        Text(MedicationText.pastToggle(pastCount, showPast), textDecoration = TextDecoration.Underline)
                                    }
                                }
                                s.today.forEachIndexed { index, occurrence ->
                                    if (index == firstAhead) NowMarker(s.minute)
                                    if (index >= firstAhead || (showPast && !inCard(occurrence))) {
                                        TodayRow(occurrence, past = index < firstAhead) { onOpen(occurrence.medication.id) }
                                    }
                                }
                                if (firstAhead == s.today.size) NowMarker(s.minute)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
                // Desligados e terminados somem da Hoje: diz quais e por quê (pré-revisão heurística, 30/10, N5).
                s?.hidden?.takeIf { s.hasMedications }?.let {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                }
            }
            // Na frente da coluna (vazia neste caso), para o leitor de tela também encontrar o aviso.
            if (s?.hasMedications == false) {
                EmptyMessage(
                    "Você ainda não possui lembretes.",
                    "Cadastre um medicamento e seus horários para receber lembretes.",
                    Modifier.align(Alignment.Center).tutorialTarget(TutorialTarget.NEXT),
                )
            }
        }
        BottomActions {
            AddMedicationButton(onClick = onAdd, modifier = Modifier.tutorialTarget(TutorialTarget.ADD_BUTTON))
        }
    }
}

/** Cartão verde do próximo lembrete; só informa. Sem mais horários hoje, mostra o do próximo dia. */
@Composable
private fun NextCard(next: List<Occurrence>, upcoming: Pair<LocalDate, List<Occurrence>>?, today: LocalDate, onOpen: (Long) -> Unit) {
    if (next.isEmpty() && upcoming != null) {
        Text(
            "Os lembretes de hoje já passaram.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        UpcomingCard(upcoming, today, onOpen)
        return
    }
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).tutorialTarget(TutorialTarget.NEXT),
    ) {
        Column(Modifier.padding(20.dp)) {
            if (next.isEmpty()) {
                Text("Os lembretes de hoje já passaram.", style = MaterialTheme.typography.titleLarge)
                return@Column
            }
            Text(nextTitle(next), color = GenkiCardSubtext)
            Text(MedicationText.time(next.first().time), style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            MedicationGroup(next, onOpen)
        }
    }
}

/**
 * Um aviso: cartão próprio. Vários do mesmo tipo (tocando ou adiados): um cartão com a lista e botões para todos;
 * "Ver um por um" mostra os cartões individuais.
 */
@Composable
private fun AlertGroup(
    group: List<Pair<ActiveAlert, Medication>>,
    intervalMinutes: Int,
    repeatEnabled: Boolean,
    onAcknowledge: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onAcknowledgeAll: (List<Long>) -> Unit,
    onSnoozeAll: (List<Long>) -> Unit,
) {
    var oneByOne by rememberSaveable { mutableStateOf(false) }
    if (group.size == 1 || oneByOne) {
        group.forEach { (alert, medication) ->
            AlertCard(
                alert = alert,
                medication = medication,
                intervalMinutes = intervalMinutes,
                repeatEnabled = repeatEnabled,
                onAcknowledge = { onAcknowledge(alert.timeId) },
                onSnooze = { onSnooze(alert.timeId) },
            )
        }
        if (group.size > 1) {
            TextButton(onClick = { oneByOne = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Ver juntos") }
        }
        return
    }
    val ids = group.map { it.first.timeId }
    val snoozed = group.first().first.snoozedUntil != null
    val colors = alertColors(snoozed)
    AlertSurface(colors) {
        Column(Modifier.padding(20.dp)) {
            Text(if (snoozed) "Lembretes adiados" else "Agora", color = colors.subtext, fontWeight = FontWeight.Bold)
            // Conta avisos (um remédio com dois horários são dois), por isso "lembretes".
            Text("${group.size} lembretes", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            group.forEach { (alert, medication) ->
                val time = alert.snoozedUntil?.atZone(ZoneId.systemDefault())?.toLocalTime()
                Text(
                    "${MedicationText.time(alert.time)} · ${medication.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    MedicationText.doseLine(medication.concentration, medication.dose) +
                        (time?.let { " · novo alerta às ${MedicationText.time(it)}" } ?: ""),
                    color = colors.subtext,
                )
            }
            if (!snoozed && repeatEnabled) RepeatNote(intervalMinutes, colors)
            Spacer(Modifier.height(16.dp))
            AcknowledgeButton(colors, onClick = { onAcknowledgeAll(ids) })
            if (!snoozed) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onSnoozeAll(ids) },
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GenkiCardSubtext),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) { Text("Adiar $intervalMinutes min") }
            }
            TextButton(
                onClick = { oneByOne = true },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.content),
                modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp),
            ) { Text("Ver um por um") }
        }
    }
}

/**
 * Cartão verde "Agora" (aviso tocando: "Entendi" e "Adiar N min") ou "Lembrete adiado" (só "Entendi"),
 * como no protótipo. Os botões fazem o mesmo que os da notificação. O adiado é verde-claro com borda, para não se
 * confundir com o que está tocando (bateria do autor no A31, 28/10); o título também diz "adiado".
 */
@Composable
private fun AlertCard(
    alert: ActiveAlert,
    medication: Medication,
    intervalMinutes: Int,
    repeatEnabled: Boolean,
    onAcknowledge: () -> Unit,
    onSnooze: () -> Unit,
) {
    val snoozedUntil = alert.snoozedUntil?.atZone(ZoneId.systemDefault())?.toLocalTime()
    val colors = alertColors(snoozedUntil != null)
    AlertSurface(colors) {
        Column(Modifier.padding(20.dp)) {
            if (snoozedUntil == null) {
                Text("Agora", color = colors.subtext, fontWeight = FontWeight.Bold)
                Text(MedicationText.time(alert.time), style = MaterialTheme.typography.displayLarge)
            } else {
                Text("Lembrete adiado", color = colors.subtext, fontWeight = FontWeight.Bold)
                Text("Novo alerta às ${MedicationText.time(snoozedUntil)}", style = MaterialTheme.typography.titleLarge)
                Text("Horário original: ${MedicationText.time(alert.time)}", color = colors.subtext)
            }
            Spacer(Modifier.height(8.dp))
            MedicationLine(medication, colors)
            if (snoozedUntil == null && repeatEnabled) RepeatNote(intervalMinutes, colors)
            Spacer(Modifier.height(16.dp))
            AcknowledgeButton(colors, onClick = onAcknowledge)
            if (snoozedUntil == null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSnooze,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GenkiCardSubtext),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) { Text("Adiar $intervalMinutes min") }
            }
        }
    }
}

/** Sem resposta, o Genki avisa de novo (RF09; repetição de Ajustes, até [ReminderReceiver.MAX_REPEATS] vezes). */
@Composable
private fun RepeatNote(intervalMinutes: Int, colors: AlertColors) {
    Text(
        "Sem resposta, o Genki avisa de novo em $intervalMinutes min (até ${ReminderReceiver.MAX_REPEATS} vezes).",
        color = colors.subtext,
        modifier = Modifier.padding(top = 8.dp),
    )
}

private const val NOTICE_MILLIS = 6_000L
private const val ACK_NOTICE = "Lembrete encerrado. O Genki não registra se o remédio foi tomado."

/** Cores do cartão de aviso: verde-escuro tocando; verde-claro com borda verde adiado (contraste 9,5:1 e 13,4:1). */
private data class AlertColors(val container: Color, val content: Color, val subtext: Color, val border: Color?)

@Composable
private fun alertColors(snoozed: Boolean) = if (snoozed) {
    AlertColors(GenkiPrimaryContainer, GenkiOnPrimaryContainer, GenkiOnIconBackground, MaterialTheme.colorScheme.primary)
} else {
    AlertColors(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, GenkiCardSubtext, null)
}

@Composable
private fun AlertSurface(colors: AlertColors, content: @Composable () -> Unit) {
    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(20.dp),
        border = colors.border?.let { BorderStroke(1.5.dp, it) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        content = content,
    )
}

/** "Entendi" com contraste no cartão: branco no verde-escuro, verde no verde-claro. */
@Composable
private fun AcknowledgeButton(colors: AlertColors, onClick: () -> Unit) {
    val green = MaterialTheme.colorScheme.primary
    val white = MaterialTheme.colorScheme.onPrimary
    val light = colors.border != null
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (light) green else white,
            contentColor = if (light) white else green,
        ),
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
    ) { Text("Entendi", fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun MedicationLine(medication: Medication, colors: AlertColors = alertColors(snoozed = false), onClick: (() -> Unit)? = null) {
    // No cartão "Próximo lembrete", tocar no remédio abre os Detalhes, como as linhas da lista (P25).
    val open = if (onClick != null) Modifier.clickable(onClickLabel = "Abrir detalhes de ${medication.name}", onClick = onClick) else Modifier
    Row(open.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).background(colors.content.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PillsIcon, contentDescription = null, tint = colors.subtext, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(medication.name, style = MaterialTheme.typography.titleLarge)
            Text(MedicationText.doseLine(medication.concentration, medication.dose), color = colors.subtext)
        }
    }
}

/** Próximo lembrete em outro dia ("Amanhã" ou a data), no mesmo cartão verde. */
@Composable
private fun UpcomingCard(upcoming: Pair<LocalDate, List<Occurrence>>, today: LocalDate, onOpen: (Long) -> Unit) {
    val (date, group) = upcoming
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().tutorialTarget(TutorialTarget.NEXT),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(nextTitle(group), color = GenkiCardSubtext)
            Text(MedicationText.upcomingDay(date, today), style = MaterialTheme.typography.titleLarge)
            Text(MedicationText.time(group.first().time), style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            MedicationGroup(group, onOpen)
        }
    }
}

/** "Próximo lembrete · 3 medicamentos": deixa claro, antes de abrir, que não é um só. */
private fun nextTitle(group: List<Occurrence>) =
    if (group.size > 1) "Próximo lembrete · ${group.size} medicamentos" else "Próximo lembrete"

/** "08:00 · Losartana e mais 2", na linha "Depois:" quando há avisos na tela. */
private fun groupLine(group: List<Occurrence>): String {
    val first = group.first()
    val more = if (group.size > 1) " e mais ${group.size - 1}" else ""
    return "${MedicationText.time(first.time)} · ${first.medication.name}$more"
}

/**
 * Medicamentos do mesmo horário no cartão verde: o primeiro sempre aparece; os outros com "Ver mais"
 * (teste informal, 29/10: com vários remédios, parecia haver um só).
 */
@Composable
private fun MedicationGroup(group: List<Occurrence>, onOpen: (Long) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    MedicationLine(group.first().medication, onClick = { onOpen(group.first().medication.id) })
    if (group.size == 1) return
    if (expanded) {
        group.drop(1).forEach {
            Spacer(Modifier.height(12.dp))
            MedicationLine(it.medication, onClick = { onOpen(it.medication.id) })
        }
    }
    val others = group.size - 1
    OutlinedButton(
        onClick = { expanded = !expanded },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
        modifier = Modifier.padding(top = 12.dp).heightIn(min = 48.dp),
    ) {
        Text(
            if (expanded) "Ver menos" else "Ver mais $others ${if (others == 1) "medicamento" else "medicamentos"}",
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Faixa "Agora · 16:42" na lista do dia. */
@Composable
private fun NowMarker(minute: LocalTime) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Agora · ${MedicationText.time(minute)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.primary))
    }
}

/**
 * Notificações ou alarme exato desligados: avisa na Hoje, não só em Ajustes, porque o lembrete pode não aparecer
 * ou atrasar (o MyTherapy faz o mesmo; no Pixel 8a o alarme exato estava desligado).
 */
@Composable
private fun PermissionWarning(notificationsAllowed: Boolean, onOpenSettings: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (notificationsAllowed) "Os lembretes podem atrasar" else "Os lembretes não vão aparecer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (notificationsAllowed) {
                    "Os alarmes no horário exato estão desligados para o Genki."
                } else {
                    "As notificações do Genki estão desligadas."
                },
            )
            TextButton(
                onClick = onOpenSettings,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.heightIn(min = 48.dp),
            ) { Text("Resolver em Ajustes", fontWeight = FontWeight.SemiBold) }
        }
    }
}

/**
 * Um horário do dia; tocar abre os Detalhes do medicamento. O que já passou fica em fundo cinza, sem borda e com
 * "Passou" (não diz se foi tomado, DEC-004); cor secundária em vez de transparência, para manter o contraste.
 */
@Composable
private fun TodayRow(occurrence: Occurrence, past: Boolean, onOpen: () -> Unit) {
    val textColor = if (past) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    val content = @Composable {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            // "Passou" embaixo do horário, não à direita: com fonte ampliada o nome e a dose ficam com a largura (RV-HOJE-03).
            Column {
                Text(MedicationText.time(occurrence.time), style = MaterialTheme.typography.titleLarge, color = textColor)
                if (past) {
                    Text("Passou", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(occurrence.medication.name, style = MaterialTheme.typography.titleMedium, color = textColor)
                Text(
                    MedicationText.doseLine(occurrence.medication.concentration, occurrence.medication.dose),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (past) {
        Surface(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            content = content,
        )
    } else {
        GenkiCard(onClick = onOpen, content = content)
    }
}


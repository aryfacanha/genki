package br.edu.ifrs.genki.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.reminders.AlertStore
import br.edu.ifrs.genki.reminders.Notifications
import br.edu.ifrs.genki.reminders.ReminderScheduler
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.GenkiCard
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.genkiContainer
import br.edu.ifrs.genki.ui.theme.GenkiCardSubtext
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
    /** Primeiro horário de hoje depois do minuto atual, fora os avisos pendentes; `null` = os de hoje já passaram. */
    val next: Occurrence?,
    /** Primeiro horário de outro dia, para quando os de hoje acabaram (`null` = nenhum). */
    val upcoming: Pair<LocalDate, Occurrence>? = null,
    /** Avisos tocando ou adiados, com o medicamento (DEC-029). */
    val alerts: List<Pair<ActiveAlert, Medication>> = emptyList(),
    val intervalMinutes: Int = 20,
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
            next = ActiveAlert.nextOccurrence(today, alerts, minute),
            upcoming = OccurrenceCalculator.firstAfter(medications, now.toLocalDate(), ZoneId.systemDefault()),
            alerts = withMedication,
            intervalMinutes = reminderSettings.intervalMinutes,
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
    Column(Modifier.fillMaxSize()) {
        val s = state
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Column(Modifier.padding(top = 14.dp, bottom = 10.dp)) {
                Text("Genki", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
                Text(
                    MedicationText.longDate(s?.date ?: LocalDate.now()),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (s?.hasMedications == true && (!notificationsAllowed || !exactAllowed)) {
                PermissionWarning(notificationsAllowed, onOpenSettings)
            }
            when {
                s == null -> Unit
                !s.hasMedications -> EmptyState()
                // Avisos pendentes vêm antes de "Nenhum lembrete para hoje": um adiado pode passar da meia-noite
                // depois do último dia do tratamento (RV-M2B-01).
                s.today.isEmpty() && s.alerts.isEmpty() -> {
                    Text(
                        "Nenhum lembrete para hoje.",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                    )
                    s.upcoming?.let { UpcomingCard(it, s.date) }
                }
                else -> {
                    // Vários avisos ao mesmo tempo: um cartão só, com "Ver um por um" (pedido do autor, 27/10).
                    val (ringing, snoozed) = s.alerts.partition { it.first.snoozedUntil == null }
                    listOf(ringing, snoozed).filter { it.isNotEmpty() }.forEach { group ->
                        AlertGroup(
                            group = group,
                            intervalMinutes = s.intervalMinutes,
                            onAcknowledge = viewModel::acknowledge,
                            onSnooze = viewModel::snooze,
                            onAcknowledgeAll = viewModel::acknowledgeAll,
                            onSnoozeAll = viewModel::snoozeAll,
                        )
                    }
                    if (s.alerts.isEmpty()) {
                        NextCard(s.next, s.upcoming, s.date)
                    } else {
                        val after = s.next?.let { "${MedicationText.time(it.time)} · ${it.medication.name}" }
                            ?: s.upcoming?.let { (date, occurrence) ->
                                "${MedicationText.upcomingDay(date, s.date).lowercase()}, " +
                                    "${MedicationText.time(occurrence.time)} · ${occurrence.medication.name}"
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
                            "Hoje",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp).semantics { heading() },
                        )
                        // Linha "Agora" entre os horários que já passaram e os que faltam; não diz se foi tomado (DEC-004).
                        val firstAhead = s.today.indexOfFirst { it.time > s.minute }.let { if (it == -1) s.today.size else it }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            s.today.forEachIndexed { index, occurrence ->
                                if (index == firstAhead && index > 0) NowMarker(s.minute)
                                TodayRow(occurrence, past = index < firstAhead)
                            }
                            if (firstAhead == s.today.size) NowMarker(s.minute)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
        BottomActions {
            PrimaryButton("Adicionar medicamento", onClick = onAdd, icon = Icons.Filled.Add)
        }
    }
}

/** Cartão verde do próximo lembrete; só informa. Sem mais horários hoje, mostra o do próximo dia. */
@Composable
private fun NextCard(next: Occurrence?, upcoming: Pair<LocalDate, Occurrence>?, today: LocalDate) {
    if (next == null && upcoming != null) {
        Text(
            "Os lembretes de hoje já passaram.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        UpcomingCard(upcoming, today)
        return
    }
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            if (next == null) {
                Text("Os lembretes de hoje já passaram.", style = MaterialTheme.typography.titleLarge)
                return@Column
            }
            Text("Próximo lembrete", color = GenkiCardSubtext)
            Text(MedicationText.time(next.time), style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            MedicationLine(next.medication)
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
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(if (snoozed) "Lembretes adiados" else "Agora", color = GenkiCardSubtext, fontWeight = FontWeight.Bold)
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
                    color = GenkiCardSubtext,
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onAcknowledgeAll(ids) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Entendi", fontWeight = FontWeight.SemiBold) }
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
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp),
            ) { Text("Ver um por um") }
        }
    }
}

/**
 * Cartão verde "Agora" (aviso tocando: "Entendi" e "Adiar N min") ou "Lembrete adiado" (só "Entendi"),
 * como no protótipo. Os botões fazem o mesmo que os da notificação.
 */
@Composable
private fun AlertCard(
    alert: ActiveAlert,
    medication: Medication,
    intervalMinutes: Int,
    onAcknowledge: () -> Unit,
    onSnooze: () -> Unit,
) {
    val snoozedUntil = alert.snoozedUntil?.atZone(ZoneId.systemDefault())?.toLocalTime()
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            if (snoozedUntil == null) {
                Text("Agora", color = GenkiCardSubtext, fontWeight = FontWeight.Bold)
                Text(MedicationText.time(alert.time), style = MaterialTheme.typography.displayLarge)
            } else {
                Text("Lembrete adiado", color = GenkiCardSubtext, fontWeight = FontWeight.Bold)
                Text("Novo alerta às ${MedicationText.time(snoozedUntil)}", style = MaterialTheme.typography.titleLarge)
                Text("Horário original: ${MedicationText.time(alert.time)}", color = GenkiCardSubtext)
            }
            Spacer(Modifier.height(8.dp))
            MedicationLine(medication)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Entendi", fontWeight = FontWeight.SemiBold) }
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

@Composable
private fun MedicationLine(medication: Medication) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PillsIcon, contentDescription = null, tint = GenkiCardSubtext, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(medication.name, style = MaterialTheme.typography.titleLarge)
            Text(MedicationText.doseLine(medication.concentration, medication.dose), color = GenkiCardSubtext)
        }
    }
}

/** Próximo lembrete em outro dia ("Amanhã" ou a data), no mesmo cartão verde. */
@Composable
private fun UpcomingCard(upcoming: Pair<LocalDate, Occurrence>, today: LocalDate) {
    val (date, occurrence) = upcoming
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Próximo lembrete", color = GenkiCardSubtext)
            Text(MedicationText.upcomingDay(date, today), style = MaterialTheme.typography.titleLarge)
            Text(MedicationText.time(occurrence.time), style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            MedicationLine(occurrence.medication)
        }
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

/** Horário que já passou fica mais claro; a linha "Agora" diz isso ao TalkBack. */
@Composable
private fun TodayRow(occurrence: Occurrence, past: Boolean) {
    GenkiCard(Modifier.alpha(if (past) 0.6f else 1f)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(MedicationText.time(occurrence.time), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(occurrence.medication.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    MedicationText.doseLine(occurrence.medication.concentration, occurrence.medication.dose),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(56.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PillsIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Você ainda não possui lembretes.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Cadastre um medicamento e seus horários para receber lembretes.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

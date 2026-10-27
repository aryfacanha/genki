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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
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
import br.edu.ifrs.genki.reminders.AlertStore
import br.edu.ifrs.genki.reminders.ReminderAction
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
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class HomeState(
    val hasMedications: Boolean,
    val date: LocalDate,
    val today: List<Occurrence>,
    /** Primeiro horário de hoje a partir do minuto atual, fora os avisos pendentes; `null` = os de hoje já passaram. */
    val next: Occurrence?,
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
            next = ActiveAlert.nextOccurrence(today, alerts, minute),
            alerts = withMedication,
            intervalMinutes = reminderSettings.intervalMinutes,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** "Entendi": nada é registrado (DEC-004). */
    fun acknowledge(timeId: Long) = scheduler.respond(ReminderAction.ACK, timeId)

    fun snooze(timeId: Long) = scheduler.respond(ReminderAction.SNOOZE, timeId)

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
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
            when {
                s == null -> Unit
                !s.hasMedications -> EmptyState()
                // Avisos pendentes vêm antes de "Nenhum lembrete para hoje": um adiado pode passar da meia-noite
                // depois do último dia do tratamento (RV-M2B-01).
                s.today.isEmpty() && s.alerts.isEmpty() -> Text(
                    "Nenhum lembrete para hoje.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
                else -> {
                    s.alerts.forEach { (alert, medication) ->
                        AlertCard(
                            alert = alert,
                            medication = medication,
                            intervalMinutes = s.intervalMinutes,
                            onAcknowledge = { viewModel.acknowledge(alert.timeId) },
                            onSnooze = { viewModel.snooze(alert.timeId) },
                        )
                    }
                    if (s.alerts.isEmpty()) {
                        NextCard(s.next)
                    } else {
                        s.next?.let { next ->
                            Text(
                                "Depois: ${MedicationText.time(next.time)} · ${next.medication.name}",
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            s.today.forEach { TodayRow(it) }
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

/** Cartão verde do próximo lembrete; só informa. */
@Composable
private fun NextCard(next: Occurrence?) {
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

@Composable
private fun TodayRow(occurrence: Occurrence) {
    GenkiCard {
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

package br.edu.ifrs.genki.reminders

import android.os.Build
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.ui.theme.GenkiTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription

/**
 * Tela de alarme do modo "Como alarme" (DEC-048): abre por cima da tela bloqueada e acende a tela, como o despertador
 * do celular, com o horário, os remédios que estão avisando, **Entendi** e **Adiar**. Entendi não registra ingestão
 * (DEC-004). Com o celular em uso, o Android mostra só o aviso no topo; esta tela abre ao tocar nele.
 */
class AlarmActivity : ComponentActivity() {

    /** Horários que abriram esta tela; outros avisos sem resposta ficam na Hoje (não são respondidos aqui). */
    private val timeIds = mutableStateListOf<Long>()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        add(intent)
    }

    private fun add(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_TIME_ID, -1) ?: -1
        if (id >= 0 && id !in timeIds) timeIds += id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        add(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        enableEdgeToEdge()
        val container = (application as GenkiApplication).container
        setContent {
            GenkiTheme {
                val alerts by container.alertStore.alerts.collectAsStateWithLifecycle()
                val medications by container.medicationRepository.observeAll().collectAsStateWithLifecycle(null)
                val settings by container.settings.reminders.collectAsStateWithLifecycle()
                // Os que abriram a tela e os outros do mesmo horário (o Android abre a tela só pelo primeiro aviso).
                val times = alerts.filter { it.timeId in timeIds }.map { it.time }.toSet()
                val ringing = alerts.filter { it.snoozedUntil == null && (it.timeId in timeIds || it.time in times) }
                // Respondido em outro lugar (notificação, Hoje): a tela fecha.
                LaunchedEffect(ringing.isEmpty()) { if (ringing.isEmpty()) finish() }
                if (ringing.isEmpty() || medications == null) return@GenkiTheme
                val rows = ringing.mapNotNull { alert ->
                    medications!!.firstOrNull { it.id == alert.medicationId }?.let { m ->
                        AlarmRow(alert.timeId, m.name, MedicationText.doseLine(m.concentration, m.dose))
                    }
                }
                val scheduler = container.reminderScheduler
                AlarmContent(
                    time = MedicationText.time(ringing.maxOf { it.time }),
                    rows = rows,
                    intervalMinutes = settings.intervalMinutes,
                    // Um por um: o confirmado sai da lista; o som segue até o último (pedido do autor, 29/10).
                    onAcknowledge = { scheduler.acknowledge(it) },
                    onAcknowledgeAll = { ringing.forEach { scheduler.acknowledge(it.timeId) }; finish() },
                    onSnoozeAll = { ringing.forEach { scheduler.snooze(it.timeId) }; finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_TIME_ID = "timeId"
    }
}

private data class AlarmRow(val timeId: Long, val name: String, val dose: String)

@Composable
private fun AlarmContent(
    time: String,
    rows: List<AlarmRow>,
    intervalMinutes: Int,
    onAcknowledge: (Long) -> Unit,
    onAcknowledgeAll: () -> Unit,
    onSnoozeAll: () -> Unit,
) {
    val several = rows.size > 1
    Surface(color = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.safeDrawingPadding().padding(24.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
                Text(
                    if (several) "Hora do remédio · ${rows.size} remédios" else "Hora do remédio",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                Text(time, style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(8.dp))
                rows.forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(row.name, style = MaterialTheme.typography.headlineSmall)
                            Text(row.dose, style = MaterialTheme.typography.titleMedium)
                        }
                        if (several) {
                            OutlinedButton(
                                onClick = { onAcknowledge(row.timeId) },
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Entendi, ${row.name}" },
                            ) { Text("Entendi") }
                        }
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(if (several) "Entendi todos" else "Entendi", onClick = onAcknowledgeAll)
                    SecondaryButton(if (several) "Adiar todos $intervalMinutes min" else "Adiar $intervalMinutes min", onClick = onSnoozeAll)
                }
            }
        }
    }
}

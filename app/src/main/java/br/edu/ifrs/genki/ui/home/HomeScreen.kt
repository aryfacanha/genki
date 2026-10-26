package br.edu.ifrs.genki.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.data.MedicationRepository
import br.edu.ifrs.genki.domain.Occurrence
import br.edu.ifrs.genki.domain.OccurrenceCalculator
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
import java.time.temporal.ChronoUnit

data class HomeState(
    val hasMedications: Boolean,
    val date: LocalDate,
    val today: List<Occurrence>,
    /** Primeiro horário de hoje a partir do minuto atual; `null` = os de hoje já passaram. */
    val next: Occurrence?,
)

class HomeViewModel(repository: MedicationRepository) : ViewModel() {
    /** Relógio do aparelho a cada minuto, para a Hoje andar sozinha. */
    private val minutes = flow {
        while (true) {
            val now = LocalDateTime.now()
            emit(now)
            delay(ChronoUnit.MILLIS.between(now, now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)) + 50)
        }
    }

    val state: StateFlow<HomeState?> = combine(repository.observeAll(), minutes) { medications, now ->
        val today = OccurrenceCalculator.today(medications, now.toLocalDate())
        val minute = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
        HomeState(medications.isNotEmpty(), now.toLocalDate(), today, today.firstOrNull { it.time >= minute })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { HomeViewModel(genkiContainer().medicationRepository) }
        }
    }
}

/**
 * Hoje (RF09, visualização), como `GenkiHome.tsx`, mas só com os horários do dia:
 * sem "Visto às"/"Adiado para", porque reconhecer não registra nada (DEC-004, DEC-028).
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
                s.today.isEmpty() -> Text(
                    "Nenhum lembrete para hoje.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
                else -> {
                    NextCard(s.next)
                    Text(
                        "Hoje",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp).semantics { heading() },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        s.today.forEach { TodayRow(it) }
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

/** Cartão verde do próximo lembrete; só informa (as respostas ficam na notificação). */
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(PillsIcon, contentDescription = null, tint = GenkiCardSubtext, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(next.medication.name, style = MaterialTheme.typography.titleLarge)
                    Text(MedicationText.doseLine(next.medication.concentration, next.medication.dose), color = GenkiCardSubtext)
                }
            }
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

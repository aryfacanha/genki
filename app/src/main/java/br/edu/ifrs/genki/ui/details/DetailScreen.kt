package br.edu.ifrs.genki.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.GenkiEval
import br.edu.ifrs.genki.data.MedicationRepository
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.TreatmentType
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.components.GenkiCard
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.InfoRow
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.ui.genkiContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import br.edu.ifrs.genki.domain.OccurrenceCalculator
import java.time.LocalDate

class DetailViewModel(private val id: Long, private val repository: MedicationRepository) : ViewModel() {
    /** `null` enquanto carrega ou depois de excluído. */
    val medication: StateFlow<Medication?> =
        repository.observe(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Só grava a escolha; o efeito nos alarmes entra no M2 (RF10). */
    fun setRemindersEnabled(enabled: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.setRemindersEnabled(id, enabled)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id)
            GenkiEval.log("medicamento_excluido", "id" to id)
            onDone()
        }
    }

    companion object {
        fun factory(id: Long) = viewModelFactory {
            initializer { DetailViewModel(id, genkiContainer().medicationRepository) }
        }
    }
}

/** Detalhes (RF02, RF10), como `DetalhesMedicamento.tsx`. Exclusão com confirmação e "Desfazer" no aviso (DEC-055). */
@Composable
fun DetailScreen(
    id: Long,
    backLabel: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    /** Recebe o medicamento excluído, para o "Desfazer" do aviso. */
    onDeleted: (Medication) -> Unit,
    showMessage: (String) -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.factory(id)),
) {
    val medication by viewModel.medication.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        GenkiTopBar("Detalhes do medicamento", onBack = onBack, backLabel = backLabel)
        val med = medication ?: return@Column
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(PillsIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Text(med.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            }
            val ended = OccurrenceCalculator.ended(med, LocalDate.now())
            RemindersStatus(med.remindersEnabled, ended)

            GenkiCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    med.concentration?.let { InfoRow("Concentração", it) }
                    InfoRow("Quanto toma", med.dose)
                    InfoRow("Dias", MedicationText.days(med.days))
                    InfoRow("Horários", MedicationText.times(med.times))
                    InfoRow(
                        "Duração",
                        MedicationText.treatment(med.treatment),
                        secondary = if (med.treatment == TreatmentType.TEMPORARY) MedicationText.period(med.startDate, med.endDate) else null,
                        help = if (med.treatment == TreatmentType.UNDEFINED) MedicationText.UNKNOWN_DURATION_HELP else null,
                    )
                }
            }

            PrimaryButton("Editar", onClick = onEdit)
            SecondaryButton(
                if (med.remindersEnabled) "Desativar lembretes" else "Ativar lembretes",
                onClick = {
                    val enable = !med.remindersEnabled
                    viewModel.setRemindersEnabled(enable) {
                        showMessage(MedicationText.remindersToggled(enable, med.endDate?.takeIf { ended }))
                    }
                },
            )
            SecondaryButton(
                "Excluir medicamento",
                onClick = { confirmDelete = true },
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Excluir ${med.name}?") },
                text = { Text("O medicamento será removido e todos os lembretes associados serão cancelados.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDelete = false
                        viewModel.delete { onDeleted(med) }
                    }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
            )
        }
    }
}

/**
 * Estado dos lembretes em destaque, logo abaixo do nome (bateria do autor, 30/10: "Ativos" na lista passava despercebido).
 * Com o tratamento terminado não há mais lembretes, mesmo ligados (pré-revisão heurística, 30/10, N4).
 */
@Composable
private fun RemindersStatus(on: Boolean, ended: Boolean) {
    val enabled = on && !ended
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth()
            .background(if (enabled) colors.primaryContainer else colors.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (enabled) Icons.Filled.Notifications else Icons.Filled.Clear,
            contentDescription = null,
            tint = if (enabled) colors.primary else colors.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            when {
                ended -> "Sem lembretes: o tratamento terminou"
                enabled -> "Lembretes ativos"
                else -> "Lembretes desativados"
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) colors.onPrimaryContainer else colors.onSurface,
        )
    }
}

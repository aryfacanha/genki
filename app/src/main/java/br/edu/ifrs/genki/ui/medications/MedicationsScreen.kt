package br.edu.ifrs.genki.ui.medications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.data.MedicationRepository
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.MedicationOrder
import br.edu.ifrs.genki.ui.MedicationText
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.EmptyMessage
import br.edu.ifrs.genki.ui.components.GenkiCard
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.genkiContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class MedicationListViewModel(repository: MedicationRepository) : ViewModel() {
    /** `null` enquanto o banco não respondeu (evita piscar o estado vazio). */
    val medications: StateFlow<List<Medication>?> =
        repository.observeAll().map { MedicationOrder.byName(it) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory = viewModelFactory {
            initializer { MedicationListViewModel(genkiContainer().medicationRepository) }
        }
    }
}

/** Lista (RF02), como `MedicamentosScreen.tsx` do protótipo. */
@Composable
fun MedicationsScreen(
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit,
    viewModel: MedicationListViewModel = viewModel(factory = MedicationListViewModel.Factory),
) {
    val medications by viewModel.medications.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        // O aviso de lista vazia fica no centro da área toda acima do botão, como na Hoje (mesma altura).
        Box(Modifier.weight(1f)) {
            val list = medications
            Column(Modifier.fillMaxSize()) {
                GenkiTopBar("Medicamentos")
                if (!list.isNullOrEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(list, key = { it.id }) { medication ->
                            MedicationCard(medication, onClick = { onOpen(medication.id) })
                        }
                    }
                }
            }
            if (list?.isEmpty() == true) {
                EmptyMessage(
                    "Nenhum medicamento cadastrado.",
                    "Adicione um medicamento para começar a organizar seus lembretes.",
                    Modifier.align(Alignment.Center),
                )
            }
        }
        BottomActions {
            PrimaryButton("Adicionar medicamento", onClick = onAdd, icon = Icons.Filled.Add)
        }
    }
}

@Composable
private fun MedicationCard(medication: Medication, onClick: () -> Unit) {
    GenkiCard(onClick = onClick) {
        Row(
            Modifier.heightIn(min = 88.dp).padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(medication.name, style = MaterialTheme.typography.titleMedium)
                val secondary = MaterialTheme.colorScheme.onSurfaceVariant
                Text(MedicationText.doseLine(medication.concentration, medication.dose), color = secondary)
                Text(MedicationText.schedule(medication), color = secondary)
                Text(
                    MedicationText.treatmentLine(medication, LocalDate.now()),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                if (!medication.remindersEnabled) {
                    Text("Lembretes desligados", fontWeight = FontWeight.SemiBold)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}


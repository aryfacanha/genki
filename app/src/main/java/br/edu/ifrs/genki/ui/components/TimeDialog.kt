package br.edu.ifrs.genki.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Problem
import br.edu.ifrs.genki.ui.MedicationText
import java.time.LocalTime

/** Relógio de 24 h do formulário, usado também na pergunta dos horários da voz (pedido do autor, 28/10). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDialog(initial: LocalTime, taken: Set<LocalTime>, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    // Horário já da lista: avisa no próprio relógio e não deixa confirmar (o validador continua checando).
    val repeated = LocalTime.of(state.hour, state.minute) in taken
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha o horário") },
        text = {
            Column {
                TimePicker(state)
                FieldError(if (repeated) MedicationText.error(Field.TIMES, Problem.DUPLICATE_TIME) else null)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }, enabled = !repeated) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

package br.edu.ifrs.genki.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Problem
import br.edu.ifrs.genki.ui.MedicationText
import java.time.LocalTime

/** Hora e minuto digitados; `null` se faltar um dos dois ou estiver fora de 0–23 / 0–59. */
fun typedTime(hour: String, minute: String): LocalTime? {
    val h = hour.trim().toIntOrNull()?.takeIf { it in 0..23 } ?: return null
    val m = minute.trim().toIntOrNull()?.takeIf { it in 0..59 } ?: return null
    return LocalTime.of(h, m)
}

/**
 * Relógio de 24 h do formulário, usado também na pergunta dos horários da voz (pedido do autor, 28/10).
 * "Digitar o horário" troca o mostrador por dois campos (hora e minuto): no toque o mostrador arredonda o minuto para
 * múltiplos de 5 e o minuto exato exige arrastar (WCAG 2.5.7; achados 3–4 da pré-revisão de 29/10). Campos próprios,
 * sem pular de um para o outro sozinhos: a entrada digitada do Material 3 pulava e apagava o campo anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDialog(initial: LocalTime, taken: Set<LocalTime>, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    var typing by rememberSaveable { mutableStateOf(false) }
    var hour by remember { mutableStateOf(TextFieldValue("%02d".format(initial.hour))) }
    var minute by remember { mutableStateOf(TextFieldValue("%02d".format(initial.minute))) }
    val chosen: LocalTime? = if (typing) typedTime(hour.text, minute.text) else LocalTime.of(state.hour, state.minute)
    // Horário já da lista: avisa e não deixa confirmar (o validador continua checando).
    val repeated = chosen != null && chosen in taken
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Escolha o horário") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (typing) {
                    val focus = remember { FocusRequester() }
                    LaunchedEffect(Unit) { focus.requestFocus() }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TimeField("Hora", hour, { hour = it }, Modifier.focusRequester(focus))
                        Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
                        TimeField("Minuto", minute, { minute = it })
                    }
                    FieldError(
                        when {
                            chosen == null -> "Use hora de 0 a 23 e minuto de 0 a 59."
                            repeated -> MedicationText.error(Field.TIMES, Problem.DUPLICATE_TIME)
                            else -> null
                        },
                    )
                } else {
                    TimePicker(state)
                    FieldError(if (repeated) MedicationText.error(Field.TIMES, Problem.DUPLICATE_TIME) else null)
                }
                TextButton(
                    onClick = {
                        if (typing) {
                            // Leva para o relógio o que foi digitado, se for válido.
                            typedTime(hour.text, minute.text)?.let { state.hour = it.hour; state.minute = it.minute }
                        } else {
                            hour = TextFieldValue("%02d".format(state.hour))
                            minute = TextFieldValue("%02d".format(state.minute))
                        }
                        typing = !typing
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(if (typing) "Escolher no relógio" else "Digitar o horário", textDecoration = TextDecoration.Underline)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { chosen?.let(onConfirm) }, enabled = chosen != null && !repeated) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

/** Campo de dois dígitos; ao tocar, seleciona tudo para o que for digitado substituir. */
@Composable
private fun TimeField(label: String, value: TextFieldValue, onChange: (TextFieldValue) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> if (new.text.length <= 2 && new.text.all(Char::isDigit)) onChange(new) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
        textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
        colors = genkiFieldColors(),
        modifier = modifier.width(112.dp).onFocusChanged { focused ->
            if (focused.isFocused) onChange(value.copy(selection = TextRange(0, value.text.length)))
        },
    )
}

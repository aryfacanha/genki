package br.edu.ifrs.genki.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.edu.ifrs.genki.data.Settings

/** Controles dos ajustes, usados em Ajustes e na introdução (DEC-040), para os dois dizerem o mesmo. */

@Composable
internal fun SwitchRow(title: String, text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .toggleable(checked, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** Repetição do lembrete (RF25). Sem negativa e sem os nomes dos botões (retorno informal, 30/10). */
@Composable
internal fun RepeatSwitch(checked: Boolean, onChange: (Boolean) -> Unit) = SwitchRow(
    "Repetir o lembrete",
    "Se o lembrete ficar sem resposta, o Genki avisa outra vez depois do tempo escolhido abaixo, até 3 vezes.",
    checked,
    onChange,
)

/** "Tempo para avisar de novo": 10, 20 ou 30 min (DEC-028). */
@Composable
internal fun IntervalChoices(selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text("Tempo para avisar de novo", style = MaterialTheme.typography.titleSmall)
        Text(
            "Vale para repetir o lembrete e para adiá-lo.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Settings.INTERVALS.forEach { minutes ->
                Choice(selected = selected == minutes, modifier = Modifier.weight(1f), onClick = { onSelect(minutes) }) {
                    Text("$minutes min", fontWeight = if (selected == minutes) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

/** Som do lembrete: notificação comum ou alarme (DEC-032). */
@Composable
internal fun SoundChoices(alarm: Boolean, onSelect: (Boolean) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text("Som do lembrete", style = MaterialTheme.typography.titleSmall)
        Column(Modifier.selectableGroup().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                false to ("Como notificação" to "Toca como as outras notificações e respeita o modo silencioso."),
                true to ("Como alarme" to "Toca como despertador, mesmo no silencioso."),
            ).forEach { (value, texts) ->
                Choice(selected = alarm == value, modifier = Modifier.fillMaxWidth(), onClick = { onSelect(value) }, start = 12.dp) {
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(texts.first, fontWeight = FontWeight.Bold)
                        Text(texts.second, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** Alarme falado (DEC-052): só aparece com "Como alarme". */
@Composable
internal fun AlarmSpeechSwitch(checked: Boolean, onChange: (Boolean) -> Unit) = SwitchRow(
    "Falar o nome dos remédios no alarme",
    "O Genki diz o horário e o nome do remédio enquanto o alarme toca.",
    checked,
    onChange,
)

/** Assistente de voz (DEC-049), na introdução e em Ajustes. */
@Composable
internal fun VoiceAssistantSwitch(checked: Boolean, onChange: (Boolean) -> Unit) = SwitchRow(
    "Ler o tutorial em voz alta",
    "No cadastro por voz, as perguntas são sempre lidas; para calar, toque em Silenciar a voz.",
    checked,
    onChange,
)

/** Na voz, ouvir sozinho depois da pergunta (DEC-040). */
@Composable
internal fun AutoListenSwitch(checked: Boolean, onChange: (Boolean) -> Unit) = SwitchRow(
    "Na voz, ouvir sozinho depois da pergunta",
    "O microfone abre sozinho depois de cada pergunta lida em voz alta. Sem a leitura, ou com isto desligado, toque no microfone para responder.",
    checked,
    onChange,
)

/** Opção marcada com ✓ e borda verde, não só pela cor. */
@Composable
private fun Choice(
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    start: Dp = 0.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.5.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        modifier = modifier.heightIn(min = 52.dp).selectable(selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(
            Modifier.padding(start = start),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
            } else if (start > 0.dp) {
                // Opções em lista: o texto fica no mesmo lugar, marcada ou não.
                Spacer(Modifier.width(22.dp))
            }
            content()
        }
    }
}

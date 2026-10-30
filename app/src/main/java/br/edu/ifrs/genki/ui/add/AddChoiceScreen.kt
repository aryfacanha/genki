package br.edu.ifrs.genki.ui.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.edu.ifrs.genki.ui.components.CameraIcon
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.tutorial.TutorialTarget
import br.edu.ifrs.genki.ui.tutorial.tutorialTarget
import br.edu.ifrs.genki.ui.components.MicIcon

/**
 * "Como você quer cadastrar?" (`CadastrarMedicamento.tsx`): voz em destaque, digitar e câmera.
 */
@Composable
fun AddChoiceScreen(onBack: () -> Unit, onVoice: () -> Unit, onType: () -> Unit, onCamera: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        GenkiTopBar("Adicionar medicamento", onBack = onBack, actionLabel = "Cancelar", onAction = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(
                "Como você quer cadastrar?",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp).semantics { heading() },
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OptionCard(
                    icon = MicIcon,
                    label = "Usar voz",
                    supportingText = "Conte quais medicamentos você usa, quanto toma e em quais horários.",
                    prominent = true,
                    onClick = onVoice,
                    modifier = Modifier.tutorialTarget(TutorialTarget.VOICE),
                )
                OptionCard(
                    icon = Icons.Filled.Edit,
                    label = "Digitar",
                    supportingText = "Preencha as informações manualmente.",
                    onClick = onType,
                    modifier = Modifier.tutorialTarget(TutorialTarget.TYPE),
                )
                OptionCard(
                    icon = CameraIcon,
                    label = "Usar câmera",
                    supportingText = "Use a embalagem para ajudar a preencher os dados.",
                    badge = "Experimental",
                    onClick = onCamera,
                    modifier = Modifier.tutorialTarget(TutorialTarget.CAMERA),
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    icon: ImageVector,
    label: String,
    supportingText: String,
    onClick: () -> Unit,
    prominent: Boolean = false,
    badge: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (prominent) colors.primaryContainer else colors.surfaceContainerLowest,
        border = BorderStroke(1.5.dp, if (prominent) colors.primary else colors.outline),
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(if (prominent) colors.primary else colors.secondaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = if (prominent) colors.onPrimary else colors.onSecondaryContainer)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.titleMedium)
                // Leitura ainda erra com frequência (bateria do autor, 30/10).
                badge?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onTertiaryContainer,
                        modifier = Modifier.padding(vertical = 4.dp)
                            .background(colors.tertiaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
                Text(supportingText, color = colors.onSurfaceVariant)
            }
        }
    }
}

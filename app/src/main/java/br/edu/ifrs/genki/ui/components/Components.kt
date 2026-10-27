package br.edu.ifrs.genki.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// Peças repetidas nas telas, com as medidas do protótipo (barra de 56 dp, botões de 56 dp, cantos de 16 dp).

// Ícone de comprimidos, copiado do protótipo (`IconPills` em `src/shared.tsx`).
val PillsIcon: ImageVector = ImageVector.Builder(
    name = "Pills",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(
        "M4.22 11.29l6.36-6.36a4.243 4.243 0 016 6L10.22 17.3a4.243 4.243 0 01-6-6z" +
            "m10.25 7.45l1.06-1.06a2.12 2.12 0 000-3l-1.06-1.06-4.24 4.24 1.06 1.06c.83.83 2.17.83 3-.18z"
    ),
    fill = SolidColor(Color.Black),
).build()

// Câmera do Material Icons ("photo_camera"; o pacote "core" não traz).
val CameraIcon: ImageVector = ImageVector.Builder(
    name = "Camera",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(
        "M12 15.2a3.2 3.2 0 100-6.4 3.2 3.2 0 000 6.4z" +
            "M9 2L7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9z" +
            "m3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z"
    ),
    fill = SolidColor(Color.Black),
).build()

// Microfone do Material Icons (o pacote "core" não traz).
val MicIcon: ImageVector = ImageVector.Builder(
    name = "Mic",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(
        "M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z" +
            "m5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z"
    ),
    fill = SolidColor(Color.Black),
).build()

/** Barra do topo; [actionLabel]/[onAction]: botão de texto à direita ("Cancelar" no cadastro, pedido do autor, 27/10). */
@Composable
fun GenkiTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Voltar",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = if (onBack != null) 4.dp else 20.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backLabel)
                }
                Spacer(Modifier.width(4.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) { Text(actionLabel) }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** Faixa fixa de ações no rodapé, com linha fina acima. */
@Composable
fun BottomActions(content: @Composable ColumnScope.() -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (color == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.outline else color),
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = color)
    }
}

/** Mensagem de erro do campo, anunciada pelo leitor de tela quando aparece. */
@Composable
fun FieldError(message: String?) {
    if (message == null) return
    Row(
        Modifier.padding(top = 6.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Cartão branco com borda, como os do protótipo. */
@Composable
fun GenkiCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
    val color = MaterialTheme.colorScheme.surfaceContainerLowest
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, border = border, color = color, content = content)
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = shape, border = border, color = color, content = content)
    }
}

/** Rótulo e valor (Detalhes e revisão); [help] em texto secundário abaixo. */
@Composable
fun InfoRow(label: String, value: String, help: String? = null, secondary: String? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall)
        if (secondary != null) {
            Text(secondary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (help != null) {
            Text(help, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Título de seção em verde (formulário e revisão). */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.semantics { heading() },
    )
}

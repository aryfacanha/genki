package br.edu.ifrs.genki.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

// Alto-falante e alto-falante cortado do Material Icons (o pacote "core" não traz): botão de silenciar a voz.
val VolumeUpIcon: ImageVector = ImageVector.Builder(
    name = "VolumeUp",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(
        "M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z" +
            "M14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"
    ),
    fill = SolidColor(Color.Black),
).build()

val VolumeOffIcon: ImageVector = ImageVector.Builder(
    name = "VolumeOff",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = addPathNodes(
        "M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63z" +
            "m2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71z" +
            "M4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3z" +
            "M12 4L9.91 6.09 12 8.18V4z"
    ),
    fill = SolidColor(Color.Black),
).build()

/**
 * Alto-falante do assistente de voz (DEC-049): cala a fala desta tela sem mudar Ajustes; tocar de novo volta a falar.
 * Pisca de leve enquanto a voz fala e fica parado quando calado (pedido do autor, 30/10).
 */
@Composable
fun VoiceMuteButton(muted: Boolean, speaking: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onToggle, modifier = modifier.size(48.dp)) {
        SpeakerIcon(muted, speaking, if (muted) "Voz desligada. Tocar para ouvir" else "Silenciar a voz")
    }
}

/**
 * Na voz (DEC-050), o alto-falante fica abaixo do microfone, com texto, onde a pessoa já está olhando: no topo da tela
 * ele passava despercebido (pedido do autor, 30/10). Liga ou cala a leitura das perguntas só neste cadastro.
 */
@Composable
fun ReadAloudButton(on: Boolean, speaking: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onToggle, modifier = modifier.heightIn(min = 48.dp)) {
        SpeakerIcon(muted = !on, speaking = speaking, description = null)
        Spacer(Modifier.width(8.dp))
        Text(if (on) "Silenciar a voz" else "Ouvir as perguntas em voz alta")
    }
}

@Composable
private fun SpeakerIcon(muted: Boolean, speaking: Boolean, description: String?) {
    val alpha = if (speaking && !muted) {
        rememberInfiniteTransition(label = "falando").animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "falando",
        ).value
    } else {
        1f
    }
    Icon(
        if (muted) VolumeOffIcon else VolumeUpIcon,
        contentDescription = description,
        tint = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        modifier = Modifier.alpha(alpha),
    )
}

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
            // Com fonte ampliada e "Cancelar" ao lado, "Novo medicamento" quebrava no meio da palavra (A31-04):
            // até duas linhas, encolhendo só o necessário.
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.titleMedium.fontSize,
                    maxFontSize = MaterialTheme.typography.headlineSmall.fontSize,
                ),
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
fun FieldError(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Row(
        modifier.padding(top = 6.dp).semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Campo para preencher em branco sobre o fundo esverdeado da tela, para não se confundir com títulos e textos
 * (pedido do autor, 28/10; contraste da borda 3,8:1, WCAG 1.4.11).
 */
@Composable
fun genkiFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
)

/**
 * Aviso de lista vazia (Hoje e Medicamentos). Cada tela o centraliza na área entre o topo e o botão de baixo, para
 * os dois avisos ficarem na mesma altura (bateria do autor no A31, 28/10).
 */
@Composable
fun EmptyMessage(title: String, text: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(56.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(PillsIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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

private val ChoiceGap = 8.dp

/**
 * Opções de toque em grade: no mesmo grupo, todas as caixas têm a mesma largura e altura e ficam alinhadas em
 * colunas (pedido do autor, 27/10). As colunas saem do maior rótulo e da largura da tela, até [maxColumns], e
 * diminuem sozinhas com fonte grande. Com [selected], a caixa marcada mostra ✓; sem ele, são respostas rápidas.
 */
@Composable
fun <T> ChoiceGrid(
    items: List<T>,
    label: (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    selected: ((T) -> Boolean)? = null,
    description: ((T) -> String)? = null,
    maxColumns: Int = 3,
) {
    val style = MaterialTheme.typography.labelLarge
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val widest = items.maxOfOrNull { measurer.measure(label(it), style).size.width } ?: 0
        // Rótulo + 4 dp de cada lado + espaço do ✓ (16 dp e 2 dp de folga).
        val cell = with(density) { widest.toDp() } + 8.dp + if (selected != null) 18.dp else 0.dp
        val columns = ((maxWidth + ChoiceGap) / (cell + ChoiceGap)).toInt().coerceIn(1, maxColumns)
        Column(verticalArrangement = Arrangement.spacedBy(ChoiceGap)) {
            items.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(ChoiceGap)) {
                    row.forEach { item ->
                        ChoiceCell(
                            text = label(item),
                            checked = selected?.invoke(item),
                            description = description?.invoke(item),
                            onClick = { onClick(item) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    // Linha incompleta: as caixas mantêm a largura das de cima.
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun ChoiceCell(text: String, checked: Boolean?, description: String?, onClick: () -> Unit, modifier: Modifier) {
    val on = checked == true
    val colors = MaterialTheme.colorScheme
    val semantics = Modifier.heightIn(min = 48.dp).semantics {
        description?.let { contentDescription = it }
        role = if (checked != null) Role.Checkbox else Role.Button
    }
    val content: @Composable () -> Unit = {
        Row(
            Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ✓ só na marcada; a caixa tem largura fixa, então não muda de tamanho, e o texto das outras fica centrado.
            if (on) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(2.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center, modifier = Modifier.weight(1f, fill = false))
        }
    }
    val shape = RoundedCornerShape(8.dp)
    val border = if (on) null else BorderStroke(1.dp, colors.outline)
    if (checked != null) {
        Surface(
            selected = on,
            onClick = onClick,
            shape = shape,
            color = if (on) colors.secondaryContainer else colors.surface,
            contentColor = if (on) colors.onSecondaryContainer else colors.onSurfaceVariant,
            border = border,
            modifier = modifier.then(semantics),
            content = content,
        )
    } else {
        Surface(
            onClick = onClick,
            shape = shape,
            color = colors.surface,
            contentColor = colors.onSurfaceVariant,
            border = border,
            modifier = modifier.then(semantics),
            content = content,
        )
    }
}

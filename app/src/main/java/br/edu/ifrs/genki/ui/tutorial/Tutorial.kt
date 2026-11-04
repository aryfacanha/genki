package br.edu.ifrs.genki.ui.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.ui.components.VoiceMuteButton
import br.edu.ifrs.genki.voice.Speaker

/** Partes da tela que o tutorial destaca; cada uma marcada com [tutorialTarget] onde é desenhada. */
enum class TutorialTarget { ADD_BUTTON, NEXT, VOICE, TYPE, CAMERA, TAB_MEDICATIONS, TAB_SETTINGS }

/** Tela em que o passo acontece: o tutorial navega sozinho entre a Hoje e "Adicionar medicamento". */
enum class TutorialScreen { HOME, ADD }

data class TutorialStep(val target: TutorialTarget, val screen: TutorialScreen, val title: String, val text: String)

/**
 * Tutorial guiado (DEC-044, pedido do autor depois do teste informal; fora da proposta): escurece a tela, deixa acesa
 * só a parte explicada e mostra um balão com "Próximo" e "Pular tutorial". Abre depois da introdução e em Ajustes.
 */
object TutorialSteps {
    val all = listOf(
        TutorialStep(TutorialTarget.ADD_BUTTON, TutorialScreen.HOME, "Cadastrar um remédio",
            "Toque aqui para cadastrar um remédio novo."),
        TutorialStep(TutorialTarget.NEXT, TutorialScreen.HOME, "Próximo lembrete",
            "Quando houver remédios cadastrados, aqui aparecem o próximo horário e os remédios dele. Na hora, o aviso aparece nesta tela, com “Entendi” e “Adiar”."),
        TutorialStep(TutorialTarget.VOICE, TutorialScreen.ADD, "Usar voz",
            "Diga o nome, quanto toma e os horários. O Genki pergunta o que faltar."),
        TutorialStep(TutorialTarget.TYPE, TutorialScreen.ADD, "Digitar",
            "Preencha as informações em três passos."),
        TutorialStep(TutorialTarget.CAMERA, TutorialScreen.ADD, "Usar câmera",
            "Experimental. Fotografe a caixa: o Genki tenta ler o nome e a concentração. Você sempre confere tudo antes de salvar; para desistir, toque em “Cancelar”."),
        TutorialStep(TutorialTarget.TAB_MEDICATIONS, TutorialScreen.HOME, "Medicamentos",
            "Todos os remédios cadastrados. Toque em um para ver os detalhes, editar ou excluir."),
        TutorialStep(TutorialTarget.TAB_SETTINGS, TutorialScreen.HOME, "Ajustes",
            "Som do aviso, repetição, voz e permissões. Lá você também pode ver este tutorial de novo."),
    )

    /** Balão acima do destaque quando ele está na metade de baixo da tela; senão, abaixo. */
    fun balloonAbove(target: Rect, screenHeight: Float): Boolean = target.center.y > screenHeight / 2
}

class TutorialController {
    /** Passo atual; `null` = tutorial fechado. */
    var step by mutableStateOf<Int?>(null)
        private set
    /** Onde cada parte está na tela, em coordenadas da raiz; some quando a parte sai da tela. */
    val targets = mutableStateMapOf<TutorialTarget, Rect>()

    /** Perguntando, antes de começar, se a pessoa quer o tutorial (primeira abertura). */
    var offering by mutableStateOf(false)
        private set

    /** Assistente de voz silenciado só neste tutorial (DEC-049); volta ao normal quando ele fecha. */
    var muted by mutableStateOf(false)
        private set

    /** Lendo o passo agora: o alto-falante pisca. */
    var speaking by mutableStateOf(false)

    val current: TutorialStep? get() = step?.let(TutorialSteps.all::getOrNull)

    /** Passo atual (0 = o primeiro); `-1` com o tutorial fechado. */
    val index: Int get() = step ?: -1

    fun offer() { offering = true }
    fun start() { offering = false; step = 0 }
    fun next() { step = step?.let { if (it + 1 < TutorialSteps.all.size) it + 1 else null }; if (step == null) muted = false }
    fun back() { step = step?.let { if (it > 0) it - 1 else null }; if (step == null) muted = false }
    fun close() { offering = false; step = null; muted = false }
    fun toggleMute() { muted = !muted }
}

val LocalTutorial = staticCompositionLocalOf<TutorialController?> { null }

/** Marca esta parte da tela como alvo do tutorial. */
@Composable
fun Modifier.tutorialTarget(target: TutorialTarget): Modifier {
    val controller = LocalTutorial.current ?: return this
    DisposableEffect(controller, target) { onDispose { controller.targets.remove(target) } }
    return onGloballyPositioned { controller.targets[target] = it.boundsInRoot() }
}

/**
 * Bloqueia toques na tela de baixo sem consumir os eventos: consumir também os movimentos fazia o botão do balão
 * cancelar o clique quando o dedo deslizava um pouco ("Próximo" às vezes não passava no A31, 29/10).
 */
private fun Modifier.blockTouches() = pointerInput(Unit) {
    awaitPointerEventScope { while (true) awaitPointerEvent() }
}

private const val OFFER_TITLE = "Quer ver um tutorial rápido?"
private const val OFFER_TEXT =
    "Em sete passos, o Genki mostra onde cadastrar seus remédios e para que serve cada tela. Você também pode ver depois, em Ajustes."

/** Camada escura com o recorte no alvo e o balão; bloqueia toques na tela de baixo. */
@Composable
fun TutorialOverlay(controller: TutorialController) {
    if (!controller.offering && controller.step == null) return
    val voiceOn = (LocalContext.current.applicationContext as GenkiApplication).container.settings.voiceAssistant
    if (voiceOn) TutorialVoice(controller)
    if (controller.offering) {
        Offer(controller, voiceOn)
        return
    }
    val index = controller.step ?: return
    val step = controller.current ?: return
    val density = LocalDensity.current
    // Posição da própria camada na raiz, para os alvos caírem no lugar certo mesmo com recuos da janela.
    var origin by remember { mutableStateOf(Offset.Zero) }
    var height by remember { mutableStateOf(0f) }
    val target = controller.targets[step.target]?.translate(-origin)
    val margin = with(density) { 8.dp.toPx() }
    BackHandler { controller.back() }

    Box(
        Modifier.fillMaxSize()
            .onGloballyPositioned { origin = it.boundsInRoot().topLeft; height = it.size.height.toFloat() }
            .blockTouches(),
    ) {
        Canvas(Modifier.fillMaxSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
            drawRect(Color.Black.copy(alpha = 0.72f))
            if (target != null) {
                val topLeft = Offset(target.left - margin, target.top - margin)
                val size = Size(target.width + 2 * margin, target.height + 2 * margin)
                val corner = CornerRadius(16.dp.toPx())
                drawRoundRect(Color.Transparent, topLeft, size, corner, blendMode = BlendMode.Clear)
                drawRoundRect(Color.White, topLeft, size, corner, style = Stroke(3.dp.toPx()))
            }
        }
        val balloon = @Composable { modifier: Modifier -> Balloon(index, step, controller, voiceOn, modifier) }
        when {
            target == null -> balloon(Modifier.align(Alignment.Center))
            TutorialSteps.balloonAbove(target, height) -> Box(
                Modifier.fillMaxWidth().height(with(density) { (target.top - margin).coerceAtLeast(0f).toDp() }),
            ) { balloon(Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) }
            else -> Box(
                Modifier.fillMaxSize().padding(top = with(density) { (target.bottom + margin).toDp() }),
            ) { balloon(Modifier.align(Alignment.TopCenter).padding(top = 12.dp)) }
        }
    }
}

/**
 * Assistente de voz no tutorial (DEC-049): lê a pergunta inicial e cada passo; o botão de silenciar cala só este
 * tutorial e, ao ligar de novo, relê o passo da tela.
 */
@Composable
private fun TutorialVoice(controller: TutorialController) {
    val context = LocalContext.current
    val speaker = remember { Speaker(context) }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    val text = if (controller.offering) "$OFFER_TITLE $OFFER_TEXT" else controller.current?.let { "${it.title}. ${it.text}" }
    LaunchedEffect(text, controller.muted) {
        if (text != null && !controller.muted) speaker.speak(text) else speaker.stop()
    }
    val speaking = speaker.speaking
    SideEffect { controller.speaking = speaking }
}

/** Pergunta, na tela (e em voz alta com o assistente de voz), se a pessoa quer o tutorial (pedido do autor, 29/10). */
@Composable
private fun Offer(controller: TutorialController, voiceOn: Boolean) {
    BackHandler { controller.close() }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f)).blockTouches()) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp).widthIn(max = 480.dp).fillMaxWidth(),
        ) {
            Column(Modifier.padding(start = 20.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        OFFER_TITLE,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).padding(end = 8.dp).semantics { heading() },
                    )
                    if (voiceOn) VoiceMuteButton(controller.muted, controller.speaking, controller::toggleMute)
                }
                Text(
                    OFFER_TEXT,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, end = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = controller::close, modifier = Modifier.heightIn(min = 48.dp)) { Text("Agora não") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = controller::start, modifier = Modifier.heightIn(min = 48.dp)) { Text("Ver tutorial") }
                }
            }
        }
    }
}

@Composable
private fun Balloon(index: Int, step: TutorialStep, controller: TutorialController, voiceOn: Boolean, modifier: Modifier) {
    val last = index == TutorialSteps.all.size - 1
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier.padding(horizontal = 16.dp).widthIn(max = 480.dp).fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${index + 1} de ${TutorialSteps.all.size}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (voiceOn) VoiceMuteButton(controller.muted, controller.speaking, controller::toggleMute)
            }
            Text(
                step.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp, end = 8.dp).semantics { heading() },
            )
            Text(
                step.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 6.dp, end = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!last) {
                    TextButton(onClick = controller::close, modifier = Modifier.heightIn(min = 48.dp)) { Text("Pular tutorial") }
                }
                Spacer(Modifier.weight(1f))
                if (controller.index > 0) {
                    TextButton(onClick = controller::back, modifier = Modifier.heightIn(min = 48.dp)) { Text("Voltar") }
                }
                Button(onClick = controller::next, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(if (last) "Concluir" else "Próximo")
                }
            }
        }
    }
}

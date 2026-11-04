package br.edu.ifrs.genki.ui.intro

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.reminders.Notifications
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.ScrollHintColumn
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.ui.settings.AlarmSpeechSwitch
import br.edu.ifrs.genki.ui.settings.SoundChoices
import br.edu.ifrs.genki.ui.settings.VoiceAssistantSwitch

/**
 * Introdução na primeira abertura (DEC-041, pedido do autor; fora da proposta): o que o Genki faz e os avisos (som e
 * permissão). Repetição e escuta automática da voz ficam só em Ajustes (DEC-043). Ao terminar, ou em "Pular", abre o
 * tutorial guiado sobre as telas (DEC-044), que substitui as páginas estáticas de cadastro e de telas.
 *
 * O conteúdo são quatro blocos; quantas páginas eles ocupam depende do tamanho da fonte (ver [PagedBlocks]).
 */
@Composable
fun IntroScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val settings = (context.applicationContext as GenkiApplication).container.settings
    var page by rememberSaveable { mutableIntStateOf(0) }
    // Quantas páginas o conteúdo ocupa com a fonte atual; sai da medição em [PagedBlocks]. Salvo junto com a página: se a
    // tela é recriada (fonte do sistema mudou, processo morto), a página não volta para a 2 antes da primeira medição
    // (RV-125-01).
    var pageCount by rememberSaveable { mutableIntStateOf(2) }
    LaunchedEffect(pageCount) { if (page > pageCount - 1) page = pageCount - 1 }
    val finish = {
        settings.introDone = true
        onDone()
    }
    BackHandler(enabled = page > 0) { page-- }

    // Permissão muda fora do app; relê ao voltar.
    var resumes by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { resumes++ }
    val allowed = remember(resumes) { Notifications.canNotify(context) }
    val exactAllowed = remember(resumes) {
        (context.applicationContext as GenkiApplication).container.reminderScheduler.canScheduleExact()
    }
    // Recusou o pedido do Android: fica aqui; só um novo toque leva às configurações do celular (RV-4041-03).
    var asked by rememberSaveable { mutableStateOf(false) }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { resumes++ }
    val permit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.hasPermission(context) && !asked) {
            asked = true
            request.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            context.startActivity(Notifications.settingsIntent(context))
        }
    }
    // Na tela inicial, desligado por padrão (DEC-049).
    var voiceAssistant by rememberSaveable { mutableStateOf(settings.voiceAssistant) }
    val reminders by settings.reminders.collectAsStateWithLifecycle()

    // Com fonte pequena os blocos cabem juntos (duas páginas, como antes); com fonte grande cada página leva só os que
    // cabem sem rolar, e a introdução passa de duas a quatro páginas (pedido do autor, 03/11).
    val blocks = listOf<@Composable () -> Unit>(
        { WelcomeTop() },
        { WelcomeRest(voiceAssistant) { voiceAssistant = it; settings.voiceAssistant = it } },
        { SoundBlock(reminders.alarmSound, settings::setAlarmSound, reminders.alarmSpeech, settings::setAlarmSpeech) },
        { PermissionBlock(allowed, exactAllowed, permit) },
    )
    val last = page >= pageCount - 1

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${page + 1} de $pageCount",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (!last) {
                TextButton(onClick = finish, modifier = Modifier.heightIn(min = 48.dp)) { Text("Pular") }
            }
        }
        PagedBlocks(blocks, page, onPages = { pageCount = it }, modifier = Modifier.weight(1f))
        BottomActions {
            when {
                !last -> PrimaryButton("Continuar", onClick = { page++ })
                allowed -> PrimaryButton("Começar", onClick = finish)
                // Sem a permissão, "Permitir notificações" é o botão principal da página (P9).
                else -> SecondaryButton("Começar sem permitir", onClick = finish)
            }
        }
    }
}

/**
 * Mede cada bloco com a largura e a fonte atuais, junta em cada página os que cabem sem rolar ([IntroPagination]) e
 * mostra a página [page]. O bloco que sozinho passa da altura rola, com o indicador "ver mais".
 */
@Composable
private fun PagedBlocks(
    blocks: List<@Composable () -> Unit>,
    page: Int,
    onPages: (Int) -> Unit,
    modifier: Modifier,
) {
    SubcomposeLayout(modifier) { constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
        val heights = blocks.mapIndexed { i, block ->
            subcompose("medida$i") { Column { block() } }.sumOf { it.measure(loose).height }
        }
        val padding = 16.dp.roundToPx()
        val pages = IntroPagination.pack(heights, constraints.maxHeight - padding)
        val current = page.coerceIn(0, pages.lastIndex)
        val body = subcompose("pagina") {
            SideEffect { onPages(pages.size) }
            key(current) {
                Column(Modifier.fillMaxSize()) {
                    ScrollHintColumn(Modifier.padding(vertical = 8.dp)) { pages[current].forEach { blocks[it]() } }
                }
            }
        }.map { it.measure(constraints) }
        layout(constraints.maxWidth, constraints.maxHeight) { body.forEach { it.place(0, 0) } }
    }
}

@Composable
private fun Title(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 12.dp).semantics { heading() },
    )
}

@Composable
private fun Paragraph(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
}

@Composable
private fun WelcomeTop() {
    Box(
        Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp).size(64.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(PillsIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(32.dp))
    }
    Title("Boas-vindas ao Genki")
    Paragraph("O Genki avisa na hora de tomar seus remédios.")
    Paragraph("Ele não registra se você tomou e não substitui a orientação do médico ou do farmacêutico.")
}

@Composable
private fun WelcomeRest(voiceAssistant: Boolean, onVoiceAssistant: (Boolean) -> Unit) {
    Paragraph("Tudo fica só neste celular.")
    Paragraph("Para letras maiores, aumente o tamanho da fonte nas configurações do celular.")
    Paragraph("Depois, você pode ver um tutorial rápido.")
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(top = 8.dp))
    VoiceAssistantSwitch(voiceAssistant, onVoiceAssistant)
}

@Composable
private fun SoundBlock(alarm: Boolean, onSelect: (Boolean) -> Unit, speech: Boolean, onSpeech: (Boolean) -> Unit) {
    Title("Avisos")
    SoundChoices(alarm, onSelect)
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    // Como em Ajustes: com "Como alarme" a opção de falar o nome aparece logo abaixo, na mesma página (autor, 03/11).
    if (alarm) {
        AlarmSpeechSwitch(speech, onSpeech)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun PermissionBlock(allowed: Boolean, exactAllowed: Boolean, onPermit: () -> Unit) {
    Paragraph("Para avisar na hora, o Genki precisa mostrar notificações.")
    if (allowed) {
        Text(
            "Notificações permitidas.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
    } else {
        PrimaryButton("Permitir notificações", onClick = onPermit, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        Paragraph("Se preferir, você pode permitir depois em Ajustes.")
    }
    // Sem alarmes exatos o lembrete pode atrasar; só Ajustes dizia (P10).
    if (!exactAllowed) {
        Paragraph("Este celular não permite alarmes no horário exato: o lembrete pode atrasar alguns minutos. Dá para mudar em Ajustes.")
    }
    Spacer(Modifier.height(8.dp))
    Paragraph("Se o aviso aparecer pequeno, abra as configurações do celular, procure as notificações do Genki e escolha mostrá-las como pop-up ou banner. O nome da opção muda conforme o celular.")
    Paragraph("Repetir o aviso e outros ajustes ficam em Ajustes.")
}

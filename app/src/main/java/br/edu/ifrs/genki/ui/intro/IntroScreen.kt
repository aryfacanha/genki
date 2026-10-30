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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.reminders.Notifications
import br.edu.ifrs.genki.ui.components.BottomActions
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.data.Settings
import br.edu.ifrs.genki.ui.settings.SoundChoices
import br.edu.ifrs.genki.ui.settings.VoiceAssistantSwitch

/**
 * Introdução na primeira abertura (DEC-041, pedido do autor; fora da proposta): o que o Genki faz e os avisos (som e
 * permissão). Repetição e escuta automática da voz ficam só em Ajustes (DEC-043). Ao terminar, ou em "Pular", abre o
 * tutorial guiado sobre as telas (DEC-044), que substitui as páginas estáticas de cadastro e de telas.
 */
@Composable
fun IntroScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val settings = (context.applicationContext as GenkiApplication).container.settings
    var page by rememberSaveable { mutableIntStateOf(0) }
    val finish = {
        settings.introDone = true
        onDone()
    }
    BackHandler(enabled = page > 0) { page-- }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${page + 1} de $PAGES",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (page < PAGES - 1) {
                TextButton(onClick = finish, modifier = Modifier.heightIn(min = 48.dp)) { Text("Pular") }
            }
        }
        // Cada página começa do topo.
        val scroll = rememberScrollState()
        LaunchedEffect(page) { scroll.scrollTo(0) }
        Column(Modifier.weight(1f).verticalScroll(scroll).padding(vertical = 8.dp)) {
            when (page) {
                0 -> Welcome(settings)
                else -> Notices()
            }
        }
        BottomActions {
            if (page < PAGES - 1) {
                PrimaryButton("Continuar", onClick = { page++ })
            } else {
                PrimaryButton("Começar", onClick = finish)
            }
        }
    }
}

private const val PAGES = 2

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
private fun Welcome(settings: Settings) {
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
    Paragraph("Tudo fica só neste celular.")
    Paragraph("Para letras maiores, aumente o tamanho da fonte nas configurações do celular.")
    Paragraph("Depois, você pode ver um tutorial rápido.")
    // Na tela inicial, desligado por padrão (DEC-049).
    var voiceAssistant by rememberSaveable { mutableStateOf(settings.voiceAssistant) }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(top = 8.dp))
    VoiceAssistantSwitch(voiceAssistant) { voiceAssistant = it; settings.voiceAssistant = it }
}

@Composable
private fun Notices() {
    val context = LocalContext.current
    val settings = (context.applicationContext as GenkiApplication).container.settings
    val reminders by settings.reminders.collectAsStateWithLifecycle()
    // Permissão muda fora do app; relê ao voltar.
    var resumes by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { resumes++ }
    val allowed = remember(resumes) { Notifications.canNotify(context) }
    // Recusou o pedido do Android: fica aqui; só um novo toque leva às configurações do celular (RV-4041-03).
    var asked by rememberSaveable { mutableStateOf(false) }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { resumes++ }
    Title("Avisos")
    SoundChoices(reminders.alarmSound, settings::setAlarmSound)
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Paragraph("Para avisar na hora, o Genki precisa mostrar notificações.")
    if (allowed) {
        Text(
            "Notificações permitidas.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
    } else {
        SecondaryButton(
            "Permitir notificações",
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.hasPermission(context) && !asked) {
                    asked = true
                    request.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.startActivity(Notifications.settingsIntent(context))
                }
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        Paragraph("Se preferir, você pode permitir depois em Ajustes.")
    }
    Spacer(Modifier.height(8.dp))
    Paragraph("Se o aviso aparecer pequeno, nas configurações de notificação do Genki escolha mostrar como pop-up.")
    Paragraph("Repetir o aviso e outros ajustes ficam em Ajustes.")
}

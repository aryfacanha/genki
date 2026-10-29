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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import br.edu.ifrs.genki.ui.components.CameraIcon
import br.edu.ifrs.genki.ui.components.MicIcon
import br.edu.ifrs.genki.ui.components.PillsIcon
import br.edu.ifrs.genki.ui.components.PrimaryButton
import br.edu.ifrs.genki.ui.components.SecondaryButton
import br.edu.ifrs.genki.ui.settings.SoundChoices

/**
 * Introdução na primeira abertura (DEC-041, pedido do autor; fora da proposta), também tutorial básico (DEC-043):
 * o que o Genki faz, como cadastrar passo a passo, para que serve cada tela e os avisos (som e permissão).
 * Repetição e escuta automática da voz ficam só em Ajustes, para a primeira abertura ser mais rápida (teste informal,
 * 29/10). "Pular" em todas as telas; em Ajustes, "Ver como o Genki funciona" abre de novo.
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
                0 -> Welcome()
                1 -> HowToAdd()
                2 -> Screens()
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

private const val PAGES = 4

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
private fun Welcome() {
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
}

@Composable
private fun HowToAdd() {
    Title("Como cadastrar um remédio")
    Step(1, "Na tela Hoje, toque em \u201cAdicionar medicamento\u201d.")
    Step(2, "Escolha como quer cadastrar:")
    Way(MicIcon, "Falar", "Diga o nome, quanto toma e os horários. O Genki pergunta o que faltar.")
    Way(Icons.Filled.Edit, "Digitar", "Preencha as informações em três passos.")
    Way(CameraIcon, "Câmera", "Fotografe a caixa: o Genki lê o nome e a concentração.")
    Step(3, "Confira tudo e toque em \u201cConfirmar e salvar\u201d.")
    Paragraph("Para desistir, toque em \u201cCancelar\u201d, no alto da tela.")
}

@Composable
private fun Step(number: Int, text: String) {
    Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
        Text("$number.", style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 28.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

/** As três abas, na ordem da barra de baixo. */
@Composable
private fun Screens() {
    Title("As telas do Genki")
    Paragraph("Use a barra de baixo para trocar de tela.")
    Way(Icons.Filled.Home, "Hoje", "Os horários do dia e o próximo lembrete. Quando for a hora, o aviso aparece aqui, com \u201cEntendi\u201d e \u201cAdiar\u201d.")
    Way(PillsIcon, "Medicamentos", "Todos os remédios cadastrados. Toque em um para ver os detalhes, editar ou excluir.")
    Way(Icons.Filled.Settings, "Ajustes", "Som do aviso, repetição, voz e permissões. Lá você também pode ver este tutorial de novo.")
}

@Composable
private fun Way(icon: ImageVector, title: String, text: String) {
    Row(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
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

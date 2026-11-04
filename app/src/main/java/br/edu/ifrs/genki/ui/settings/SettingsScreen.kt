package br.edu.ifrs.genki.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.ifrs.genki.BuildConfig
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.reminders.Notifications
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.SecondaryButton

/**
 * Ajustes (RF24, RF25), como `AjustesScreen.tsx`, com o intervalo sempre visível (DEC-028)
 * e a situação das permissões de notificação e de alarme exato (ESPEC §6).
 */
@Composable
fun SettingsScreen(onTutorial: () -> Unit, scrollToPermissions: Boolean = false, onScrolled: () -> Unit = {}) {
    val context = LocalContext.current
    val container = (context.applicationContext as GenkiApplication).container
    val settings by container.settings.reminders.collectAsStateWithLifecycle()
    var autoListen by remember { mutableStateOf(container.settings.autoListen) }
    var voiceAssistant by remember { mutableStateOf(container.settings.voiceAssistant) }
    // Permissões mudam fora do app; relê ao voltar para a tela.
    var resumes by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { resumes++ }
    // O canal verificado depende do som escolhido (RV-4041-02).
    val notificationsAllowed = remember(resumes, settings.alarmSound) { Notifications.canNotify(context) }
    val exactAllowed = remember(resumes) { container.reminderScheduler.canScheduleExact() }
    val fullScreenAllowed = remember(resumes) { Notifications.canShowFullScreen(context) }
    // Depois de duas recusas o sistema não pergunta mais; aí, ou com a categoria desligada, abre o ajuste do sistema.
    val requestNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!Notifications.canNotify(context)) openNotificationSettings(context)
        resumes++
    }

    val scroll = rememberScrollState()
    var permissionsY by remember { mutableIntStateOf(-1) }
    LaunchedEffect(scrollToPermissions, permissionsY) {
        if (scrollToPermissions && permissionsY >= 0) {
            scroll.animateScrollTo(permissionsY)
            onScrolled()
        }
    }

    Column(Modifier.fillMaxSize()) {
        GenkiTopBar("Ajustes")
        Column(Modifier.weight(1f).verticalScroll(scroll)) {
            SectionHeader("Lembretes")
            RepeatSwitch(settings.repeatEnabled, container.settings::setRepeatEnabled)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            IntervalChoices(settings.intervalMinutes, container.settings::setIntervalMinutes)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SoundChoices(settings.alarmSound, container.settings::setAlarmSound)
            if (settings.alarmSound) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AlarmSpeechSwitch(settings.alarmSpeech, container.settings::setAlarmSpeech)
            }

            SectionHeader("Voz")
            VoiceAssistantSwitch(voiceAssistant) { voiceAssistant = it; container.settings.voiceAssistant = it }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            AutoListenSwitch(autoListen) { autoListen = it; container.settings.autoListen = it }

            SectionHeader("Permissões", Modifier.onGloballyPositioned { permissionsY = it.positionInParent().y.toInt() })
            PermissionRow(
                title = "Notificações",
                status = if (notificationsAllowed) "Permitidas" else "Não permitidas. Sem elas, os lembretes não aparecem.",
                action = if (notificationsAllowed) null else "Permitir notificações",
                onAction = {
                    if (!Notifications.hasPermission(context)) {
                        requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openNotificationSettings(context)
                    }
                },
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PermissionRow(
                    title = "Alarmes no horário exato",
                    status = if (exactAllowed) "Permitidos" else "Não permitidos. Sem eles, o lembrete pode atrasar alguns minutos.",
                    action = if (exactAllowed) null else "Abrir ajuste do sistema",
                    onAction = {
                        context.startActivity(
                            Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                        )
                    },
                )
            }

            // Android 14+: sem esta permissão, o "Como alarme" toca, mas não abre a tela de alarme (DEC-048).
            if (settings.alarmSound && !fullScreenAllowed) {
                PermissionRow(
                    title = "Tela de alarme",
                    status = "Não permitida. O alarme toca, mas não aparece na tela bloqueada.",
                    action = "Abrir ajuste do sistema",
                    onAction = { context.startActivity(Notifications.fullScreenSettingsIntent(context)) },
                )
            }

            SectionHeader("Sobre")
            Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text("Genki", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Aplicativo para gerenciamento de medicamentos e lembretes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Versão ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Privacidade e dados", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp))
                PRIVACY_TEXT.forEach {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
                SecondaryButton("Ver o tutorial", onClick = onTutorial, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

/** Texto de "Sobre" aprovado pelo autor em 03/11/2026 (P21). */
private val PRIVACY_TEXT = listOf(
    "Tudo fica só neste celular. O Genki não tem conta e não acessa a internet.",
    "A voz é reconhecida pelo reconhecedor de voz do próprio celular. O Genki não grava nem guarda o áudio; o que o reconhecedor faz com ele depende do aparelho.",
    "Não há cópia de segurança: se você desinstalar o Genki, apagar os dados ou trocar de celular, os cadastros se perdem.",
)

private fun openNotificationSettings(context: Context) {
    context.startActivity(Notifications.settingsIntent(context))
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp).semantics { heading() },
    )
}

@Composable
private fun PermissionRow(title: String, status: String, action: String?, onAction: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(status, color = if (action == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
        if (action != null) {
            Spacer(Modifier.size(8.dp))
            SecondaryButton(action, onClick = onAction)
        }
    }
}

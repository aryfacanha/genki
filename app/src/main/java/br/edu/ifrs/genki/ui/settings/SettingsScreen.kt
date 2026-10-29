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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import br.edu.ifrs.genki.ui.components.GenkiTopBar
import br.edu.ifrs.genki.ui.components.SecondaryButton

/**
 * Ajustes (RF24, RF25), como `AjustesScreen.tsx`, com o intervalo sempre visível (DEC-028)
 * e a situação das permissões de notificação e de alarme exato (ESPEC §6).
 */
@Composable
fun SettingsScreen(onIntro: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as GenkiApplication).container
    val settings by container.settings.reminders.collectAsStateWithLifecycle()
    var autoListen by remember { mutableStateOf(container.settings.autoListen) }
    // Permissões mudam fora do app; relê ao voltar para a tela.
    var resumes by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { resumes++ }
    // O canal verificado depende do som escolhido (RV-4041-02).
    val notificationsAllowed = remember(resumes, settings.alarmSound) { Notifications.canNotify(context) }
    val exactAllowed = remember(resumes) { container.reminderScheduler.canScheduleExact() }
    // Depois de duas recusas o sistema não pergunta mais; aí, ou com a categoria desligada, abre o ajuste do sistema.
    val requestNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!Notifications.canNotify(context)) openNotificationSettings(context)
        resumes++
    }

    Column(Modifier.fillMaxSize()) {
        GenkiTopBar("Ajustes")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            SectionHeader("Lembretes")
            RepeatSwitch(settings.repeatEnabled, container.settings::setRepeatEnabled)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            IntervalChoices(settings.intervalMinutes, container.settings::setIntervalMinutes)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SoundChoices(settings.alarmSound, container.settings::setAlarmSound)

            SectionHeader("Voz")
            AutoListenSwitch(autoListen) { autoListen = it; container.settings.autoListen = it }

            SectionHeader("Permissões")
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

            SectionHeader("Sobre")
            Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text("Genki", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Aplicativo para gerenciamento de medicamentos e lembretes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SecondaryButton("Ver como o Genki funciona", onClick = onIntro, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

private fun openNotificationSettings(context: Context) {
    context.startActivity(Notifications.settingsIntent(context))
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp).semantics { heading() },
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

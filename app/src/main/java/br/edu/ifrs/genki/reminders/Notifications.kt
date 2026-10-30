package br.edu.ifrs.genki.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.MainActivity
import br.edu.ifrs.genki.R
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.ui.MedicationText
import java.time.LocalTime
import android.net.Uri

/** Notificação do lembrete. Reconhecer não é ingestão: nunca "Tomado" (DEC-004). */
object Notifications {
    private const val CHANNEL = "lembretes"
    /** Canal antigo do "Como alarme" (DEC-032), com o som na notificação; apagado na criação dos canais. */
    private const val OLD_ALARM_CHANNEL = "lembretes_alarme"
    /**
     * "Como alarme" (DEC-048): canal sem som, porque quem toca é o [AlarmSoundService] (em loop, no volume de alarme);
     * a notificação abre a [AlarmActivity] em tela cheia.
     */
    private const val ALARM_CHANNEL = "lembretes_despertador"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL, "Lembretes de medicamentos", NotificationManager.IMPORTANCE_HIGH)
            .apply { description = "Avisos nos horários cadastrados." }
        val alarm = NotificationChannel(ALARM_CHANNEL, "Lembretes como alarme", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Tela de alarme nos horários cadastrados; o som de alarme é tocado pelo Genki, mesmo no silencioso."
            setSound(null, null)
            enableVibration(false)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.deleteNotificationChannel(OLD_ALARM_CHANNEL)
        manager.createNotificationChannels(listOf(channel, alarm))
    }

    /** "Como alarme" escolhido em Ajustes. */
    fun alarmMode(context: Context): Boolean =
        (context.applicationContext as GenkiApplication).container.settings.reminders.value.alarmSound

    /** Canal em uso: o de alarme se a pessoa escolheu "Como alarme". */
    private fun channel(context: Context): String = if (alarmMode(context)) ALARM_CHANNEL else CHANNEL

    /** Android 14+: a tela cheia depende de uma permissão especial que a pessoa pode tirar. */
    fun canShowFullScreen(context: Context): Boolean =
        Build.VERSION.SDK_INT < 34 || context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

    /** Ajuste do sistema da tela cheia (Android 14+). */
    fun fullScreenSettingsIntent(context: Context): Intent =
        Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT", Uri.parse("package:${context.packageName}"))

    /** Permissão, notificações do app e a categoria em uso ligadas (RV-HOJE-01). */
    fun canNotify(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            hasPermission(context) &&
            !channelBlocked(context)

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun channelBlocked(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java).getNotificationChannel(channel(context))?.importance ==
            NotificationManager.IMPORTANCE_NONE

    /** Ajuste do sistema que resolve: a categoria, se só ela estiver desligada; senão, as notificações do app. */
    fun settingsIntent(context: Context): Intent =
        if (NotificationManagerCompat.from(context).areNotificationsEnabled() && channelBlocked(context)) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, channel(context))
        } else {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

    /**
     * "Lembrete das 08:00" / "Losartana 50 mg · 1 comprimido", com "Entendi" e "Adiar N min". No modo alarme, abre a
     * tela de alarme em tela cheia. `true` = notificação postada.
     */
    fun show(context: Context, timeId: Long, medication: Medication, time: LocalTime, intervalMinutes: Int, snoozed: Boolean): Boolean {
        if (!canNotify(context)) return false
        val title = "Lembrete das ${MedicationText.time(time)}" + if (snoozed) " (adiado)" else ""
        val text = listOfNotNull(medication.name, medication.concentration).joinToString(" ") + " · " + medication.dose
        val openApp = PendingIntent.getActivity(
            context, 0,
            // SINGLE_TOP reaproveita a tela aberta (sem recriar o app nem perder um cadastro em andamento) e pede a Hoje.
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.EXTRA_OPEN_HOME, true),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val ack = ReminderScheduler.pendingIntent(context, ReminderAction.ACK, timeId)
        val builder = NotificationCompat.Builder(context, channel(context))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setContentIntent(openApp)
            .setDeleteIntent(ack) // deslizar para fora = Entendi
            .addAction(0, "Entendi", ack)
            .addAction(0, "Adiar $intervalMinutes min", ReminderScheduler.pendingIntent(context, ReminderAction.SNOOZE, timeId))
        if (alarmMode(context)) {
            // Um PendingIntent por horário (requestCode = timeId): a tela mostra só os lembretes que a abriram.
            val alarmScreen = PendingIntent.getActivity(
                context, timeId.toInt(),
                Intent(context, AlarmActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                    .putExtra(AlarmActivity.EXTRA_TIME_ID, timeId),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(alarmScreen, true)
                .setContentIntent(alarmScreen)
        }
        return try {
            NotificationManagerCompat.from(context).notify(timeId.toInt(), builder.build())
            true
        } catch (_: SecurityException) {
            false // permissão retirada entre a checagem e o aviso
        }
    }

    fun cancel(context: Context, timeId: Long) = NotificationManagerCompat.from(context).cancel(timeId.toInt())
}

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

/** Notificação do lembrete. Reconhecer não é ingestão: nunca "Tomado" (DEC-004). */
object Notifications {
    private const val CHANNEL = "lembretes"
    /** Som de alarme (DEC-032): o Android não deixa trocar o som de um canal depois de criado, por isso outro canal. */
    private const val ALARM_CHANNEL = "lembretes_alarme"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL, "Lembretes de medicamentos", NotificationManager.IMPORTANCE_HIGH)
            .apply { description = "Avisos nos horários cadastrados." }
        val alarm = NotificationChannel(ALARM_CHANNEL, "Lembretes como alarme", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Avisos nos horários cadastrados, com som de alarme, mesmo no modo silencioso."
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(channel, alarm))
    }

    /** Canal em uso: o de alarme se a pessoa escolheu "Como alarme". */
    private fun channel(context: Context): String =
        if ((context.applicationContext as GenkiApplication).container.settings.reminders.value.alarmSound) ALARM_CHANNEL else CHANNEL

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

    /** "Lembrete das 08:00" / "Losartana 50 mg · 1 comprimido", com "Entendi" e "Adiar N min". */
    fun show(context: Context, timeId: Long, medication: Medication, time: LocalTime, intervalMinutes: Int, snoozed: Boolean) {
        if (!canNotify(context)) return
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
        val notification = NotificationCompat.Builder(context, channel(context))
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
            .build()
        try {
            NotificationManagerCompat.from(context).notify(timeId.toInt(), notification)
        } catch (_: SecurityException) {
            // permissão retirada entre a checagem e o aviso
        }
    }

    fun cancel(context: Context, timeId: Long) = NotificationManagerCompat.from(context).cancel(timeId.toInt())
}

package br.edu.ifrs.genki.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.edu.ifrs.genki.MainActivity
import br.edu.ifrs.genki.R
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.ui.MedicationText
import java.time.LocalTime

/** Notificação do lembrete. Reconhecer não é ingestão: nunca "Tomado" (DEC-004). */
object Notifications {
    private const val CHANNEL = "lembretes"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL, "Lembretes de medicamentos", NotificationManager.IMPORTANCE_HIGH)
            .apply { description = "Avisos nos horários cadastrados." }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)

    /** "Lembrete das 08:00" / "Losartana 50 mg · 1 comprimido", com "Entendi" e "Adiar N min". */
    fun show(context: Context, timeId: Long, medication: Medication, time: LocalTime, intervalMinutes: Int, snoozed: Boolean) {
        if (!canNotify(context)) return
        val title = "Lembrete das ${MedicationText.time(time)}" + if (snoozed) " (adiado)" else ""
        val text = listOfNotNull(medication.name, medication.concentration).joinToString(" ") + " · " + medication.dose
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val ack = ReminderScheduler.pendingIntent(context, ReminderAction.ACK, timeId)
        val notification = NotificationCompat.Builder(context, CHANNEL)
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

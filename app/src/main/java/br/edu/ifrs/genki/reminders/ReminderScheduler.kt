package br.edu.ifrs.genki.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import br.edu.ifrs.genki.data.MedicationWithTimes
import br.edu.ifrs.genki.data.Settings
import br.edu.ifrs.genki.data.minuteOfDayToTime
import br.edu.ifrs.genki.data.toDomain
import br.edu.ifrs.genki.domain.OccurrenceCalculator
import java.time.ZonedDateTime

/** Ações do [ReminderReceiver] (ESPEC §6). Estado só nos extras do Intent, sem tabela de alertas (S2). */
enum class ReminderAction { FIRE, REPEAT, ACK, SNOOZE, FIRE_SNOOZED }

/**
 * Um alarme por horário (`genki://time/{timeId}/{ação}`): exato quando o sistema permite,
 * senão `setAndAllowWhileIdle` (pode atrasar; Ajustes avisa).
 */
class ReminderScheduler(
    private val context: Context,
    private val settings: Settings,
    private val now: () -> ZonedDateTime = { ZonedDateTime.now() },
) {
    private val alarms = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()

    /** Agenda o próximo FIRE de cada horário; lembretes desligados ou tratamento encerrado cancelam tudo. */
    fun sync(entity: MedicationWithTimes, after: ZonedDateTime = now()) {
        val medication = entity.toDomain()
        entity.times.forEach { time ->
            val next = if (medication.remindersEnabled) {
                OccurrenceCalculator.next(medication, minuteOfDayToTime(time.minuteOfDay), after)
            } else null
            if (next == null) cancel(listOf(time.id)) else set(ReminderAction.FIRE, time.id, next.toInstant().toEpochMilli())
        }
    }

    /** Cancela alarmes, repetições, adiamentos e a notificação dos horários. */
    fun cancel(timeIds: List<Long>) {
        timeIds.forEach { timeId ->
            ReminderAction.entries.forEach { alarms.cancel(pendingIntent(context, it, timeId)) }
            Notifications.cancel(context, timeId)
        }
    }

    /** Repetição sem resposta (RF24), [count] = 1..3. */
    fun scheduleRepeat(timeId: Long, count: Int) =
        set(ReminderAction.REPEAT, timeId, afterInterval(), count)

    /** "Adiar N min" (RF25). */
    fun scheduleSnoozed(timeId: Long) = set(ReminderAction.FIRE_SNOOZED, timeId, afterInterval())

    fun cancelFollowUps(timeId: Long) {
        alarms.cancel(pendingIntent(context, ReminderAction.REPEAT, timeId))
        alarms.cancel(pendingIntent(context, ReminderAction.FIRE_SNOOZED, timeId))
    }

    private fun afterInterval() =
        now().plusMinutes(settings.reminders.value.intervalMinutes.toLong()).toInstant().toEpochMilli()

    private fun set(action: ReminderAction, timeId: Long, atMillis: Long, count: Int = 0) {
        val intent = pendingIntent(context, action, timeId, count)
        if (canScheduleExact()) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        }
    }

    companion object {
        const val EXTRA_TIME_ID = "timeId"
        const val EXTRA_COUNT = "count"

        /** Mesmo horário e ação = mesmo PendingIntent (o `data` diferencia); os extras são atualizados. */
        fun pendingIntent(context: Context, action: ReminderAction, timeId: Long, count: Int = 0): PendingIntent {
            val intent = Intent(context, ReminderReceiver::class.java)
                .setAction(action.name)
                .setData(Uri.parse("genki://time/$timeId/${action.name}"))
                .putExtra(EXTRA_TIME_ID, timeId)
                .putExtra(EXTRA_COUNT, count)
            return PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

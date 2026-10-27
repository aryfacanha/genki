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
import br.edu.ifrs.genki.domain.ReminderPlan
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
    private val alertStore: AlertStore,
    private val now: () -> ZonedDateTime = { ZonedDateTime.now() },
) {
    private val alarms = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()

    /** Agenda o próximo FIRE de cada horário (ver [ReminderPlan]). */
    fun sync(entity: MedicationWithTimes, after: ZonedDateTime = now()) {
        val medication = entity.toDomain()
        entity.times.forEach { time ->
            apply(time.id, ReminderPlan.of(medication, minuteOfDayToTime(time.minuteOfDay), after))
        }
    }

    /** Depois de um FIRE, reagenda só o horário que tocou (RV-M2-02). */
    fun syncFired(entity: MedicationWithTimes, timeId: Long) {
        val time = entity.times.firstOrNull { it.id == timeId } ?: return
        apply(timeId, ReminderPlan.afterFire(entity.toDomain(), minuteOfDayToTime(time.minuteOfDay), now()))
    }

    private fun apply(timeId: Long, plan: ReminderPlan) = when (plan) {
        is ReminderPlan.Schedule -> set(ReminderAction.FIRE, timeId, plan.at.toInstant().toEpochMilli())
        // Só o FIRE: o aviso da última ocorrência fica até Entendi/deslizar (RV-M2-01).
        ReminderPlan.NoMoreOccurrences -> alarms.cancel(pendingIntent(context, ReminderAction.FIRE, timeId))
        ReminderPlan.Disabled -> cancel(listOf(timeId))
    }

    /** Cancela alarmes, repetições, adiamentos, a notificação e o cartão da Hoje dos horários. */
    fun cancel(timeIds: List<Long>) {
        timeIds.forEach { timeId ->
            ReminderAction.entries.forEach { alarms.cancel(pendingIntent(context, it, timeId)) }
            Notifications.cancel(context, timeId)
        }
        alertStore.remove(timeIds)
    }

    /** Repetição sem resposta (RF24), [count] = 1..3; [snoozed] mantém "(adiado)" no título. */
    fun scheduleRepeat(timeId: Long, count: Int, snoozed: Boolean = false) =
        set(ReminderAction.REPEAT, timeId, afterInterval(), count, snoozed)

    /** "Adiar N min" (RF25). Devolve quando volta a avisar. */
    fun scheduleSnoozed(timeId: Long): Long = afterInterval().also { set(ReminderAction.FIRE_SNOOZED, timeId, it) }

    /** Botões do cartão da Hoje: mesmo caminho dos botões da notificação. */
    fun respond(action: ReminderAction, timeId: Long) = pendingIntent(context, action, timeId).send()

    fun cancelFollowUps(timeId: Long) {
        alarms.cancel(pendingIntent(context, ReminderAction.REPEAT, timeId))
        alarms.cancel(pendingIntent(context, ReminderAction.FIRE_SNOOZED, timeId))
    }

    private fun afterInterval() =
        now().plusMinutes(settings.reminders.value.intervalMinutes.toLong()).toInstant().toEpochMilli()

    private fun set(action: ReminderAction, timeId: Long, atMillis: Long, count: Int = 0, snoozed: Boolean = false) {
        val intent = pendingIntent(context, action, timeId, count, snoozed)
        if (canScheduleExact()) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, intent)
        }
    }

    companion object {
        const val EXTRA_TIME_ID = "timeId"
        const val EXTRA_COUNT = "count"
        const val EXTRA_SNOOZED = "snoozed"

        /** Mesmo horário e ação = mesmo PendingIntent (o `data` diferencia); os extras são atualizados. */
        fun pendingIntent(
            context: Context,
            action: ReminderAction,
            timeId: Long,
            count: Int = 0,
            snoozed: Boolean = false,
        ): PendingIntent {
            val intent = Intent(context, ReminderReceiver::class.java)
                .setAction(action.name)
                .setData(Uri.parse("genki://time/$timeId/${action.name}"))
                .putExtra(EXTRA_TIME_ID, timeId)
                .putExtra(EXTRA_COUNT, count)
                .putExtra(EXTRA_SNOOZED, snoozed)
            return PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}

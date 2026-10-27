package br.edu.ifrs.genki.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.edu.ifrs.genki.AppContainer
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.GenkiEval
import br.edu.ifrs.genki.data.minuteOfDayToTime
import br.edu.ifrs.genki.data.toDomain
import br.edu.ifrs.genki.reminders.ReminderScheduler.Companion.EXTRA_COUNT
import br.edu.ifrs.genki.reminders.ReminderScheduler.Companion.EXTRA_TIME_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** FIRE, REPEAT (até 3), ACK, SNOOZE e FIRE_SNOOZED (ESPEC §6, RF09, RF24, RF25). */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action?.let { name -> ReminderAction.entries.firstOrNull { it.name == name } } ?: return
        val timeId = intent.getLongExtra(EXTRA_TIME_ID, -1)
        val count = intent.getIntExtra(EXTRA_COUNT, 0)
        val container = (context.applicationContext as GenkiApplication).container
        val scheduler = container.reminderScheduler
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ReminderAction.ACK -> {
                        Notifications.cancel(context, timeId)
                        scheduler.cancelFollowUps(timeId)
                        GenkiEval.log("lembrete_reconhecido", "timeId" to timeId)
                    }
                    ReminderAction.SNOOZE -> {
                        Notifications.cancel(context, timeId)
                        scheduler.cancelFollowUps(timeId)
                        scheduler.scheduleSnoozed(timeId)
                        GenkiEval.log("lembrete_adiado", "timeId" to timeId)
                    }
                    else -> notify(context, container, action, timeId, count)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun notify(
        context: Context,
        container: AppContainer,
        action: ReminderAction,
        timeId: Long,
        count: Int,
    ) {
        // Revalida: horário apagado ou lembretes desligados depois do agendamento não avisam.
        val entity = container.medicationRepository.byTime(timeId) ?: return
        val medication = entity.toDomain()
        if (!medication.remindersEnabled) return
        val time = minuteOfDayToTime(entity.times.first { it.id == timeId }.minuteOfDay)
        val settings = container.settings.reminders.value
        val scheduler = container.reminderScheduler

        Notifications.show(context, timeId, medication, time, settings.intervalMinutes, snoozed = action == ReminderAction.FIRE_SNOOZED)
        GenkiEval.log("lembrete_emitido", "timeId" to timeId, "acao" to action, "repeticao" to count)

        when (action) {
            ReminderAction.FIRE, ReminderAction.FIRE_SNOOZED -> if (settings.repeatEnabled) scheduler.scheduleRepeat(timeId, 1)
            ReminderAction.REPEAT -> if (settings.repeatEnabled && count < MAX_REPEATS) scheduler.scheduleRepeat(timeId, count + 1)
            else -> Unit
        }
        if (action == ReminderAction.FIRE) scheduler.syncFired(entity, timeId)
    }

    companion object {
        const val MAX_REPEATS = 3
    }
}

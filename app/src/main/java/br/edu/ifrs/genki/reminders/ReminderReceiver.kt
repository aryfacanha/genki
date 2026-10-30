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
import br.edu.ifrs.genki.reminders.ReminderScheduler.Companion.EXTRA_SNOOZED
import br.edu.ifrs.genki.reminders.ReminderScheduler.Companion.EXTRA_TIME_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import br.edu.ifrs.genki.domain.ActiveAlert

/** FIRE, REPEAT (até 3), ACK, SNOOZE e FIRE_SNOOZED (ESPEC §6, RF09, RF24, RF25). */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action?.let { name -> ReminderAction.entries.firstOrNull { it.name == name } } ?: return
        val timeId = intent.getLongExtra(EXTRA_TIME_ID, -1)
        val count = intent.getIntExtra(EXTRA_COUNT, 0)
        // Aviso adiado e as repetições dele mostram "(adiado)".
        val snoozed = action == ReminderAction.FIRE_SNOOZED || intent.getBooleanExtra(EXTRA_SNOOZED, false)
        val container = (context.applicationContext as GenkiApplication).container
        val scheduler = container.reminderScheduler
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ReminderAction.ACK -> scheduler.acknowledge(timeId)
                    ReminderAction.SNOOZE -> scheduler.snooze(timeId)
                    else -> notify(context, container, action, timeId, count, snoozed)
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
        snoozed: Boolean,
    ) {
        // Revalida: horário apagado ou lembretes desligados depois do agendamento não avisam.
        val entity = container.medicationRepository.byTime(timeId) ?: return
        val medication = entity.toDomain()
        if (!medication.remindersEnabled) return
        val time = minuteOfDayToTime(entity.times.first { it.id == timeId }.minuteOfDay)
        val settings = container.settings.reminders.value
        val scheduler = container.reminderScheduler
        // Adiado que já está tocando: a recuperação na abertura do app pode ter reagendado o mesmo (RV-047-01).
        if (action == ReminderAction.FIRE_SNOOZED &&
            !ActiveAlert.shouldFireSnoozed(container.alertStore.alerts.value.firstOrNull { it.timeId == timeId })
        ) return

        val posted = Notifications.show(context, timeId, medication, time, settings.intervalMinutes, snoozed)
        if (posted && settings.alarmSound) AlarmSoundService.start(context, timeId)
        container.alertStore.ringing(timeId, medication.id, time)
        GenkiEval.log("lembrete_emitido", "timeId" to timeId, "acao" to action, "repeticao" to count)

        when (action) {
            ReminderAction.FIRE, ReminderAction.FIRE_SNOOZED -> if (settings.repeatEnabled) scheduler.scheduleRepeat(timeId, 1, snoozed)
            ReminderAction.REPEAT -> if (settings.repeatEnabled && count < MAX_REPEATS) scheduler.scheduleRepeat(timeId, count + 1, snoozed)
            else -> Unit
        }
        if (action == ReminderAction.FIRE) scheduler.syncFired(entity, timeId)
    }

    companion object {
        const val MAX_REPEATS = 3
    }
}

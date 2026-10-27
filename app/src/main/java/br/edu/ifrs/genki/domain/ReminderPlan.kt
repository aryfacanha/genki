package br.edu.ifrs.genki.domain

import java.time.LocalTime
import java.time.ZonedDateTime

/** O que fazer com o alarme de um horário (ESPEC §6). */
sealed interface ReminderPlan {
    data class Schedule(val at: ZonedDateTime) : ReminderPlan

    /** Tratamento terminou: não há outro FIRE, mas o aviso já emitido e suas repetições continuam (RV-M2-01). */
    data object NoMoreOccurrences : ReminderPlan

    /** Lembretes desligados: cancela tudo, inclusive o aviso na tela. */
    data object Disabled : ReminderPlan

    companion object {
        fun of(medication: Medication, time: LocalTime, after: ZonedDateTime): ReminderPlan {
            if (!medication.remindersEnabled) return Disabled
            return OccurrenceCalculator.next(medication, time, after)?.let(::Schedule) ?: NoMoreOccurrences
        }

        /**
         * Depois que [fired] tocou: só esse horário é reagendado, um minuto à frente para não repetir a ocorrência
         * que acabou de tocar. Os outros horários do medicamento não mudam (RV-M2-02).
         */
        fun afterFire(medication: Medication, fired: LocalTime, now: ZonedDateTime): ReminderPlan =
            of(medication, fired, now.plusMinutes(1))
    }
}

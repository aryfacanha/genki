package br.edu.ifrs.genki.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** Um horário de um medicamento num dia (lista Hoje). */
data class Occurrence(val time: LocalTime, val medication: Medication)

/**
 * Quando cada horário acontece (ESPEC §6, RF09). Datas civis no fuso recebido, que é o do aparelho.
 * Não olha `remindersEnabled`: quem agenda e quem monta a Hoje decidem isso.
 */
object OccurrenceCalculator {

    fun isScheduledOn(medication: Medication, date: LocalDate): Boolean =
        date.dayOfWeek in medication.days &&
            date >= medication.startDate &&
            (medication.endDate == null || date <= medication.endDate)

    /** Próxima data/hora estritamente depois de [after]; `null` = não agendar (tratamento terminou). */
    fun next(medication: Medication, time: LocalTime, after: ZonedDateTime): ZonedDateTime? {
        val first = maxOf(after.toLocalDate(), medication.startDate)
        // Uma semana e um dia cobrem qualquer combinação de dias marcados.
        for (offset in 0L..7L) {
            val date = first.plusDays(offset)
            if (medication.endDate != null && date > medication.endDate) return null
            if (!isScheduledOn(medication, date)) continue
            val candidate = ZonedDateTime.of(date, time, after.zone)
            if (candidate > after) return candidate
        }
        return null
    }

    /** Horários do dia dos medicamentos com lembretes ligados, por horário e depois por nome (divergência #17). */
    fun today(medications: List<Medication>, date: LocalDate): List<Occurrence> =
        medications
            .filter { it.remindersEnabled && isScheduledOn(it, date) }
            .flatMap { medication -> medication.times.map { Occurrence(it, medication) } }
            .sortedWith(compareBy({ it.time }, { it.medication.name.lowercase() }))

    /**
     * Primeiro horário depois do dia [date] (lembretes ligados), para a Hoje quando os de hoje acabaram
     * (comparação com o MyTherapy, 27/10); `null` = nenhum (tratamentos terminados ou lembretes desligados).
     */
    fun firstAfter(medications: List<Medication>, date: LocalDate, zone: ZoneId): Pair<LocalDate, Occurrence>? {
        val endOfDay = ZonedDateTime.of(date, LocalTime.MAX, zone)
        return medications
            .filter { it.remindersEnabled }
            .flatMap { medication ->
                medication.times.mapNotNull { time ->
                    next(medication, time, endOfDay)?.let { it.toLocalDate() to Occurrence(time, medication) }
                }
            }
            .minWithOrNull(compareBy({ it.first }, { it.second.time }, { it.second.medication.name.lowercase() }))
    }
}

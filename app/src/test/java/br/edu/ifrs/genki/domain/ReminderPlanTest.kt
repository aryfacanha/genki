package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderPlanTest {

    private val zone = ZoneId.of("America/Sao_Paulo")

    // Segunda-feira, 26/10/2026
    private val monday = LocalDate.of(2026, 10, 26)
    private val t2045 = LocalTime.of(20, 45)
    private val t2046 = LocalTime.of(20, 46)

    private fun med(end: LocalDate? = null, enabled: Boolean = true) = Medication(
        id = 1, name = "Losartana", concentration = "50 mg", dose = "1 comprimido",
        days = DayOfWeek.entries.toSet(), times = listOf(t2045, t2046),
        treatment = if (end == null) TreatmentType.CONTINUOUS else TreatmentType.TEMPORARY,
        startDate = monday, endDate = end, remindersEnabled = enabled,
    )

    private fun at(date: LocalDate, time: LocalTime) = ZonedDateTime.of(date, time, zone)

    @Test
    fun lastOccurrenceOnlyStopsNextFire() {
        // RV-M2-01: último dia, depois do disparo das 20:45 não há outro FIRE, mas não é "Disabled"
        // (que cancelaria o aviso na tela e as repetições).
        assertEquals(ReminderPlan.NoMoreOccurrences, ReminderPlan.afterFire(med(end = monday), t2045, at(monday, t2045)))
    }

    @Test
    fun treatmentAlreadyEndedIsNotDisabled() {
        // Abrir o app depois do último aviso também não pode apagá-lo.
        assertEquals(ReminderPlan.NoMoreOccurrences, ReminderPlan.of(med(end = monday), t2045, at(monday, t2046)))
    }

    @Test
    fun disabledCancelsEverything() {
        assertEquals(ReminderPlan.Disabled, ReminderPlan.of(med(enabled = false), t2045, at(monday, LocalTime.NOON)))
    }

    @Test
    fun firedTimeMovesToNextDay() {
        assertEquals(
            ReminderPlan.Schedule(at(monday.plusDays(1), t2045)),
            ReminderPlan.afterFire(med(), t2045, at(monday, t2045)),
        )
    }

    @Test
    fun consecutiveMinuteKeepsItsOccurrence() {
        // RV-M2-02: quando 20:45 toca, o horário das 20:46 continua hoje, inclusive no último dia.
        val now = at(monday, t2045)
        assertEquals(ReminderPlan.Schedule(at(monday, t2046)), ReminderPlan.of(med(), t2046, now))
        assertEquals(ReminderPlan.Schedule(at(monday, t2046)), ReminderPlan.of(med(end = monday), t2046, now))
    }

    @Test
    fun lateFireStillSchedulesNextDay() {
        // Alarme inexato que chega minutos depois não repete o mesmo dia.
        assertEquals(
            ReminderPlan.Schedule(at(monday.plusDays(1), t2045)),
            ReminderPlan.afterFire(med(), t2045, at(monday, LocalTime.of(20, 52))),
        )
    }
}

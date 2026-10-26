package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class OccurrenceCalculatorTest {

    private val zone = ZoneId.of("America/Sao_Paulo")

    // Segunda-feira, 26/10/2026
    private val monday = LocalDate.of(2026, 10, 26)

    private fun med(
        name: String = "Losartana",
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        times: List<LocalTime> = listOf(LocalTime.of(8, 0)),
        start: LocalDate = monday,
        end: LocalDate? = null,
        enabled: Boolean = true,
    ) = Medication(
        id = 1, name = name, concentration = null, dose = "1 comprimido", days = days, times = times,
        treatment = if (end == null) TreatmentType.CONTINUOUS else TreatmentType.TEMPORARY,
        startDate = start, endDate = end, remindersEnabled = enabled,
    )

    private fun at(date: LocalDate, hour: Int, minute: Int = 0) = ZonedDateTime.of(date, LocalTime.of(hour, minute), zone)

    @Test
    fun laterTodayWhenTimeHasNotPassed() {
        assertEquals(at(monday, 8), OccurrenceCalculator.next(med(), LocalTime.of(8, 0), at(monday, 7, 59)))
    }

    @Test
    fun exactlyNowIsNotNext() {
        assertEquals(at(monday.plusDays(1), 8), OccurrenceCalculator.next(med(), LocalTime.of(8, 0), at(monday, 8)))
    }

    @Test
    fun skipsDaysNotSelected() {
        val m = med(days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        // depois das 8h de segunda, o próximo é sexta
        assertEquals(at(monday.plusDays(4), 8), OccurrenceCalculator.next(m, LocalTime.of(8, 0), at(monday, 9)))
        // depois de sexta, volta a segunda da semana seguinte
        assertEquals(at(monday.plusDays(7), 8), OccurrenceCalculator.next(m, LocalTime.of(8, 0), at(monday.plusDays(4), 9)))
    }

    @Test
    fun waitsForStartDate() {
        val m = med(start = monday.plusDays(3))
        assertEquals(at(monday.plusDays(3), 8), OccurrenceCalculator.next(m, LocalTime.of(8, 0), at(monday, 6)))
    }

    @Test
    fun stopsAfterEndDate() {
        val m = med(end = monday.plusDays(1))
        assertEquals(at(monday.plusDays(1), 8), OccurrenceCalculator.next(m, LocalTime.of(8, 0), at(monday, 9)))
        assertNull(OccurrenceCalculator.next(m, LocalTime.of(8, 0), at(monday.plusDays(1), 9)))
    }

    @Test
    fun endDateIsInclusive() {
        val m = med(end = monday)
        assertEquals(at(monday, 20), OccurrenceCalculator.next(m, LocalTime.of(20, 0), at(monday, 9)))
    }

    @Test
    fun noSelectedDayBeforeEndMeansNoReminder() {
        // só sábado, mas o tratamento termina na quarta
        val m = med(days = setOf(DayOfWeek.SATURDAY), end = monday.plusDays(2))
        assertNull(OccurrenceCalculator.next(m, LocalTime.of(8, 0), at(monday, 6)))
    }

    @Test
    fun scheduledOnRespectsDaysAndPeriod() {
        val m = med(days = setOf(DayOfWeek.MONDAY), start = monday, end = monday.plusDays(7))
        assertTrue(OccurrenceCalculator.isScheduledOn(m, monday))
        assertFalse(OccurrenceCalculator.isScheduledOn(m, monday.plusDays(1)))
        assertTrue(OccurrenceCalculator.isScheduledOn(m, monday.plusDays(7)))
        assertFalse(OccurrenceCalculator.isScheduledOn(m, monday.minusDays(7)))
    }

    @Test
    fun todaySortsByTimeThenNameAndSkipsDisabled() {
        val list = OccurrenceCalculator.today(
            listOf(
                med(name = "Metformina", times = listOf(LocalTime.of(12, 0), LocalTime.of(8, 0))),
                med(name = "Atenolol", times = listOf(LocalTime.of(8, 0))),
                med(name = "Sinvastatina", times = listOf(LocalTime.of(7, 0)), enabled = false),
                med(name = "Futuro", start = monday.plusDays(1)),
            ),
            monday,
        )
        assertEquals(
            listOf("08:00 Atenolol", "08:00 Metformina", "12:00 Metformina"),
            list.map { "${it.time} ${it.medication.name}" },
        )
    }
}

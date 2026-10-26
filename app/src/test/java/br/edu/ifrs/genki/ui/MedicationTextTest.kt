package br.edu.ifrs.genki.ui

import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Problem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class MedicationTextTest {

    @Test
    fun timesJoinWithCommaAndE() {
        assertEquals("", MedicationText.times(emptyList()))
        assertEquals("08:00", MedicationText.times(listOf(LocalTime.of(8, 0))))
        assertEquals("08:00 e 20:00", MedicationText.times(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))))
        assertEquals(
            "07:05, 12:00 e 20:30",
            MedicationText.times(listOf(LocalTime.of(7, 5), LocalTime.of(12, 0), LocalTime.of(20, 30))),
        )
    }

    @Test
    fun allSevenDaysReadAsEveryDay() {
        assertEquals("Todos os dias", MedicationText.days(DayOfWeek.entries.toSet()))
    }

    @Test
    fun specificDaysStartOnSunday() {
        assertEquals(
            "domingo, quarta e sábado",
            MedicationText.days(setOf(DayOfWeek.SATURDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY)),
        )
        assertEquals("segunda", MedicationText.days(setOf(DayOfWeek.MONDAY)))
    }

    @Test
    fun doseLineOmitsMissingConcentration() {
        assertEquals("50 mg · 1 comprimido", MedicationText.doseLine("50 mg", "1 comprimido"))
        assertEquals("1 comprimido", MedicationText.doseLine(null, "1 comprimido"))
    }

    @Test
    fun periodUsesBrazilianDates() {
        assertEquals(
            "De 01/11/2026 até 07/11/2026",
            MedicationText.period(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7)),
        )
    }

    @Test
    fun longDateIsPortuguese() {
        assertEquals("Segunda-feira, 26 de outubro", MedicationText.longDate(LocalDate.of(2026, 10, 26)))
        assertEquals("Domingo, 1 de novembro", MedicationText.longDate(LocalDate.of(2026, 11, 1)))
    }

    @Test
    fun duplicateTimeHasItsOwnMessage() {
        assertEquals("Este horário já foi adicionado.", MedicationText.error(Field.TIMES, Problem.DUPLICATE_TIME))
        assertEquals("Adicione ao menos um horário.", MedicationText.error(Field.TIMES, Problem.MISSING))
    }
}

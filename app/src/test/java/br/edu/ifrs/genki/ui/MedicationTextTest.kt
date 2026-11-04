package br.edu.ifrs.genki.ui

import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.TreatmentType
import br.edu.ifrs.genki.domain.Problem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun upcomingDaySaysTomorrowOrTheDate() {
        val today = LocalDate.of(2026, 10, 27)
        assertEquals("Amanhã", MedicationText.upcomingDay(LocalDate.of(2026, 10, 28), today))
        assertEquals("Quinta-feira, 29 de outubro", MedicationText.upcomingDay(LocalDate.of(2026, 10, 29), today))
    }

    private fun medication(treatment: TreatmentType, end: LocalDate?) = Medication(
        id = 1, name = "Dipirona", concentration = null, dose = "40 gotas", days = DayOfWeek.entries.toSet(),
        times = listOf(LocalTime.of(8, 0)), treatment = treatment, startDate = LocalDate.of(2026, 10, 30), endDate = end,
        remindersEnabled = true,
    )

    @Test
    fun treatmentLineShowsTheEndAndWhetherItPassed() {
        val today = LocalDate.of(2026, 11, 5)
        assertEquals("Até 05/11/2026", MedicationText.treatmentLine(medication(TreatmentType.TEMPORARY, today), today))
        assertEquals(
            "Tratamento terminou em 04/11/2026",
            MedicationText.treatmentLine(medication(TreatmentType.TEMPORARY, today.minusDays(1)), today),
        )
        assertEquals("Uso contínuo (sem data para parar)", MedicationText.treatmentLine(medication(TreatmentType.CONTINUOUS, null), today))
    }

    @Test
    fun hiddenFromTodaySaysWhichAndWhy() {
        assertNull(MedicationText.hiddenFromToday(emptyList(), emptyList()))
        assertEquals("Não aparece aqui: Metformina (lembretes desligados).", MedicationText.hiddenFromToday(listOf("Metformina"), emptyList()))
        assertEquals(
            "Não aparecem aqui: Metformina e Losartana (lembretes desligados); Dipirona (tratamento terminou).",
            MedicationText.hiddenFromToday(listOf("Metformina", "Losartana"), listOf("Dipirona")),
        )
    }

    @Test
    fun pastToggleCountsTheHiddenTimes() {
        assertEquals("Ver o horário que já passou", MedicationText.pastToggle(1, shown = false))
        assertEquals("Ver os 19 horários que já passaram", MedicationText.pastToggle(19, shown = false))
        assertEquals("Esconder os horários que já passaram", MedicationText.pastToggle(19, shown = true))
    }

    @Test
    fun enablingAnEndedTreatmentSaysHowToGetRemindersBack() {
        assertEquals("Lembretes ativados", MedicationText.remindersToggled(true, null))
        assertEquals("Lembretes desligados", MedicationText.remindersToggled(false, LocalDate.of(2026, 10, 29)))
        assertEquals(
            "O tratamento terminou em 29/10/2026. Para receber lembretes, edite a duração.",
            MedicationText.remindersToggled(true, LocalDate.of(2026, 10, 29)),
        )
    }

    @Test
    fun capitalizedDaysForASingleLineValue() {
        assertEquals("Todos os dias", MedicationText.daysCapitalized(DayOfWeek.entries.toSet()))
        assertEquals("Segunda e sexta", MedicationText.daysCapitalized(setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY)))
    }
}

package br.edu.ifrs.genki.ui.form

import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Frequency
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.Problem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** Passagem do rascunho da voz/câmera para o formulário ("Prefiro digitar", "Continuar digitando"). */
class FormFieldsTest {

    private val today = LocalDate.of(2026, 10, 27)

    @Test
    fun draftWithoutDaysOpensWithNothingSelected() {
        // Decisão do autor (27/10): sem dias ditos, nenhuma opção marcada; o validador pede a escolha.
        val fields = FormFields.from(MedicationDraft(source = DraftSource.CAMERA, name = "Losartana", concentration = "50 mg"))
        assertNull(fields.daysMode)
        assertNull(fields.toDraft(null).days)
        val result = DraftValidator.normalizeAndValidate(fields.toDraft(null), today) as DraftValidator.Result.Invalid
        assertTrue(Field.DAYS to Problem.MISSING in result.problems)
    }

    @Test
    fun manualFormStillStartsWithEveryDay() {
        assertEquals(DaysMode.ALL, FormFields().daysMode)
    }

    @Test
    fun spokenDaysAreKept() {
        val all = FormFields.from(MedicationDraft(source = DraftSource.VOICE, days = DayOfWeek.entries.toSet()))
        assertEquals(DaysMode.ALL, all.daysMode)
        val some = FormFields.from(MedicationDraft(source = DraftSource.VOICE, days = setOf(DayOfWeek.MONDAY)))
        assertEquals(DaysMode.SPECIFIC, some.daysMode)
        assertEquals(setOf(DayOfWeek.MONDAY), some.specificDays)
    }

    @Test
    fun pendingFrequencyFillsTimesFromFirst() {
        // RV-M3-03: "duas vezes ao dia" sem horário sobrevive à troca para digitação.
        val fields = FormFields.from(MedicationDraft(source = DraftSource.VOICE, name = "Metformina", everyHours = 12))
        assertEquals(12, fields.everyHours)
        val filled = fields.addTime(LocalTime.of(7, 0))
        assertEquals(listOf(LocalTime.of(7, 0), LocalTime.of(19, 0)), filled.times)
        assertNull(filled.everyHours)
        // Depois disso, novo horário só se soma.
        assertEquals(3, filled.addTime(LocalTime.of(12, 0)).times.size)
    }

    @Test
    fun frequencyFill() {
        assertEquals(
            listOf(LocalTime.of(6, 0), LocalTime.of(14, 0), LocalTime.of(22, 0)),
            Frequency.fill(LocalTime.of(6, 0), 8),
        )
        assertEquals(listOf(LocalTime.of(3, 0), LocalTime.of(15, 0)), Frequency.fill(LocalTime.of(15, 0), 12))
    }
}

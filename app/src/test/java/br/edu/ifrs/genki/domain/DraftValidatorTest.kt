package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class DraftValidatorTest {

    private val today = LocalDate.of(2026, 10, 27)

    private val complete = MedicationDraft(
        source = DraftSource.MANUAL,
        name = "Losartana",
        concentration = "50 mg",
        dose = "1 comprimido",
        days = DayOfWeek.entries.toSet(),
        times = listOf(LocalTime.of(8, 0)),
        treatment = TreatmentType.CONTINUOUS,
    )

    private fun validate(draft: MedicationDraft) = DraftValidator.normalizeAndValidate(draft, today)

    private fun problems(draft: MedicationDraft) =
        (validate(draft) as DraftValidator.Result.Invalid).problems

    private fun valid(draft: MedicationDraft) = (validate(draft) as DraftValidator.Result.Valid).draft

    @Test
    fun completeDraftIsValid() {
        assertTrue(validate(complete) is DraftValidator.Result.Valid)
    }

    @Test
    fun emptyDraftReportsEveryRequiredField() {
        assertEquals(
            listOf(
                Field.NAME to Problem.MISSING,
                Field.DOSE to Problem.MISSING,
                Field.DAYS to Problem.MISSING,
                Field.TIMES to Problem.MISSING,
                Field.TREATMENT to Problem.MISSING,
            ),
            problems(MedicationDraft(source = DraftSource.MANUAL)),
        )
    }

    @Test
    fun blankTextCountsAsMissing() {
        assertEquals(
            listOf(Field.NAME to Problem.MISSING, Field.DOSE to Problem.MISSING),
            problems(complete.copy(name = "   ", dose = "")),
        )
    }

    @Test
    fun emptyDaySetCountsAsMissing() {
        assertEquals(listOf(Field.DAYS to Problem.MISSING), problems(complete.copy(days = emptySet())))
    }

    @Test
    fun trimsTextAndDropsBlankConcentration() {
        val draft = valid(complete.copy(name = "  Losartana ", concentration = "  ", dose = " 1 comprimido "))
        assertEquals("Losartana", draft.name)
        assertNull(draft.concentration)
        assertEquals("1 comprimido", draft.dose)
    }

    @Test
    fun concentrationIsNeverCopiedToDose() {
        assertEquals(listOf(Field.DOSE to Problem.MISSING), problems(complete.copy(dose = null)))
    }

    @Test
    fun sortsTimes() {
        val draft = valid(complete.copy(times = listOf(LocalTime.of(20, 0), LocalTime.of(8, 0))))
        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)), draft.times)
    }

    @Test
    fun repeatedTimeIsReportedNotDropped() {
        assertEquals(
            listOf(Field.TIMES to Problem.DUPLICATE_TIME),
            problems(complete.copy(times = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0), LocalTime.of(8, 0)))),
        )
    }

    @Test
    fun startDateDefaultsToToday() {
        assertEquals(today, valid(complete).startDate)
    }

    @Test
    fun endDateIsDroppedWhenNotTemporary() {
        val end = today.plusDays(5)
        assertNull(valid(complete.copy(endDate = end)).endDate)
        assertNull(valid(complete.copy(treatment = TreatmentType.UNDEFINED, endDate = end)).endDate)
    }

    @Test
    fun temporaryRequiresEndDate() {
        assertEquals(
            listOf(Field.END_DATE to Problem.MISSING),
            problems(complete.copy(treatment = TreatmentType.TEMPORARY)),
        )
    }

    @Test
    fun temporaryEndBeforeStartIsInvalid() {
        assertEquals(
            listOf(Field.END_DATE to Problem.END_BEFORE_START),
            problems(complete.copy(treatment = TreatmentType.TEMPORARY, startDate = today, endDate = today.minusDays(1))),
        )
    }

    @Test
    fun temporaryEndingOnStartDayIsValid() {
        val draft = valid(complete.copy(treatment = TreatmentType.TEMPORARY, endDate = today))
        assertEquals(today, draft.endDate)
    }

    @Test
    fun durationDaysIncludesTheStartDay() {
        // "por 7 dias" começando hoje termina daqui a 6 dias (DEC-003)
        val draft = valid(complete.copy(treatment = TreatmentType.TEMPORARY, durationDays = 7))
        assertEquals(today.plusDays(6), draft.endDate)
        assertNull(draft.durationDays)
    }

    @Test
    fun treatmentHasNoSilentDefault() {
        assertEquals(listOf(Field.TREATMENT to Problem.MISSING), problems(complete.copy(treatment = null)))
    }
}

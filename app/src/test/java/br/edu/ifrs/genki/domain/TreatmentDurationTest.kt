package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TreatmentDurationTest {
    private val start = LocalDate.of(2026, 11, 3)

    @Test
    fun startDayCounts() {
        assertEquals(LocalDate.of(2026, 11, 3), TreatmentDuration.endDate(start, 1))
        assertEquals(LocalDate.of(2026, 11, 9), TreatmentDuration.endDate(start, 7))
        assertEquals(LocalDate.of(2026, 12, 2), TreatmentDuration.endDate(start, 30))
    }

    @Test
    fun sameRuleAsTheValidator() {
        val draft = MedicationDraft(
            source = DraftSource.MANUAL, name = "Amoxicilina", dose = "1 comprimido", days = java.time.DayOfWeek.entries.toSet(),
            times = listOf(java.time.LocalTime.of(8, 0)), treatment = TreatmentType.TEMPORARY, startDate = start, durationDays = 7,
        )
        val valid = DraftValidator.normalizeAndValidate(draft, start) as DraftValidator.Result.Valid
        assertEquals(TreatmentDuration.endDate(start, 7), valid.draft.endDate)
    }

    @Test
    fun invalidDaysGiveNoDate() {
        assertNull(TreatmentDuration.endDate(start, 0))
        assertNull(TreatmentDuration.endDate(start, -3))
        assertNull(TreatmentDuration.endDate(start, TreatmentDuration.MAX_DAYS + 1))
    }

    @Test
    fun daysBetweenCountsBothEnds() {
        assertEquals(7, TreatmentDuration.days(start, LocalDate.of(2026, 11, 9)))
        assertEquals(1, TreatmentDuration.days(start, start))
        assertNull(TreatmentDuration.days(start, null))
        assertNull(TreatmentDuration.days(start, LocalDate.of(2026, 11, 2)))
    }

    @Test
    fun parseKeepsEmptyValidAndInvalidApart() {
        assertEquals(TreatmentDuration.DaysInput.Empty, TreatmentDuration.parse(""))
        assertEquals(TreatmentDuration.DaysInput.Valid(7), TreatmentDuration.parse("7"))
        assertEquals(TreatmentDuration.DaysInput.Valid(999), TreatmentDuration.parse("999"))
        assertEquals(TreatmentDuration.DaysInput.Invalid, TreatmentDuration.parse("0"))
        assertEquals(TreatmentDuration.DaysInput.Invalid, TreatmentDuration.parse("1000"))
    }
}

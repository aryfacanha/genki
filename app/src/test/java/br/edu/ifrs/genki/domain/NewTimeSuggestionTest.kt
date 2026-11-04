package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class NewTimeSuggestionTest {
    private fun t(h: Int) = LocalTime.of(h, 0)

    @Test
    fun emptyListStartsAtEight() = assertEquals(t(8), NewTimeSuggestion.firstFree(emptyList()))

    @Test
    fun skipsTimesAlreadyInTheList() {
        assertEquals(t(9), NewTimeSuggestion.firstFree(listOf(t(8))))
        assertEquals(t(10), NewTimeSuggestion.firstFree(listOf(t(8), t(9))))
    }

    @Test
    fun onlyExactHoursCount() = assertEquals(t(8), NewTimeSuggestion.firstFree(listOf(LocalTime.of(8, 30))))

    @Test
    fun wrapsPastMidnight() {
        val all = (8..23).map(::t)
        assertEquals(t(0), NewTimeSuggestion.firstFree(all))
    }

    @Test
    fun fullDayFallsBackToEight() = assertEquals(t(8), NewTimeSuggestion.firstFree((0..23).map(::t)))
}

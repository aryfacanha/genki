package br.edu.ifrs.genki.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class MappersTest {

    @Test
    fun everyDayIs127() {
        assertEquals(127, DaysMask.of(DayOfWeek.entries.toSet()))
        assertEquals(DayOfWeek.entries.toSet(), DaysMask.toDays(127))
    }

    @Test
    fun maskRoundTrips() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY)
        assertEquals(1 or 4 or 64, DaysMask.of(days))
        assertEquals(days, DaysMask.toDays(DaysMask.of(days)))
    }

    @Test
    fun minuteOfDayRoundTrips() {
        assertEquals(0, LocalTime.MIDNIGHT.toMinuteOfDay())
        assertEquals(1439, LocalTime.of(23, 59).toMinuteOfDay())
        assertEquals(LocalTime.of(8, 30), minuteOfDayToTime(510))
    }

    @Test
    fun datesAreStoredAsIsoText() {
        val converters = Converters()
        assertEquals("2026-11-02", converters.dateToText(LocalDate.of(2026, 11, 2)))
        assertEquals(LocalDate.of(2026, 11, 2), converters.textToDate("2026-11-02"))
    }
}

package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleTimeDiffTest {

    private val existing = listOf(10L to 480, 11L to 1200)

    @Test
    fun sameTimesKeepEveryId() {
        val diff = ScheduleTimeDiff.of(existing, listOf(480, 1200))
        assertEquals(emptyList<Long>(), diff.removedIds)
        assertEquals(emptyList<Int>(), diff.addedMinutes)
    }

    @Test
    fun orderDoesNotMatter() {
        val diff = ScheduleTimeDiff.of(existing, listOf(1200, 480))
        assertEquals(ScheduleTimeDiff(emptyList(), emptyList()), diff)
    }

    @Test
    fun changedTimeRemovesOnlyItsIdAndAddsTheNewMinute() {
        val diff = ScheduleTimeDiff.of(existing, listOf(480, 1260))
        assertEquals(listOf(11L), diff.removedIds)
        assertEquals(listOf(1260), diff.addedMinutes)
    }

    @Test
    fun addedTimeKeepsTheOthers() {
        val diff = ScheduleTimeDiff.of(existing, listOf(480, 720, 1200))
        assertEquals(emptyList<Long>(), diff.removedIds)
        assertEquals(listOf(720), diff.addedMinutes)
    }

    @Test
    fun removedTimeIsReported() {
        val diff = ScheduleTimeDiff.of(existing, listOf(1200))
        assertEquals(listOf(10L), diff.removedIds)
        assertEquals(emptyList<Int>(), diff.addedMinutes)
    }

    @Test
    fun emptyEditRemovesEverything() {
        assertEquals(listOf(10L, 11L), ScheduleTimeDiff.of(existing, emptyList()).removedIds)
    }

    @Test
    fun repeatedMinutesAreInsertedOnce() {
        // O validador recusa repetidos, mas a função não deve gerar dois inserts (índice único).
        assertEquals(listOf(720), ScheduleTimeDiff.of(existing, listOf(480, 720, 720, 1200)).addedMinutes)
    }

    @Test
    fun newMedicationWithoutTimesYet() {
        assertEquals(listOf(480), ScheduleTimeDiff.of(emptyList(), listOf(480)).addedMinutes)
    }
}

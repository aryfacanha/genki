package br.edu.ifrs.genki.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollHintTest {
    @Test
    fun showsWhileThereIsContentBelow() {
        assertTrue(showScrollHint(value = 0, maxValue = 300))
        assertTrue(showScrollHint(value = 299, maxValue = 300))
    }

    @Test
    fun hidesAtTheEndOrWhenEverythingFits() {
        assertFalse(showScrollHint(value = 300, maxValue = 300))
        assertFalse(showScrollHint(value = 0, maxValue = 0))
    }

    @Test
    fun hidesBeforeTheFirstMeasurement() {
        assertFalse(showScrollHint(value = 0, maxValue = Int.MAX_VALUE))
    }
}

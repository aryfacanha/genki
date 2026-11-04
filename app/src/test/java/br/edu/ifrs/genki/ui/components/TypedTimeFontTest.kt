package br.edu.ifrs.genki.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TypedTimeFontTest {
    @Test
    fun opensTypedOnlyWithLargeFonts() {
        assertFalse(opensTyped(1.0f))
        assertFalse(opensTyped(1.3f))
        assertTrue(opensTyped(1.5f))
        assertTrue(opensTyped(2.0f))
    }
}

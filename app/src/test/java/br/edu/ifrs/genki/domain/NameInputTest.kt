package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NameInputTest {
    @Test
    fun lineBreakBecomesOneSpace() {
        assertEquals("Losartana potássica", NameInput.singleLine("Losartana\npotássica"))
        assertEquals("Losartana potássica", NameInput.singleLine("Losartana \r\n  potássica"))
    }

    @Test
    fun plainNameIsUntouched() {
        assertEquals("Ácido fólico", NameInput.singleLine("Ácido fólico"))
        assertEquals("", NameInput.singleLine(""))
    }
}

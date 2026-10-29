package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DuplicateNamesTest {

    @Test
    fun sameNameIgnoringAccentCaseAndSpaces() {
        val existing = listOf("Losartana", "Ácido fólico")
        assertEquals(listOf("losartana "), DuplicateNames.find(listOf("losartana "), existing))
        assertEquals(listOf("acido  folico"), DuplicateNames.find(listOf("acido  folico"), existing))
        assertEquals(emptyList<String>(), DuplicateNames.find(listOf("Losartana potássica", "Atenolol"), existing))
    }

    @Test
    fun repeatedOnlyOnceAndListed() {
        val found = DuplicateNames.find(listOf("Losartana", "LOSARTANA", "Atenolol"), listOf("losartana", "atenolol"))
        assertEquals(listOf("Losartana", "Atenolol"), found)
        assertEquals("Losartana e Atenolol", DuplicateNames.list(found))
        assertEquals("A, B e C", DuplicateNames.list(listOf("A", "B", "C")))
    }
}

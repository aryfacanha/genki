package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MedicationOrderTest {
    private fun med(id: Long, name: String) = Medication(
        id, name, null, "1 comprimido", emptySet(), emptyList(), TreatmentType.CONTINUOUS, LocalDate.of(2026, 11, 3), null, true,
    )

    @Test
    fun ignoresAccentAndCase() {
        val ordered = MedicationOrder.byName(listOf(med(1, "Brometo"), med(2, "ácido fólico"), med(3, "Atenolol")))
        assertEquals(listOf("ácido fólico", "Atenolol", "Brometo"), ordered.map { it.name })
    }

    @Test
    fun accentedNameComesBeforeLaterLetters() {
        val ordered = MedicationOrder.byName(listOf(med(1, "Brometo"), med(2, "Ácido fólico")))
        assertEquals(listOf("Ácido fólico", "Brometo"), ordered.map { it.name })
    }

    @Test
    fun tieGoesToTheOlderRegistration() {
        val ordered = MedicationOrder.byName(listOf(med(5, "Losartana"), med(2, "LOSARTANA")))
        assertEquals(listOf(2L, 5L), ordered.map { it.id })
    }
}

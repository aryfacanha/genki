package br.edu.ifrs.genki.ui.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Quantidade + unidade da dose no formulário; o texto salvo é o mesmo de antes. */
class DoseUnitTest {

    @Test
    fun buildsSingularAndPlural() {
        assertEquals("1 comprimido", DoseUnit.TABLET.dose("1"))
        assertEquals("2 comprimidos", DoseUnit.TABLET.dose(" 2 "))
        assertEquals("meio comprimido", DoseUnit.TABLET.dose("meio"))
        assertEquals("1/2 comprimido", DoseUnit.TABLET.dose("1/2"))
        assertEquals("10 gotas", DoseUnit.DROP.dose("10"))
        assertEquals("1 gota", DoseUnit.DROP.dose("1"))
        assertEquals("5 ml", DoseUnit.ML.dose("5"))
        assertEquals("2 cápsulas", DoseUnit.CAPSULE.dose("2"))
    }

    @Test
    fun missingAmountMeansNoDose() {
        assertEquals("", DoseUnit.TABLET.dose(""))
        assertEquals("", DoseUnit.TABLET.dose("  "))
    }

    @Test
    fun parsesWhatVoiceAndOldRecordsSave() {
        assertNull(DoseUnit.parse(""))
        assertEquals("1" to DoseUnit.TABLET, DoseUnit.parse("1 comprimido"))
        assertEquals("2" to DoseUnit.TABLET, DoseUnit.parse("2 comprimidos"))
        assertEquals("meio" to DoseUnit.TABLET, DoseUnit.parse("meio comprimido"))
        assertEquals("10" to DoseUnit.DROP, DoseUnit.parse("10 gotas"))
        assertEquals("1" to DoseUnit.CAPSULE, DoseUnit.parse("1 cápsula"))
        assertEquals("5" to DoseUnit.ML, DoseUnit.parse("5 ml"))
    }

    @Test
    fun anythingElseStaysWholeAsOther() {
        assertEquals("1 comprimido e meio" to DoseUnit.OTHER, DoseUnit.parse("1 comprimido e meio"))
        assertEquals("1 colher de sopa" to DoseUnit.OTHER, DoseUnit.parse("1 colher de sopa"))
        assertEquals("2 jatos" to DoseUnit.OTHER, DoseUnit.parse("2 jatos"))
        assertEquals("comprimido" to DoseUnit.OTHER, DoseUnit.parse("comprimido"))
    }

    @Test
    fun everyQuickAmountComesBackTheSame() {
        DoseUnit.CHOICES.forEach { unit ->
            unit.amounts.forEach { amount -> assertEquals(amount to unit, DoseUnit.parse(unit.dose(amount))) }
        }
        assertEquals("meio comprimido", DoseUnit.TABLET.dose("meio"))
    }
}

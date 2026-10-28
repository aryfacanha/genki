package br.edu.ifrs.genki.ui.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals("1 colher de sopa" to DoseUnit.OTHER, DoseUnit.parse("1 colher de sopa"))
        assertEquals("2 jatos" to DoseUnit.OTHER, DoseUnit.parse("2 jatos"))
        assertEquals("comprimido" to DoseUnit.OTHER, DoseUnit.parse("comprimido"))
    }

    @Test
    fun changingUnitKeepsOnlyAmountsItHas() {
        assertEquals("2", DoseUnit.CAPSULE.keep("2"))
        assertEquals("", DoseUnit.CAPSULE.keep("meio"))
        assertEquals("", DoseUnit.ML.keep("20"))
        assertEquals("", DoseUnit.CAPSULE.keep("4"))
        assertEquals("", DoseUnit.TABLET.keep(""))
    }

    @Test
    fun halvesOfTabletAreWrittenTheWayPeopleSay() {
        assertEquals("1 comprimido e meio", DoseUnit.TABLET.dose("1 e meio"))
        assertEquals("2 comprimidos e meio", DoseUnit.TABLET.dose("2 e meio"))
        assertEquals("1 comprimido e meio", DoseUnit.TABLET.dose("1,5"))
        assertEquals("meio comprimido", DoseUnit.TABLET.dose("0,5"))
        assertEquals("2,5 ml", DoseUnit.ML.dose("2.5"))
        assertEquals("1 e meio" to DoseUnit.TABLET, DoseUnit.parse("1 comprimido e meio"))
        assertEquals("2 e meio" to DoseUnit.TABLET, DoseUnit.parse("2 comprimidos e meio"))
        assertEquals("2,5" to DoseUnit.ML, DoseUnit.parse("2,5 ml"))
    }

    @Test
    fun plusAndMinusWalkOneStepOfTheUnit() {
        assertEquals("1 e meio", DoseUnit.TABLET.plus("1"))
        assertEquals("2", DoseUnit.TABLET.plus("1 e meio"))
        assertEquals("1", DoseUnit.TABLET.plus("meio"))
        assertEquals("meio", DoseUnit.TABLET.minus("1"))
        assertEquals("meio", DoseUnit.TABLET.minus("meio"))
        assertFalse(DoseUnit.TABLET.canMinus("meio"))
        assertTrue(DoseUnit.TABLET.canMinus("1"))
        assertEquals("4", DoseUnit.CAPSULE.plus("3"))
        assertEquals("1", DoseUnit.CAPSULE.minus("1"))
        assertFalse(DoseUnit.CAPSULE.canMinus("1"))
        assertEquals("21", DoseUnit.DROP.plus("20"))
        assertEquals("2,5", DoseUnit.ML.minus("3"))
        assertEquals("3", DoseUnit.ML.plus("2,5"))
        // O que não é número (digitado) recomeça em um passo.
        assertEquals("meio", DoseUnit.TABLET.plus("abc"))
        assertEquals("100", DoseUnit.DROP.plus("100"))
    }

    @Test
    fun everyStepperAmountComesBackTheSame() {
        DoseUnit.CHOICES.filter { it != DoseUnit.OTHER }.forEach { unit ->
            (1..60).map { it * unit.step }.forEach { value ->
                val amount = unit.amountOf(value)
                assertEquals(amount to unit, DoseUnit.parse(unit.dose(amount)))
            }
        }
    }

    @Test
    fun everyQuickAmountComesBackTheSame() {
        DoseUnit.CHOICES.forEach { unit ->
            unit.amounts.forEach { amount -> assertEquals(amount to unit, DoseUnit.parse(unit.dose(amount))) }
        }
        assertEquals("meio comprimido", DoseUnit.TABLET.dose("meio"))
    }
}

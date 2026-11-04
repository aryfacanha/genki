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
        // "1/2" digitado sai como o − e + escrevem (RV-DVC-04).
        assertEquals("meio comprimido", DoseUnit.TABLET.dose("1/2"))
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
        assertTrue(DoseUnit.TABLET.canMinus("meio"))
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
    fun tabletGoesDownToAQuarter() {
        // DEC-055 (autor, 03/11: "às vezes se toma 1/4"): de meio o − vai a 1/4 e para; o + volta a meio.
        assertEquals("1/4", DoseUnit.TABLET.minus("meio"))
        assertFalse(DoseUnit.TABLET.canMinus("1/4"))
        assertEquals("1/4", DoseUnit.TABLET.minus("1/4"))
        assertEquals("meio", DoseUnit.TABLET.plus("1/4"))
        assertEquals("1/4", DoseUnit.TABLET.minus("3/4"))
        assertEquals("1/4 de comprimido", DoseUnit.TABLET.dose("1/4"))
        assertEquals("3/4 de comprimido", DoseUnit.TABLET.dose("0,75"))
        assertEquals("1/4 de comprimido", DoseUnit.TABLET.dose("0,25"))
        assertEquals("1 comprimido e 1/4", DoseUnit.TABLET.dose("1,25"))
        assertEquals("2 comprimidos e 3/4", DoseUnit.TABLET.dose("2,75"))
        assertEquals("1/4" to DoseUnit.TABLET, DoseUnit.parse("1/4 de comprimido"))
        assertEquals("3/4" to DoseUnit.TABLET, DoseUnit.parse("3/4 de comprimido"))
        assertEquals("1 e 1/4" to DoseUnit.TABLET, DoseUnit.parse("1 comprimido e 1/4"))
        assertEquals(1.25, DoseUnit.TABLET.value("1 e 1/4")!!, 1e-9)
        // As outras unidades não mudam: ml continua parando em meio.
        assertEquals("0,5", DoseUnit.ML.minus("0,5"))
        assertEquals("0,25 ml", DoseUnit.ML.dose("0,25"))
    }

    @Test
    fun stepperNeverLosesWhatWasTyped() {
        // RV-DVC-01: o + não diminui o que foi digitado acima do limite; fica parado.
        assertEquals("120", DoseUnit.DROP.plus("120"))
        assertFalse(DoseUnit.DROP.canPlus("120"))
        assertTrue(DoseUnit.DROP.canPlus("99"))
        assertEquals("119", DoseUnit.DROP.minus("120"))
        // RV-DVC-02: fração que não é meio continua.
        assertEquals("0,75", DoseUnit.ML.plus("0,25"))
        assertEquals("1,75", DoseUnit.ML.plus("1,25"))
        assertEquals("0,25", DoseUnit.ML.minus("0,75"))
        assertFalse(DoseUnit.ML.canMinus("0,25"))
        assertEquals("1,3", DoseUnit.DROP.plus("0,3"))
        assertEquals("0,75 ml", DoseUnit.ML.dose(DoseUnit.ML.plus("0,25")))
        // RV-DVC-04: "1/2 comprimido" de antes ou da voz anda para 1.
        assertEquals("1", DoseUnit.TABLET.plus("1/2"))
        assertEquals("1", DoseUnit.TABLET.plus("½"))
        assertEquals("1", DoseUnit.TABLET.plus(DoseUnit.parse("1/2 comprimido")!!.first))
    }

    @Test
    fun everyStepperAmountComesBackTheSame() {
        DoseUnit.CHOICES.filter { it != DoseUnit.OTHER }.forEach { unit ->
            ((1..60).map { it * unit.step } + listOf(0.25, 0.75, 1.25)).forEach { value ->
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

    @Test
    fun warnsOnlyAboveTheUnusualAmount() {
        assertTrue(DoseUnit.isUnusual("999 comprimidos"))
        assertTrue(DoseUnit.isUnusual("21 gotas"))
        assertFalse(DoseUnit.isUnusual("20 gotas"))
        assertFalse(DoseUnit.isUnusual("2 comprimidos"))
        assertFalse(DoseUnit.isUnusual("1/4 de comprimido"))
        assertFalse(DoseUnit.isUnusual("1 comprimido e meio"))
    }

    @Test
    fun freeTextEmptyOrNonNumericNeverWarns() {
        assertFalse(DoseUnit.isUnusual(""))
        assertFalse(DoseUnit.isUnusual("999 colheres de sopa"))
        assertFalse(DoseUnit.isUnusual("muitos comprimidos"))
    }

    @Test
    fun editingAnotherFieldOfAHighDoseDoesNotWarnAgain() {
        assertFalse(DoseUnit.needsWarning("30 comprimidos", saved = "30 comprimidos", accepted = null))
        assertTrue(DoseUnit.needsWarning("40 comprimidos", saved = "30 comprimidos", accepted = null))
    }

    @Test
    fun warnsOnceForTheSameNewDose() {
        assertTrue(DoseUnit.needsWarning("999 comprimidos", saved = null, accepted = null))
        assertFalse(DoseUnit.needsWarning("999 comprimidos", saved = null, accepted = "999 comprimidos"))
        assertFalse(DoseUnit.needsWarning("2 comprimidos", saved = null, accepted = null))
    }
}

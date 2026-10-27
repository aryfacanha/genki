package br.edu.ifrs.genki.camera

import br.edu.ifrs.genki.domain.DraftSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regras da ESPEC §8 com textos no formato que o ML Kit devolve de embalagens. */
class OcrParserTest {

    private val losartana = listOf(
        OcrLine("LOSARTANA POTÁSSICA", 90),
        OcrLine("50 mg", 70),
        OcrLine("30 comprimidos revestidos", 40),
        OcrLine("Medicamento genérico Lei nº 9.787/99", 20),
        OcrLine("USO ORAL USO ADULTO", 25),
    )

    @Test
    fun nameAndStrength() {
        val draft = OcrParser.parse(losartana)
        assertEquals("Losartana potássica", draft.name)
        assertEquals("50 mg", draft.concentration)
        assertEquals(DraftSource.CAMERA, draft.source)
    }

    @Test
    fun neverFillsDoseScheduleOrTreatment() {
        // RF23: "30 comprimidos" está na embalagem, mas não é dose.
        val draft = OcrParser.parse(losartana)
        assertNull(draft.dose)
        assertNull(draft.days)
        assertTrue(draft.times.isEmpty())
        assertNull(draft.treatment)
        assertNull(draft.startDate)
        assertNull(draft.endDate)
        assertNull(draft.durationDays)
        assertNull(draft.everyHours)
    }

    @Test
    fun nameSkipsLinesWithDigits() {
        val lines = listOf(OcrLine("500 mg", 100), OcrLine("Dipirona Monoidratada", 80), OcrLine("Neo Química", 60))
        assertEquals("Dipirona Monoidratada", OcrParser.name(lines))
    }

    @Test
    fun nameOnlyAmongThreeTallest() {
        val lines = listOf(
            OcrLine("12 cp", 100), OcrLine("20 mg", 90), OcrLine("EMS", 80),
            OcrLine("Omeprazol cápsulas de liberação retardada", 30),
        )
        // "EMS" é a única das 3 maiores sem algarismo; a linha longa é pequena demais.
        assertEquals("Ems", OcrParser.name(lines))
    }

    @Test
    fun strengthFormats() {
        assertEquals("20 mg", OcrParser.strength(listOf(OcrLine("OMEPRAZOL 20MG", 50))))
        assertEquals("2,5 mg", OcrParser.strength(listOf(OcrLine("Anlodipino 2.5 mg", 50))))
        assertEquals("1000 UI", OcrParser.strength(listOf(OcrLine("Vitamina D 1000 UI", 50))))
        // "gotas" não é grama.
        assertNull(OcrParser.strength(listOf(OcrLine("20 gotas", 50))))
    }

    @Test
    fun nothingReadable() {
        val draft = OcrParser.parse(listOf(OcrLine("12", 40)))
        assertNull(draft.name)
        assertNull(draft.concentration)
    }
}

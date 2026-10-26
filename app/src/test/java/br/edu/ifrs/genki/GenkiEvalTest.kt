package br.edu.ifrs.genki

import org.junit.Assert.assertEquals
import org.junit.Test

class GenkiEvalTest {
    @Test
    fun format_semCampos_retornaSoOEvento() {
        assertEquals("app_iniciado", GenkiEval.format("app_iniciado"))
    }

    @Test
    fun format_comCampos_juntaChaveEValor() {
        assertEquals("voz_fim ms=1200 texto=null", GenkiEval.format("voz_fim", "ms" to 1200, "texto" to null))
    }
}

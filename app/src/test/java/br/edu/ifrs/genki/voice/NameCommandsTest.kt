package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.voice.NameCommands.Command
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NameCommandsTest {

    private fun draft(name: String?) = MedicationDraft(source = DraftSource.VOICE, name = name)
    private val riva = draft("Rivaroxabana")
    private val meto = draft("Metoprolol")
    private val sinva = draft("Sinvastatina")
    private val list = listOf(riva, meto, sinva)

    @Test
    fun confirmWords() {
        listOf("pronto", "Sim", "está certo", "Tá certo.", "continuar", "isso mesmo", "tudo certo").forEach {
            assertEquals(it, Command.Confirm, NameCommands.parse(it, list))
        }
    }

    @Test
    fun removeByNameAndBySimilarName() {
        assertEquals(Command.Remove(listOf(meto.localId)), NameCommands.parse("remover metoprolol", list))
        // Reconhecimento troca letras: "simvastatina" acha "Sinvastatina".
        assertEquals(Command.Remove(listOf(sinva.localId)), NameCommands.parse("tirar o remédio simvastatina", list))
        assertEquals(Command.Remove(listOf(riva.localId, meto.localId)), NameCommands.parse("apagar rivaroxabana e metoprolol", list))
    }

    @Test
    fun removePartOfTwoNamesJoined() {
        val joined = draft("Rivaroxabana metoprolol")
        assertEquals(Command.Rename(joined.localId, listOf("Rivaroxabana")), NameCommands.parse("remover metoprolol", listOf(joined)))
    }

    @Test
    fun renameForms() {
        assertEquals(Command.Rename(sinva.localId, listOf("Simvastatina")), NameCommands.parse("trocar sinvastatina por simvastatina", list))
        assertEquals(Command.Rename(meto.localId, listOf("Atenolol")), NameCommands.parse("mudar o metoprolol para atenolol", list))
        assertEquals(Command.Rename(meto.localId, listOf("Atenolol")), NameCommands.parse("não é metoprolol, é atenolol", list))
    }

    @Test
    fun splitJoinedNames() {
        val joined = draft("Rivaroxabana metoprolol")
        assertEquals(
            Command.Rename(joined.localId, listOf("Rivaroxabana", "Metoprolol")),
            NameCommands.parse("separar rivaroxabana metoprolol", listOf(joined)),
        )
        assertTrue(NameCommands.canSplit("Rivaroxabana metoprolol"))
        // Nome de verdade com "de" não é oferecido para separar.
        assertFalse(NameCommands.canSplit("Carbonato de cálcio"))
        assertFalse(NameCommands.canSplit("Losartana"))
    }

    @Test
    fun unknownNameAndOtherSpeech() {
        assertEquals(Command.NotFound("Dipirona"), NameCommands.parse("remover dipirona", list))
        // Outro remédio ou informação: segue para o interpretador.
        assertEquals(Command.None, NameCommands.parse("atenolol 50 mg", list))
    }

    @Test
    fun splitNamesFromSpeechOrTyping() {
        assertEquals(listOf("Rivaroxabana", "Metoprolol", "Sinvastatina"), NameCommands.splitNames("rivaroxabana, metoprolol e sinvastatina"))
        assertEquals(listOf("Losartana"), NameCommands.splitNames("o nome é losartana."))
    }

    @Test
    fun spokenSummary() {
        assertEquals("Entendi 3 remédios: Rivaroxabana, Metoprolol e Sinvastatina. Está certo?", NameCommands.summary(list))
        assertEquals("Agora é 1 remédio: Rivaroxabana. Está certo?", NameCommands.summary(listOf(riva), changed = true))
    }

    @Test
    fun closeButDifferentNamesDoNotMatch() {
        val predni = draft("Prednisolona")
        // Duas letras de diferença: outro remédio, não erro do reconhecimento.
        assertEquals(Command.NotFound("Prednisona"), NameCommands.parse("remover prednisona", listOf(predni)))
        // Dois parecidos na lista: não escolhe nenhum.
        val a = draft("Clonazepam")
        val b = draft("Clonazepan")
        assertEquals(Command.NotFound("Clonazepam"), NameCommands.parse("remover clonazepam", listOf(b, draft("Clonazepao"))))
        assertEquals(Command.Remove(listOf(a.localId)), NameCommands.parse("remover clonazepam", listOf(a, b)))
        // Primeiras palavras iguais continuam valendo.
        val lp = draft("Losartana potássica")
        assertEquals(Command.Remove(listOf(lp.localId)), NameCommands.parse("remover losartana", listOf(lp)))
    }
}

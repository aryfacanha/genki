package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.TreatmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** Ordem das perguntas da voz (RF15) e respostas por toque passando pelo mesmo interpretador. */
class VoiceQuestionsTest {

    private val today = LocalDate.of(2026, 10, 27)

    private fun drafts(text: String) = VoiceParser.parse(text, emptyList(), today).drafts

    @Test
    fun asksMissingFieldsInOrder() {
        var list = drafts("losartana às 8")
        val asked = mutableListOf<Field>()
        val answers = mapOf(Field.DOSE to "1 comprimido", Field.DAYS to "Todos os dias", Field.TREATMENT to "Uso contínuo")
        while (true) {
            val q = VoiceQuestions.next(list, today) ?: break
            asked += q.field
            list = VoiceParser.parse(answers.getValue(q.field), list, today, q.draftId, q.field).drafts
        }
        assertEquals(listOf(Field.DOSE, Field.DAYS, Field.TREATMENT), asked)
        assertEquals(TreatmentType.CONTINUOUS, list.single().treatment)
    }

    @Test
    fun finishesFirstMedicationBeforeSecond() {
        val list = drafts("losartana 1 comprimido todo dia às 8 uso contínuo e metformina")
        val q = VoiceQuestions.next(list, today)!!
        assertEquals("Metformina", list.first { it.localId == q.draftId }.name)
        assertEquals("Quanto você toma de Metformina por vez?", q.text)
    }

    @Test
    fun everyHoursAsksFirstTime() {
        val list = drafts("amoxicilina 1 cápsula de 8 em 8 horas por 7 dias")
        assertEquals("Qual é o primeiro horário de Amoxicilina no dia?", VoiceQuestions.next(list, today)!!.text)
    }

    @Test
    fun completeDraftHasNoQuestion() {
        assertNull(VoiceQuestions.next(drafts("losartana 1 comprimido todo dia às 8 uso contínuo"), today))
    }

    @Test
    fun temporaryWithoutEndAsksEndDate() {
        val list = VoiceParser.parse("losartana 1 comprimido todo dia às 8", emptyList(), today).drafts
            .map { it.copy(treatment = TreatmentType.TEMPORARY) }
        val q = VoiceQuestions.next(list, today)!!
        assertEquals(Field.END_DATE, q.field)
        val answered = VoiceParser.parse("até dia 15", list, today, q.draftId, q.field).drafts
        assertNull(VoiceQuestions.next(answered, today))
    }

    @Test
    fun confirmationSaysWhatWasJustUnderstood() {
        // DEC-040: o que foi anotado é falado antes da próxima pergunta.
        val first = drafts("losartana 50 mg um comprimido todos os dias às 8 uso contínuo")
        assertEquals(
            "Anotei Losartana, 50 mg, 1 comprimido, todos os dias, às 08:00 e uso contínuo.",
            VoiceQuestions.confirmation(emptyList(), first),
        )
        val asked = drafts("metformina")
        val answered = VoiceParser.parse("2 comprimidos", asked, today, asked.single().localId, Field.DOSE).drafts
        assertEquals("Anotei 2 comprimidos.", VoiceQuestions.confirmation(asked, answered))
        val days = VoiceParser.parse("segunda e quarta", answered, today, asked.single().localId, Field.DAYS).drafts
        assertEquals("Anotei segunda e quarta.", VoiceQuestions.confirmation(answered, days))
        val week = VoiceParser.parse("por 7 dias", days, today, asked.single().localId, Field.TREATMENT).drafts
        assertEquals("Anotei por 7 dias.", VoiceQuestions.confirmation(days, week))
        assertNull(VoiceQuestions.confirmation(week, week))
    }

    @Test
    fun adjustedTimesOfEveryHoursAreKept() {
        // E12 (DEC-040): os horários gerados, com um trocado no relógio, voltam como foram escolhidos.
        val list = drafts("metformina um comprimido todos os dias de 8 em 8 horas uso contínuo")
        val chosen = listOf(LocalTime.of(7, 0), LocalTime.of(14, 0), LocalTime.of(23, 0))
        val answered = VoiceParser.parse(VoiceQuestions.timesAnswer(chosen), list, today, list.single().localId, Field.TIMES)
        assertEquals(chosen, answered.drafts.single().times)
        assertNull(VoiceQuestions.next(answered.drafts, today))
    }
}

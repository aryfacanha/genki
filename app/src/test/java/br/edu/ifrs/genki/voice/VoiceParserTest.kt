package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.TreatmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate
import java.time.LocalTime

/**
 * Corpus do parser de voz (ESPEC §7, RF06, RF11–RF16). Frases no formato que o reconhecedor do Android costuma
 * devolver (minúsculas, números em algarismos ou por extenso, "20h", "8:30"). Hoje = terça, 27/10/2026.
 */
class VoiceParserTest {

    private val today = LocalDate.of(2026, 10, 27)
    private val all = DayOfWeek.entries.toSet()

    private fun parse(text: String, drafts: List<MedicationDraft> = emptyList(), target: String? = null, expecting: Field? = null) =
        VoiceParser.parse(text, drafts, today, target, expecting)

    private fun one(text: String) = parse(text).drafts.single()

    private fun t(h: Int, m: Int = 0) = LocalTime.of(h, m)

    private fun voice(name: String, vararg times: LocalTime) =
        MedicationDraft(source = DraftSource.VOICE, name = name, times = times.toList())

    // ── fala única completa (RF12) ──────────────────────────────────────

    @Test
    fun completeSentence() {
        val d = one("Eu tomo losartana 50 mg, 1 comprimido às 8 da manhã todos os dias, uso contínuo")
        assertEquals("Losartana", d.name)
        assertEquals("50 mg", d.concentration)
        assertEquals("1 comprimido", d.dose)
        assertEquals(listOf(t(8)), d.times)
        assertEquals(all, d.days)
        assertEquals(TreatmentType.CONTINUOUS, d.treatment)
        assertEquals(DraftSource.VOICE, d.source)
    }

    @Test
    fun strengthNeverBecomesDose() {
        val d = one("losartana 50 mg")
        assertEquals("50 mg", d.concentration)
        assertNull(d.dose)
    }

    @Test
    fun strengthInWords() {
        assertEquals("850 mg", one("metformina oitocentos e cinquenta miligramas").concentration)
        assertEquals("20 mg", one("omeprazol 20mg").concentration)
        assertEquals("2,5 mg", one("anlodipino 2,5 mg").concentration)
    }

    @Test
    fun doses() {
        assertEquals("meio comprimido", one("losartana meio comprimido").dose)
        assertEquals("10 gotas", one("dipirona 10 gotas").dose)
        assertEquals("1 cápsula", one("omeprazol uma cápsula").dose)
        assertEquals("1 comprimido e meio", one("losartana um comprimido e meio").dose)
        assertEquals("2 colheres de sopa", one("xarope 2 colheres de sopa").dose)
    }

    // ── horários ─────────────────────────────────────────────────────

    @Test
    fun timeFormats() {
        assertEquals(listOf(t(8)), one("losartana às 8").times)
        assertEquals(listOf(t(20)), one("losartana 20h").times)
        assertEquals(listOf(t(20)), one("losartana às 8 da noite").times)
        assertEquals(listOf(t(14)), one("losartana às 2 da tarde").times)
        assertEquals(listOf(t(8, 30)), one("losartana às 8:30").times)
        assertEquals(listOf(t(8, 30)), one("losartana às 8 e meia").times)
        assertEquals(listOf(t(12)), one("losartana ao meio-dia").times)
        assertEquals(listOf(t(22)), one("losartana às vinte e duas horas").times)
    }

    @Test
    fun eightAndTwentyIsEightTwenty() {
        // ESPEC §7: "8 e 20" = 08:20.
        assertEquals(listOf(t(8, 20)), one("losartana às 8 e 20").times)
    }

    @Test
    fun twoTimes() {
        assertEquals(listOf(t(8), t(20)), one("losartana às 8 e às 20").times)
        assertEquals(listOf(t(8), t(20)), one("losartana às 8 e 20 horas").times)
        assertEquals(listOf(t(8), t(14), t(22)), one("losartana às 8, 14 e 22").times)
    }

    // ── frequência (RF06) ───────────────────────────────────────────────

    @Test
    fun everyEightHoursWithFirstTime() {
        val d = one("amoxicilina de 8 em 8 horas a partir das 6")
        assertEquals(listOf(t(6), t(14), t(22)), d.times)
        assertNull(d.everyHours)
    }

    @Test
    fun twiceADayWaitsForFirstTime() {
        val d = one("metformina duas vezes ao dia")
        assertEquals(12, d.everyHours)
        assertTrue(d.times.isEmpty())
        assertEquals(all, d.days)
        // Resposta à pergunta do primeiro horário.
        val answered = parse("às 7", listOf(d), target = d.localId, expecting = Field.TIMES).drafts.single()
        assertEquals(listOf(t(7), t(19)), answered.times)
    }

    @Test
    fun everyTwelveHours() {
        assertEquals(listOf(t(9), t(21)), one("losartana a cada 12 horas às 9").times)
    }

    // ── dias ──────────────────────────────────────────────────────────

    @Test
    fun days() {
        assertEquals(setOf(MONDAY, WEDNESDAY), one("losartana segunda e quarta").days)
        assertEquals(setOf(MONDAY, WEDNESDAY, FRIDAY), one("losartana segunda, quarta e sexta-feira").days)
        assertEquals(DayOfWeek.entries.take(5).toSet(), one("losartana de segunda a sexta").days)
        assertEquals(setOf(SATURDAY, SUNDAY), one("losartana no fim de semana").days)
        assertEquals(all, one("losartana todo dia").days)
    }

    @Test
    fun alternateDaysIsNotGuessed() {
        // "Dia sim, dia não" está fora do escopo: fica sem dias e a tela pergunta.
        assertNull(one("losartana dia sim dia não").days)
    }

    // ── tratamento ───────────────────────────────────────────────────────

    @Test
    fun treatment() {
        one("amoxicilina por 7 dias").let {
            assertEquals(TreatmentType.TEMPORARY, it.treatment)
            assertEquals(7, it.durationDays)
        }
        assertEquals(14, one("amoxicilina por duas semanas").durationDays)
        one("amoxicilina até dia 15").let {
            assertEquals(TreatmentType.TEMPORARY, it.treatment)
            assertEquals(LocalDate.of(2026, 11, 15), it.endDate)
        }
        assertEquals(LocalDate.of(2026, 10, 30), one("amoxicilina até dia 30").endDate)
        assertEquals(LocalDate.of(2027, 1, 10), one("amoxicilina até 10 de janeiro").endDate)
        assertEquals(TreatmentType.UNDEFINED, one("amoxicilina ainda não sei").treatment)
        assertEquals(TreatmentType.CONTINUOUS, one("losartana para sempre").treatment)
    }

    @Test
    fun treatmentAnswers() {
        val d = voice("Amoxicilina", t(8))
        assertEquals(TreatmentType.UNDEFINED, parse("não sei", listOf(d), d.localId, Field.TREATMENT).drafts.single().treatment)
        assertEquals(LocalDate.of(2026, 11, 5), parse("dia 5", listOf(d), d.localId, Field.TREATMENT).drafts.single().endDate)
    }

    // ── vários medicamentos (RF14) ───────────────────────────────────────

    @Test
    fun twoMedicationsInOneSentence() {
        val drafts = parse("losartana 50 mg às 8 e metformina 850 mg às 8 e às 20").drafts
        assertEquals(listOf("Losartana", "Metformina"), drafts.map { it.name })
        assertEquals(listOf(t(8)), drafts[0].times)
        assertEquals(listOf(t(8), t(20)), drafts[1].times)
        assertEquals("850 mg", drafts[1].concentration)
    }

    @Test
    fun namesJoinedShareInformation() {
        val drafts = parse("losartana e sinvastatina um comprimido às 21").drafts
        assertEquals(2, drafts.size)
        drafts.forEach {
            assertEquals("1 comprimido", it.dose)
            assertEquals(listOf(t(21)), it.times)
        }
    }

    @Test
    fun compoundName() {
        assertEquals("Carbonato de cálcio", one("carbonato de cálcio 500 mg").name)
        assertEquals("Vitamina D", one("vitamina D 7 gotas").name)
    }

    @Test
    fun commonWordsAreNotNames() {
        val drafts = parse("eu tomo o remédio da pressão em jejum às 7").drafts
        assertEquals(listOf<String?>(null), drafts.map { it.name })
        assertEquals(listOf(t(7)), drafts.single().times)
    }

    // ── fala progressiva e contexto (RF12, RF13) ───────────────────────────

    @Test
    fun answerGoesToAskedMedication() {
        val l = voice("Losartana", t(8))
        val m = voice("Metformina", t(8))
        val result = parse("dois comprimidos", listOf(l, m), target = l.localId, expecting = Field.DOSE).drafts
        assertEquals("2 comprimidos", result[0].dose)
        assertNull(result[1].dose)
    }

    @Test
    fun bareNumberIsTimeOnlyWhenAskedForTime() {
        val l = voice("Losartana")
        assertEquals(listOf(t(8)), parse("8", listOf(l), l.localId, Field.TIMES).drafts.single().times)
        assertTrue(parse("8", listOf(l), l.localId, Field.DOSE).drafts.single().times.isEmpty())
    }

    @Test
    fun answerCanAddAnotherMedication() {
        val l = voice("Losartana", t(8))
        val result = parse("também tomo metformina às 20", listOf(l), target = l.localId).drafts
        assertEquals(listOf("Losartana", "Metformina"), result.map { it.name })
        assertEquals(listOf(t(8)), result[0].times)
        assertEquals(listOf(t(20)), result[1].times)
    }

    @Test
    fun mentioningExistingNameUpdatesIt() {
        val l = voice("Losartana", t(8))
        val m = voice("Metformina", t(8))
        val result = parse("a losartana é 1 comprimido", listOf(l, m)).drafts
        assertEquals(2, result.size)
        assertEquals("1 comprimido", result[0].dose)
    }

    @Test
    fun timesAccumulate() {
        val l = voice("Losartana", t(8))
        assertEquals(listOf(t(8), t(20)), parse("e também às 20", listOf(l), l.localId).drafts.single().times)
    }

    // ── correção (RF16) ──────────────────────────────────────────────────

    @Test
    fun correctionReplacesNearestTime() {
        val l = voice("Losartana", t(8), t(20))
        assertEquals(listOf(t(9), t(20)), parse("na verdade é às 9", listOf(l), l.localId).drafts.single().times)
    }

    @Test
    fun onlyReplacesAllTimes() {
        val l = voice("Losartana", t(8), t(20))
        assertEquals(listOf(t(9)), parse("só às 9", listOf(l), l.localId).drafts.single().times)
    }

    @Test
    fun bothAlsoAddsToAll() {
        val l = voice("Losartana", t(8))
        val m = voice("Metformina", t(8))
        val result = parse("os dois também às 18", listOf(l, m)).drafts
        result.forEach { assertEquals(listOf(t(8), t(18)), it.times) }
    }

    @Test
    fun doseCorrectionOverwrites() {
        val l = voice("Losartana").copy(dose = "1 comprimido")
        assertEquals("2 comprimidos", parse("na verdade são 2 comprimidos", listOf(l), l.localId).drafts.single().dose)
    }

    // ── casos da revisão Codex de 27/10 (REVISAO_M3_M4_CODEX.md) ─────────

    @Test
    fun compoundStrengthIsNotDose() {
        // RV-M3-01: "por 5 ml" faz parte da concentração; a dose continua vazia e será perguntada.
        one("amoxicilina 250 mg por 5 ml").let {
            assertEquals("250 mg/5 ml", it.concentration)
            assertNull(it.dose)
        }
        assertEquals("250 mg/5 ml", one("amoxicilina 250mg/5ml").concentration)
        assertEquals("500 mg/ml", one("dipirona 500 mg por ml").concentration)
        // Com a dose dita depois, cada uma no seu campo.
        one("amoxicilina 250 mg por 5 ml, tomo 10 ml de 8 em 8 horas").let {
            assertEquals("250 mg/5 ml", it.concentration)
            assertEquals("10 ml", it.dose)
        }
    }

    @Test
    fun daysSpokenLaterAreAdded() {
        // RV-M3-02: "e sexta" completa; correção explícita substitui.
        val first = one("losartana segunda e quarta")
        assertEquals(setOf(MONDAY, WEDNESDAY, FRIDAY), parse("e sexta", listOf(first), first.localId).drafts.single().days)
        assertEquals(setOf(FRIDAY), parse("na verdade é sexta", listOf(first), first.localId).drafts.single().days)
        assertEquals(setOf(FRIDAY), parse("só sexta", listOf(first), first.localId).drafts.single().days)
        assertEquals(all, parse("todos os dias", listOf(first), first.localId).drafts.single().days)
    }

    @Test
    fun negatedTimeIsReplaced() {
        // RV-M3-04: o "não" desfaz o último horário dito.
        assertEquals(listOf(t(8), t(21)), one("losartana às 8 e às 20, não, às 21").times)
        assertEquals(listOf(t(9)), one("losartana às 8 não às 9").times)
        // "Não sei" continua sendo resposta de duração.
        val d = voice("Amoxicilina", t(8))
        assertEquals(TreatmentType.UNDEFINED, parse("não sei", listOf(d), d.localId, Field.TREATMENT).drafts.single().treatment)
    }

    @Test
    fun letterAndNumberStayInName() {
        // RV-M3-06.
        assertEquals("Vitamina B12", one("vitamina B12 um comprimido às 8 todos os dias uso contínuo").name)
        assertEquals("Vitamina B12", one("vitamina b 12 um comprimido").name)
        assertEquals("Vitamina D3", one("vitamina d3 2000 ui").name)
        // Letra seguida de concentração não junta.
        one("vitamina d 7 gotas").let {
            assertEquals("Vitamina d", it.name)
            assertEquals("7 gotas", it.dose)
        }
    }

    // ── casos da revisão Codex da correção (REVISAO_CORRECAO_M3_M4_CODEX.md) ──

    @Test
    fun daysInSeparateStretchesOfOneUtterance() {
        // RV-M3-02 (restante).
        assertEquals(setOf(MONDAY, WEDNESDAY, FRIDAY), one("losartana segunda e quarta e também sexta").days)
        val first = one("losartana segunda e quarta")
        assertEquals(
            setOf(MONDAY, WEDNESDAY, FRIDAY, SATURDAY),
            parse("e sexta e também sábado", listOf(first), first.localId).drafts.single().days,
        )
        // Correção explícita substitui o anterior, mas soma os trechos da própria fala.
        assertEquals(setOf(FRIDAY, SATURDAY), parse("na verdade é sexta e também sábado", listOf(first), first.localId).drafts.single().days)
    }

    @Test
    fun negationRemovesTimeUnderstoodBefore() {
        // RV-M3-04 (restante): 20:00 já estava no rascunho.
        val l = voice("Losartana", t(8), t(20))
        assertEquals(listOf(t(8), t(21)), parse("às 20 não às 21", listOf(l), l.localId).drafts.single().times)
        // Sem negação, continua somando.
        assertEquals(listOf(t(8), t(20), t(21)), parse("e às 21", listOf(l), l.localId).drafts.single().times)
    }

    @Test
    fun numberThatIsTimeDoesNotJoinName() {
        // RV-M3C-01.
        one("vitamina d 8 horas").let {
            assertEquals("Vitamina d", it.name)
            assertEquals(listOf(t(8)), it.times)
        }
        val d = voice("Vitamina d")
        val answered = parse("vitamina d 8", listOf(d), d.localId, Field.TIMES).drafts
        assertEquals(1, answered.size)
        assertEquals(listOf(t(8)), answered.single().times)
        // B12 continua.
        assertEquals("Vitamina B12", one("vitamina b 12 um comprimido").name)
    }

    @Test
    fun fractionDose() {
        // RV-M3C-02.
        one("losartana 1/2 comprimido às 8").let {
            assertEquals("1/2 comprimido", it.dose)
            assertEquals(listOf(t(8)), it.times)
        }
    }

    @Test
    fun slashDate() {
        // RV-M3C-03: hoje 27/10/2026.
        assertEquals(LocalDate.of(2026, 12, 15), one("amoxicilina até 15/12").endDate)
        assertEquals(LocalDate.of(2026, 11, 15), one("amoxicilina até 15/11").endDate)
        assertEquals(LocalDate.of(2026, 12, 15), one("amoxicilina até 15/12/2026").endDate)
        // Mês inválido não vira data: a duração fica para perguntar.
        assertNull(one("amoxicilina até 15/13").endDate)
    }

    // ── casos da segunda revisão da correção (REVISAO_CORRECAO2_M3_M4_CODEX.md) ──

    @Test
    fun correctionInsideUtteranceDropsEarlierDays() {
        // RV-M3-02 (regressão).
        assertEquals(setOf(FRIDAY), one("losartana segunda e quarta na verdade só sexta").days)
        assertEquals(setOf(FRIDAY), one("losartana segunda e quarta, não, sexta").days)
        // Trechos aditivos continuam somando.
        assertEquals(setOf(MONDAY, WEDNESDAY, FRIDAY, SATURDAY), one("losartana segunda, quarta e também sexta e sábado").days)
        val first = one("losartana segunda e quarta")
        assertEquals(setOf(SATURDAY, SUNDAY), parse("na verdade só sábado e também domingo", listOf(first), first.localId).drafts.single().days)
    }

    @Test
    fun invalidDateIsNotReadAsAnotherDay() {
        // RV-M3C-03: nem sem contexto nem como resposta da duração ou da data.
        val d = voice("Amoxicilina", t(8)).copy(dose = "1 cápsula", days = all)
        for (field in listOf(null, Field.TREATMENT, Field.END_DATE)) {
            for (text in listOf("até 31/2", "até 15/13", "31/2", "até 31 de fevereiro")) {
                val result = parse(text, listOf(d), d.localId, field).drafts.single()
                assertNull("$text ($field)", result.endDate)
                assertNull("$text ($field)", result.treatment)
            }
        }
    }

    @Test
    fun spokenYearIsRespected() {
        // RV-M3D-01: hoje 27/10/2026.
        assertEquals(LocalDate.of(2027, 12, 15), one("amoxicilina até 15/12/2027").endDate)
        assertEquals(LocalDate.of(2026, 12, 15), one("amoxicilina até 15/12/26").endDate)
        assertEquals(LocalDate.of(2027, 1, 10), one("amoxicilina até 10 de janeiro de 2027").endDate)
        // Data já passada não vira término nem muda de ano sozinha.
        one("amoxicilina até 15/12/2025").let {
            assertNull(it.endDate)
            assertNull(it.treatment)
        }
        // Sem ano, continua a próxima ocorrência.
        assertEquals(LocalDate.of(2027, 1, 1), one("amoxicilina até 1/1").endDate)
    }

    // ── retorno do autor no Samsung A31 (27/10) ───────────────────────────

    @Test
    fun approximateTimeWords() {
        val l = voice("Losartana")
        listOf("eu tomo umas 9 da noite", "lá pelas 9 da noite", "por volta das 21", "mais ou menos às 9 da noite").forEach {
            val result = parse(it, listOf(l), l.localId, Field.TIMES).drafts
            assertEquals(it, 1, result.size)
            assertEquals(it, listOf(t(21)), result.single().times)
        }
    }

    @Test
    fun recognizerHearingComoForTomo() {
        // Reconhecimento real no A31: "tomo metoprolol" saiu "como metoprolol".
        assertEquals("Metoprolol", one("como metoprolol").name)
    }

    @Test
    fun correctingNameDuringQuestions() {
        // "Algo errado? Corrigir por voz" durante as perguntas: nome dito troca o do medicamento da pergunta.
        val wrong = one("como metoprolol").copy(name = "Como metoprolol")
        parse("metoprolol", listOf(wrong), wrong.localId, Field.NAME).drafts.single().let {
            assertEquals("Metoprolol", it.name)
        }
        parse("na verdade é metoprolol um comprimido", listOf(wrong), wrong.localId, Field.NAME).drafts.single().let {
            assertEquals("Metoprolol", it.name)
            assertEquals("1 comprimido", it.dose)
        }
        // "O nome é …" em fala livre também troca.
        assertEquals("Metoprolol", parse("o nome é metoprolol", listOf(wrong), wrong.localId).drafts.single().name)
    }

    @Test
    fun correctionAfterDayListWithAnotherMedication() {
        // RV-M3-02 (resto): "na verdade" logo após a lista de dias.
        val drafts = parse("losartana segunda e quarta na verdade sexta e metformina sábado").drafts
        assertEquals(setOf(FRIDAY), drafts.first { it.name == "Losartana" }.days)
        assertEquals(setOf(SATURDAY), drafts.first { it.name == "Metformina" }.days)
        // Sem correção, a lista continua somando.
        assertEquals(setOf(MONDAY, WEDNESDAY, FRIDAY), one("losartana segunda e quarta e na sexta").days)
    }

    @Test
    fun nameIsDuringAnyQuestion() {
        // RV-M3E-01: "o nome é X" na pergunta dos dias troca o nome, sem criar medicamento.
        val l = voice("Losartana").copy(dose = "1 comprimido")
        val result = parse("o nome é atenolol", listOf(l), l.localId, Field.DAYS)
        assertTrue(result.understood)
        assertEquals(listOf("Atenolol"), result.drafts.map { it.name })
        // Nome solto continua ignorado na resposta.
        assertFalse(parse("atenolol", listOf(l), l.localId, Field.DAYS).understood)
    }

    @Test
    fun quickAnswersAreUnderstood() {
        // Cada botão de resposta passa pelo interpretador e preenche o campo da pergunta.
        val base = voice("Losartana").copy(dose = "1 comprimido", days = all, times = listOf(t(8)))
        for (field in listOf(Field.DOSE, Field.DAYS, Field.TIMES, Field.TREATMENT)) {
            val question = VoiceQuestions.ask(base, field)
            question.quickAnswers.forEach { answer ->
                assertTrue("${field}: ${answer.label}", parse(answer.text, listOf(base), base.localId, field).understood)
            }
        }
    }

    @Test
    fun answerToQuestionNeverCreatesMedication() {
        // Respostas direcionadas: durante uma pergunta, tudo vale para o medicamento perguntado.
        val l = voice("Losartana")
        val result = parse("também tomo metformina às 20", listOf(l), l.localId, Field.TIMES).drafts
        assertEquals(listOf("Losartana"), result.map { it.name })
        assertEquals(listOf(t(20)), result.single().times)
        // Palavra desconhecida sozinha não é entendida (a tela diz "Não entendi").
        assertFalse(parse("sei lá umas coisas", listOf(l), l.localId, Field.TIMES).understood)
        // Sem pergunta (fala livre ou "Corrigir por voz"), nome novo continua criando medicamento.
        assertEquals(2, parse("também tomo metformina às 20", listOf(l), l.localId).drafts.size)
        // A pergunta do nome continua aceitando nome.
        val unnamed = MedicationDraft(source = DraftSource.VOICE)
        assertEquals("Losartana", parse("losartana", listOf(unnamed), unnamed.localId, Field.NAME).drafts.single().name)
    }

    // ── não entendido (RF15) ───────────────────────────────────────────

    @Test
    fun nothingUnderstood() {
        val l = voice("Losartana", t(8))
        val result = parse("hum não sei", listOf(l), l.localId, Field.DOSE)
        assertFalse(result.understood)
        assertEquals(listOf(l), result.drafts)
    }
}

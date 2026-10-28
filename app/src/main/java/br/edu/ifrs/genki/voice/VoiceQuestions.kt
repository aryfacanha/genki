package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.MedicationDraft
import java.time.LocalDate
import java.time.LocalTime

/** Botão de resposta: [label] na tela, [text] vai para o [VoiceParser] como se fosse falado. */
data class QuickAnswer(val label: String, val text: String = label)

/** Pergunta da voz sobre um campo que falta (RF15), escrita na tela e falada (RF19). */
data class VoiceQuestion(
    val draftId: String,
    val field: Field,
    val text: String,
    val example: String,
    /** Respostas por toque (voz principal, toque para agilizar; pedido do autor, 27/10). */
    val quickAnswers: List<QuickAnswer> = emptyList(),
    /** Pergunta do primeiro horário ("de 8 em 8 horas"): o relógio escolhe só um e mostra os outros (RV-DVC-03). */
    val everyHours: Int? = null,
)

object VoiceQuestions {

    /** Onde o relógio abre: 08:00, depois 12 h após o último, pulando horário já escolhido (A31-06). */
    fun nextClockTime(chosen: List<LocalTime>): LocalTime {
        var time = chosen.maxOrNull()?.plusHours(12) ?: LocalTime.of(8, 0)
        repeat(24) { if (time in chosen) time = time.plusHours(1) }
        return time
    }

    /**
     * Horários escolhidos no relógio da voz (pedido do autor, 28/10) como frase para o mesmo interpretador:
     * "às 08:00", "às 08:00 e às 20:00", "às 08:00, às 14:30 e às 20:00".
     */
    fun timesAnswer(times: List<LocalTime>): String {
        val each = times.sorted().map { "às %02d:%02d".format(it.hour, it.minute) }
        return if (each.size <= 1) each.joinToString() else each.dropLast(1).joinToString(", ") + " e " + each.last()
    }

    /** Primeiro campo que falta, rascunho por rascunho, na ordem do validador; `null` = tudo pronto para a revisão. */
    fun next(drafts: List<MedicationDraft>, today: LocalDate): VoiceQuestion? {
        drafts.forEach { draft ->
            val result = DraftValidator.normalizeAndValidate(draft, today)
            if (result is DraftValidator.Result.Invalid) return ask(draft, result.problems.first().first)
        }
        return null
    }

    fun ask(draft: MedicationDraft, field: Field): VoiceQuestion {
        val name = draft.name ?: "o medicamento"
        return when (field) {
            Field.NAME -> VoiceQuestion(draft.localId, field, "Qual é o nome do medicamento?", "Exemplo: losartana")
            Field.DOSE -> VoiceQuestion(
                draft.localId, field, "Quanto você toma de $name por vez?", "Exemplo: 1 comprimido",
                quickAnswers = listOf("1 comprimido", "2 comprimidos", "meio comprimido", "1 cápsula", "10 gotas").map(::QuickAnswer),
            )
            Field.DAYS -> VoiceQuestion(
                draft.localId, field, "Em quais dias você toma $name?", "Exemplo: segunda e quarta",
                quickAnswers = listOf("Todos os dias", "Segunda a sexta", "Fim de semana").map(::QuickAnswer),
            )
            Field.TIMES -> if (draft.everyHours != null) {
                VoiceQuestion(
                    draft.localId, field, "Qual é o primeiro horário de $name no dia?", "Exemplo: às 8",
                    quickAnswers = listOf(6, 7, 8).map { QuickAnswer("%02d:00".format(it), "às $it") },
                    everyHours = draft.everyHours,
                )
            } else {
                VoiceQuestion(
                    draft.localId, field, "Em quais horários você toma $name?", "Exemplo: às 8 e às 20",
                    quickAnswers = listOf(8, 12, 20).map { QuickAnswer("%02d:00".format(it), "às $it") } +
                        QuickAnswer("08:00 e 20:00", "às 8 e às 20"),
                )
            }
            Field.TREATMENT -> VoiceQuestion(
                draft.localId, field, "Por quanto tempo você vai usar $name?", "Exemplo: por 7 dias",
                quickAnswers = listOf("Uso contínuo", "Ainda não sei", "Por 7 dias", "Por 10 dias").map(::QuickAnswer),
            )
            Field.END_DATE -> VoiceQuestion(draft.localId, field, "Até que dia você vai usar $name?", "Exemplo: até dia 15")
        }
    }
}

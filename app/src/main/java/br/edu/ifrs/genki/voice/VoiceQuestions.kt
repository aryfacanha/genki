package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.MedicationDraft
import java.time.LocalDate

/** Pergunta da voz sobre um campo que falta (RF15), escrita na tela e falada (RF19). */
data class VoiceQuestion(
    val draftId: String,
    val field: Field,
    val text: String,
    val example: String,
    /** Respostas por toque; cada uma vai para o [VoiceParser] como se fosse falada. */
    val quickAnswers: List<String> = emptyList(),
)

object VoiceQuestions {

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
            Field.DOSE -> VoiceQuestion(draft.localId, field, "Quanto você toma de $name por vez?", "Exemplo: 1 comprimido")
            Field.DAYS -> VoiceQuestion(
                draft.localId, field, "Em quais dias você toma $name?", "Exemplo: segunda e quarta",
                quickAnswers = listOf("Todos os dias"),
            )
            Field.TIMES -> if (draft.everyHours != null) {
                VoiceQuestion(draft.localId, field, "Qual é o primeiro horário de $name no dia?", "Exemplo: às 8")
            } else {
                VoiceQuestion(draft.localId, field, "Em quais horários você toma $name?", "Exemplo: às 8 e às 20")
            }
            Field.TREATMENT -> VoiceQuestion(
                draft.localId, field, "Por quanto tempo você vai usar $name?", "Exemplo: por 7 dias",
                quickAnswers = listOf("Uso contínuo", "Ainda não sei"),
            )
            Field.END_DATE -> VoiceQuestion(draft.localId, field, "Até que dia você vai usar $name?", "Exemplo: até dia 15")
        }
    }
}

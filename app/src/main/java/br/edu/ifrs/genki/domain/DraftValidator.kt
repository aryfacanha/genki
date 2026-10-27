package br.edu.ifrs.genki.domain

import java.time.LocalDate

enum class Field { NAME, DOSE, DAYS, TIMES, TREATMENT, END_DATE }

enum class Problem { MISSING, END_BEFORE_START, DUPLICATE_TIME }

/**
 * Único caminho até o banco (DEC-014): manual, edição, voz e câmera passam por aqui,
 * e o repositório só aceita [Result.Valid].
 */
object DraftValidator {

    sealed interface Result {
        data class Valid(val draft: MedicationDraft) : Result
        data class Invalid(val problems: List<Pair<Field, Problem>>) : Result
    }

    /** [today] é a data civil no fuso do aparelho; nunca UTC. */
    fun normalizeAndValidate(draft: MedicationDraft, today: LocalDate): Result {
        val treatment = draft.treatment
        val start = draft.startDate ?: today
        val end = when {
            treatment != TreatmentType.TEMPORARY -> null
            draft.durationDays != null -> start.plusDays(draft.durationDays - 1L)
            else -> draft.endDate
        }
        val normalized = draft.copy(
            name = draft.name.clean(),
            concentration = draft.concentration.clean(),
            dose = draft.dose.clean(),
            days = draft.days?.ifEmpty { null },
            // Ordena, mas não remove repetidos: o usuário é avisado (DEC-027).
            times = draft.times.sorted(),
            startDate = start,
            endDate = end,
            durationDays = null,
            everyHours = null,
        )

        val problems = buildList {
            if (normalized.name == null) add(Field.NAME to Problem.MISSING)
            if (normalized.dose == null) add(Field.DOSE to Problem.MISSING)
            if (normalized.days == null) add(Field.DAYS to Problem.MISSING)
            when {
                normalized.times.isEmpty() -> add(Field.TIMES to Problem.MISSING)
                normalized.times.toSet().size != normalized.times.size -> add(Field.TIMES to Problem.DUPLICATE_TIME)
            }
            if (treatment == null) add(Field.TREATMENT to Problem.MISSING)
            if (treatment == TreatmentType.TEMPORARY) {
                when {
                    end == null -> add(Field.END_DATE to Problem.MISSING)
                    end < start -> add(Field.END_DATE to Problem.END_BEFORE_START)
                }
            }
        }
        return if (problems.isEmpty()) Result.Valid(normalized) else Result.Invalid(problems)
    }

    private fun String?.clean(): String? = this?.trim()?.ifEmpty { null }
}

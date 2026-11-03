package br.edu.ifrs.genki.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * "Por N dias" no cadastro manual (P20, refinamento 1.0.25): só interface, grava a data de término. Mesma regra do
 * `DraftValidator` para `durationDays` (DEC-003, DEC-014): o dia de início conta, então "por 7 dias" termina em início + 6.
 */
object TreatmentDuration {
    const val MAX_DAYS = 999

    /** Data de término de [days] dias a partir de [start]; `null` se [days] não está entre 1 e [MAX_DAYS]. */
    fun endDate(start: LocalDate, days: Int): LocalDate? =
        if (days in 1..MAX_DAYS) start.plusDays(days - 1L) else null

    /** Quantos dias vão de [start] a [end], contando os dois; `null` se não houver término ou ele vier antes do início. */
    fun days(start: LocalDate, end: LocalDate?): Int? =
        end?.takeIf { !it.isBefore(start) }?.let { (ChronoUnit.DAYS.between(start, it) + 1).toInt() }
}

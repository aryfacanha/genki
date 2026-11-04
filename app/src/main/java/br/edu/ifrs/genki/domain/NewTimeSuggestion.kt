package br.edu.ifrs.genki.domain

import java.time.LocalTime

/**
 * Horário que o relógio mostra ao tocar em "Adicionar horário" (P18, refinamento 1.0.25): 08:00 e, se já está na lista,
 * o próximo na hora cheia que ainda não está. Antes abria sempre em 08:00, com o aviso "já foi adicionado" e o OK desativado.
 */
object NewTimeSuggestion {
    private val DEFAULT = LocalTime.of(8, 0)

    fun firstFree(existing: List<LocalTime>): LocalTime {
        var candidate = DEFAULT
        repeat(24) {
            if (candidate !in existing) return candidate
            candidate = candidate.plusHours(1)
        }
        return DEFAULT
    }
}

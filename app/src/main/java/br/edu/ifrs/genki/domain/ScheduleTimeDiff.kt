package br.edu.ifrs.genki.domain

/**
 * O que a edição muda nos horários de um medicamento. Quem não mudou de minuto mantém o id: o adiado e a repetição
 * pendentes são ligados ao id do horário e eram perdidos ao regravar todos (CC-01 da avaliação heurística da 1.0.23).
 */
data class ScheduleTimeDiff(
    /** Ids dos horários que deixaram de existir; só esses têm alarmes e avisos cancelados. */
    val removedIds: List<Long>,
    /** Minutos do dia (0..1439) que ainda não existem e devem ser inseridos. */
    val addedMinutes: List<Int>,
) {
    companion object {
        /** [existing] = pares (id, minuto do dia) gravados; [minutes] = os horários da edição. */
        fun of(existing: List<Pair<Long, Int>>, minutes: List<Int>): ScheduleTimeDiff {
            val wanted = minutes.toSet()
            val kept = existing.map { it.second }.toSet()
            return ScheduleTimeDiff(
                removedIds = existing.filter { it.second !in wanted }.map { it.first },
                addedMinutes = minutes.distinct().filter { it !in kept },
            )
        }
    }
}

package br.edu.ifrs.genki.domain

import java.time.LocalTime

/** "De 8 em 8 horas", "duas vezes ao dia" (RF06): o primeiro horário gera os outros do dia. */
object Frequency {

    /** Horários do dia a partir de [first], a cada [everyHours] horas (24 divisível por [everyHours]). */
    fun fill(first: LocalTime, everyHours: Int): List<LocalTime> =
        (0 until 24 / everyHours).map { first.plusHours(it.toLong() * everyHours) }.distinct().sorted()
}

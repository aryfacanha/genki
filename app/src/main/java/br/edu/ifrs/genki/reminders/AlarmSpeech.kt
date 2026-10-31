package br.edu.ifrs.genki.reminders

import java.time.LocalTime

/**
 * Frase falada no alarme (DEC-052, pedido do autor): com um remédio, o horário e o nome ("Lembrete das 8 horas:
 * Losartana."); com vários, quantos são ("Lembrete das 8 horas: 3 remédios."). O horário é o do lembrete, não o do
 * relógio, para valer também no adiado e nas repetições. Não diz "tomar" nem confirma nada (lembrete não é ingestão).
 */
object AlarmSpeech {

    fun text(ringing: List<Pair<String, LocalTime>>): String? {
        if (ringing.isEmpty()) return null
        val times = ringing.map { it.second }.distinct()
        val start = if (times.size == 1) "Lembrete ${spokenTime(times.single())}" else "Lembretes"
        return if (ringing.size == 1) "$start: ${ringing.single().first}." else "$start: ${ringing.size} remédios."
    }

    /** "das 8 horas", "da 1 hora e 30 minutos", "do meio-dia", "da meia-noite e 15 minutos". */
    fun spokenTime(time: LocalTime): String {
        val hour = when {
            time.hour == 0 -> "meia-noite"
            time.hour == 12 && time.minute == 0 -> "meio-dia"
            time.hour == 1 -> "1 hora"
            else -> "${time.hour} horas"
        }
        val preposition = when (hour) {
            "meia-noite", "1 hora" -> "da"
            "meio-dia" -> "do"
            else -> "das"
        }
        val minutes = when (time.minute) {
            0 -> ""
            1 -> " e 1 minuto"
            else -> " e ${time.minute} minutos"
        }
        return "$preposition $hour$minutes"
    }
}

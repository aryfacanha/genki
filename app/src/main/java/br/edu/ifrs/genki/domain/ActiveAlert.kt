package br.edu.ifrs.genki.domain

import java.time.Instant
import java.time.LocalTime

/**
 * Aviso emitido e ainda sem resposta (tocando) ou adiado, para a Hoje mostrar com os botões (DEC-029).
 * Some com "Entendi": não é registro de ingestão nem histórico (DEC-004).
 */
data class ActiveAlert(
    val timeId: Long,
    val medicationId: Long,
    val time: LocalTime,
    /** `null` = tocando agora; senão, quando volta a avisar. */
    val snoozedUntil: Instant? = null,
) {
    fun encode(): String = "$medicationId;${time.hour * 60 + time.minute};${snoozedUntil?.toEpochMilli() ?: ""}"

    fun matches(occurrence: Occurrence) = occurrence.medication.id == medicationId && occurrence.time == time

    companion object {
        /** Lê o que [encode] gravou; valor estragado = `null` (o aviso só deixa de aparecer na Hoje). */
        fun decode(timeId: Long, value: String): ActiveAlert? {
            val parts = value.split(';')
            if (parts.size != 3) return null
            val medicationId = parts[0].toLongOrNull() ?: return null
            val minute = parts[1].toIntOrNull()?.takeIf { it in 0..1439 } ?: return null
            val snoozed = if (parts[2].isEmpty()) null else Instant.ofEpochMilli(parts[2].toLongOrNull() ?: return null)
            return ActiveAlert(timeId, medicationId, LocalTime.of(minute / 60, minute % 60), snoozed)
        }

        /** Próximo horário de hoje a partir de [minute], sem os que já estão avisando ou adiados. */
        fun nextOccurrence(today: List<Occurrence>, alerts: List<ActiveAlert>, minute: LocalTime): Occurrence? =
            today.firstOrNull { occurrence -> occurrence.time >= minute && alerts.none { it.matches(occurrence) } }
    }
}

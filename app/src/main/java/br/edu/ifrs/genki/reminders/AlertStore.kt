package br.edu.ifrs.genki.reminders

import android.content.Context
import br.edu.ifrs.genki.domain.ActiveAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalTime

/**
 * Avisos pendentes (tocando ou adiados) em SharedPreferences, uma chave por horário (DEC-029).
 * Só o que está pendente: "Entendi", desativar ou excluir apagam a entrada; reiniciar o aparelho limpa tudo.
 */
class AlertStore(context: Context) {
    private val prefs = context.getSharedPreferences("genki_alerts", Context.MODE_PRIVATE)

    private val _alerts = MutableStateFlow(read())
    val alerts: StateFlow<List<ActiveAlert>> = _alerts.asStateFlow()

    private fun read(): List<ActiveAlert> = prefs.all.mapNotNull { (key, value) ->
        val timeId = key.toLongOrNull() ?: return@mapNotNull null
        (value as? String)?.let { ActiveAlert.decode(timeId, it) }
    }.sortedBy { it.time }

    @Synchronized
    fun ringing(timeId: Long, medicationId: Long, time: LocalTime) = put(ActiveAlert(timeId, medicationId, time))

    @Synchronized
    fun snoozed(timeId: Long, until: Instant) {
        val current = _alerts.value.firstOrNull { it.timeId == timeId } ?: return
        put(current.copy(snoozedUntil = until))
    }

    @Synchronized
    fun remove(timeIds: List<Long>) {
        prefs.edit().apply { timeIds.forEach { remove(it.toString()) } }.commit()
        _alerts.value = read()
    }

    @Synchronized
    fun clear() {
        prefs.edit().clear().commit()
        _alerts.value = emptyList()
    }

    private fun put(alert: ActiveAlert) {
        prefs.edit().putString(alert.timeId.toString(), alert.encode()).commit()
        _alerts.value = read()
    }
}

package br.edu.ifrs.genki.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReminderSettings(val repeatEnabled: Boolean = true, val intervalMinutes: Int = 20)

/** Os dois ajustes do app em SharedPreferences (S3). O intervalo vale para repetir e para adiar (DEC-028). */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("genki_settings", Context.MODE_PRIVATE)

    private val _reminders = MutableStateFlow(read())
    val reminders: StateFlow<ReminderSettings> = _reminders.asStateFlow()

    private fun read() = ReminderSettings(
        repeatEnabled = prefs.getBoolean(KEY_REPEAT, true),
        intervalMinutes = prefs.getInt(KEY_INTERVAL, 20),
    )

    fun setRepeatEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REPEAT, enabled).apply()
        _reminders.value = read()
    }

    fun setIntervalMinutes(minutes: Int) {
        require(minutes in INTERVALS)
        prefs.edit().putInt(KEY_INTERVAL, minutes).apply()
        _reminders.value = read()
    }

    companion object {
        val INTERVALS = listOf(10, 20, 30)
        private const val KEY_REPEAT = "repeat_enabled"
        private const val KEY_INTERVAL = "interval_minutes"
    }
}

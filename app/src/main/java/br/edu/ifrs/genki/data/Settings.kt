package br.edu.ifrs.genki.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** [alarmSound]: tocar como alarme, mesmo no silencioso (DEC-032, escolhido na introdução ou em Ajustes). */
data class ReminderSettings(val repeatEnabled: Boolean = true, val intervalMinutes: Int = 20, val alarmSound: Boolean = false)

/**
 * Ajustes do app em SharedPreferences (S3). O intervalo vale para repetir e para adiar (DEC-028). A introdução (DEC-040)
 * acrescenta o som como alarme, a escuta automática da voz e se ela já foi vista.
 */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("genki_settings", Context.MODE_PRIVATE)

    private val _reminders = MutableStateFlow(read())
    val reminders: StateFlow<ReminderSettings> = _reminders.asStateFlow()

    private fun read() = ReminderSettings(
        repeatEnabled = prefs.getBoolean(KEY_REPEAT, true),
        intervalMinutes = prefs.getInt(KEY_INTERVAL, 20),
        alarmSound = prefs.getBoolean(KEY_ALARM, false),
    )

    fun setAlarmSound(alarm: Boolean) {
        prefs.edit().putBoolean(KEY_ALARM, alarm).apply()
        _reminders.value = read()
    }

    /** Na voz, o microfone abre sozinho depois da pergunta (DEC-040). */
    var autoListen: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LISTEN, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LISTEN, value).apply()

    /** Introdução concluída ou pulada; não aparece de novo sozinha. */
    var introDone: Boolean
        get() = prefs.getBoolean(KEY_INTRO_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_INTRO_DONE, value).apply()

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
        private const val KEY_ALARM = "alarm_sound"
        private const val KEY_AUTO_LISTEN = "voice_auto_listen"
        private const val KEY_INTRO_DONE = "intro_done"
    }
}

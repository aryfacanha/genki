package br.edu.ifrs.genki.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class AlarmSpeechTest {

    private fun t(hour: Int, minute: Int = 0) = LocalTime.of(hour, minute)

    @Test
    fun oneMedicationSaysTimeAndName() {
        assertEquals("Lembrete das 8 horas: Losartana.", AlarmSpeech.text(listOf("Losartana" to t(8))))
    }

    @Test
    fun severalAtSameTimeSayHowMany() {
        val ringing = listOf("Losartana" to t(8), "Metformina" to t(8), "Vitamina D" to t(8))
        assertEquals("Lembrete das 8 horas: 3 remédios.", AlarmSpeech.text(ringing))
    }

    @Test
    fun severalAtDifferentTimesSkipTheTime() {
        // Adiado das 8:00 tocando junto com o das 8:20.
        assertEquals("Lembretes: 2 remédios.", AlarmSpeech.text(listOf("Losartana" to t(8), "Metformina" to t(8, 20))))
    }

    @Test
    fun nothingRingingSaysNothing() {
        assertNull(AlarmSpeech.text(emptyList()))
    }

    @Test
    fun spokenTimes() {
        assertEquals("das 8 horas e 30 minutos", AlarmSpeech.spokenTime(t(8, 30)))
        assertEquals("das 21 horas e 1 minuto", AlarmSpeech.spokenTime(t(21, 1)))
        assertEquals("da 1 hora", AlarmSpeech.spokenTime(t(1)))
        assertEquals("da 1 hora e 15 minutos", AlarmSpeech.spokenTime(t(1, 15)))
        assertEquals("do meio-dia", AlarmSpeech.spokenTime(t(12)))
        assertEquals("das 12 horas e 30 minutos", AlarmSpeech.spokenTime(t(12, 30)))
        assertEquals("da meia-noite", AlarmSpeech.spokenTime(t(0)))
        assertEquals("da meia-noite e 15 minutos", AlarmSpeech.spokenTime(t(0, 15)))
    }
}

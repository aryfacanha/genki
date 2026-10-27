package br.edu.ifrs.genki.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class ActiveAlertTest {

    private val t0800 = LocalTime.of(8, 0)
    private val t2000 = LocalTime.of(20, 0)

    private fun med(id: Long) = Medication(
        id = id, name = "Losartana", concentration = "50 mg", dose = "1 comprimido",
        days = DayOfWeek.entries.toSet(), times = listOf(t0800, t2000), treatment = TreatmentType.CONTINUOUS,
        startDate = LocalDate.of(2026, 10, 27), endDate = null, remindersEnabled = true,
    )

    private fun today(id: Long) = med(id).times.map { Occurrence(it, med(id)) }

    @Test
    fun ringingRoundTrip() {
        val alert = ActiveAlert(timeId = 7, medicationId = 3, time = t0800)
        assertEquals(alert, ActiveAlert.decode(7, alert.encode()))
    }

    @Test
    fun snoozedRoundTrip() {
        val alert = ActiveAlert(7, 3, t2000, Instant.ofEpochMilli(1_793_000_000_000))
        assertEquals(alert, ActiveAlert.decode(7, alert.encode()))
    }

    @Test
    fun brokenValueIsIgnored() {
        assertNull(ActiveAlert.decode(7, "x;480;"))
        assertNull(ActiveAlert.decode(7, "3;1440;"))
        assertNull(ActiveAlert.decode(7, "3;480"))
    }

    @Test
    fun nextSkipsOccurrenceThatIsRinging() {
        // 08:00 tocando às 08:00: o "Depois" é o das 20:00, não o próprio aviso.
        assertEquals(t2000, ActiveAlert.nextOccurrence(today(1), listOf(ActiveAlert(1, 1, t0800)), t0800)?.time)
    }

    @Test
    fun acknowledgedInSameMinuteIsNotNext() {
        // Às 08:00:30, depois de "Entendi" (sem aviso pendente), o próximo é o das 20:00, não o das 08:00 de novo.
        assertEquals(t2000, ActiveAlert.nextOccurrence(today(1), emptyList(), t0800)?.time)
    }

    @Test
    fun beforeTheMinuteItIsNext() {
        assertEquals(t0800, ActiveAlert.nextOccurrence(today(1), emptyList(), LocalTime.of(7, 59))?.time)
        // Depois do último horário: nada mais hoje.
        assertNull(ActiveAlert.nextOccurrence(today(1), emptyList(), t2000))
    }
}

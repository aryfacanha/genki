package br.edu.ifrs.genki.data

import androidx.room.TypeConverter
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.MedicationDraft
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** `LocalDate` ↔ texto ISO (ESPEC §3): data civil, sem fuso. */
class Converters {
    @TypeConverter fun dateToText(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun textToDate(text: String?): LocalDate? = text?.let(LocalDate::parse)
}

object DaysMask {
    fun of(days: Set<DayOfWeek>): Int = days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

    fun toDays(mask: Int): Set<DayOfWeek> = DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()
}

fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

fun minuteOfDayToTime(minute: Int): LocalTime = LocalTime.of(minute / 60, minute % 60)

/** Só chamado com rascunho validado: campos obrigatórios já preenchidos. */
fun MedicationDraft.toEntity(id: Long = 0, remindersEnabled: Boolean = true) = MedicationEntity(
    id = id,
    name = requireNotNull(name),
    concentration = concentration,
    dose = requireNotNull(dose),
    daysMask = DaysMask.of(requireNotNull(days)),
    treatment = requireNotNull(treatment),
    startDate = requireNotNull(startDate),
    endDate = endDate,
    remindersEnabled = remindersEnabled,
)

fun MedicationWithTimes.toDomain() = Medication(
    id = medication.id,
    name = medication.name,
    concentration = medication.concentration,
    dose = medication.dose,
    days = DaysMask.toDays(medication.daysMask),
    times = times.map { minuteOfDayToTime(it.minuteOfDay) }.sorted(),
    treatment = medication.treatment,
    startDate = medication.startDate,
    endDate = medication.endDate,
    remindersEnabled = medication.remindersEnabled,
)

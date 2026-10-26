package br.edu.ifrs.genki.ui

import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.Problem
import br.edu.ifrs.genki.domain.TreatmentType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Textos de exibição montados a partir dos dados, com as frases do protótipo
 * (`src/data.ts`, `medicationValidation.tsx`). Kotlin puro para poder testar.
 */
object MedicationText {

    /** Semana começando no domingo, como no protótipo. */
    val WEEK: List<DayOfWeek> = listOf(DayOfWeek.SUNDAY) + DayOfWeek.entries.filter { it != DayOfWeek.SUNDAY }

    private val dayNames = mapOf(
        DayOfWeek.SUNDAY to "domingo", DayOfWeek.MONDAY to "segunda", DayOfWeek.TUESDAY to "terça",
        DayOfWeek.WEDNESDAY to "quarta", DayOfWeek.THURSDAY to "quinta", DayOfWeek.FRIDAY to "sexta",
        DayOfWeek.SATURDAY to "sábado",
    )

    private val dayShort = mapOf(
        DayOfWeek.SUNDAY to "Dom", DayOfWeek.MONDAY to "Seg", DayOfWeek.TUESDAY to "Ter",
        DayOfWeek.WEDNESDAY to "Qua", DayOfWeek.THURSDAY to "Qui", DayOfWeek.FRIDAY to "Sex",
        DayOfWeek.SATURDAY to "Sáb",
    )

    const val UNKNOWN_DURATION_HELP = "Os lembretes continuam até você alterar ou desativar este medicamento."

    fun dayName(day: DayOfWeek): String = dayNames.getValue(day)

    fun dayShort(day: DayOfWeek): String = dayShort.getValue(day)

    fun time(time: LocalTime): String = "%02d:%02d".format(time.hour, time.minute)

    /** "08:00", "08:00 e 20:00", "08:00, 12:00 e 20:00". */
    fun times(times: List<LocalTime>): String = joinWithAnd(times.map(::time))

    /** "Todos os dias" ou "segunda, quarta e sexta". */
    fun days(days: Set<DayOfWeek>): String =
        if (days.size == 7) "Todos os dias" else joinWithAnd(WEEK.filter { it in days }.map(::dayName))

    fun schedule(medication: Medication): String = "${days(medication.days)} • ${times(medication.times)}"

    /** "50 mg · 1 comprimido"; sem concentração, só a dose. */
    fun doseLine(concentration: String?, dose: String): String =
        listOfNotNull(concentration?.takeIf { it.isNotBlank() }, dose).joinToString(" · ")

    /** Rótulos do protótipo (DEC-027). */
    fun treatment(type: TreatmentType): String = when (type) {
        TreatmentType.CONTINUOUS -> "Uso contínuo (sem data para parar)"
        TreatmentType.TEMPORARY -> "Tem data para terminar"
        TreatmentType.UNDEFINED -> "Ainda não sei"
    }

    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun date(date: LocalDate): String = date.format(dateFormat)

    /** "De 01/11/2026 até 07/11/2026". */
    fun period(start: LocalDate, end: LocalDate?): String =
        "De ${date(start)}" + (end?.let { " até ${date(it)}" } ?: "")

    fun error(field: Field, problem: Problem): String = when (field) {
        Field.NAME -> "Informe o nome do medicamento."
        Field.DOSE -> "Informe quanto você toma por vez."
        Field.DAYS -> "Selecione pelo menos um dia."
        Field.TIMES ->
            if (problem == Problem.DUPLICATE_TIME) "Este horário já foi adicionado." else "Adicione ao menos um horário."
        Field.TREATMENT -> "Selecione a duração do tratamento."
        Field.END_DATE ->
            if (problem == Problem.END_BEFORE_START) "A data de término não pode ser anterior à data de início."
            else "Informe a data de término."
    }

    private fun joinWithAnd(items: List<String>): String =
        if (items.size < 2) items.joinToString("") else items.dropLast(1).joinToString(", ") + " e " + items.last()
}

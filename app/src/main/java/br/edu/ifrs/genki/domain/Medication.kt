package br.edu.ifrs.genki.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/** "Uso contínuo", "Tem data para terminar", "Ainda não sei" (DEC-009: agenda como contínuo, sem data final). */
enum class TreatmentType { CONTINUOUS, TEMPORARY, UNDEFINED }

/** Medicamento salvo (ESPEC §3). Concentração ("50 mg") e dose ("1 comprimido") nunca se misturam. */
data class Medication(
    val id: Long,
    val name: String,
    val concentration: String?,
    val dose: String,
    val days: Set<DayOfWeek>,
    val times: List<LocalTime>,
    val treatment: TreatmentType,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val remindersEnabled: Boolean,
)

/** Rascunho de edição: mesmo caminho de validação do cadastro. */
fun Medication.toDraft(): MedicationDraft = MedicationDraft(
    existingId = id,
    source = DraftSource.MANUAL,
    name = name,
    concentration = concentration,
    dose = dose,
    days = days,
    times = times,
    treatment = treatment,
    startDate = startDate,
    endDate = endDate,
)

package br.edu.ifrs.genki.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

enum class DraftSource { MANUAL, VOICE, CAMERA }

/** Rascunho único de cadastro e edição (DEC-014). Campos nulos = ainda não informados; sem padrão silencioso. */
data class MedicationDraft(
    val localId: String = UUID.randomUUID().toString(),
    val existingId: Long? = null,
    val source: DraftSource,
    val name: String? = null,
    val concentration: String? = null,
    val dose: String? = null,            // OCR nunca preenche (RF23)
    val days: Set<DayOfWeek>? = null,
    val times: List<LocalTime> = emptyList(),
    val treatment: TreatmentType? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val durationDays: Int? = null,       // "por 7 dias" → endDate = início + 6 (DEC-003)
)

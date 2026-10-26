package br.edu.ifrs.genki.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import br.edu.ifrs.genki.domain.TreatmentType
import java.time.LocalDate

// Tabelas da ESPEC §3 (DEC-008).
@Entity(tableName = "medication")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val concentration: String?,      // opcional, ex. "50 mg". Nunca copiar para dose
    val dose: String,                // obrigatória, ex. "1 comprimido"
    val daysMask: Int,               // bit = DayOfWeek.value - 1; 127 = todos os dias
    val treatment: TreatmentType,
    val startDate: LocalDate,
    val endDate: LocalDate?,         // só TEMPORARY; inclusiva
    val remindersEnabled: Boolean = true,
)

@Entity(
    tableName = "schedule_time",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["medicationId", "minuteOfDay"], unique = true)],
)
data class ScheduleTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val minuteOfDay: Int,            // 0..1439
)

data class MedicationWithTimes(
    @Embedded val medication: MedicationEntity,
    @Relation(parentColumn = "id", entityColumn = "medicationId")
    val times: List<ScheduleTimeEntity>,
)

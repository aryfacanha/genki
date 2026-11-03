package br.edu.ifrs.genki.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.edu.ifrs.genki.domain.ScheduleTimeDiff
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MedicationDao {

    // Ordem de cadastro, como no protótipo.
    @Transaction
    @Query("SELECT * FROM medication ORDER BY id")
    abstract fun observeAll(): Flow<List<MedicationWithTimes>>

    @Transaction
    @Query("SELECT * FROM medication WHERE id = :id")
    abstract fun observe(id: Long): Flow<MedicationWithTimes?>

    @Transaction
    @Query("SELECT * FROM medication WHERE id = :id")
    abstract suspend fun get(id: Long): MedicationWithTimes?

    @Transaction
    @Query("SELECT * FROM medication")
    abstract suspend fun getAll(): List<MedicationWithTimes>

    /** Medicamento dono do horário; `null` se o horário foi apagado (alarme antigo). */
    @Transaction
    @Query("SELECT medication.* FROM medication JOIN schedule_time ON schedule_time.medicationId = medication.id WHERE schedule_time.id = :timeId")
    abstract suspend fun findByTime(timeId: Long): MedicationWithTimes?

    @Query("SELECT id FROM schedule_time WHERE medicationId = :medicationId")
    abstract suspend fun timeIds(medicationId: Long): List<Long>

    @Insert
    protected abstract suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    protected abstract suspend fun updateMedication(medication: MedicationEntity)

    @Insert
    protected abstract suspend fun insertTimes(times: List<ScheduleTimeEntity>)

    @Query("SELECT * FROM schedule_time WHERE medicationId = :medicationId")
    protected abstract suspend fun times(medicationId: Long): List<ScheduleTimeEntity>

    @Query("DELETE FROM schedule_time WHERE id IN (:ids)")
    protected abstract suspend fun deleteTimes(ids: List<Long>)

    @Query("SELECT remindersEnabled FROM medication WHERE id = :id")
    protected abstract suspend fun remindersEnabled(id: Long): Boolean?

    @Query("DELETE FROM medication WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Query("UPDATE medication SET remindersEnabled = :enabled WHERE id = :id")
    abstract suspend fun setRemindersEnabled(id: Long, enabled: Boolean)

    @Transaction
    open suspend fun insert(medication: MedicationEntity, minutes: List<Int>): Long {
        val id = insertMedication(medication)
        insertTimes(minutes.map { ScheduleTimeEntity(medicationId = id, minuteOfDay = it) })
        return id
    }

    /**
     * Atualiza os horários sem regravar os que mantêm o minuto: o id deles continua o mesmo, e com ele o adiado e a
     * repetição pendentes (CC-01). Mantém a escolha de lembretes ligados/desligados. Devolve os ids que deixaram de
     * existir, para o repositório cancelar só os alarmes deles.
     */
    @Transaction
    open suspend fun update(medication: MedicationEntity, minutes: List<Int>): List<Long> {
        val enabled = remindersEnabled(medication.id) ?: return emptyList()
        updateMedication(medication.copy(remindersEnabled = enabled))
        val diff = ScheduleTimeDiff.of(times(medication.id).map { it.id to it.minuteOfDay }, minutes)
        deleteTimes(diff.removedIds)
        insertTimes(diff.addedMinutes.map { ScheduleTimeEntity(medicationId = medication.id, minuteOfDay = it) })
        return diff.removedIds
    }
}

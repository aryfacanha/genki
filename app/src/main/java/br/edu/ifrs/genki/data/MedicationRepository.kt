package br.edu.ifrs.genki.data

import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Gravação só com [DraftValidator.Result.Valid] (DEC-014).
 * A partir do M2, cada mutação chama `ReminderScheduler.sync` depois do commit (ESPEC §5).
 */
class MedicationRepository(private val dao: MedicationDao) {

    fun observeAll(): Flow<List<Medication>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observe(id: Long): Flow<Medication?> = dao.observe(id).map { it?.toDomain() }

    suspend fun insert(valid: DraftValidator.Result.Valid): Long =
        dao.insert(valid.draft.toEntity(), valid.draft.times.map { it.toMinuteOfDay() })

    suspend fun update(id: Long, valid: DraftValidator.Result.Valid) =
        dao.update(valid.draft.toEntity(id = id), valid.draft.times.map { it.toMinuteOfDay() })

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun setRemindersEnabled(id: Long, enabled: Boolean) = dao.setRemindersEnabled(id, enabled)
}

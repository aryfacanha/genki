package br.edu.ifrs.genki.data

import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.toDraft
import br.edu.ifrs.genki.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Gravação só com [DraftValidator.Result.Valid] (DEC-014).
 * Cada mutação reagenda os lembretes depois do commit (ESPEC §5); horários trocados ou apagados
 * têm os alarmes cancelados pelos ids antigos.
 */
class MedicationRepository(private val dao: MedicationDao, private val scheduler: ReminderScheduler) {

    fun observeAll(): Flow<List<Medication>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observe(id: Long): Flow<Medication?> = dao.observe(id).map { it?.toDomain() }

    suspend fun insert(valid: DraftValidator.Result.Valid): Long {
        val id = dao.insert(valid.draft.toEntity(), valid.draft.times.map { it.toMinuteOfDay() })
        dao.get(id)?.let(scheduler::sync)
        return id
    }

    suspend fun update(id: Long, valid: DraftValidator.Result.Valid) {
        val oldTimes = dao.timeIds(id)
        dao.update(valid.draft.toEntity(id = id), valid.draft.times.map { it.toMinuteOfDay() })
        scheduler.cancel(oldTimes)
        dao.get(id)?.let(scheduler::sync)
    }

    suspend fun delete(id: Long) {
        val oldTimes = dao.timeIds(id)
        dao.delete(id)
        scheduler.cancel(oldTimes)
    }

    /**
     * "Desfazer" da exclusão (DEC-055): grava de novo o medicamento excluído, com o mesmo id (mesmo lugar na lista) e
     * a mesma escolha de lembretes, passando pelo [DraftValidator] como qualquer gravação.
     */
    suspend fun restore(medication: Medication, today: LocalDate = LocalDate.now()) {
        val valid = DraftValidator.normalizeAndValidate(medication.toDraft(), today) as? DraftValidator.Result.Valid ?: return
        dao.insert(valid.draft.toEntity(id = medication.id, remindersEnabled = medication.remindersEnabled),
            valid.draft.times.map { it.toMinuteOfDay() })
        dao.get(medication.id)?.let(scheduler::sync)
    }

    suspend fun setRemindersEnabled(id: Long, enabled: Boolean) {
        dao.setRemindersEnabled(id, enabled)
        dao.get(id)?.let(scheduler::sync)
    }

    /** Medicamento dono do horário, para o receptor revalidar antes de avisar. */
    suspend fun byTime(timeId: Long): MedicationWithTimes? = dao.findByTime(timeId)

    /** Abertura do app, reinício do aparelho, troca de hora ou de fuso; também os adiados pendentes. */
    suspend fun syncAll() {
        dao.getAll().forEach(scheduler::sync)
        scheduler.restoreSnoozes()
    }
}

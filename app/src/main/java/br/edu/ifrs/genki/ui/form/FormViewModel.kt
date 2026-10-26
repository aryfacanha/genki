package br.edu.ifrs.genki.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.GenkiEval
import br.edu.ifrs.genki.data.MedicationRepository
import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Medication
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.Problem
import br.edu.ifrs.genki.domain.TreatmentType
import br.edu.ifrs.genki.domain.toDraft
import br.edu.ifrs.genki.ui.genkiContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

enum class DaysMode { ALL, SPECIFIC }

/** O que está na tela. "Todos os dias" e dias específicos ficam separados, como no protótipo. */
data class FormFields(
    val name: String = "",
    val concentration: String = "",
    val dose: String = "",
    val daysMode: DaysMode = DaysMode.ALL,
    val specificDays: Set<DayOfWeek> = emptySet(),
    val times: List<LocalTime> = emptyList(),
    val treatment: TreatmentType? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
) {
    fun toDraft(existingId: Long?) = MedicationDraft(
        existingId = existingId,
        source = DraftSource.MANUAL,
        name = name,
        concentration = concentration,
        dose = dose,
        days = if (daysMode == DaysMode.ALL) DayOfWeek.entries.toSet() else specificDays,
        times = times,
        treatment = treatment,
        startDate = startDate,
        endDate = endDate,
    )

    companion object {
        /** Marcar os 7 dias reabre como "Todos os dias" (divergência #1, sem efeito nos lembretes). */
        fun from(medication: Medication): FormFields {
            val draft = medication.toDraft()
            val everyDay = medication.days.size == 7
            return FormFields(
                name = draft.name.orEmpty(),
                concentration = draft.concentration.orEmpty(),
                dose = draft.dose.orEmpty(),
                daysMode = if (everyDay) DaysMode.ALL else DaysMode.SPECIFIC,
                specificDays = if (everyDay) emptySet() else medication.days,
                times = draft.times,
                treatment = draft.treatment,
                startDate = draft.startDate,
                endDate = draft.endDate,
            )
        }
    }
}

data class FormUiState(
    val fields: FormFields = FormFields(),
    val errors: Map<Field, Problem> = emptyMap(),
    /** 0..2 = etapas do cadastro; [REVIEW] = "Confira antes de salvar". A edição fica sempre em 0, página única. */
    val step: Int = 0,
    /** Etapa aberta pelo "Alterar" da revisão: ao continuar, volta para a revisão. */
    val fromReview: Boolean = false,
    val review: MedicationDraft? = null,
    val loaded: Boolean = false,
    val changed: Boolean = false,
    /** Mensagem do aviso depois de salvar; a tela fecha quando aparece. */
    val savedMessage: String? = null,
)

const val REVIEW = 3

/** Campos de cada etapa do cadastro manual (`MANUAL_STEPS` do protótipo). */
val STEP_FIELDS: List<Set<Field>> = listOf(
    setOf(Field.NAME, Field.DOSE),
    setOf(Field.DAYS, Field.TIMES),
    setOf(Field.TREATMENT, Field.END_DATE),
)

/** Cadastro manual (RF01, RF03–RF05, RF07, RF08) com revisão (RF17) e edição (RF02). */
class FormViewModel(
    private val editId: Long?,
    private val repository: MedicationRepository,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _state = MutableStateFlow(FormUiState(loaded = editId == null))
    val state: StateFlow<FormUiState> = _state.asStateFlow()

    val isEdit: Boolean get() = editId != null

    private var baseline = FormFields()
    private var beforeSectionEdit: FormFields? = null
    private var saving = false

    init {
        if (editId == null) {
            GenkiEval.log("cadastro_iniciado", "fonte" to DraftSource.MANUAL)
        } else {
            viewModelScope.launch {
                val medication = repository.observe(editId).first() ?: return@launch
                baseline = FormFields.from(medication)
                _state.update { it.copy(fields = baseline, loaded = true) }
            }
        }
    }

    /** Altera os campos e apaga o erro de quem foi mexido. */
    fun edit(vararg clears: Field, change: (FormFields) -> FormFields) {
        _state.update { s ->
            val fields = change(s.fields)
            s.copy(fields = fields, errors = s.errors - clears.toSet(), changed = fields != baseline)
        }
    }

    private fun validate() = DraftValidator.normalizeAndValidate(_state.value.fields.toDraft(editId), today())

    /** Data mostrada no campo "Data de início" quando ainda não foi escolhida. */
    fun startDateOrToday(): LocalDate = _state.value.fields.startDate ?: today()

    /** Continuar: valida só a etapa na tela; na última (ou vinda da revisão) abre a revisão. */
    fun next() {
        val s = _state.value
        val result = validate()
        val stepProblems = (result as? DraftValidator.Result.Invalid)?.problems.orEmpty()
            .filter { it.first in STEP_FIELDS[s.step] }
        if (stepProblems.isNotEmpty()) {
            _state.update { it.copy(errors = stepProblems.toMap()) }
            return
        }
        val toReview = s.fromReview || s.step == STEP_FIELDS.lastIndex
        if (toReview && result is DraftValidator.Result.Invalid) {
            // Campo de outra etapa ficou inválido: leva até ele.
            val first = result.problems.first().first
            _state.update { it.copy(step = stepOf(first), fromReview = false, errors = result.problems.toMap()) }
            return
        }
        beforeSectionEdit = null
        _state.update {
            it.copy(
                errors = emptyMap(),
                step = if (toReview) REVIEW else it.step + 1,
                fromReview = false,
                review = (result as? DraftValidator.Result.Valid)?.draft,
            )
        }
    }

    /** "Alterar" da revisão: abre só aquela etapa. */
    fun editSection(step: Int) {
        beforeSectionEdit = _state.value.fields
        _state.update { it.copy(step = step, fromReview = true, errors = emptyMap()) }
    }

    /** Voltar dentro do fluxo. `false` = sair da tela (quem chama pergunta antes de descartar). */
    fun back(): Boolean {
        val s = _state.value
        when {
            s.step == REVIEW -> _state.update { it.copy(step = STEP_FIELDS.lastIndex) }
            s.fromReview -> {
                val previous = beforeSectionEdit ?: s.fields
                beforeSectionEdit = null
                _state.update { it.copy(fields = previous, step = REVIEW, fromReview = false, errors = emptyMap()) }
            }
            !isEdit && s.step > 0 -> _state.update { it.copy(step = it.step - 1, errors = emptyMap()) }
            else -> return false
        }
        return true
    }

    /** "Confirmar e salvar" (cadastro) ou "Salvar alterações" (edição). Sempre pelo validador. */
    fun save() {
        if (saving) return
        val result = validate()
        if (result is DraftValidator.Result.Invalid) {
            _state.update {
                it.copy(
                    errors = result.problems.toMap(),
                    step = if (isEdit) 0 else stepOf(result.problems.first().first),
                )
            }
            return
        }
        val valid = result as DraftValidator.Result.Valid
        saving = true
        viewModelScope.launch {
            val message = if (editId == null) {
                val id = repository.insert(valid)
                GenkiEval.log("medicamento_salvo", "fonte" to DraftSource.MANUAL, "id" to id)
                "Medicamento adicionado"
            } else {
                repository.update(editId, valid)
                GenkiEval.log("medicamento_editado", "id" to editId)
                "Medicamento atualizado"
            }
            _state.update { it.copy(savedMessage = message) }
        }
    }

    private fun stepOf(field: Field) = STEP_FIELDS.indexOfFirst { field in it }

    companion object {
        fun factory(editId: Long?) = viewModelFactory {
            initializer { FormViewModel(editId, genkiContainer().medicationRepository) }
        }
    }
}

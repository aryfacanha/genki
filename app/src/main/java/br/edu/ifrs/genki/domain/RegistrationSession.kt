package br.edu.ifrs.genki.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Rascunhos do cadastro em andamento, compartilhados entre voz, formulário e revisão (ESPEC §4, RNF09).
 * Só em memória: limpa ao salvar ou descartar; se o processo morrer, o rascunho se perde (limitação).
 */
class RegistrationSession {
    private val _drafts = MutableStateFlow<List<MedicationDraft>>(emptyList())
    val drafts: StateFlow<List<MedicationDraft>> = _drafts.asStateFlow()

    fun set(drafts: List<MedicationDraft>) { _drafts.value = drafts }

    fun get(localId: String): MedicationDraft? = _drafts.value.firstOrNull { it.localId == localId }

    /** Troca o rascunho de mesmo `localId` (correção por toque). */
    fun replace(draft: MedicationDraft) = _drafts.update { list -> list.map { if (it.localId == draft.localId) draft else it } }

    fun remove(localId: String) = _drafts.update { list -> list.filterNot { it.localId == localId } }

    fun clear() { _drafts.value = emptyList() }
}

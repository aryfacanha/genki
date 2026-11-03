package br.edu.ifrs.genki.domain

/**
 * Ordem da lista de Medicamentos (P22, refinamento 1.0.25): alfabética, sem diferença de acento ou maiúsculas, com o
 * id como desempate. Só a lista usa; a Hoje, o alarme e o formulário seguem a ordem de cadastro do banco.
 */
object MedicationOrder {
    fun byName(list: List<Medication>): List<Medication> =
        list.sortedWith(compareBy({ DuplicateNames.normalize(it.name) }, { it.id }))
}

package br.edu.ifrs.genki.ui.form

/**
 * Unidade da dose para tocar no formulário, como no MyTherapy (pedido do autor, 27/10): quantidade + unidade viram
 * o mesmo texto livre de antes ("2 comprimidos"); nada muda no banco nem no `DraftValidator`. O que não cabe numa
 * unidade da lista ("1 colher de sopa", "1 envelope") fica em [OTHER], com o texto inteiro.
 */
enum class DoseUnit(val singular: String, val plural: String, val amounts: List<String>) {
    // Quantidades mais comuns para tocar, sem digitar (pedido do autor, 27/10); "Outra" no formulário abre o campo.
    TABLET("comprimido", "comprimidos", listOf("meio", "1", "2", "3")),
    CAPSULE("cápsula", "cápsulas", listOf("1", "2", "3")),
    DROP("gota", "gotas", listOf("5", "10", "15", "20")),
    ML("ml", "ml", listOf("5", "10", "15")),
    OTHER("Outro", "Outro", emptyList());

    /** "1 comprimido", "meio comprimido", "2 comprimidos"; vazio se faltar a quantidade. */
    fun dose(amount: String): String {
        val a = amount.trim()
        if (a.isEmpty() || this == OTHER) return ""
        return "$a ${if (isSingular(a)) singular else plural}"
    }

    companion object {
        val CHOICES = entries.toList()

        private val SINGULAR_AMOUNTS = setOf("1", "um", "uma", "meio", "meia", "1/2", "½", "0,5", "0.5")

        private fun isSingular(amount: String) = amount.lowercase() in SINGULAR_AMOUNTS

        /**
         * Separa uma dose salva ou vinda da voz em quantidade e unidade. `null` = vazia (nada escolhido ainda).
         * "1 comprimido e meio" e "1 colher de sopa" não são só quantidade + unidade: vão inteiros para [OTHER].
         */
        fun parse(dose: String): Pair<String, DoseUnit>? {
            val text = dose.trim()
            if (text.isEmpty()) return null
            val space = text.lastIndexOf(' ')
            if (space > 0) {
                val amount = text.substring(0, space).trim()
                val word = text.substring(space + 1).lowercase()
                val unit = entries.firstOrNull { it != OTHER && (word == it.singular || word == it.plural) }
                if (unit != null && amount.isNotEmpty() && ' ' !in amount) return amount to unit
            }
            return text to OTHER
        }
    }
}

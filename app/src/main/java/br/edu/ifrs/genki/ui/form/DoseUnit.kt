package br.edu.ifrs.genki.ui.form

/**
 * Unidade da dose para tocar no formulário, como no MyTherapy (pedido do autor, 27/10): quantidade + unidade viram
 * o mesmo texto livre de antes ("2 comprimidos"); nada muda no banco nem no `DraftValidator`. O que não cabe numa
 * unidade da lista ("1 colher de sopa") fica em [OTHER], com o texto inteiro.
 */
enum class DoseUnit(val singular: String, val plural: String, val amounts: List<String>, val step: Double) {
    // Quantidades mais comuns para tocar, sem digitar (pedido do autor, 27/10); "Outra" no formulário abre o − e +,
    // que anda de [step] em [step] (DEC-038: comprimido e ml de meio em meio, cápsula e gota de um em um).
    TABLET("comprimido", "comprimidos", listOf("meio", "1", "2", "3"), 0.5),
    CAPSULE("cápsula", "cápsulas", listOf("1", "2", "3"), 1.0),
    DROP("gota", "gotas", listOf("5", "10", "15", "20"), 1.0),
    ML("ml", "ml", listOf("5", "10", "15"), 0.5),
    OTHER("Outro", "Outro", emptyList(), 1.0);

    /**
     * "1 comprimido", "meio comprimido", "2 comprimidos", "1 comprimido e meio", "2,5 ml"; vazio se faltar a
     * quantidade. Número digitado com meio ("1,5") sai no mesmo formato do − e + ("1 comprimido e meio").
     */
    fun dose(amount: String): String {
        val typed = amount.trim()
        if (typed.isEmpty() || this == OTHER) return ""
        val a = value(typed)?.takeIf { it > 0 && it * 2 == Math.floor(it * 2) }?.let(::amountOf) ?: typed
        if (a.endsWith(HALF)) {
            val whole = a.removeSuffix(HALF)
            return "$whole ${if (isSingular(whole)) singular else plural}$HALF"
        }
        return "$a ${if (isSingular(a)) singular else plural}"
    }

    /** Quantidade como número: "meio" = 0,5, "1 e meio" = 1,5, "2,5" = 2,5; `null` se não for número. */
    fun value(amount: String): Double? {
        val a = amount.trim().lowercase()
        return when {
            a == "meio" || a == "meia" -> 0.5
            a.endsWith(HALF) -> a.removeSuffix(HALF).toIntOrNull()?.let { it + 0.5 }
            else -> a.replace(',', '.').toDoubleOrNull()
        }
    }

    /** Texto da quantidade: comprimido "meio", "1 e meio", "2"; as outras "2,5", "3". */
    fun amountOf(value: Double): String {
        val whole = value.toInt()
        val half = value - whole >= 0.5
        return when {
            !half -> "$whole"
            this == TABLET -> if (whole == 0) "meio" else "$whole$HALF"
            else -> "$whole,5"
        }
    }

    /** Um toque no + (DEC-038); quantidade que não é número recomeça em um passo. */
    fun plus(amount: String): String = amountOf(minOf(MAX, (value(amount) ?: 0.0) + step))

    /** Um toque no −, sem passar de um passo. */
    fun minus(amount: String): String = amountOf(maxOf(step, (value(amount) ?: step) - step))

    fun canMinus(amount: String): Boolean = (value(amount) ?: 0.0) > step

    /**
     * Quantidade que continua ao trocar para esta unidade (decisão do autor, 28/10): só se for um botão dela
     * (2 comprimidos → 2 cápsulas); senão fica vazia, para não gerar "meio cápsula" nem "20 ml" vindo de gotas.
     */
    fun keep(amount: String): String = amount.takeIf { it in amounts }.orEmpty()

    companion object {
        val CHOICES = entries.toList()

        private const val HALF = " e meio"
        private const val MAX = 100.0

        private val SINGULAR_AMOUNTS = setOf("1", "um", "uma", "meio", "meia", "1/2", "½", "0,5", "0.5")
        private val TABLET_AND_HALF = Regex("""(\d+) comprimidos? e meio""")

        private fun isSingular(amount: String) = amount.lowercase() in SINGULAR_AMOUNTS

        /**
         * Separa uma dose salva ou vinda da voz em quantidade e unidade. `null` = vazia (nada escolhido ainda).
         * "1 comprimido e meio" vira "1 e meio" de comprimido; "1 colher de sopa" não é só quantidade + unidade e
         * vai inteira para [OTHER].
         */
        fun parse(dose: String): Pair<String, DoseUnit>? {
            val text = dose.trim()
            if (text.isEmpty()) return null
            TABLET_AND_HALF.matchEntire(text.lowercase())?.let { return "${it.groupValues[1]}$HALF" to TABLET }
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

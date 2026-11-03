package br.edu.ifrs.genki.ui.form

/**
 * Unidade da dose para tocar no formulário, como no MyTherapy (pedido do autor, 27/10): quantidade + unidade viram
 * o mesmo texto livre de antes ("2 comprimidos"); nada muda no banco nem no `DraftValidator`. O que não cabe numa
 * unidade da lista ("1 colher de sopa") fica em [OTHER], com o texto inteiro.
 */
enum class DoseUnit(val singular: String, val plural: String, val amounts: List<String>, val step: Double) {
    // Quantidades mais comuns para tocar, sem digitar (pedido do autor, 27/10); "Outra" no formulário abre o − e +,
    // que anda de [step] em [step] (DEC-038: comprimido e ml de meio em meio, cápsula e gota de um em um). Comprimido
    // desce ainda de meio para 1/4 (autor, 03/11: "às vezes se toma 1/4", DEC-055).
    TABLET("comprimido", "comprimidos", listOf("meio", "1", "2", "3"), 0.5),
    CAPSULE("cápsula", "cápsulas", listOf("1", "2", "3"), 1.0),
    DROP("gota", "gotas", listOf("5", "10", "15", "20"), 1.0),
    ML("ml", "ml", listOf("5", "10", "15"), 0.5),
    OTHER("Outro", "Outro", emptyList(), 1.0);

    /**
     * "1 comprimido", "meio comprimido", "1/4 de comprimido", "2 comprimidos", "1 comprimido e meio", "2,5 ml"; vazio
     * se faltar a quantidade. Número digitado com meio ou quarto ("1,5", "0,25") sai no mesmo formato do − e +.
     */
    fun dose(amount: String): String {
        val typed = amount.trim()
        if (typed.isEmpty() || this == OTHER) return ""
        val a = value(typed)?.takeIf { it > 0 && it * 4 == Math.floor(it * 4) }?.let(::amountOf) ?: typed
        TABLET_PARTS.firstOrNull { a.endsWith(it) && a != it.trim() }?.let { part ->
            val whole = a.removeSuffix(part)
            return "$whole ${if (isSingular(whole)) singular else plural}$part"
        }
        if (a in QUARTERS) return "$a de $singular"
        return "$a ${if (isSingular(a)) singular else plural}"
    }

    /**
     * Quantidade como número: "meio" e "1/2" = 0,5, "1/4" = 0,25, "1 e meio" = 1,5, "1 e 3/4" = 1,75, "2,5" = 2,5;
     * `null` se não for número.
     */
    fun value(amount: String): Double? {
        val a = amount.trim().lowercase()
        return when {
            a in HALVES -> 0.5
            a == QUARTER -> 0.25
            a == THREE_QUARTERS -> 0.75
            a.endsWith(HALF) -> a.removeSuffix(HALF).toIntOrNull()?.let { it + 0.5 }
            a.endsWith(" e $QUARTER") -> a.removeSuffix(" e $QUARTER").toIntOrNull()?.let { it + 0.25 }
            a.endsWith(" e $THREE_QUARTERS") -> a.removeSuffix(" e $THREE_QUARTERS").toIntOrNull()?.let { it + 0.75 }
            else -> a.replace(',', '.').toDoubleOrNull()
        }
    }

    /**
     * Texto da quantidade: comprimido "1/4", "meio", "3/4", "1 e meio", "1 e 1/4", "2"; as outras "2,5", "3". Outras
     * frações digitadas continuam ("0,3"), até milésimos, para o − e + não arredondarem a dose (RV-DVC-02).
     */
    fun amountOf(value: Double): String {
        val thousandths = Math.round(value * 1000)
        val whole = thousandths / 1000
        val rest = thousandths % 1000
        val tabletPart = if (this == TABLET) mapOf(250L to QUARTER, 500L to "meio", 750L to THREE_QUARTERS)[rest] else null
        return when {
            rest == 0L -> "$whole"
            tabletPart != null -> if (whole == 0L) tabletPart else "$whole e $tabletPart"
            else -> "$whole," + "%03d".format(rest).trimEnd('0')
        }
    }

    /**
     * Um toque no + (DEC-038); quantidade que não é número recomeça em um passo. Acima de [MAX] fica como está. De 1/4
     * de comprimido, o + volta a meio.
     */
    fun plus(amount: String): String {
        val current = value(amount) ?: 0.0
        if (this == TABLET && Math.abs(current - 0.25) < EPSILON) return "meio"
        return if (canPlus(amount)) amountOf(current + step) else amount
    }

    /** Um toque no −, sem chegar a zero; comprimido para em 1/4 (DEC-055). */
    fun minus(amount: String): String {
        val current = value(amount) ?: return amountOf(step)
        return when {
            current - step > EPSILON -> amountOf(current - step)
            canMinus(amount) -> QUARTER
            else -> amount
        }
    }

    fun canMinus(amount: String): Boolean {
        val current = value(amount) ?: 0.0
        return current - step > EPSILON || (this == TABLET && current - 0.25 > EPSILON)
    }

    /** O + nunca diminui a quantidade: 120 gotas digitadas não viram 100 (RV-DVC-01). */
    fun canPlus(amount: String): Boolean = (value(amount) ?: 0.0) + step <= MAX + EPSILON

    /**
     * Quantidade que continua ao trocar para esta unidade (decisão do autor, 28/10): só se for um botão dela
     * (2 comprimidos → 2 cápsulas); senão fica vazia, para não gerar "meio cápsula" nem "20 ml" vindo de gotas.
     */
    fun keep(amount: String): String = amount.takeIf { it in amounts }.orEmpty()

    companion object {
        val CHOICES = entries.toList()

        private const val HALF = " e meio"
        private const val QUARTER = "1/4"
        private const val THREE_QUARTERS = "3/4"
        private val QUARTERS = setOf(QUARTER, THREE_QUARTERS)
        private val TABLET_PARTS = listOf(HALF, " e $QUARTER", " e $THREE_QUARTERS")
        private const val MAX = 100.0

        /**
         * Acima disto (em comprimidos, cápsulas, gotas ou ml, o número digitado) o formulário pergunta "Está certo?"
         * antes de gravar (P19). **Valor proposto, a decidir pelo autor**: uma constante só, para todas as unidades.
         */
        const val UNUSUAL_AMOUNT = 20.0

        /** Dose salva ou digitada com quantidade acima de [UNUSUAL_AMOUNT]; texto livre ("Outro") nunca. */
        fun isUnusual(dose: String): Boolean {
            val (amount, unit) = parse(dose) ?: return false
            if (unit == OTHER) return false
            return (unit.value(amount) ?: return false) > UNUSUAL_AMOUNT
        }
        private const val EPSILON = 1e-9
        private val HALVES = setOf("meio", "meia", "1/2", "½")

        private val SINGULAR_AMOUNTS = setOf("1", "um", "uma", "meio", "meia", "1/2", "½", "0,5", "0.5")
        private val TABLET_AND_PART = Regex("""(\d+) comprimidos? e (meio|1/4|3/4)""")
        private val PART_OF_TABLET = Regex("""(1/4|3/4) de comprimido""")

        private fun isSingular(amount: String) = amount.lowercase() in SINGULAR_AMOUNTS

        /**
         * Separa uma dose salva ou vinda da voz em quantidade e unidade. `null` = vazia (nada escolhido ainda).
         * "1 comprimido e meio" vira "1 e meio" de comprimido; "1/4 de comprimido", "1/4"; "1 colher de sopa" não é só quantidade + unidade e
         * vai inteira para [OTHER].
         */
        fun parse(dose: String): Pair<String, DoseUnit>? {
            val text = dose.trim()
            if (text.isEmpty()) return null
            TABLET_AND_PART.matchEntire(text.lowercase())?.let { return "${it.groupValues[1]} e ${it.groupValues[2]}" to TABLET }
            PART_OF_TABLET.matchEntire(text.lowercase())?.let { return it.groupValues[1] to TABLET }
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

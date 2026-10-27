package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.Frequency
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.TreatmentType
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * Interpreta o texto reconhecido e atualiza os rascunhos (ESPEC §7, RF06, RF11–RF16). Kotlin puro, só regras:
 * sem LLM e sem chute de campo. O que não entende fica nulo e a tela pergunta (RF15).
 *
 * - Nome: palavras que não são número, unidade, dia, horário nem palavra comum. Nome novo = rascunho novo (RF14);
 *   nome já citado volta para aquele rascunho. "A e B" sem nada entre os dois recebem as mesmas informações.
 * - Sem nome na fala, vale para [target] (o rascunho da pergunta na tela), senão para o último (RF13).
 * - Respondendo a uma pergunta ([expecting] que não seja o nome), tudo vale para [target]: nomes ditos são ignorados e
 *   não criam medicamento novo (pedido do autor, 27/10, após teste no Samsung A31: "umas 9 da noite" virava remédio).
 * - Horários se somam; "na verdade" troca o horário mais próximo; "só" troca todos (RF16).
 */
object VoiceParser {

    data class Result(val drafts: List<MedicationDraft>, val understood: Boolean)

    fun parse(
        text: String,
        drafts: List<MedicationDraft>,
        today: LocalDate,
        target: String? = null,
        expecting: Field? = null,
    ): Result = Parse(tokenize(text), drafts, today, target, expecting).run()

    private class Token(val text: String, val norm: String)

    private val TOKEN = Regex("""\d{1,2}[:h]\d{2}\b|\d+(?:[.,]\d+)?|\p{L}\d{1,3}\b|\p{L}+(?:-\p{L}+)*|/""")
    private val CLOCK = Regex("""(\d{1,2})[:h](\d{2})""")

    private fun tokenize(text: String) = TOKEN.findAll(text).map { Token(it.value, normalize(it.value)) }.toList()

    fun normalize(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

    private val NUMBER_WORDS = mapOf(
        "zero" to 0, "um" to 1, "uma" to 1, "dois" to 2, "duas" to 2, "tres" to 3, "quatro" to 4, "cinco" to 5,
        "seis" to 6, "sete" to 7, "oito" to 8, "nove" to 9, "dez" to 10, "onze" to 11, "doze" to 12, "treze" to 13,
        "catorze" to 14, "quatorze" to 14, "quinze" to 15, "dezesseis" to 16, "dezessete" to 17, "dezoito" to 18,
        "dezenove" to 19, "vinte" to 20, "trinta" to 30, "quarenta" to 40, "cinquenta" to 50, "sessenta" to 60,
        "setenta" to 70, "oitenta" to 80, "noventa" to 90, "cem" to 100, "cento" to 100, "duzentos" to 200,
        "trezentos" to 300, "quatrocentos" to 400, "quinhentos" to 500, "seiscentos" to 600, "setecentos" to 700,
        "oitocentos" to 800, "novecentos" to 900, "mil" to 1000,
    )

    /** Unidade de concentração → forma curta ("500 miligramas" → "500 mg"). */
    private val STRENGTH_UNITS = mapOf(
        "mg" to "mg", "miligrama" to "mg", "miligramas" to "mg", "g" to "g", "grama" to "g", "gramas" to "g",
        "mcg" to "mcg", "micrograma" to "mcg", "microgramas" to "mcg", "ui" to "UI",
    )

    /** Unidades de dose: ficam como foram ditas ("2 gotas"), exceto mililitro → "ml". */
    private val DOSE_UNITS = setOf(
        "comprimido", "comprimidos", "capsula", "capsulas", "gota", "gotas", "ml", "mililitro", "mililitros",
        "colher", "colheres", "sache", "saches", "ampola", "ampolas", "unidade", "unidades", "jato", "jatos",
        "puff", "puffs", "inalacao", "inalacoes", "aplicacao", "aplicacoes", "adesivo", "adesivos", "envelope",
        "envelopes", "dragea", "drageas", "pastilha", "pastilhas", "supositorio", "supositorios", "medida", "medidas",
    )

    private val DAYS = mapOf(
        "segunda" to DayOfWeek.MONDAY, "terca" to DayOfWeek.TUESDAY, "quarta" to DayOfWeek.WEDNESDAY,
        "quinta" to DayOfWeek.THURSDAY, "sexta" to DayOfWeek.FRIDAY, "sabado" to DayOfWeek.SATURDAY,
        "domingo" to DayOfWeek.SUNDAY,
    )

    private val MONTHS = listOf(
        "janeiro", "fevereiro", "marco", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro",
        "novembro", "dezembro",
    )

    /** Palavras que nunca viram nome de medicamento. */
    private val COMMON = setOf(
        "eu", "tomo", "tomar", "toma", "tomando", "tome", "uso", "usar", "usando", "bebo", "pingo", "passo", "aplico",
        "o", "a", "os", "as", "de", "do", "da", "dos", "das", "e", "em", "no", "na", "nos", "nas", "ao", "aos", "pela",
        "pelo", "pelas", "pelos", "por", "para", "pra", "com", "sem", "que", "tambem", "mais", "remedio", "remedios",
        "medicamento", "medicamentos", "medicacao", "preciso", "tenho", "vezes", "vez", "dia", "dias", "hora", "horas",
        "h", "manha", "tarde", "noite", "madrugada", "cedo", "meio", "meia", "jejum", "antes", "depois", "cafe",
        "almoco", "jantar", "janta", "dormir", "deitar", "acordar", "refeicao", "refeicoes", "todo", "todos", "toda",
        "todas", "sim", "nao", "ok", "entao", "agora", "ainda", "sei", "so", "somente", "apenas", "cada", "comecando",
        "partir", "primeira", "primeiro", "ate", "durante", "semana", "semanas", "mes", "meses", "continuo",
        "continua", "sempre", "isso", "esse", "essa", "este", "esta", "ele", "ela", "eles", "elas", "ambos", "ambas",
        "verdade", "corrigindo", "corrige", "quer", "dizer", "errei", "outro", "outra", "e", "sao", "seria", "ser",
        "diariamente", "proximo", "dose", "doses", "feira", "uteis", "fim", "fins", "final", "finais", "data",
        "parar", "terminar", "acabar", "quando", "quanto", "tempo", "tem", "minha", "meu", "minhas", "meus",
        "pressao", "diabetes", "colesterol", "dor", "cabeca", "estomago", "coracao", "tireoide", "sopa", "cha",
        "alias", "melhor", "dizendo", "hoje", "amanha", "lembrar", "lembrete", "cadastrar", "colocar", "anota",
        "anotar", "bom", "boa", "olha", "assim", "tipo", "la", "aqui", "ai", "certo", "isso", "mesmo", "mesma",
        "horario", "horarios", "vou", "vai", "estou", "to", "ta", "foi", "era", "deve", "devo", "mandou", "medico",
        "medica", "receita", "receitou", "hum", "hmm", "ahn", "ah", "eh", "oh", "ne", "pois", "bem", "sabe",
        // Aproximação e hábito: "umas 9", "lá pelas 8", "por volta das 7", "mais ou menos", "geralmente".
        "umas", "uns", "volta", "cerca", "aproximadamente", "ou", "menos", "quase", "geralmente", "normalmente",
        "costumo", "costuma", "acho", "acredito", "talvez", "creio",
        // "Tomo" ouvido como "como" pelo reconhecedor do Samsung A31 (27/10); "como" também é palavra comum.
        "como", "pego", "faco", "remedinho",
    )

    private val ALL_DAYS = DayOfWeek.entries.toSet()
    private val WEEKDAYS = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

    private class Parse(
        val t: List<Token>,
        input: List<MedicationDraft>,
        val today: LocalDate,
        val target: String?,
        val expecting: Field?,
    ) {
        val drafts = input.toMutableList()
        val before = input.associate { it.localId to it.times }
        val beforeDays = input.associate { it.localId to it.days }
        /** Dias já ditos nesta fala, por rascunho: trechos separados ("segunda e quarta e também sexta") se somam. */
        val spokenDays = mutableMapOf<String, Set<DayOfWeek>>()
        /** Horários de antes desta fala desfeitos com "não" ("às 20, não, às 21" com 20:00 já entendido). */
        val negatedBefore = mutableMapOf<String, MutableSet<LocalTime>>()
        val newTimes = linkedMapOf<String, MutableList<LocalTime>>()
        var group = mutableListOf<String>()
        var groupHasInfo = false
        var namedInUtterance = false
        var lastNameEnd = -1
        var correction = false
        var replaceAll = false
        /** Último horário dito nesta fala e para quem; "não, às 21" troca esse horário (RV-M3-04). */
        var lastTime: Pair<List<String>, LocalTime>? = null
        var negateLast = false
        var understood = false
        /** Horários seguidos na fala ("às 8, 14 e 22"): número solto continua a lista. */
        var timeRun = 0

        fun norm(i: Int) = t.getOrNull(i)?.norm
        fun isAny(i: Int, vararg words: String) = norm(i) in words

        fun run(): Result {
            group = initialGroup()
            var i = 0
            while (i < t.size) {
                val next = step(i)
                i = if (next > i) next else i + 1
            }
            finish()
            return Result(drafts.toList(), understood)
        }

        private fun initialGroup(): MutableList<String> = when {
            target != null && drafts.any { it.localId == target } -> mutableListOf(target)
            drafts.isNotEmpty() -> mutableListOf(drafts.last().localId)
            else -> mutableListOf()
        }

        /** Tenta cada regra em [i]; devolve o próximo índice (ou [i] se nada casou). */
        private fun step(i: Int): Int {
            listOf(::cue, ::scope, ::frequency, ::strength, ::dose, ::treatment, ::days, ::time, ::name).forEach { rule ->
                val runBefore = timeRun
                val next = rule(i)
                if (next > i) {
                    // Qualquer coisa que não seja horário encerra a lista de horários.
                    if (timeRun == runBefore) timeRun = 0
                    return next
                }
            }
            if (norm(i) != "e") timeRun = 0
            return i
        }

        // ── aplicar ──────────────────────────────────────────────────────────

        private fun apply(change: (MedicationDraft) -> MedicationDraft) {
            if (group.isEmpty()) {
                val draft = MedicationDraft(source = DraftSource.VOICE)
                drafts += draft
                group = mutableListOf(draft.localId)
            }
            group.forEach { id ->
                val index = drafts.indexOfFirst { it.localId == id }
                if (index >= 0) drafts[index] = change(drafts[index])
            }
            groupHasInfo = true
            understood = true
        }

        private fun addTime(time: LocalTime) {
            apply { it }
            val negated = lastTime
            if (negateLast && negated != null) {
                negated.first.forEach { id ->
                    newTimes[id]?.remove(negated.second)
                    if (before[id].orEmpty().contains(negated.second)) negatedBefore.getOrPut(id) { mutableSetOf() } += negated.second
                }
                negateLast = false
            }
            group.forEach { newTimes.getOrPut(it) { mutableListOf() } += time }
            lastTime = group.toList() to time
        }

        private fun finish() {
            newTimes.forEach { (id, spoken) ->
                val index = drafts.indexOfFirst { it.localId == id }
                if (index < 0) return@forEach
                val draft = drafts[index]
                val old = before[id].orEmpty() - negatedBefore[id].orEmpty()
                val times = when {
                    replaceAll || old.isEmpty() -> spoken
                    correction -> replaceNearest(old, spoken)
                    else -> old + spoken
                }
                drafts[index] = draft.copy(times = times.distinct().sorted())
            }
            // "De 8 em 8 horas" + primeiro horário → os outros horários do dia (RF06).
            drafts.replaceAll { draft ->
                val every = draft.everyHours ?: return@replaceAll draft
                val perDay = 24 / every
                when {
                    draft.times.isEmpty() -> draft
                    draft.times.size >= perDay -> draft.copy(everyHours = null)
                    else -> draft.copy(times = Frequency.fill(draft.times.min(), every), everyHours = null)
                }
            }
        }

        private fun replaceNearest(old: List<LocalTime>, spoken: List<LocalTime>): List<LocalTime> {
            val result = old.toMutableList()
            spoken.forEach { time ->
                val nearest = result.indices.minByOrNull { minutesApart(result[it], time) }
                if (nearest == null) result += time else result[nearest] = time
            }
            return result
        }

        private fun minutesApart(a: LocalTime, b: LocalTime): Int {
            val d = Math.abs(a.toSecondOfDay() - b.toSecondOfDay()) / 60
            return minOf(d, 1440 - d)
        }

        // ── números ──────────────────────────────────────────────────────────

        /** Número em algarismos ("50", "2,5") ou por extenso ("oitocentos e cinquenta"). */
        fun number(i: Int): Pair<Double, Int>? {
            val token = t.getOrNull(i) ?: return null
            token.text.replace(',', '.').toDoubleOrNull()?.let { return it to i + 1 }
            var value = NUMBER_WORDS[token.norm] ?: return null
            var j = i + 1
            while (isAny(j, "e")) {
                val part = NUMBER_WORDS[norm(j + 1)] ?: break
                val fits = (value >= 100 && value % 100 == 0 && part < 100) || (value in 20..90 && value % 10 == 0 && part in 1..9)
                if (!fits) break
                value += part
                j += 2
            }
            return value.toDouble() to j
        }

        fun whole(i: Int): Pair<Int, Int>? = number(i)?.takeIf { it.first % 1.0 == 0.0 }?.let { it.first.toInt() to it.second }

        fun format(value: Double): String =
            if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

        // ── regras ───────────────────────────────────────────────────────────

        /** Correção a partir daqui: os dias ditos antes nesta mesma fala também deixam de valer (RV-M3-02). */
        private fun correct(end: Int): Int {
            correction = true
            spokenDays.clear()
            return end
        }

        private fun cue(i: Int): Int = when {
            isAny(i, "na") && isAny(i + 1, "verdade") -> correct(i + 2)
            isAny(i, "quer") && isAny(i + 1, "dizer") -> correct(i + 2)
            isAny(i, "melhor") && isAny(i + 1, "dizendo") -> correct(i + 2)
            isAny(i, "corrigindo", "corrige", "errei", "alias") -> correct(i + 1)
            isAny(i, "so", "somente", "apenas") -> { replaceAll = true; spokenDays.clear(); i + 1 }
            // "Não sei" é resposta de duração (regra do tratamento).
            isAny(i, "nao") && isAny(i + 1, "sei") -> i
            // "às 8 e às 20, não, às 21": o "não" desfaz o último horário dito.
            isAny(i, "nao") && lastTime != null && (isAny(i + 1, "as", "das", "e") || whole(i + 1) != null) -> {
                negateLast = true; i + 1
            }
            isAny(i, "nao") -> correct(i + 1)
            else -> i
        }

        /** "Os dois", "ambos", "todos eles": vale para todos os rascunhos. */
        private fun scope(i: Int): Int {
            val end = when {
                isAny(i, "os", "as") && isAny(i + 1, "dois", "duas", "tres") -> i + 2
                isAny(i, "ambos", "ambas") -> i + 1
                isAny(i, "todos", "todas") && isAny(i + 1, "eles", "elas") -> i + 2
                else -> return i
            }
            group = drafts.map { it.localId }.toMutableList()
            groupHasInfo = false
            return end
        }

        /** "De 8 em 8 horas", "a cada 12 horas", "duas vezes ao dia" (RF06). */
        private fun frequency(i: Int): Int {
            var every: Int? = null
            var end = i
            if (isAny(i, "de")) {
                val (n, j) = whole(i + 1) ?: return i
                if (!isAny(j, "em")) return i
                val (m, k) = whole(j + 1) ?: return i
                if (m != n) return i
                every = n
                end = if (isAny(k, "h", "hora", "horas")) k + 1 else k
            } else if (isAny(i, "cada") || (isAny(i, "a") && isAny(i + 1, "cada"))) {
                val start = if (isAny(i, "cada")) i + 1 else i + 2
                val (n, j) = whole(start) ?: return i
                if (!isAny(j, "h", "hora", "horas")) return i
                every = n
                end = j + 1
            } else {
                val (n, j) = whole(i) ?: return i
                if (!isAny(j, "vez", "vezes") || !isAny(j + 1, "ao", "por", "no", "a") || !isAny(j + 2, "dia")) return i
                if (n !in 1..24 || 24 % n != 0) return i
                every = 24 / n
                end = j + 3
            }
            if (every == null || every !in 1..24 || 24 % every != 0) return i
            apply { it.copy(everyHours = every, days = it.days ?: ALL_DAYS) }
            return end
        }

        /**
         * "50 mg", "500 miligramas", "20mg"; composta "250 mg por 5 ml", "250 mg/5 ml", "500 mg por ml" (RV-M3-01).
         * Concentração nunca vira dose: o volume da composição não é quanto a pessoa toma.
         */
        private fun strength(i: Int): Int {
            val (value, j) = number(i) ?: return i
            val unit = STRENGTH_UNITS[norm(j)] ?: return i
            var text = "${format(value)} $unit"
            var end = j + 1
            if (isAny(end, "por", "/")) {
                val volume = number(end + 1)
                val volumeUnit = if (volume != null) volume.second else end + 1
                if (isAny(volumeUnit, "ml", "mililitro", "mililitros")) {
                    text += "/" + (volume?.let { "${format(it.first)} " } ?: "") + "ml"
                    end = volumeUnit + 1
                }
            }
            apply { it.copy(concentration = text) }
            return end
        }

        /** "1 comprimido", "meio comprimido", "10 gotas", "uma cápsula", "1 comprimido e meio". */
        private fun dose(i: Int): Int {
            val half = isAny(i, "meio", "meia")
            // "1/2 comprimido": a fração fica como foi dita (o denominador não é a dose).
            val fraction = number(i)?.let { (n, k) ->
                if (isAny(k, "/")) number(k + 1)?.let { (d, m) -> "${format(n)}/${format(d)}" to m } else null
            }
            val (amount, j) = fraction
                ?: if (half) t[i].text.lowercase() to i + 1 else number(i)?.let { format(it.first) to it.second } ?: return i
            val unitNorm = norm(j) ?: return i
            if (unitNorm !in DOSE_UNITS) return i
            var text = "$amount ${if (unitNorm.startsWith("mililitro")) "ml" else t[j].text.lowercase()}"
            var end = j + 1
            if (isAny(end, "de") && isAny(end + 1, "sopa", "cha")) {
                text += " de ${t[end + 1].text.lowercase()}"
                end += 2
            }
            if (isAny(end, "e") && isAny(end + 1, "meio", "meia")) {
                text += " e ${t[end + 1].text.lowercase()}"
                end += 2
            }
            apply { it.copy(dose = text) }
            return end
        }

        /** "Uso contínuo", "por 7 dias", "até dia 15", "ainda não sei". */
        private fun treatment(i: Int): Int {
            fun set(type: TreatmentType, end: LocalDate? = null, duration: Int? = null) =
                apply { it.copy(treatment = type, endDate = end, durationDays = duration) }
            val askedAboutTime = expecting == Field.TREATMENT || expecting == Field.END_DATE
            when {
                isAny(i, "uso") && isAny(i + 1, "continuo") -> { set(TreatmentType.CONTINUOUS); return i + 2 }
                isAny(i, "continuo", "continua") -> { set(TreatmentType.CONTINUOUS); return i + 1 }
                isAny(i, "para", "pra") && isAny(i + 1, "sempre") -> { set(TreatmentType.CONTINUOUS); return i + 2 }
                isAny(i, "sem") && isAny(i + 1, "data") -> { set(TreatmentType.CONTINUOUS); return i + 2 }
                isAny(i, "ainda") && isAny(i + 1, "nao") && isAny(i + 2, "sei") -> { set(TreatmentType.UNDEFINED); return i + 3 }
                isAny(i, "nao") && isAny(i + 1, "sei") && (askedAboutTime || isAny(i + 2, "ate", "por")) -> {
                    set(TreatmentType.UNDEFINED); return i + 2
                }
            }
            if (isAny(i, "por", "durante")) {
                val (n, j) = whole(i + 1) ?: return i
                val days = when (norm(j)) {
                    "dia", "dias" -> n
                    "semana", "semanas" -> n * 7
                    else -> return i
                }
                if (days < 1) return i
                set(TreatmentType.TEMPORARY, duration = days)
                return j + 1
            }
            val dateStart = when {
                isAny(i, "ate") && isAny(i + 1, "o") && isAny(i + 2, "dia") -> i + 3
                isAny(i, "ate") && isAny(i + 1, "dia") -> i + 2
                isAny(i, "ate") -> i + 1
                askedAboutTime && isAny(i, "dia") -> i + 1
                askedAboutTime -> i
                else -> return i
            }
            val (day, j) = whole(dateStart) ?: return i
            var next = j
            var month: Int? = null
            var year: Int? = null
            when {
                isAny(j, "de") && MONTHS.indexOf(norm(j + 1)) >= 0 -> {
                    month = MONTHS.indexOf(norm(j + 1)) + 1
                    next = j + 2
                    if (isAny(next, "de")) whole(next + 1)?.let { (y, k) -> year = y; next = k }
                }
                isAny(j, "/") -> {
                    val (m, k) = whole(j + 1) ?: return i
                    month = m
                    next = k
                    if (isAny(k, "/")) whole(k + 1)?.let { (y, after) -> year = y; next = after }
                }
            }
            // Data impossível ou já passada: a expressão inteira é consumida (os pedaços não viram outro dia)
            // e nada é preenchido; a tela pergunta de novo (RV-M3C-03, RV-M3D-01).
            val end = date(day, month, year) ?: return next
            set(TreatmentType.TEMPORARY, end = end)
            return next
        }

        /** Dia/mês(/ano) dito; sem ano, a próxima ocorrência; com ano, exatamente ele. `null` = impossível ou passado. */
        private fun date(day: Int, month: Int?, year: Int?): LocalDate? {
            if (month == null) return civilDate(day, null)
            if (month !in 1..12) return null
            if (year == null) return civilDate(day, month - 1)
            val fullYear = if (year < 100) 2000 + year else year
            val ym = YearMonth.of(fullYear, month)
            if (day !in 1..ym.lengthOfMonth()) return null
            return ym.atDay(day).takeIf { it >= today }
        }

        /** Dia [day] (do mês [month], 0 = janeiro) a partir de hoje; sem mês, o próximo dia [day]. */
        private fun civilDate(day: Int, month: Int?): LocalDate? {
            if (day !in 1..31) return null
            if (month != null) {
                val ym = YearMonth.of(today.year, month + 1)
                if (day > ym.lengthOfMonth()) return null
                val date = ym.atDay(day)
                return if (date < today) YearMonth.of(today.year + 1, month + 1).atDay(day) else date
            }
            var ym = YearMonth.from(today)
            repeat(12) {
                if (day <= ym.lengthOfMonth() && ym.atDay(day) >= today) return ym.atDay(day)
                ym = ym.plusMonths(1)
            }
            return null
        }

        /** "Todo dia", "de segunda a sexta", "segunda e quarta", "fim de semana", "dias úteis". */
        private fun days(i: Int): Int {
            fun set(days: Set<DayOfWeek>, end: Int, additive: Boolean = false): Int {
                apply { draft ->
                    val id = draft.localId
                    val previous = beforeDays[id]
                    val keepPrevious = additive && !correction && !replaceAll && previous != null && previous != ALL_DAYS
                    val thisUtterance = if (additive) spokenDays[id].orEmpty() + days else days
                    spokenDays[id] = thisUtterance
                    draft.copy(days = if (keepPrevious) previous.orEmpty() + thisUtterance else thisUtterance)
                }
                return end
            }
            when {
                isAny(i, "todo", "todos", "toda", "todas") && isAny(i + 1, "dia", "dias") -> return set(ALL_DAYS, i + 2)
                isAny(i, "todo", "todos") && isAny(i + 1, "o", "os") && isAny(i + 2, "dia", "dias") -> return set(ALL_DAYS, i + 3)
                isAny(i, "diariamente") -> return set(ALL_DAYS, i + 1)
                isAny(i, "dias") && isAny(i + 1, "uteis") -> return set(WEEKDAYS, i + 2)
                isAny(i, "fim", "fins", "final", "finais") && isAny(i + 1, "de") && isAny(i + 2, "semana") ->
                    return set(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), i + 3)
                expecting == Field.DAYS && isAny(i, "todos", "todo", "tudo") -> return set(ALL_DAYS, i + 1)
            }
            val first = day(i)
            if (first == null) {
                if (!isAny(i, "de")) return i
                val from = day(i + 1) ?: return i
                if (!isAny(i + 2, "a", "ate")) return i
                val to = day(i + 3) ?: return i
                return set(range(from, to), i + 4, additive = true)
            }
            if (isAny(i + 1, "a", "ate")) day(i + 2)?.let { to -> return set(range(first, to), i + 3, additive = true) }
            val found = mutableSetOf(first)
            var j = i + 1
            while (j < t.size) {
                val d = day(j)
                when {
                    d != null -> found += d
                    isAny(j, "e", "na", "no", "nas", "nos", "feira") -> Unit
                    else -> break
                }
                j++
            }
            return set(found, j, additive = true)
        }

        private fun day(i: Int): DayOfWeek? {
            val word = norm(i)?.removeSuffix("-feira")?.removeSuffix("-feiras") ?: return null
            return DAYS[word] ?: DAYS[word.removeSuffix("s")]
        }

        private fun range(from: DayOfWeek, to: DayOfWeek): Set<DayOfWeek> {
            val result = mutableSetOf(from)
            var d = from
            while (d != to) { d = d.plus(1); result += d }
            return result
        }

        /** "Às 8", "8 da manhã", "20h", "8 e 20", "8:30", "meio-dia"; números soltos seguem uma lista de horários. */
        private fun time(i: Int): Int {
            val token = t.getOrNull(i) ?: return i
            CLOCK.matchEntire(token.norm)?.let { match ->
                val h = match.groupValues[1].toInt()
                val m = match.groupValues[2].toInt()
                if (h > 23 || m > 59) return i
                return emit(h, m, if (isAny(i + 1, "h", "horas")) i + 2 else i + 1)
            }
            when {
                isAny(i, "meio-dia") || (isAny(i, "meio") && isAny(i + 1, "dia")) ->
                    return emit(12, 0, if (isAny(i, "meio-dia")) i + 1 else i + 2, fixed = true)
                isAny(i, "meia-noite") || (isAny(i, "meia") && isAny(i + 1, "noite")) ->
                    return emit(0, 0, if (isAny(i, "meia-noite")) i + 1 else i + 2, fixed = true)
            }
            val announcedAlone = isAny(i, "as", "das", "pelas")
            val announced = announcedAlone || (isAny(i, "e") && isAny(i + 1, "as"))
            val start = when {
                announcedAlone -> i + 1
                announced -> i + 2
                isAny(i, "e") && timeRun >= 1 -> i + 1
                else -> i
            }
            // "às 8 e às 20" e "às 8, 14 e 22" (lista) recomeçam aqui; "às 8 e 20" vira 08:20 (ESPEC §7).
            val (h, j) = whole(start) ?: return i
            if (h > 23) return i
            if (STRENGTH_UNITS.containsKey(norm(j)) || norm(j) in DOSE_UNITS) return i
            val hasMarker = isAny(j, "h", "hora", "horas") || periodWord(j)
            val allowed = announced || start != i || hasMarker || timeRun >= 1 || expecting == Field.TIMES
            if (!allowed) return i
            var end = j
            var minute = 0
            if (isAny(end, "h", "hora", "horas")) {
                end++
                whole(end)?.let { (m, k) -> if (m in 1..59 && !isAny(k, "h", "hora", "horas")) { minute = m; end = k } }
            } else if (isAny(end, "e") && isAny(end + 1, "meia")) {
                minute = 30; end += 2
            } else if (isAny(end, "e") && (announced || timeRun == 0)) {
                whole(end + 1)?.let { (m, k) ->
                    val looksLikeHour = isAny(k, "h", "hora", "horas") || periodWord(k)
                    if (m in 1..59 && !looksLikeHour) { minute = m; end = k }
                }
            }
            if (minute != 0 && isAny(end, "h", "horas", "minutos")) end++
            return emit(h, minute, end)
        }

        private fun periodWord(i: Int) = isAny(i, "da", "de", "pela", "a") && isAny(i + 1, "manha", "tarde", "noite", "madrugada")

        private fun emit(h: Int, m: Int, end: Int, fixed: Boolean = false): Int {
            var hour = h
            var next = end
            if (!fixed && periodWord(end)) {
                when (norm(end + 1)) {
                    "tarde" -> if (hour in 1..11) hour += 12
                    "noite" -> if (hour in 6..11) hour += 12 else if (hour == 12) hour = 0
                }
                next = end + 2
            }
            addTime(LocalTime.of(hour, m))
            timeRun++
            return next
        }

        /** Palavras seguidas que não são outra coisa = nome. "A e B" juntos; "carbonato de cálcio" fica inteiro. */
        private fun name(i: Int): Int {
            if (!isNameWord(i, first = true)) return i
            var j = i
            while (j < t.size && isNameWord(j, first = j == i)) j++
            // Resposta a uma pergunta: o nome dito (repetido ou palavra desconhecida) não muda de medicamento.
            if (target != null && expecting != null && expecting != Field.NAME) return j
            val words = t.subList(i, j).map { it.text }.toMutableList()
            val digits = t.getOrNull(j)?.text
            val unitAfter = norm(j + 1)?.let { it in STRENGTH_UNITS || it in DOSE_UNITS } == true
            // "vitamina d 8 horas" e resposta à pergunta do horário: o número é hora, não parte do nome.
            val timeAfter = isAny(j + 1, "h", "hora", "horas") || periodWord(j + 1) || expecting == Field.TIMES
            if (words.last().length == 1 && digits != null && digits.length <= 3 && digits.all(Char::isDigit) && !unitAfter && !timeAfter) {
                words[words.lastIndex] = words.last() + digits
                j++
            }
            // Letra + número ("b12", "d3") em maiúscula.
            val spoken = words.joinToString(" ") { if (it.length <= 4 && it.drop(1).all(Char::isDigit) && it.length > 1) it.uppercase() else it }
            val joinsPrevious = lastNameEnd >= 0 && i == lastNameEnd + 1 && isAny(lastNameEnd, "de", "do", "da")
            if (joinsPrevious && group.size == 1) {
                val id = group.single()
                val index = drafts.indexOfFirst { it.localId == id }
                if (index >= 0) drafts[index] = drafts[index].copy(name = "${drafts[index].name} ${t[lastNameEnd].text} $spoken")
                lastNameEnd = j
                return j
            }
            val existing = drafts.firstOrNull { it.name != null && sameName(it.name, spoken) }
            when {
                existing != null -> {
                    group = mutableListOf(existing.localId)
                    groupHasInfo = false
                }
                group.size == 1 && drafts.first { it.localId == group[0] }.name == null -> {
                    val id = group[0]
                    drafts.replaceAll { if (it.localId == id) it.copy(name = capitalize(spoken)) else it }
                }
                else -> {
                    val draft = MedicationDraft(source = DraftSource.VOICE, name = capitalize(spoken))
                    drafts += draft
                    val joined = namedInUtterance && !groupHasInfo && i > 0 && isAny(i - 1, "e")
                    if (joined) group += draft.localId else group = mutableListOf(draft.localId)
                    groupHasInfo = false
                }
            }
            namedInUtterance = true
            understood = true
            lastNameEnd = j
            return j
        }

        private fun isNameWord(i: Int, first: Boolean): Boolean {
            val token = t.getOrNull(i) ?: return false
            if (!token.text.first().isLetter()) return false
            val n = token.norm
            if (n in COMMON || n in NUMBER_WORDS || n in STRENGTH_UNITS || n in DOSE_UNITS || n in MONTHS) return false
            if (day(i) != null) return false
            return !(first && n.length < 2)
        }

        private fun sameName(a: String, b: String): Boolean {
            val x = normalize(a)
            val y = normalize(b)
            val (short, long) = listOf(x.split(' '), y.split(' ')).sortedBy { it.size }
            return long.take(short.size) == short
        }

        private fun capitalize(name: String) = name.replaceFirstChar { it.uppercase() }
    }
}

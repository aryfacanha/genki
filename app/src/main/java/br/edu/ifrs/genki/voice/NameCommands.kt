package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.MedicationDraft

/**
 * Etapa "Confira os remédios" da voz (DEC-046, pedido do autor depois do teste no A31): antes das doses e horários,
 * a pessoa confere a lista de nomes entendidos e corrige por toque ou por voz. Kotlin puro, só regras.
 *
 * Comandos por voz: "remover X" (também tirar, apagar, excluir), "trocar X por Y" (também mudar, corrigir, alterar,
 * substituir; "para" no lugar de "por"), "não é X, é Y", "separar X" e "pronto"/"sim"/"está certo" para seguir.
 * Qualquer outra fala volta para o [VoiceParser] (um nome novo vira mais um remédio).
 */
object NameCommands {

    sealed interface Command {
        data object Confirm : Command
        /**
         * Tira [localIds] e, de nomes que juntaram dois remédios, só a parte dita ([partial]: rascunho → nome que sobra).
         * Tudo resolvido antes de aplicar: se uma parte não for encontrada, nada muda (RV-INF-05).
         */
        data class Remove(val localIds: List<String>, val partial: Map<String, String> = emptyMap()) : Command
        /** O primeiro nome troca o do rascunho; os outros viram rascunhos novos com as mesmas informações. */
        data class Rename(val localId: String, val names: List<String>) : Command
        data class NotFound(val spoken: String) : Command
        /** Mais de um remédio combina com o nome dito: nenhum é alterado (RV-INF-03). */
        data class Ambiguous(val spoken: String) : Command
        /** Não é comando: [text] vai para o [VoiceParser] (mais remédios ou informações). */
        data class Other(val text: String) : Command
    }

    /** Fala só de confirmação: todas as palavras daqui ("sim", "isso mesmo", "está tudo certo", "pode seguir"). */
    private val CONFIRM_WORDS = setOf(
        "sim", "isso", "certo", "correto", "pronto", "ok", "continuar", "continua", "confirmar", "confirmo", "seguir",
        "esta", "ta", "tudo", "mesmo", "pode", "exato", "exatamente", "perfeito",
    )
    private val CONFIRM_CORE = setOf("sim", "isso", "certo", "correto", "pronto", "ok", "continuar", "continua", "confirmar", "confirmo", "seguir", "exato", "exatamente", "perfeito")
    /** "Sim, mas falta atenolol": a confirmação do começo e estas ligações saem, e o resto é tratado (RV-INF-01). */
    private val LEAD_WORDS = CONFIRM_WORDS + setOf("mas", "porem", "so", "que", "e", "falta", "faltou", "faltam", "tambem", "ainda", "mais", "inclui", "incluir", "adiciona", "adicionar", "acrescenta", "acrescentar", "coloca", "colocar")
    private const val ARTICLE = """(?:(?:o|a|os|as)\s+)?(?:(?:rem[eé]dio|medicamento)s?\s+)?"""
    private val REMOVE = Regex("""^(?:remover|remove|remova|tirar|tira|tire|apagar|apaga|apague|excluir|exclui|exclua|deletar|deleta)\s+$ARTICLE(.+)$""", RegexOption.IGNORE_CASE)
    private val RENAME = Regex("""^(?:trocar|troca|troque|mudar|muda|mude|corrigir|corrige|corrija|alterar|altera|altere|substituir|substitui|substitua)\s+$ARTICLE(.+?)\s+(?:por|para|pra)\s+(.+)$""", RegexOption.IGNORE_CASE)
    /** "Não é X, é Y": o segundo "é" tem acento; "e" sem acento é conjunção ("não é A e B") e não troca nada (RV-INF-02). */
    private val NOT_IS = Regex("""^n[aã]o\s+[ée]\s+(.+?)(?:\s*,\s*|\s+)é\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val SPLIT = Regex("""^(?:separar|separa|separe|dividir|divide|divida)\s+$ARTICLE(.+)$""", RegexOption.IGNORE_CASE)
    /** Palavras que ligam partes de um nome só ("carbonato de cálcio"): esse nome não é separado. */
    private val CONNECTORS = setOf("de", "do", "da", "dos", "das")

    fun parse(text: String, drafts: List<MedicationDraft>): Command {
        val clean = text.trim().trimEnd('.', '!', '?').trim()
        val tokens = VoiceParser.normalize(clean).replace(Regex("""[.,!?]"""), " ").trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isNotEmpty() && tokens.all { it in CONFIRM_WORDS } && tokens.any { it in CONFIRM_CORE || it == "certo" }) {
            return Command.Confirm
        }
        // Confirmação com ressalva: trata só o que vem depois, sem seguir para as perguntas.
        if (tokens.firstOrNull() in CONFIRM_CORE) {
            val rest = dropLead(clean)
            if (rest.isNotEmpty() && rest != clean) return parse(rest, drafts)
        }

        RENAME.find(clean)?.let { m -> return rename(m.groupValues[1], m.groupValues[2], drafts) }
        NOT_IS.find(clean)?.let { m -> return rename(m.groupValues[1], m.groupValues[2], drafts) }
        SPLIT.find(clean)?.let { m ->
            val draft = resolve(drafts, m.groupValues[1]).let { it as? Match.One }?.draft
                ?: return unresolved(drafts, m.groupValues[1])
            return Command.Rename(draft.localId, split(draft.name.orEmpty()))
        }
        REMOVE.find(clean)?.let { m -> return remove(splitNames(m.groupValues[1]), drafts) }
        return Command.Other(clean)
    }

    /** Tira do começo as palavras de confirmação e de ligação ("sim, mas falta"), mantendo o resto como foi dito. */
    private fun dropLead(clean: String): String {
        val words = clean.split(Regex("""[\s,]+""")).filter { it.isNotEmpty() }
        val first = words.indexOfFirst { VoiceParser.normalize(it).trim('.', '!', '?') !in LEAD_WORDS }
        return if (first < 0) "" else words.drop(first).joinToString(" ")
    }

    private sealed interface Match {
        data class One(val draft: MedicationDraft) : Match
        data object Many : Match
        data object Zero : Match
    }

    private fun resolve(drafts: List<MedicationDraft>, spoken: String): Match {
        val found = matches(drafts, spoken)
        return when (found.size) {
            0 -> Match.Zero
            1 -> Match.One(found.single())
            else -> Match.Many
        }
    }

    private fun unresolved(drafts: List<MedicationDraft>, spoken: String): Command =
        if (resolve(drafts, spoken) == Match.Many) Command.Ambiguous(capitalize(spoken.trim())) else Command.NotFound(capitalize(spoken.trim()))

    /**
     * "remover A e B": cada nome é um remédio inteiro ou parte de um nome que juntou dois ("Rivaroxabana metoprolol").
     * Resolve tudo antes; com algum nome não encontrado ou ambíguo, nada muda.
     */
    private fun remove(spoken: List<String>, drafts: List<MedicationDraft>): Command {
        val removed = linkedSetOf<String>()
        val remaining = mutableMapOf<String, List<String>>()
        for (name in spoken) {
            when (val match = resolve(drafts, name)) {
                is Match.One -> removed += match.draft.localId
                Match.Many -> return Command.Ambiguous(name)
                Match.Zero -> {
                    val parts = drafts.filter { d ->
                        d.name != null && words(d.name).size > 1 && words(d.name).any { similar(it, name) }
                    }
                    when (parts.size) {
                        0 -> return Command.NotFound(name)
                        1 -> {
                            val draft = parts.single()
                            val current = remaining[draft.localId] ?: words(draft.name!!)
                            remaining[draft.localId] = current.filterNot { similar(it, name) }
                        }
                        else -> return Command.Ambiguous(name)
                    }
                }
            }
        }
        val partial = mutableMapOf<String, String>()
        remaining.forEach { (id, rest) ->
            if (id in removed) return@forEach
            if (rest.isEmpty()) removed += id else partial[id] = capitalize(rest.joinToString(" "))
        }
        return Command.Remove(removed.toList(), partial)
    }

    private fun rename(old: String, new: String, drafts: List<MedicationDraft>): Command {
        val draft = (resolve(drafts, old) as? Match.One)?.draft ?: return unresolved(drafts, old)
        val names = splitNames(new)
        return if (names.isEmpty()) Command.Other(new) else Command.Rename(draft.localId, names)
    }

    /** "Rivaroxabana, metoprolol e sinvastatina" → três nomes; "o nome é X" e pontuação saem. */
    fun splitNames(text: String): List<String> =
        text.replace(Regex("""^\s*(?:o\s+)?nome\s+(?:certo\s+)?[ée]\s+""", RegexOption.IGNORE_CASE), "")
            .split(Regex("""\s*,\s*|\s+e\s+""", RegexOption.IGNORE_CASE))
            .map { it.trim().trimEnd('.', '!', '?').trim() }
            .filter { it.isNotEmpty() }
            .map(::capitalize)

    /** Um nome com várias palavras, separado em um remédio por palavra; nomes com "de" ficam inteiros. */
    fun split(name: String): List<String> {
        val parts = words(name)
        return if (parts.size < 2 || parts.any { VoiceParser.normalize(it) in CONNECTORS }) listOf(name) else parts.map(::capitalize)
    }

    /** Mostrar "Separar": nome de várias palavras sem "de"/"do"/"da" (pode ser dois remédios juntos). */
    fun canSplit(name: String?): Boolean = name != null && split(name).size > 1

    /**
     * Rascunho com o nome dito: igual, primeiras palavras iguais ("losartana" acha "Losartana potássica") ou uma letra
     * de diferença (erro do reconhecimento, ex.: simvastatina/sinvastatina). Remédios diferentes podem ter nomes
     * próximos (Prednisona × Prednisolona): mais de uma letra não casa e, com dois parecidos na lista, nenhum é escolhido.
     */
    fun find(drafts: List<MedicationDraft>, spoken: String): MedicationDraft? = matches(drafts, spoken).singleOrNull()

    /** Candidatos para o nome dito: o igual, se houver; senão todos os parecidos (vários = ambíguo). */
    private fun matches(drafts: List<MedicationDraft>, spoken: String): List<MedicationDraft> {
        val s = VoiceParser.normalize(spoken).trim()
        if (s.isEmpty()) return emptyList()
        val named = drafts.filter { it.name != null }
        named.filter { VoiceParser.normalize(it.name!!) == s }.takeIf { it.isNotEmpty() }?.let { return it }
        return named.filter { similar(it.name!!, spoken) }
    }

    private fun similar(a: String, b: String): Boolean {
        val x = VoiceParser.normalize(a).trim()
        val y = VoiceParser.normalize(b).trim()
        if (x == y) return true
        val (short, long) = listOf(words(x), words(y)).sortedBy { it.size }
        if (short.size < long.size && long.take(short.size) == short) return true
        return minOf(x.length, y.length) >= 5 && distance(x, y) <= 1
    }

    private fun distance(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val cur = IntArray(b.length + 1)
            cur[0] = i
            for (j in 1..b.length) {
                cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
            }
            prev = cur
        }
        return prev[b.length]
    }

    private fun words(name: String) = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    private fun capitalize(name: String) = name.replaceFirstChar { it.uppercase() }

    /**
     * Frase falada da lista: "Entendi 3 remédios: A, B e C. Se estiver certo, toque em Continuar." Instrui o toque em
     * vez de perguntar por voz (bateria do autor, 30/10).
     */
    fun summary(drafts: List<MedicationDraft>, changed: Boolean = false): String {
        val names = drafts.map { it.name ?: "um sem nome" }
        val list = if (names.size == 1) names.single() else names.dropLast(1).joinToString(", ") + " e " + names.last()
        val count = if (drafts.size == 1) "1 remédio" else "${drafts.size} remédios"
        val start = when {
            !changed -> "Entendi"
            drafts.size == 1 -> "Agora é"
            else -> "Agora são"
        }
        return "$start $count: $list. Se estiver certo, toque em Continuar."
    }
}

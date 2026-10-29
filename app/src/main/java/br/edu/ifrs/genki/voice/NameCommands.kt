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
        data class Remove(val localIds: List<String>) : Command
        /** O primeiro nome troca o do rascunho; os outros viram rascunhos novos com as mesmas informações. */
        data class Rename(val localId: String, val names: List<String>) : Command
        data class NotFound(val spoken: String) : Command
        data object None : Command
    }

    private val CONFIRM_FIRST = setOf("sim", "isso", "certo", "correto", "pronto", "ok", "continuar", "continua", "confirmar", "confirmo", "seguir")
    private val CONFIRM_PHRASES = setOf("esta certo", "ta certo", "esta correto", "tudo certo", "esta tudo certo", "pode seguir", "pode continuar")
    private const val ARTICLE = """(?:(?:o|a|os|as)\s+)?(?:(?:rem[eé]dio|medicamento)s?\s+)?"""
    private val REMOVE = Regex("""^(?:remover|remove|remova|tirar|tira|tire|apagar|apaga|apague|excluir|exclui|exclua|deletar|deleta)\s+$ARTICLE(.+)$""", RegexOption.IGNORE_CASE)
    private val RENAME = Regex("""^(?:trocar|troca|troque|mudar|muda|mude|corrigir|corrige|corrija|alterar|altera|altere|substituir|substitui|substitua)\s+$ARTICLE(.+?)\s+(?:por|para|pra)\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val NOT_IS = Regex("""^n[aã]o\s+[ée]\s+(.+?),?\s+[ée]\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val SPLIT = Regex("""^(?:separar|separa|separe|dividir|divide|divida)\s+$ARTICLE(.+)$""", RegexOption.IGNORE_CASE)
    /** Palavras que ligam partes de um nome só ("carbonato de cálcio"): esse nome não é separado. */
    private val CONNECTORS = setOf("de", "do", "da", "dos", "das")

    fun parse(text: String, drafts: List<MedicationDraft>): Command {
        val clean = text.trim().trimEnd('.', '!', '?').trim()
        val norm = VoiceParser.normalize(clean).replace(Regex("""[.,!?]"""), "").trim()
        if (norm in CONFIRM_PHRASES || norm.split(Regex("\\s+")).firstOrNull() in CONFIRM_FIRST) return Command.Confirm

        RENAME.find(clean)?.let { m -> return rename(m.groupValues[1], m.groupValues[2], drafts) }
        NOT_IS.find(clean)?.let { m -> return rename(m.groupValues[1], m.groupValues[2], drafts) }
        SPLIT.find(clean)?.let { m ->
            val draft = find(drafts, m.groupValues[1]) ?: return Command.NotFound(m.groupValues[1])
            return Command.Rename(draft.localId, split(draft.name.orEmpty()))
        }
        REMOVE.find(clean)?.let { m ->
            val spoken = splitNames(m.groupValues[1])
            val removed = mutableListOf<String>()
            spoken.forEach { name ->
                val draft = find(drafts, name)
                if (draft != null) {
                    removed += draft.localId
                } else {
                    // Parte de um nome que juntou dois remédios ("Rivaroxabana Metoprolol"): tira só a parte dita.
                    val partOf = drafts.firstOrNull { d -> d.name != null && words(d.name).any { similar(it, name) } && words(d.name).size > 1 }
                        ?: return Command.NotFound(name)
                    val rest = words(partOf.name!!).filterNot { similar(it, name) }.joinToString(" ")
                    return Command.Rename(partOf.localId, listOf(capitalize(rest)))
                }
            }
            return Command.Remove(removed)
        }
        return Command.None
    }

    private fun rename(old: String, new: String, drafts: List<MedicationDraft>): Command {
        val draft = find(drafts, old) ?: return Command.NotFound(old)
        val names = splitNames(new)
        return if (names.isEmpty()) Command.None else Command.Rename(draft.localId, names)
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

    /** Rascunho com o nome dito: igual, começo igual ou parecido (erro do reconhecimento, ex.: simvastatina/sinvastatina). */
    fun find(drafts: List<MedicationDraft>, spoken: String): MedicationDraft? {
        val s = VoiceParser.normalize(spoken).trim()
        if (s.isEmpty()) return null
        val named = drafts.filter { it.name != null }
        return named.firstOrNull { VoiceParser.normalize(it.name!!) == s }
            ?: named.firstOrNull { similar(it.name!!, spoken) }
    }

    private fun similar(a: String, b: String): Boolean {
        val x = VoiceParser.normalize(a).trim()
        val y = VoiceParser.normalize(b).trim()
        if (x == y) return true
        if (minOf(x.length, y.length) >= 4 && (x.startsWith(y) || y.startsWith(x))) return true
        return distance(x, y) <= maxOf(1, minOf(x.length, y.length) / 5)
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

    /** Frase falada da lista: "Entendi 3 remédios: A, B e C. Está certo?". */
    fun summary(drafts: List<MedicationDraft>, changed: Boolean = false): String {
        val names = drafts.map { it.name ?: "um sem nome" }
        val list = if (names.size == 1) names.single() else names.dropLast(1).joinToString(", ") + " e " + names.last()
        val count = if (drafts.size == 1) "1 remédio" else "${drafts.size} remédios"
        val start = when {
            !changed -> "Entendi"
            drafts.size == 1 -> "Agora é"
            else -> "Agora são"
        }
        return "$start $count: $list. Está certo?"
    }
}

package br.edu.ifrs.genki.camera

import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.MedicationDraft

/** Linha lida pelo ML Kit, com a altura do retângulo (texto maior = mais destaque na embalagem). */
data class OcrLine(val text: String, val height: Int)

/**
 * Sugestões da embalagem (ESPEC §8, RF20–RF23). Só nome e concentração: dose, horários, dias e tratamento
 * ficam vazios sempre (RF23); o usuário confere e completa por voz ou digitando.
 */
object OcrParser {

    private val STRENGTH = Regex("""(\d+(?:[.,]\d+)?)\s?(mg|mcg|g|ml|ui)\b""", RegexOption.IGNORE_CASE)

    fun parse(lines: List<OcrLine>): MedicationDraft = MedicationDraft(
        source = DraftSource.CAMERA,
        name = name(lines),
        concentration = strength(lines),
    )

    /** Nome = a linha mais longa sem algarismos entre as 3 maiores. */
    fun name(lines: List<OcrLine>): String? = lines
        .sortedByDescending { it.height }
        .take(3)
        .map { clean(it.text) }
        .filter { text -> text.none(Char::isDigit) && text.count(Char::isLetter) >= 3 }
        .maxByOrNull { it.length }
        ?.let(::titleCase)

    /** Concentração = primeira ocorrência de número + mg/mcg/g/ml/UI, na ordem de leitura. */
    fun strength(lines: List<OcrLine>): String? {
        val match = lines.firstNotNullOfOrNull { STRENGTH.find(it.text) } ?: return null
        val (number, unit) = match.destructured
        val shortUnit = if (unit.equals("ui", ignoreCase = true)) "UI" else unit.lowercase()
        return "${number.replace('.', ',')} $shortUnit"
    }

    private fun clean(text: String) = text.trim().replace(Regex("""\s+"""), " ").trim { !it.isLetterOrDigit() }

    /** "LOSARTANA POTÁSSICA" → "Losartana potássica"; nomes já em caixa mista ficam como estão. */
    private fun titleCase(text: String): String =
        if (text.any(Char::isLowerCase)) text else text.lowercase().replaceFirstChar { it.uppercase() }
}

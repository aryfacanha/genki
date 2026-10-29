package br.edu.ifrs.genki.domain

import java.text.Normalizer

/**
 * Nome igual a um já cadastrado (achado 2 da pré-revisão de 29/10): dois cadastros iguais geram lembretes repetidos e
 * podem levar a tomar duas vezes. O app pergunta antes de gravar; não impede (pode ser o mesmo remédio em outro horário).
 */
object DuplicateNames {

    fun normalize(name: String): String =
        Normalizer.normalize(name.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("\\s+"), " ").trim()

    /** Nomes de [names] que já existem em [existing], sem diferença de acento, maiúsculas ou espaços; sem repetir. */
    fun find(names: List<String>, existing: List<String>): List<String> {
        val taken = existing.map(::normalize).toSet()
        return names.filter { normalize(it) in taken }.distinctBy(::normalize)
    }

    /** "Você já tem Losartana cadastrada…" para um ou "Losartana e Atenolol" para vários. */
    fun list(names: List<String>): String =
        if (names.size <= 1) names.joinToString() else names.dropLast(1).joinToString(", ") + " e " + names.last()
}

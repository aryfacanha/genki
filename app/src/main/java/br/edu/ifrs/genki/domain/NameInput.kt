package br.edu.ifrs.genki.domain

/**
 * Nome do medicamento digitado ou colado (RV-125-04): o campo tem até três linhas na tela, mas o nome é uma linha só.
 * Quebra de linha colada vira espaço, sem espaços duplos nas junções.
 */
object NameInput {
    fun singleLine(text: String): String = text.replace(Regex("[ \\t]*[\\r\\n]+[ \\t]*"), " ")
}

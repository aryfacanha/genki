package br.edu.ifrs.genki.ui.intro

/**
 * Divide os blocos da introdução em páginas pelo tamanho que ocupam (pedido do autor, 03/11: com fonte 200% a
 * introdução tinha de rolar muito). Cada página leva, na ordem, os blocos que cabem em [available] sem rolar; um bloco
 * que sozinho passa da altura fica numa página própria (e rola). O resultado são os índices dos blocos de cada página.
 */
object IntroPagination {
    fun pack(heights: List<Int>, available: Int): List<List<Int>> {
        val pages = mutableListOf<MutableList<Int>>()
        var used = 0
        heights.forEachIndexed { index, height ->
            if (pages.isEmpty() || used + height > available) {
                pages += mutableListOf(index)
                used = height
            } else {
                pages.last() += index
                used += height
            }
        }
        return pages
    }
}

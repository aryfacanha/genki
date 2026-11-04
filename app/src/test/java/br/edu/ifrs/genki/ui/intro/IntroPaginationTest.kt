package br.edu.ifrs.genki.ui.intro

import org.junit.Assert.assertEquals
import org.junit.Test

class IntroPaginationTest {
    @Test
    fun everythingFitsInOnePage() =
        assertEquals(listOf(listOf(0, 1, 2, 3)), IntroPagination.pack(listOf(100, 200, 150, 50), 600))

    @Test
    fun smallFontKeepsTwoPages() =
        assertEquals(listOf(listOf(0, 1), listOf(2, 3)), IntroPagination.pack(listOf(300, 250, 300, 250), 600))

    @Test
    fun largeFontSplitsIntoFourPages() =
        assertEquals(
            listOf(listOf(0), listOf(1), listOf(2), listOf(3)),
            IntroPagination.pack(listOf(500, 450, 600, 520), 700),
        )

    @Test
    fun blockTallerThanThePageStaysAlone() =
        assertEquals(listOf(listOf(0), listOf(1), listOf(2)), IntroPagination.pack(listOf(100, 900, 100), 500))

    @Test
    fun orderIsKeptAndEmptyGivesNoPages() {
        assertEquals(emptyList<List<Int>>(), IntroPagination.pack(emptyList(), 500))
        assertEquals(listOf(listOf(0, 1), listOf(2)), IntroPagination.pack(listOf(200, 200, 200), 400))
    }
}

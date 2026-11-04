package br.edu.ifrs.genki.ui

import br.edu.ifrs.genki.ui.navigation.NavLabelSize
import org.junit.Assert.assertEquals
import org.junit.Test

class NavLabelSizeTest {
    @Test
    fun keepsTheMaximumWhenItFits() = assertEquals(12f, NavLabelSize.fit(12f, 90f, 100f), 0.001f)

    @Test
    fun shrinksInProportionWhenItDoesNot() = assertEquals(6f.coerceAtLeast(NavLabelSize.MIN_SP), NavLabelSize.fit(12f, 200f, 100f), 0.001f)

    @Test
    fun shrinksOnlyAsMuchAsNeeded() = assertEquals(10f, NavLabelSize.fit(12f, 120f, 100f), 0.001f)

    @Test
    fun neverGoesBelowTheFloor() = assertEquals(NavLabelSize.MIN_SP, NavLabelSize.fit(12f, 1000f, 100f), 0.001f)
}

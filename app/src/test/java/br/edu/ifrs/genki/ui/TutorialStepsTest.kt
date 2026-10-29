package br.edu.ifrs.genki.ui

import androidx.compose.ui.geometry.Rect
import br.edu.ifrs.genki.ui.tutorial.TutorialController
import br.edu.ifrs.genki.ui.tutorial.TutorialScreen
import br.edu.ifrs.genki.ui.tutorial.TutorialSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialStepsTest {

    @Test
    fun addScreenStepsComeTogetherBetweenHomeSteps() {
        val screens = TutorialSteps.all.map { it.screen }
        assertEquals(TutorialScreen.HOME, screens.first())
        assertEquals(TutorialScreen.HOME, screens.last())
        // Uma ida e uma volta só: os passos da tela "Adicionar" são seguidos.
        val add = screens.indices.filter { screens[it] == TutorialScreen.ADD }
        assertEquals(3, add.size)
        assertEquals((add.first()..add.last()).toList(), add)
    }

    @Test
    fun everyTargetAppearsOnce() {
        val targets = TutorialSteps.all.map { it.target }
        assertEquals(targets.size, targets.toSet().size)
    }

    @Test
    fun balloonGoesOppositeToTarget() {
        assertTrue(TutorialSteps.balloonAbove(Rect(0f, 1800f, 1080f, 1900f), 2400f))
        assertFalse(TutorialSteps.balloonAbove(Rect(0f, 300f, 1080f, 700f), 2400f))
    }

    @Test
    fun controllerWalksAndCloses() {
        val controller = TutorialController()
        controller.start()
        assertEquals(0, controller.step)
        controller.back()
        assertNull(controller.step)
        controller.start()
        repeat(TutorialSteps.all.size) { controller.next() }
        assertNull(controller.step)
    }
}

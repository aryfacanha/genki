package br.edu.ifrs.genki.voice

import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.RegistrationSession
import br.edu.ifrs.genki.voice.ui.VoicePhase
import br.edu.ifrs.genki.voice.ui.VoiceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/** Fluxo da voz com a conferência dos nomes (DEC-046), com o texto que o reconhecedor entregaria. */
@OptIn(ExperimentalCoroutinesApi::class)
class VoiceNamesFlowTest {

    private val session = RegistrationSession()
    private lateinit var vm: VoiceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        vm = VoiceViewModel({ 1L }, session, today = { LocalDate.of(2026, 10, 29) })
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val names get() = vm.state.value.drafts.map { it.name }

    @Test
    fun everythingAtOnceStopsAtTheNameListFirst() {
        vm.heard("losartana 50 mg 1 comprimido todos os dias às 8 uso contínuo e metformina", null)
        assertEquals(VoicePhase.NAMES, vm.state.value.phase)
        assertEquals(listOf("Losartana", "Metformina"), names)
        assertEquals("Entendi 2 remédios: Losartana e Metformina. Está certo?", vm.state.value.prompt?.text)
        // Confirmado, segue para o que falta (Losartana completa; Metformina sem dose).
        vm.heard("pronto", null)
        assertEquals(VoicePhase.ASKING, vm.state.value.phase)
        assertEquals("Metformina", vm.state.value.drafts.first { it.localId == vm.state.value.question?.draftId }.name)
        assertEquals(Field.DOSE, vm.state.value.question?.field)
    }

    @Test
    fun voiceCommandsFixTheList() {
        vm.heard("rivaroxabana metoprolol e simvastatina", null)
        assertEquals(listOf("Rivaroxabana metoprolol", "Simvastatina"), names)
        vm.heard("separar rivaroxabana metoprolol", null)
        assertEquals(listOf("Rivaroxabana", "Metoprolol", "Simvastatina"), names)
        vm.heard("trocar simvastatina por sinvastatina", null)
        assertEquals(listOf("Rivaroxabana", "Metoprolol", "Sinvastatina"), names)
        vm.heard("remover metoprolol", null)
        assertEquals(listOf("Rivaroxabana", "Sinvastatina"), names)
        // Nome novo dito na conferência entra na lista.
        vm.heard("atenolol", null)
        assertEquals(listOf("Rivaroxabana", "Sinvastatina", "Atenolol"), names)
        assertEquals(VoicePhase.NAMES, vm.state.value.phase)
    }

    @Test
    fun tapNameThenSayOrTypeTheRightOne() {
        vm.heard("rivaroxabana metoprolol", null)
        val id = vm.state.value.drafts.single().localId
        vm.beginRename(id)
        assertEquals(id, vm.state.value.renaming)
        // A fala seguinte é o nome certo; "e" separa em dois, com as mesmas informações.
        vm.heard("rivaroxabana e metoprolol", null)
        assertEquals(listOf("Rivaroxabana", "Metoprolol"), names)
        assertNull(vm.state.value.renaming)
        vm.typedName(vm.state.value.drafts[1].localId, "Atenolol")
        assertEquals(listOf("Rivaroxabana", "Atenolol"), names)
    }

    @Test
    fun splitAndRemoveByTouch() {
        vm.heard("rivaroxabana metoprolol", null)
        vm.splitName(vm.state.value.drafts.single().localId)
        assertEquals(listOf("Rivaroxabana", "Metoprolol"), names)
        vm.removeName(vm.state.value.drafts[0].localId)
        assertEquals(listOf("Metoprolol"), names)
        vm.removeName(vm.state.value.drafts[0].localId)
        assertEquals(VoicePhase.INTRO, vm.state.value.phase)
    }

    @Test
    fun confirmationWithMoreKeepsTheList() {
        vm.heard("losartana e metformina", null)
        vm.heard("sim, mas falta atenolol", null)
        assertEquals(VoicePhase.NAMES, vm.state.value.phase)
        assertEquals(listOf("Losartana", "Metformina", "Atenolol"), names)
    }

    @Test
    fun compositeRemovalInTheFlow() {
        vm.heard("rivaroxabana metoprolol e sinvastatina", null)
        vm.heard("remover sinvastatina e metoprolol", null)
        assertEquals(listOf("Rivaroxabana"), names)
    }

    @Test
    fun leavingRenameDropsThePendingQuestion() {
        // RV-INF-04: sair da troca invalida a pergunta, que não pode mais abrir o microfone.
        vm.heard("losartana", null)
        vm.beginRename(vm.state.value.drafts.single().localId)
        vm.cancelRename()
        assertNull(vm.state.value.renaming)
        assertNull(vm.state.value.prompt)
    }
}

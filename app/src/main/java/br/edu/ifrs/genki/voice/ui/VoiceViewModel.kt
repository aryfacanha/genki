package br.edu.ifrs.genki.voice.ui

import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.GenkiEval
import br.edu.ifrs.genki.data.MedicationRepository
import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.RegistrationSession
import br.edu.ifrs.genki.ui.genkiContainer
import br.edu.ifrs.genki.voice.VoiceParser
import br.edu.ifrs.genki.voice.VoiceQuestion
import br.edu.ifrs.genki.voice.VoiceQuestions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class VoicePhase { INTRO, ASKING, REVIEW }

/**
 * Texto para falar; o [id] faz a mesma frase ser falada de novo. [listen]: ao terminar de falar, o microfone abre
 * sozinho (DEC-040, para não tocar no microfone a cada pergunta).
 */
data class Prompt(val id: Int, val text: String, val listen: Boolean = false)

data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.INTRO,
    val drafts: List<MedicationDraft> = emptyList(),
    val question: VoiceQuestion? = null,
    val listening: Boolean = false,
    val partial: String = "",
    /** Última fala reconhecida, mostrada como "Você disse". */
    val heard: String? = null,
    /** "Não entendi", falta de microfone etc. */
    val message: String? = null,
    val prompt: Prompt? = null,
    val savedMessage: String? = null,
    /** "Corrigir por voz" (revisão ou perguntas) já aplicado, esperando "Manter correção" ou "Desfazer". */
    val correctionPending: Boolean = false,
    /** "Algo errado? Corrigir por voz" tocado durante as perguntas: a próxima fala corrige o medicamento da pergunta. */
    val correcting: Boolean = false,
)

/**
 * Cadastro por voz (RF11–RF19): fala → [VoiceParser] → pergunta o primeiro campo que falta → revisão comum.
 * O reconhecedor e a fala ficam na tela ([VoiceScreen]); aqui só o fluxo. Toda gravação passa pelo validador.
 */
class VoiceViewModel(
    private val repository: MedicationRepository,
    private val session: RegistrationSession,
    /** Veio da câmera: começa pelos rascunhos da sessão e pergunta o que falta (ESPEC §8). */
    fromSession: Boolean = false,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()
    private var promptId = 0
    /** Escutas automáticas seguidas que não deram resposta entendida; depois de uma, espera o toque no microfone. */
    private var misses = 0
    private var saving = false
    /** Rascunhos antes da correção por voz feita na revisão; "Desfazer" (ou voltar) os restaura. */
    private var beforeCorrection: List<MedicationDraft>? = null

    init {
        if (!fromSession) {
            session.clear()
            GenkiEval.log("cadastro_iniciado", "fonte" to DraftSource.VOICE)
        }
        // Volta da correção por toque: a sessão muda por fora.
        viewModelScope.launch {
            session.drafts.collect { drafts -> _state.update { it.copy(drafts = drafts) } }
        }
        if (fromSession && session.drafts.value.isNotEmpty()) advance()
    }

    private fun prompt(text: String, listen: Boolean = false) = Prompt(++promptId, text, listen)

    fun listening(active: Boolean) {
        if (active) GenkiEval.log("voz_ouvindo", "pergunta" to _state.value.question?.field)
        _state.update { it.copy(listening = active, partial = "", message = if (active) null else it.message) }
    }

    fun partial(text: String) = _state.update { it.copy(partial = text) }

    /** Resposta por toque ou digitada: mesmo interpretador, sem "Você disse". */
    fun tapped(answer: String) = interpret(answer, spoken = false)

    /** "Algo errado? Corrigir por voz" durante as perguntas (pedido do autor, 27/10). */
    fun beginCorrection() {
        GenkiEval.log("voz_correcao_iniciada", "pergunta" to _state.value.question?.field)
        _state.update { it.copy(correcting = true, message = null, prompt = prompt("Diga a correção.")) }
    }

    fun cancelCorrection() = _state.update { it.copy(correcting = false, message = null) }

    /** Resultado do reconhecedor. */
    fun heard(text: String, latencyMs: Long?) {
        GenkiEval.log("voz_resultado", "texto" to "\"$text\"", "latencia_ms" to latencyMs, "pergunta" to _state.value.question?.field)
        interpret(text, spoken = true)
    }

    private fun interpret(text: String, spoken: Boolean) {
        val s = _state.value
        val heard = if (spoken) text else null
        // Corrigindo durante as perguntas: tudo vale para o medicamento da pergunta e um nome dito troca o nome dele.
        val expecting = if (s.correcting) Field.NAME else s.question?.field
        val result = VoiceParser.parse(text, session.drafts.value, today(), s.question?.draftId, expecting)
        GenkiEval.log("voz_interpretado", "entendeu" to result.understood, "rascunhos" to result.drafts.map(::summary))
        if (!result.understood) {
            val retry = "Não entendi. " + (s.question?.let { "${it.text} ${it.example}." } ?: "Diga o nome do medicamento e como você toma.")
            // Ouve de novo sozinho uma vez só, para ruído não virar um ciclo de perguntas.
            val again = s.question != null && misses++ < 1
            _state.update { it.copy(heard = heard, partial = "", message = "Não entendi. Tente de novo.", prompt = prompt(retry, again)) }
            return
        }
        misses = 0
        val confirmation = VoiceQuestions.confirmation(session.drafts.value, result.drafts)
        if (s.phase == VoicePhase.REVIEW || s.correcting) {
            // Correção (revisão ou perguntas): mostra o resultado e pede confirmação antes de seguir.
            if (beforeCorrection == null) beforeCorrection = session.drafts.value
            session.set(result.drafts)
            _state.update {
                it.copy(
                    heard = heard, partial = "", message = null, correctionPending = true, correcting = false,
                    prompt = prompt(listOfNotNull(confirmation, "Confira a correção. Toque em Manter correção ou em Desfazer.").joinToString(" ")),
                )
            }
            return
        }
        session.set(result.drafts)
        _state.update { it.copy(heard = heard, partial = "", message = null) }
        advance(confirmation)
    }

    /** "Manter correção": segue; se faltar algo, pergunta. */
    fun keepCorrection() {
        GenkiEval.log("voz_correcao_mantida")
        beforeCorrection = null
        _state.update { it.copy(correctionPending = false) }
        advance()
    }

    /** "Desfazer" ou voltar durante a correção: a tela volta como estava (revisão ou a mesma pergunta). */
    fun undoCorrection() {
        GenkiEval.log("voz_correcao_desfeita")
        beforeCorrection?.let(session::set)
        beforeCorrection = null
        _state.update { it.copy(correctionPending = false, correcting = false, heard = null) }
    }

    /** Próxima pergunta ou a revisão, depois do que foi anotado ([confirmation], falado antes). */
    private fun advance(confirmation: String? = null) {
        val question = VoiceQuestions.next(session.drafts.value, today())
        val say = { text: String -> listOfNotNull(confirmation, text).joinToString(" ") }
        _state.update {
            when {
                session.drafts.value.isEmpty() -> it.copy(phase = VoicePhase.INTRO, question = null)
                question == null -> it.copy(phase = VoicePhase.REVIEW, question = null, prompt = prompt(say("Confira antes de salvar.")))
                else -> it.copy(phase = VoicePhase.ASKING, question = question, prompt = prompt(say(question.text), listen = true))
            }
        }
    }

    fun recognitionError(code: Int) {
        GenkiEval.log("voz_erro", "codigo" to code)
        val message = when (code) {
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "Não ouvi nada. Toque no microfone e fale de novo."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> MIC_DENIED
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT, SpeechRecognizer.ERROR_SERVER ->
                "O reconhecimento de voz não está disponível agora. Você pode digitar."
            // Sem serviço de reconhecimento escolhido no sistema (ex.: GrapheneOS sem Google), visto no Pixel 8a em 27/10.
            SpeechRecognizer.ERROR_CLIENT -> UNAVAILABLE
            else -> "Não consegui ouvir. Toque no microfone e fale de novo."
        }
        // Silêncio ou erro: não abre o microfone de novo sozinho; espera o toque.
        _state.update { it.copy(message = message, partial = "", prompt = prompt(message)) }
    }

    fun micDenied() = _state.update { it.copy(message = MIC_DENIED) }

    fun unavailable() = _state.update { it.copy(message = UNAVAILABLE) }

    /**
     * "Prefiro digitar" (RF18). `true` = abrir o formulário com o rascunho (zero ou um medicamento);
     * com vários, vai para a revisão, onde cada um tem "Corrigir".
     */
    fun preferTyping(): Boolean {
        GenkiEval.log("voz_prefiro_digitar", "rascunhos" to session.drafts.value.size)
        if (session.drafts.value.size <= 1) return true
        _state.update { it.copy(phase = VoicePhase.REVIEW, question = null) }
        return false
    }

    /**
     * Dose escolhida no menu e no − número + da pergunta da dose (bateria do autor no A31, 28/10): vai para o rascunho
     * como o formulário faria, sem passar pelo interpretador; a gravação continua pelo validador.
     */
    fun chooseDose(dose: String) {
        val id = _state.value.question?.draftId ?: return
        val before = session.drafts.value
        session.set(before.map { if (it.localId == id) it.copy(dose = dose) else it })
        _state.update { it.copy(heard = null, message = null) }
        advance(VoiceQuestions.confirmation(before, session.drafts.value))
    }

    fun remove(localId: String) {
        session.remove(localId)
        advance()
    }

    /** "Confirmar e salvar": todos pelo validador; se faltar algo, volta a perguntar. */
    fun save() {
        if (saving) return
        val day = today()
        val results = session.drafts.value.map { DraftValidator.normalizeAndValidate(it, day) }
        if (results.isEmpty() || results.any { it is DraftValidator.Result.Invalid }) {
            advance()
            return
        }
        saving = true
        viewModelScope.launch {
            results.forEach { valid ->
                val id = repository.insert(valid as DraftValidator.Result.Valid)
                GenkiEval.log("medicamento_salvo", "fonte" to valid.draft.source, "id" to id)
            }
            session.clear()
            val n = results.size
            _state.update { it.copy(savedMessage = if (n == 1) "Medicamento adicionado" else "$n medicamentos adicionados") }
        }
    }

    fun discard() {
        GenkiEval.log("cadastro_descartado", "fonte" to DraftSource.VOICE)
        session.clear()
    }

    private fun summary(d: MedicationDraft) =
        "{nome=${d.name}, conc=${d.concentration}, dose=${d.dose}, dias=${d.days?.size}, horarios=${d.times}, " +
            "cada=${d.everyHours}, trat=${d.treatment}, fim=${d.endDate}, por=${d.durationDays}}"

    companion object {
        private const val UNAVAILABLE = "Este aparelho não tem reconhecimento de voz configurado. Você pode digitar."
        private const val MIC_DENIED ="Sem acesso ao microfone. Você pode permitir nas configurações ou digitar."

        fun factory(fromSession: Boolean) = viewModelFactory {
            initializer {
                val container = genkiContainer()
                VoiceViewModel(container.medicationRepository, container.registrationSession, fromSession = fromSession)
            }
        }
    }
}

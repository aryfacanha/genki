package br.edu.ifrs.genki.voice.ui

import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.ifrs.genki.GenkiEval
import br.edu.ifrs.genki.domain.DraftSource
import br.edu.ifrs.genki.domain.DraftValidator
import br.edu.ifrs.genki.domain.DuplicateNames
import br.edu.ifrs.genki.domain.Field
import br.edu.ifrs.genki.domain.MedicationDraft
import br.edu.ifrs.genki.domain.RegistrationSession
import br.edu.ifrs.genki.ui.genkiContainer
import br.edu.ifrs.genki.voice.NameCommands
import br.edu.ifrs.genki.voice.VoiceParser
import br.edu.ifrs.genki.voice.VoiceQuestion
import br.edu.ifrs.genki.voice.VoiceQuestions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

/** [NAMES]: "Confira os remédios", antes das doses e horários (DEC-046). */
enum class VoicePhase { INTRO, NAMES, ASKING, REVIEW }

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
    /** Na conferência dos nomes, o remédio tocado: a próxima fala (ou o texto digitado) é o nome certo dele. */
    val renaming: String? = null,
    /** Nomes iguais a cadastros existentes: pergunta antes de gravar (achado 2 da pré-revisão, 29/10). */
    val duplicates: List<String> = emptyList(),
    /** O aviso apareceu na conferência dos nomes, não ao salvar (bateria do autor, 30/10). */
    val duplicatesOnNames: Boolean = false,
    /** Entre um trecho e outro da mesma resposta: ainda ouvindo, pode continuar falando. */
    val pausing: Boolean = false,
)

/**
 * Cadastro por voz (RF11–RF19): fala → [VoiceParser] → pergunta o primeiro campo que falta → revisão comum.
 * O reconhecedor e a fala ficam na tela ([VoiceScreen]); aqui só o fluxo. Toda gravação passa pelo validador.
 */
class VoiceViewModel(
    /** Gravação (o repositório no app; nos testes, uma lista). */
    private val insert: suspend (DraftValidator.Result.Valid) -> Long,
    private val session: RegistrationSession,
    /** Nomes já cadastrados, para o aviso de repetido. */
    private val existingNames: suspend () -> List<String> = { emptyList() },
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
    /** Lista de nomes conferida; da câmera o nome já foi conferido lá. */
    private var namesConfirmed = fromSession
    /** Nomes repetidos que a pessoa já aceitou na conferência: não pergunta de novo ao salvar. */
    private var acceptedDuplicates = emptySet<String>()

    init {
        if (!fromSession) {
            session.clear()
            GenkiEval.log("cadastro_iniciado", "fonte" to DraftSource.VOICE)
            // Exemplo falado de como responder (bateria do autor, 30/10); o microfone continua no toque.
            _state.update { it.copy(prompt = prompt(INTRO_PROMPT)) }
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
        _state.update { it.copy(listening = active, pausing = false, partial = "", message = if (active) null else it.message) }
    }

    fun pausing(active: Boolean) = _state.update { it.copy(pausing = active) }

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
        if (s.phase == VoicePhase.NAMES) {
            interpretNames(text, heard)
            return
        }
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

    /** Fala na conferência dos nomes: comando ("remover X", "trocar X por Y"...), nome certo do tocado ou mais remédios. */
    private fun interpretNames(text: String, heard: String?) {
        val drafts = session.drafts.value
        val renaming = _state.value.renaming
        if (renaming != null) {
            val names = NameCommands.splitNames(text)
            if (names.isNotEmpty()) applyRename(renaming, names, heard) else namesMiss(heard)
            return
        }
        val command = NameCommands.parse(text, drafts)
        GenkiEval.log("voz_nomes_comando", "comando" to command::class.simpleName)
        when (command) {
            NameCommands.Command.Confirm -> confirmNames()
            is NameCommands.Command.Remove -> {
                session.set(drafts.mapNotNull { d ->
                    when (d.localId) {
                        in command.localIds -> null
                        in command.partial -> d.copy(name = command.partial.getValue(d.localId))
                        else -> d
                    }
                })
                showNames(heard, changed = true)
            }
            is NameCommands.Command.Rename -> applyRename(command.localId, command.names, heard)
            is NameCommands.Command.NotFound -> _state.update {
                val say = "Não encontrei ${command.spoken} na lista. ${NameCommands.summary(drafts)}"
                it.copy(heard = heard, partial = "", message = "Não encontrei “${command.spoken}” na lista.", prompt = prompt(say, listen = true))
            }
            is NameCommands.Command.Ambiguous -> _state.update {
                val text = "Há mais de um remédio com ${command.spoken}. Toque no nome ou no X do que quer corrigir."
                it.copy(heard = heard, partial = "", message = text, prompt = prompt(text))
            }
            is NameCommands.Command.Other -> {
                // Outro remédio ou mais informações: o mesmo interpretador de sempre.
                val result = VoiceParser.parse(command.text, drafts, today())
                if (!result.understood) return namesMiss(heard)
                session.set(result.drafts)
                showNames(heard, changed = result.drafts.size != drafts.size)
            }
        }
    }

    private fun namesMiss(heard: String?) {
        val again = misses++ < 1
        _state.update {
            it.copy(heard = heard, partial = "", message = "Não entendi. Tente de novo.",
                prompt = prompt("Não entendi. ${NameCommands.summary(session.drafts.value)}", again))
        }
    }

    /** Lista de nomes na tela e falada; vazia volta ao começo. */
    private fun showNames(heard: String? = null, changed: Boolean = false) {
        misses = 0
        val drafts = session.drafts.value
        _state.update {
            if (drafts.isEmpty()) {
                it.copy(phase = VoicePhase.INTRO, heard = heard, partial = "", message = null, renaming = null, question = null)
            } else {
                it.copy(
                    phase = VoicePhase.NAMES, heard = heard, partial = "", message = null, renaming = null, question = null,
                    // Sem pergunta para responder por voz: a pessoa confere e toca em Continuar (bateria do autor, 30/10).
                    prompt = prompt(NameCommands.summary(drafts, changed)),
                )
            }
        }
    }

    /** O primeiro nome troca o do rascunho; os outros viram rascunhos com as mesmas informações (separar dois juntos). */
    private fun applyRename(localId: String, names: List<String>, heard: String? = null) {
        val drafts = session.drafts.value
        val index = drafts.indexOfFirst { it.localId == localId }
        if (index < 0 || names.isEmpty()) return showNames(heard)
        val draft = drafts[index]
        val replaced = listOf(draft.copy(name = names.first())) +
            names.drop(1).map { draft.copy(localId = UUID.randomUUID().toString(), name = it) }
        session.set(drafts.take(index) + replaced + drafts.drop(index + 1))
        showNames(heard, changed = true)
    }

    /** Toque no nome: espera o nome certo, falado ou digitado. */
    fun beginRename(localId: String) {
        val name = session.get(localId)?.name
        _state.update {
            it.copy(renaming = localId, message = null,
                prompt = prompt(if (name != null) "Diga o nome certo de $name." else "Diga o nome do remédio.", listen = true))
        }
    }

    /** Sai da troca do nome: a pergunta pendente deixa de valer e não abre o microfone (RV-INF-04). */
    fun cancelRename() = _state.update { it.copy(renaming = null, prompt = null) }

    /** Nome digitado na conferência; "A, B" ou "A e B" separa em dois. */
    fun typedName(localId: String, text: String) {
        val names = NameCommands.splitNames(text)
        if (names.isEmpty()) cancelRename() else applyRename(localId, names)
    }

    fun splitName(localId: String) {
        val name = session.get(localId)?.name ?: return
        applyRename(localId, NameCommands.split(name))
    }

    fun removeName(localId: String) {
        session.remove(localId)
        showNames(changed = true)
    }

    /** "Continuar" (ou "pronto"/"sim" falado): segue para doses, dias e horários de cada um. */
    fun confirmNames() {
        // Nome repetido avisado já aqui, antes das perguntas (bateria do autor, 30/10); a checagem ao salvar continua.
        viewModelScope.launch {
            val repeated = repeatedNames()
            if (repeated.isNotEmpty()) {
                _state.update { it.copy(duplicates = repeated, duplicatesOnNames = true, renaming = null) }
            } else {
                namesConfirmedNow()
            }
        }
    }

    private suspend fun repeatedNames() = DuplicateNames.find(session.drafts.value.mapNotNull { it.name }, existingNames())
        .filter { DuplicateNames.normalize(it) !in acceptedDuplicates }

    private fun namesConfirmedNow() {
        GenkiEval.log("voz_nomes_confirmados", "rascunhos" to session.drafts.value.size)
        namesConfirmed = true
        _state.update { it.copy(renaming = null, heard = null, message = null) }
        advance()
    }

    /** Próxima pergunta ou a revisão, depois do que foi anotado ([confirmation], falado antes). */
    private fun advance(confirmation: String? = null) {
        if (!namesConfirmed && session.drafts.value.isNotEmpty()) return showNames()
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
        // Da conferência dos nomes: a lista vale como conferida (cada um ainda tem "Corrigir" na revisão).
        namesConfirmed = true
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
    /** "Cadastrar mesmo assim" no aviso de nome repetido. */
    fun confirmDuplicates() {
        val s = _state.value
        _state.update { it.copy(duplicates = emptyList(), duplicatesOnNames = false) }
        if (s.duplicatesOnNames) {
            acceptedDuplicates = acceptedDuplicates + s.duplicates.map(DuplicateNames::normalize)
            namesConfirmedNow()
        } else {
            save(allowDuplicates = true)
        }
    }

    /** "Voltar e conferir": na conferência dos nomes, fica nela para corrigir. */
    fun dismissDuplicates() = _state.update { it.copy(duplicates = emptyList(), duplicatesOnNames = false) }

    fun save(allowDuplicates: Boolean = false) {
        if (saving) return
        val day = today()
        val results = session.drafts.value.map { DraftValidator.normalizeAndValidate(it, day) }
        if (results.isEmpty() || results.any { it is DraftValidator.Result.Invalid }) {
            advance()
            return
        }
        saving = true
        viewModelScope.launch {
            if (!allowDuplicates) {
                val repeated = repeatedNames()
                if (repeated.isNotEmpty()) {
                    saving = false
                    _state.update { it.copy(duplicates = repeated) }
                    return@launch
                }
            }
            results.forEach { valid ->
                val id = insert(valid as DraftValidator.Result.Valid)
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
        const val EXAMPLE = "losartana, 50 miligramas, um comprimido, todos os dias, às 8 da manhã"
        private const val INTRO_PROMPT =
            "Conte quais remédios você usa, quanto toma e em quais horários. Por exemplo: $EXAMPLE. Toque no microfone para falar."
        private const val UNAVAILABLE = "Este aparelho não tem reconhecimento de voz configurado. Você pode digitar."
        private const val MIC_DENIED ="Sem acesso ao microfone. Você pode permitir nas configurações ou digitar."

        fun factory(fromSession: Boolean) = viewModelFactory {
            initializer {
                val container = genkiContainer()
                val repository = container.medicationRepository
                VoiceViewModel(
                    repository::insert, container.registrationSession,
                    existingNames = { repository.observeAll().first().map { it.name } },
                    fromSession = fromSession,
                )
            }
        }
    }
}

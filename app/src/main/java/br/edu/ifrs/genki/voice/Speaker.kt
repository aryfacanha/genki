package br.edu.ifrs.genki.voice

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.edu.ifrs.genki.GenkiEval
import java.util.Locale

/**
 * Lê as perguntas em voz alta (RF19), só em português; sem voz pt, fica só o texto na tela (como no protótipo).
 * Usa o leitor padrão do aparelho; se ele não tiver português, tenta o do Google. No Samsung A31 (27/10) as
 * perguntas não eram faladas: a checagem antiga exigia `voice.locale`, que o leitor da Samsung pode não informar.
 * Com [alarm], fala pelo canal de alarme (volume do alarme, mesmo no silencioso), como o som do "Como alarme" (DEC-052).
 */
class Speaker(context: Context, private val alarm: Boolean = false) {
    private val appContext = context.applicationContext
    private var ready = false
    private var pending: String? = null
    private var triedGoogle = false
    /** Leitor iniciado (com ou sem português); sem português, [speak] avisa o fim na hora. */
    private var settled = false
    /** Chamado quando a fala atual termina (não quando é interrompida): a voz começa a ouvir sozinha (DEC-040). */
    private var done: (() -> Unit)? = null
    private var utterance = 0
    private val main = Handler(Looper.getMainLooper())
    private lateinit var tts: TextToSpeech
    /** Falando agora: o alto-falante da tela pisca. */
    var speaking by mutableStateOf(false)
        private set

    init {
        start(engine = null)
    }

    private fun start(engine: String?) {
        tts = TextToSpeech(appContext, { status -> onInit(status) }, engine)
    }

    private fun onInit(status: Int) {
        // Sem leitor no aparelho, o Android pode avisar a falha ainda dentro do construtor.
        if (!this::tts.isInitialized) {
            GenkiEval.log("tts_iniciado", "status" to status, "pronto" to false)
            return
        }
        val result = if (status == TextToSpeech.SUCCESS) tts.setLanguage(PT_BR) else null
        ready = result != null && result >= TextToSpeech.LANG_AVAILABLE
        GenkiEval.log(
            "tts_iniciado",
            "motor" to if (triedGoogle) GOOGLE else "padrão (${tts.defaultEngine})",
            "status" to status, "idioma" to result, "pronto" to ready,
        )
        if (!ready && !triedGoogle && tts.engines.any { it.name == GOOGLE }) {
            triedGoogle = true
            tts.shutdown()
            start(GOOGLE)
            return
        }
        settled = true
        if (ready) tts.setOnUtteranceProgressListener(progress)
        if (ready && alarm) {
            tts.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
        }
        val text = pending
        pending = null
        if (text != null) speak(text, done) else if (!ready) finish()
    }

    private val progress = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit
        override fun onDone(utteranceId: String?) = ended(utteranceId)
        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = ended(utteranceId)
    }

    /** Só a última fala conta; uma fala trocada por outra ou parada não chama [done]. */
    private fun ended(utteranceId: String?) {
        main.post {
            if (utteranceId == "genki$utterance") {
                speaking = false
                finish()
            }
        }
    }

    private fun finish() {
        val callback = done
        done = null
        callback?.invoke()
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        done = onDone
        if (!ready) {
            // Sem voz em português: a pergunta fica só na tela e o fim é imediato.
            if (settled) finish() else pending = text
            return
        }
        speaking = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "genki${++utterance}") == TextToSpeech.SUCCESS
    }

    fun stop() {
        pending = null
        done = null
        speaking = false
        if (ready) tts.stop()
    }

    fun shutdown() {
        if (this::tts.isInitialized) tts.shutdown()
    }

    companion object {
        private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")
        private const val GOOGLE = "com.google.android.tts"
    }
}

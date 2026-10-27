package br.edu.ifrs.genki.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import br.edu.ifrs.genki.GenkiEval
import java.util.Locale

/**
 * Lê as perguntas em voz alta (RF19), só em português; sem voz pt, fica só o texto na tela (como no protótipo).
 * Usa o leitor padrão do aparelho; se ele não tiver português, tenta o do Google. No Samsung A31 (27/10) as
 * perguntas não eram faladas: a checagem antiga exigia `voice.locale`, que o leitor da Samsung pode não informar.
 */
class Speaker(context: Context) {
    private val appContext = context.applicationContext
    private var ready = false
    private var pending: String? = null
    private var triedGoogle = false
    private lateinit var tts: TextToSpeech

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
        pending?.let(::speak)
        pending = null
    }

    fun speak(text: String) {
        if (!ready) {
            pending = text
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "genki")
    }

    fun stop() {
        pending = null
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

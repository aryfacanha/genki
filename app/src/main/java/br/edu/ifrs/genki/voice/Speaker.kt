package br.edu.ifrs.genki.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Lê as perguntas em voz alta (RF19). Só fala com voz em português; sem ela, fica só o texto na tela
 * (como no protótipo).
 */
class Speaker(context: Context) {
    private var ready = false
    private var pending: String? = null
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech
            val result = tts.setLanguage(Locale.forLanguageTag("pt-BR"))
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED &&
                tts.voice?.locale?.language == "pt"
            pending?.let(::speak)
            pending = null
        }
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

    fun shutdown() = tts.shutdown()
}

package br.edu.ifrs.genki.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Reconhecimento de fala do Android em pt-BR (ESPEC §7). Usa o reconhecedor no aparelho quando existe (API 31+);
 * se ele não tiver português, cai para o reconhecedor padrão do sistema. Nenhum áudio é gravado (RNF07).
 * Chamar sempre na thread principal.
 */
class SpeechInput(private val context: Context, private val events: Events) {

    interface Events {
        fun listening(active: Boolean)
        fun partial(text: String)
        /** [latencyMs] = do fim da fala ao resultado (DEC-015); `null` se o fim não foi detectado. */
        fun result(text: String, latencyMs: Long?)
        fun error(code: Int)
    }

    private var recognizer: SpeechRecognizer? = null
    private var onDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    private var endOfSpeechAt: Long? = null
    /** Escuta em andamento; alguns serviços chamam `onError` duas vezes e só a primeira resposta conta. */
    private var active = false

    fun isAvailable(): Boolean = onDevice || SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        endOfSpeechAt = null
        active = true
        val current = recognizer ?: create().also { recognizer = it }
        current.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                // Pausas maiores antes de encerrar (nem todo reconhecedor respeita).
                .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L),
        )
        events.listening(true)
    }

    fun stop() {
        recognizer?.stopListening()
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun create(): SpeechRecognizer {
        val created = if (onDevice && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        created.setRecognitionListener(listener)
        return created
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onEndOfSpeech() {
            endOfSpeechAt = SystemClock.elapsedRealtime()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            first(partialResults)?.let(events::partial)
        }

        override fun onResults(results: Bundle?) {
            if (!active) return
            active = false
            events.listening(false)
            val latency = endOfSpeechAt?.let { SystemClock.elapsedRealtime() - it }
            events.result(first(results).orEmpty(), latency)
        }

        override fun onError(error: Int) {
            if (!active) return
            if (onDevice && (error == ERROR_LANGUAGE_NOT_SUPPORTED || error == ERROR_LANGUAGE_UNAVAILABLE)) {
                // Reconhecedor do aparelho sem português: tenta o padrão do sistema.
                onDevice = false
                destroy()
                start()
                return
            }
            active = false
            events.listening(false)
            events.error(error)
        }
    }

    private fun first(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.takeIf { it.isNotBlank() }

    companion object {
        // Constantes de SpeechRecognizer da API 31, repetidas para compilar com minSdk 26.
        private const val ERROR_LANGUAGE_NOT_SUPPORTED = 12
        private const val ERROR_LANGUAGE_UNAVAILABLE = 13
    }
}

package br.edu.ifrs.genki.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Reconhecimento de fala do Android em pt-BR (ESPEC §7). Usa o reconhecedor no aparelho quando existe (API 31+);
 * se ele não tiver português, cai para o reconhecedor padrão do sistema. Nenhum áudio é gravado (RNF07).
 * Chamar sempre na thread principal.
 *
 * O reconhecedor encerra numa pausa curta e a maioria ignora o pedido de esperar mais; quem pensa no meio da frase
 * era cortado (teste informal, 29/10). Por isso, ao encerrar com texto, volta a ouvir e junta os trechos; a resposta
 * sai depois de [PAUSE_MS] sem fala nova, no toque no microfone ([stop]) ou ao chegar a [MAX_PARTS] trechos.
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
    /** Trechos já reconhecidos desta resposta. */
    private val parts = mutableListOf<String>()
    /** Latência do último trecho (DEC-015), sem a espera da pausa. */
    private var lastLatency: Long? = null
    /** Tocou no microfone: o trecho que chegar encerra a resposta. */
    private var finishing = false
    private val handler = Handler(Looper.getMainLooper())
    /** Esperando a continuação depois de um trecho (`Handler.hasCallbacks` só existe a partir da API 29). */
    private var waiting = false
    /** Sem fala nova depois do último trecho: entrega o que juntou. */
    private val pauseOver = Runnable {
        waiting = false
        if (!active) return@Runnable
        recognizer?.cancel()
        deliver()
    }

    fun isAvailable(): Boolean = onDevice || SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        parts.clear()
        lastLatency = null
        finishing = false
        active = true
        listen()
        events.listening(true)
    }

    private fun listen() {
        endOfSpeechAt = null
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
    }

    fun stop() {
        if (!active) return
        finishing = true
        // Na espera entre trechos não há reconhecedor ouvindo: entrega já.
        if (waiting) {
            recognizer?.cancel()
            deliver()
        } else {
            recognizer?.stopListening()
        }
    }

    private fun deliver() {
        stopWaiting()
        active = false
        events.listening(false)
        events.result(parts.joinToString(" "), lastLatency)
    }

    private fun stopWaiting() {
        waiting = false
        handler.removeCallbacks(pauseOver)
    }

    /** Para de ouvir sem resultado (abriu o relógio, a quantidade ou o campo enquanto ouvia sozinho). */
    fun cancel() {
        if (!active) return
        active = false
        stopWaiting()
        recognizer?.cancel()
        events.listening(false)
    }

    fun destroy() {
        stopWaiting()
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
        // Voltou a falar dentro da pausa: continua juntando.
        override fun onBeginningOfSpeech() = stopWaiting()
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onEndOfSpeech() {
            endOfSpeechAt = SystemClock.elapsedRealtime()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            first(partialResults)?.let { events.partial((parts + it).joinToString(" ")) }
        }

        override fun onResults(results: Bundle?) {
            if (!active) return
            val text = first(results)
            if (text != null) {
                parts += text
                lastLatency = endOfSpeechAt?.let { SystemClock.elapsedRealtime() - it }
            }
            if (finishing || text == null || parts.size >= MAX_PARTS) {
                deliver()
                return
            }
            // Pode ser só uma pausa para pensar: ouve de novo e espera um pouco pela continuação.
            events.partial(parts.joinToString(" "))
            listen()
            waiting = true
            handler.postDelayed(pauseOver, PAUSE_MS)
        }

        override fun onError(error: Int) {
            if (!active) return
            if (onDevice && (error == ERROR_LANGUAGE_NOT_SUPPORTED || error == ERROR_LANGUAGE_UNAVAILABLE)) {
                // Reconhecedor do aparelho sem português: tenta o padrão do sistema.
                onDevice = false
                recognizer?.destroy()
                recognizer = null
                listen()
                return
            }
            // Na escuta da continuação, silêncio ou nada entendido só encerra a resposta com o que já tinha.
            if (parts.isNotEmpty()) {
                deliver()
                return
            }
            stopWaiting()
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
        /** Pausa para pensar que ainda conta como a mesma resposta. */
        // 3 s pareceu longo (29/10) e 1,5 s ainda cortava quem demora a continuar (bateria do autor, 30/10).
        private const val PAUSE_MS = 2_000L
        private const val MAX_PARTS = 4
    }
}

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
 *
 * Nem todo reconhecedor encerra sozinho: depois de atualizar da 1.0 para a 1.0.20, um celular do autor ficou ouvindo
 * até o toque no microfone (30/10). Por isso o Genki tem relógio próprio: encerra quando as palavras param de mudar
 * por [SILENCE_MS], avisa "não ouvi" depois de [NO_WORDS_MS] sem palavra nenhuma, para tudo em [MAX_ANSWER_MS] e,
 * se o reconhecedor não responder ao pedido de parar, entrega o último texto parcial depois de [FORCE_MS].
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
    /** Último texto parcial do trecho em andamento; vale como resposta se o reconhecedor não entregar o final. */
    private var segmentText: String? = null
    /** Sem fala nova depois do último trecho: entrega o que juntou. */
    private val pauseOver = Runnable {
        waiting = false
        if (!active) return@Runnable
        recognizer?.cancel()
        deliver()
    }
    /** Palavras pararam de mudar e o reconhecedor não encerrou: pede o resultado. */
    private val quietOver = Runnable { if (active && !waiting) stop() }
    /** Teto da resposta inteira, mesmo com ruído que o reconhecedor toma por fala. */
    private val answerOver = Runnable { if (active) stop() }
    /** Ouvindo e nenhuma palavra: o reconhecedor não avisou o silêncio. */
    private val noWordsOver = Runnable {
        if (!active || waiting || segmentText != null) return@Runnable
        if (parts.isNotEmpty()) {
            recognizer?.cancel()
            deliver()
        } else {
            fail(SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
        }
    }
    /** Pediu para parar e o reconhecedor não respondeu: fica com o que já tinha. */
    private val forceEnd = Runnable {
        if (!active) return@Runnable
        segmentText?.let { parts += it }
        recognizer?.cancel()
        if (parts.isNotEmpty()) deliver() else fail(SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
    }

    fun isAvailable(): Boolean = onDevice || SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        parts.clear()
        lastLatency = null
        finishing = false
        active = true
        listen()
        handler.postDelayed(answerOver, MAX_ANSWER_MS)
        events.listening(true)
    }

    private fun listen() {
        endOfSpeechAt = null
        segmentText = null
        handler.removeCallbacks(noWordsOver)
        handler.postDelayed(noWordsOver, NO_WORDS_MS)
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
        if (!active || finishing) return
        finishing = true
        // Na espera entre trechos não há reconhecedor ouvindo: entrega já.
        if (waiting) {
            recognizer?.cancel()
            deliver()
        } else {
            recognizer?.stopListening()
            handler.postDelayed(forceEnd, FORCE_MS)
        }
    }

    private fun deliver() {
        stopTimers()
        active = false
        events.listening(false)
        events.result(parts.joinToString(" "), lastLatency)
    }

    private fun fail(code: Int) {
        stopTimers()
        active = false
        recognizer?.cancel()
        events.listening(false)
        events.error(code)
    }

    private fun stopTimers() {
        stopWaiting()
        handler.removeCallbacks(quietOver)
        handler.removeCallbacks(answerOver)
        handler.removeCallbacks(noWordsOver)
        handler.removeCallbacks(forceEnd)
    }

    private fun stopWaiting() {
        waiting = false
        handler.removeCallbacks(pauseOver)
    }

    /** Para de ouvir sem resultado (abriu o relógio, a quantidade ou o campo enquanto ouvia sozinho). */
    fun cancel() {
        if (!active) return
        active = false
        stopTimers()
        recognizer?.cancel()
        events.listening(false)
    }

    fun destroy() {
        stopTimers()
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
        // Talvez voltou a falar dentro da pausa: a espera recomeça, mas só palavra nova a cancela. Há reconhecedor que
        // avisa o começo sem fala nenhuma (ruído), e cancelar aqui deixava a escuta aberta sem fim (30/10).
        override fun onBeginningOfSpeech() {
            if (waiting) {
                handler.removeCallbacks(pauseOver)
                handler.postDelayed(pauseOver, PAUSE_MS)
            }
        }
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onEndOfSpeech() {
            endOfSpeechAt = SystemClock.elapsedRealtime()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = first(partialResults) ?: return
            if (active && text != segmentText) {
                segmentText = text
                // Palavra nova: é continuação de verdade; o relógio do silêncio recomeça.
                stopWaiting()
                handler.removeCallbacks(noWordsOver)
                if (!finishing) {
                    handler.removeCallbacks(quietOver)
                    handler.postDelayed(quietOver, SILENCE_MS)
                }
            }
            events.partial((parts + text).joinToString(" "))
        }

        override fun onResults(results: Bundle?) {
            if (!active) return
            handler.removeCallbacks(forceEnd)
            handler.removeCallbacks(noWordsOver)
            handler.removeCallbacks(quietOver)
            val text = first(results) ?: segmentText
            segmentText = null
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
            // Pediu para parar e veio erro em vez do resultado: vale o último parcial.
            if (finishing) segmentText?.let { parts += it }
            // Na escuta da continuação, silêncio ou nada entendido só encerra a resposta com o que já tinha.
            if (parts.isNotEmpty()) {
                deliver()
                return
            }
            fail(error)
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
        /** Palavras paradas por este tempo encerram a resposta (o reconhecedor pede 2,5 s e costuma encerrar antes). */
        private const val SILENCE_MS = 4_000L
        /** Ouvindo sem palavra nenhuma (o Android costuma desistir em ~5 s). */
        private const val NO_WORDS_MS = 8_000L
        /** Teto de uma resposta, mesmo com ruído que o reconhecedor toma por fala. */
        private const val MAX_ANSWER_MS = 30_000L
        /** Espera pelo resultado depois de pedir para parar. */
        private const val FORCE_MS = 1_500L
    }
}

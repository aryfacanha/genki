package br.edu.ifrs.genki.reminders

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.ServiceCompat
import br.edu.ifrs.genki.GenkiApplication
import br.edu.ifrs.genki.GenkiEval
import br.edu.ifrs.genki.voice.Speaker
import java.time.LocalTime

/**
 * Som do modo "Como alarme" (DEC-048): o próprio app toca o alarme do sistema em loop, no volume de alarme (o
 * silencioso não zera) e com foco de áudio (o vídeo pausa ou abaixa), e vibra. Antes o som era o da notificação, que o
 * Android pode não tocar no silencioso e toca uma vez só (teste no A31, 29/10). Para no Entendi/Adiar ou depois de
 * [MAX_RING_MS]; a notificação e as repetições de sempre continuam.
 *
 * Com "Falar o nome dos remédios no alarme" (DEC-052), fala [AlarmSpeech] pouco depois de começar (os lembretes do mesmo
 * minuto chegam um a um) e a cada [SPEECH_EVERY_MS], até [MAX_SPEECHES] vezes; o som baixa durante a fala.
 */
class AlarmSoundService : Service() {

    private val ringing = linkedSetOf<Long>()
    private var player: MediaPlayer? = null
    private var focus: AudioFocusRequest? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { stopAll(reason = "tempo") }
    /** Nome e horário de cada lembrete tocando, para a fala. */
    private val names = mutableMapOf<Long, Pair<String, LocalTime>>()
    private var speaker: Speaker? = null
    private var speeches = 0
    private val speakNow = Runnable { speak() }
    /** O leitor pode não avisar o fim (recusa síncrona, RV-PC-02): o som volta mesmo assim. */
    private val restoreVolume = Runnable { player?.setVolume(1f, 1f) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeId = intent?.getLongExtra(EXTRA_TIME_ID, -1) ?: -1
        // Notificação própria do serviço; as dos lembretes ficam independentes dele (RV-048-01/02).
        val notification = Notifications.ringing(this, timeId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        ringing += timeId
        val name = intent?.getStringExtra(EXTRA_NAME)
        val minute = intent?.getIntExtra(EXTRA_MINUTE, -1) ?: -1
        if (name != null && minute >= 0) names[timeId] = name to LocalTime.of(minute / 60, minute % 60)
        if (player == null) startSound()
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, MAX_RING_MS)
        GenkiEval.log("alarme_tocando", "timeId" to timeId)
        return START_NOT_STICKY
    }

    private fun startSound() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val audio = getSystemService(AudioManager::class.java)
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes).build()
            .also { audio.requestAudioFocus(it) }
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        player = try {
            MediaPlayer().apply {
                setAudioAttributes(attributes)
                setDataSource(this@AlarmSoundService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
            null // sem som de alarme no aparelho: fica a vibração e a tela
        }
        vibrator = getSystemService(Vibrator::class.java)?.also {
            it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 800, 600), 0), attributes)
        }
        if ((application as GenkiApplication).container.settings.reminders.value.alarmSpeech) {
            speaker = Speaker(this, alarm = true)
            speeches = 0
            handler.postDelayed(speakNow, FIRST_SPEECH_MS)
        }
    }

    private fun speak() {
        val text = AlarmSpeech.text(ringing.mapNotNull { names[it] }) ?: return
        val current = speaker ?: return
        player?.setVolume(DUCKED_VOLUME, DUCKED_VOLUME)
        handler.removeCallbacks(restoreVolume)
        handler.postDelayed(restoreVolume, MAX_SPEECH_MS)
        current.speak(text) {
            handler.removeCallbacks(restoreVolume)
            player?.setVolume(1f, 1f)
        }
        GenkiEval.log("alarme_falado", "texto" to text)
        if (++speeches < MAX_SPEECHES) handler.postDelayed(speakNow, SPEECH_EVERY_MS)
    }

    private fun stopSpeech() {
        handler.removeCallbacks(speakNow)
        handler.removeCallbacks(restoreVolume)
        speaker?.run { stop(); shutdown() }
        speaker = null
    }

    private fun stopFor(timeId: Long) {
        names.remove(timeId)
        if (ringing.remove(timeId) && ringing.isEmpty()) stopAll(reason = "resposta")
    }

    private fun stopAll(reason: String) {
        handler.removeCallbacks(timeout)
        stopSpeech()
        player?.run { runCatching { stop() }; release() }
        player = null
        vibrator?.cancel()
        vibrator = null
        focus?.let { getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) }
        focus = null
        ringing.clear()
        names.clear()
        // Sai só a notificação do serviço; as dos lembretes sem resposta ficam, e as repetições continuam avisando.
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
        GenkiEval.log("alarme_parado", "motivo" to reason)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        stopSpeech()
        player?.release()
        vibrator?.cancel()
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_TIME_ID = "timeId"
        private const val EXTRA_NAME = "name"
        private const val EXTRA_MINUTE = "minute"
        /** Espera os outros lembretes do mesmo minuto chegarem e o som do alarme ser ouvido. */
        private const val FIRST_SPEECH_MS = 1_500L
        /** Três falas cabem no minuto do alarme (DEC-052). */
        private const val SPEECH_EVERY_MS = 20_000L
        private const val MAX_SPEECHES = 3
        /** Volume do som do alarme enquanto o Genki fala. */
        private const val DUCKED_VOLUME = 0.2f
        /** Teto de uma fala; depois o som volta ao normal mesmo sem aviso do leitor. */
        private const val MAX_SPEECH_MS = 10_000L
        /** Um minuto tocando, como despertador; depois ficam a notificação e as repetições. */
        const val MAX_RING_MS = 60_000L
        /** Fora da faixa dos ids de horário, que são as ids das notificações dos lembretes. */
        private const val NOTIFICATION_ID = Int.MAX_VALUE

        @Volatile private var instance: AlarmSoundService? = null

        /** Chamado no disparo do lembrete (alarme exato: o Android permite iniciar o serviço em primeiro plano). */
        fun start(context: Context, timeId: Long, name: String, time: LocalTime) {
            try {
                context.startForegroundService(
                    Intent(context, AlarmSoundService::class.java)
                        .putExtra(EXTRA_TIME_ID, timeId)
                        .putExtra(EXTRA_NAME, name)
                        .putExtra(EXTRA_MINUTE, time.hour * 60 + time.minute),
                )
            } catch (e: Exception) {
                // Sistema recusou o serviço em primeiro plano: fica a notificação.
                GenkiEval.log("alarme_recusado", "erro" to e.javaClass.simpleName)
            }
        }

        /** Entendi, Adiar, desativar ou excluir: para o som deste horário (mesmo processo, sem novo intent). */
        fun stop(timeId: Long) {
            val service = instance ?: return
            Handler(Looper.getMainLooper()).post { service.stopFor(timeId) }
        }
    }
}

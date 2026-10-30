package br.edu.ifrs.genki.reminders

import android.app.Notification
import android.app.NotificationManager
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
import br.edu.ifrs.genki.GenkiEval

/**
 * Som do modo "Como alarme" (DEC-048): o próprio app toca o alarme do sistema em loop, no volume de alarme (o
 * silencioso não zera) e com foco de áudio (o vídeo pausa ou abaixa), e vibra. Antes o som era o da notificação, que o
 * Android pode não tocar no silencioso e toca uma vez só (teste no A31, 29/10). Para no Entendi/Adiar ou depois de
 * [MAX_RING_MS]; a notificação e as repetições de sempre continuam.
 */
class AlarmSoundService : Service() {

    private val ringing = linkedSetOf<Long>()
    private var player: MediaPlayer? = null
    private var focus: AudioFocusRequest? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { stopAll(reason = "tempo") }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeId = intent?.getLongExtra(EXTRA_TIME_ID, -1) ?: -1
        // A notificação do lembrete (mesmo id) vira a do serviço em primeiro plano: nada a mais na barra.
        val notification = activeNotification(timeId.toInt()) ?: run { stopSelf(); return START_NOT_STICKY }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(timeId.toInt(), notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(timeId.toInt(), notification)
        }
        ringing += timeId
        if (player == null) startSound()
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, MAX_RING_MS)
        GenkiEval.log("alarme_tocando", "timeId" to timeId)
        return START_NOT_STICKY
    }

    private fun activeNotification(id: Int): Notification? =
        getSystemService(NotificationManager::class.java).activeNotifications.firstOrNull { it.id == id }?.notification

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
    }

    private fun stopFor(timeId: Long) {
        if (ringing.remove(timeId) && ringing.isEmpty()) stopAll(reason = "resposta")
    }

    private fun stopAll(reason: String) {
        handler.removeCallbacks(timeout)
        player?.run { runCatching { stop() }; release() }
        player = null
        vibrator?.cancel()
        vibrator = null
        focus?.let { getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) }
        focus = null
        ringing.clear()
        // A notificação fica (sem resposta, ela e as repetições continuam avisando).
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        stopSelf()
        GenkiEval.log("alarme_parado", "motivo" to reason)
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        player?.release()
        vibrator?.cancel()
        if (instance === this) instance = null
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_TIME_ID = "timeId"
        /** Um minuto tocando, como despertador; depois ficam a notificação e as repetições. */
        const val MAX_RING_MS = 60_000L

        @Volatile private var instance: AlarmSoundService? = null

        /** Chamado no disparo do lembrete (alarme exato: o Android permite iniciar o serviço em primeiro plano). */
        fun start(context: Context, timeId: Long) {
            try {
                context.startForegroundService(Intent(context, AlarmSoundService::class.java).putExtra(EXTRA_TIME_ID, timeId))
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

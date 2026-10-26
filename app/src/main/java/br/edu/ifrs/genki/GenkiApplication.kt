package br.edu.ifrs.genki

import android.app.Application
import br.edu.ifrs.genki.reminders.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GenkiApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannel(this)
        // Abertura do app reagenda os lembretes (ESPEC §6).
        scope.launch { container.medicationRepository.syncAll() }
        GenkiEval.log("app_iniciado")
    }
}

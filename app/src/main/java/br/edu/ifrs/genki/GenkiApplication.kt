package br.edu.ifrs.genki

import android.app.Application
import br.edu.ifrs.genki.reminders.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
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
        // Trocar o som (Ajustes ou introdução) muda o tipo de alarme agendado: reagenda horários e adiados (RV-048-03).
        scope.launch {
            container.settings.reminders.map { it.alarmSound }.distinctUntilChanged().drop(1)
                .collect { container.medicationRepository.syncAll() }
        }
        GenkiEval.log("app_iniciado")
    }
}

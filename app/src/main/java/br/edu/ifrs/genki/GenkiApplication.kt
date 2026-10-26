package br.edu.ifrs.genki

import android.app.Application

class GenkiApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        GenkiEval.log("app_iniciado")
    }
}

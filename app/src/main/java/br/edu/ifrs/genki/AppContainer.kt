package br.edu.ifrs.genki

import android.content.Context
import br.edu.ifrs.genki.data.GenkiDatabase
import br.edu.ifrs.genki.data.MedicationRepository
import br.edu.ifrs.genki.data.Settings
import br.edu.ifrs.genki.domain.RegistrationSession
import br.edu.ifrs.genki.reminders.AlertStore
import br.edu.ifrs.genki.reminders.ReminderScheduler

/** Injeção manual (DEC-013). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = GenkiDatabase.create(appContext)

    val settings = Settings(appContext)
    val registrationSession = RegistrationSession()
    val alertStore = AlertStore(appContext)
    val reminderScheduler = ReminderScheduler(appContext, settings, alertStore)
    val medicationRepository = MedicationRepository(database.medicationDao(), reminderScheduler)
}

package br.edu.ifrs.genki

import android.content.Context
import br.edu.ifrs.genki.data.GenkiDatabase
import br.edu.ifrs.genki.data.MedicationRepository

/** Injeção manual (DEC-013). O agendador de lembretes entra no M2. */
class AppContainer(context: Context) {
    private val database = GenkiDatabase.create(context.applicationContext)

    val medicationRepository = MedicationRepository(database.medicationDao())
}

package br.edu.ifrs.genki.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.edu.ifrs.genki.GenkiApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reinício, atualização do app, troca de hora/fuso e permissão de alarme exato → reagenda tudo.
 * Repetições e adiamentos pendentes não voltam (S2); no reinício, os cartões da Hoje também somem (DEC-029).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = (context.applicationContext as GenkiApplication).container
        val repository = container.medicationRepository
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) container.alertStore.clear()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.syncAll()
            } finally {
                pending.finish()
            }
        }
    }
}

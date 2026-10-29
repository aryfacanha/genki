package br.edu.ifrs.genki.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.edu.ifrs.genki.GenkiApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reinício, atualização do app, troca de hora/fuso e permissão de alarme exato → reagenda tudo, inclusive os adiados
 * pendentes. Repetições sem resposta não voltam; no reinício, somem os cartões que estavam tocando (DEC-029), mas os
 * adiados ficam e tocam no horário prometido (achado 1 da pré-revisão de 29/10).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = (context.applicationContext as GenkiApplication).container
        val repository = container.medicationRepository
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) container.alertStore.clearRinging()
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

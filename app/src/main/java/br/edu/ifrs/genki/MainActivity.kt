package br.edu.ifrs.genki

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableIntStateOf
import br.edu.ifrs.genki.ui.navigation.GenkiApp
import br.edu.ifrs.genki.ui.theme.GenkiTheme

class MainActivity : ComponentActivity() {
    /** Toques na notificação do lembrete: cada um pede para mostrar a Hoje. */
    private val openHomeRequests = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handle(intent)
        setContent {
            GenkiTheme {
                GenkiApp(openHomeRequests = openHomeRequests.intValue)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    private fun handle(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_HOME, false) == true) openHomeRequests.intValue++
    }

    companion object {
        const val EXTRA_OPEN_HOME = "abrir_hoje"
    }
}

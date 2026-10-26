package br.edu.ifrs.genki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.edu.ifrs.genki.ui.navigation.GenkiApp
import br.edu.ifrs.genki.ui.theme.GenkiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GenkiTheme {
                GenkiApp()
            }
        }
    }
}

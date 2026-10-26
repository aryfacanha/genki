package br.edu.ifrs.genki.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Só tema claro, como o protótipo; sem cor dinâmica para manter os tokens.
private val LightColorScheme = lightColorScheme(
    primary = GenkiGreen,
    onPrimary = Color.White,
    background = GenkiSurface,
    onBackground = GenkiOnSurface,
    surface = GenkiSurface,
    onSurface = GenkiOnSurface,
    error = GenkiError,
    onError = Color.White,
)

@Composable
fun GenkiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

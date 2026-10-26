package br.edu.ifrs.genki.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Só tema claro, como o protótipo; sem cor dinâmica para manter os tokens.
// Papéis sem equivalente no protótipo (secondary, tertiary, surfaceContainer*) usam tons
// dele para que nenhum componente M3 caia no roxo padrão.
private val LightColorScheme = lightColorScheme(
    primary = GenkiGreen,
    onPrimary = Color.White,
    primaryContainer = GenkiPrimaryContainer,
    onPrimaryContainer = GenkiOnPrimaryContainer,
    secondary = GenkiGreen,
    onSecondary = Color.White,
    secondaryContainer = GenkiIconBackground,
    onSecondaryContainer = GenkiOnIconBackground,
    tertiary = GenkiGreen,
    onTertiary = Color.White,
    background = GenkiSurface,
    onBackground = GenkiOnSurface,
    surface = GenkiSurface,
    onSurface = GenkiOnSurface,
    surfaceVariant = GenkiSurfaceVariant,
    onSurfaceVariant = GenkiOnSurfaceVariant,
    surfaceTint = GenkiGreen,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = GenkiSurface,
    surfaceContainer = GenkiSurface,
    surfaceContainerHigh = GenkiSurface,
    surfaceContainerHighest = GenkiSurfaceVariant,
    outline = GenkiOutline,
    outlineVariant = GenkiOutlineVariant,
    error = GenkiError,
    onError = Color.White,
    errorContainer = GenkiErrorContainer,
)

@Composable
fun GenkiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

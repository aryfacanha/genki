package br.edu.ifrs.genki.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Escala do protótipo (Roboto = fonte padrão do Android). Nenhum estilo abaixo de 16 sp,
// inclusive os rótulos que o M3 usa em componentes (labelMedium/labelSmall).
// Tamanhos em sp respeitam a fonte do sistema (RNF02).
private fun style(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: TextUnit = 0.sp) =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing,
    )

val Typography = Typography(
    displayLarge = style(48, FontWeight.Bold, 52),       // hora do cartão "Agora"
    displayMedium = style(40, FontWeight.Bold, 44),
    displaySmall = style(32, FontWeight.Bold, 38),
    headlineLarge = style(26, FontWeight.Bold, 32, (-0.3).sp), // "Genki" no topo da Home
    headlineMedium = style(22, FontWeight.Bold, 30),     // pergunta falada da voz
    headlineSmall = style(20, FontWeight.Bold, 28),      // título de tela e de diálogo
    titleLarge = style(18, FontWeight.Bold, 26),         // destaque de cartão e opção
    titleMedium = style(17, FontWeight.SemiBold, 22),    // nome do medicamento
    titleSmall = style(16, FontWeight.SemiBold, 22),
    bodyLarge = style(16, FontWeight.Normal, 24),
    bodyMedium = style(16, FontWeight.Normal, 24),
    bodySmall = style(16, FontWeight.Normal, 22),
    labelLarge = style(16, FontWeight.SemiBold, 20),     // botões
    labelMedium = style(16, FontWeight.Normal, 20),      // rótulo da barra inferior
    labelSmall = style(16, FontWeight.Normal, 20),
)

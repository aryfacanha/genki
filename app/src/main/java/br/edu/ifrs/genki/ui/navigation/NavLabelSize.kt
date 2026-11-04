package br.edu.ifrs.genki.ui.navigation

/**
 * Tamanho único dos rótulos da barra de baixo (autor, 03/11): o maior, até [maxSp], em que o rótulo mais largo cabe em
 * [available]. A largura cresce na mesma proporção do tamanho, então basta uma medição em [maxSp].
 */
object NavLabelSize {
    const val MIN_SP = 8f

    fun fit(maxSp: Float, widthAtMax: Float, available: Float): Float =
        if (widthAtMax <= available || widthAtMax <= 0f) maxSp else (maxSp * available / widthAtMax).coerceIn(MIN_SP, maxSp)
}

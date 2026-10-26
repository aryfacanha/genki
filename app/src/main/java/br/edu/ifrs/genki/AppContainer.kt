package br.edu.ifrs.genki

import android.content.Context

/** Injeção manual (DEC-013). Banco, repositório e agendador entram a partir do M1. */
class AppContainer(context: Context) {
    private val appContext: Context = context.applicationContext
}

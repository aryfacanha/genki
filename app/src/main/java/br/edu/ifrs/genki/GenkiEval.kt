package br.edu.ifrs.genki

import android.util.Log

/**
 * Registro da avaliação (DEC-015): só no build debug, filtrável no Logcat pela tag `GenkiEval`.
 * Registra tempos, transcrições e dados interpretados. Nunca registrar áudio.
 */
object GenkiEval {
    const val TAG = "GenkiEval"

    fun log(event: String, vararg fields: Pair<String, Any?>) {
        if (BuildConfig.DEBUG) Log.d(TAG, format(event, *fields))
    }

    fun format(event: String, vararg fields: Pair<String, Any?>): String =
        (listOf(event) + fields.map { (key, value) -> "$key=$value" }).joinToString(" ")
}

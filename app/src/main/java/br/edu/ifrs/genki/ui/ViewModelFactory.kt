package br.edu.ifrs.genki.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import br.edu.ifrs.genki.AppContainer
import br.edu.ifrs.genki.GenkiApplication

/** Acesso ao [AppContainer] dentro de `viewModelFactory { initializer { ... } }` (DEC-013, sem Hilt). */
fun CreationExtras.genkiContainer(): AppContainer = (this[APPLICATION_KEY] as GenkiApplication).container

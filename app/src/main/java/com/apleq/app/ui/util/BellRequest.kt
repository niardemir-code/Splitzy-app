package com.apleq.app.ui.util

import kotlinx.coroutines.flow.MutableStateFlow

/** Se activa cuando el usuario toca una notificación de aviso: la pantalla principal abre la campanita. */
object BellRequest {
    val pending = MutableStateFlow(false)
    const val EXTRA = "open_bell"
}

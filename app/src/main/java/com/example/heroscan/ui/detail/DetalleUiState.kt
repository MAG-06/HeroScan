package com.example.heroscan.ui.detail

import com.example.heroscan.model.Comic

// Estados de la pantalla de detalle: cargando el cómic, cómic listo o error (sin conexión o no encontrado).
sealed interface DetalleUiState {
    // Se está pidiendo el detalle del cómic a Metron
    data object Cargando : DetalleUiState

    // El detalle llegó bien y se puede mostrar
    data class Listo(val comic: Comic) : DetalleUiState

    // La carga falló; sinConexion indica que fue por falta de internet
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : DetalleUiState
}

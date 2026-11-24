package com.example.heroscan.ui.detail

import com.example.heroscan.model.Comic

// Estados de la pantalla de detalle: cargando el cómic, cómic listo, error (con REINTENTAR) o no encontrado (sin REINTENTAR).
sealed interface DetalleUiState {
    // Se está pidiendo el detalle del cómic a Metron
    data object Cargando : DetalleUiState

    // El detalle llegó bien y se puede mostrar
    data class Listo(val comic: Comic) : DetalleUiState

    // La carga falló; sinConexion indica que fue por falta de internet
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : DetalleUiState

    // Metron respondió 404: el cómic con ese id no existe. Reintentar no sirve, así que no hay botón.
    data object NoEncontrado : DetalleUiState
}

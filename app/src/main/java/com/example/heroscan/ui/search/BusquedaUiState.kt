package com.example.heroscan.ui.search

import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.TipoCodigo

// Estados posibles de la pantalla de búsqueda.
sealed interface BusquedaUiState {
    data object Inicial : BusquedaUiState
    data object Cargando : BusquedaUiState
    // Se está buscando un ISBN-13 en la web
    data object CargandoEnLaWeb : BusquedaUiState
    data class Encontrado(val idComic: Int, val tipoCodigo: TipoCodigo) : BusquedaUiState
    data class VariosResultados(val resultados: List<ComicResumen>) : BusquedaUiState
    // La búsqueda falló; sePuedeReintentar es false cuando repetirla daría el mismo error (código no válido o no soportado).
    data class Error(val mensaje: String, val sinConexion: Boolean = false, val sePuedeReintentar: Boolean = true) : BusquedaUiState
}

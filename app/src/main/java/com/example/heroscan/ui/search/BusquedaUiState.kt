package com.example.heroscan.ui.search

import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.TipoCodigo

// Estados posibles de la pantalla de búsqueda.
sealed interface BusquedaUiState {
    object Inicial : BusquedaUiState
    object Cargando : BusquedaUiState
    data class Encontrado(val idComic: Int, val tipoCodigo: TipoCodigo) : BusquedaUiState
    data class VariosResultados(val resultados: List<ComicResumen>) : BusquedaUiState
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : BusquedaUiState
}

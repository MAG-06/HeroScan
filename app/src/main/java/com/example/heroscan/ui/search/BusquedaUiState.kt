package com.example.heroscan.ui.search

import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicResumen

// Estados posibles de la pantalla de búsqueda.
sealed interface BusquedaUiState {
    object Inicial : BusquedaUiState
    object Cargando : BusquedaUiState
    data class Encontrado(val comic: Comic) : BusquedaUiState
    data class VariosResultados(val resultados: List<ComicResumen>) : BusquedaUiState
    data class Error(val mensaje: String) : BusquedaUiState
}

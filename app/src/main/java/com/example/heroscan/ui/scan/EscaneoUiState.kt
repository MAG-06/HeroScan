package com.example.heroscan.ui.scan

import com.example.heroscan.model.TipoCodigo

// Estados posibles de la pantalla de escaneo.
sealed interface EscaneoUiState {
    // La cámara está buscando un código de barras
    object Escaneando : EscaneoUiState

    // Se detectó un código y se está consultando la API
    object Buscando : EscaneoUiState

    // Se detectó un ISBN-13 y se está buscando en la web
    object BuscandoEnLaWeb : EscaneoUiState

    // Un solo cómic: su id y tipo de código, para abrir el detalle.
    data class Encontrado(val idComic: Int, val tipoCodigo: TipoCodigo) : EscaneoUiState

    // El código corresponde a varios cómics: se guarda el código para abrir la búsqueda con él
    data class VariosResultados(val codigo: String) : EscaneoUiState

    // El ISBN-13 corresponde a varios cómics: se guarda el título para abrir la búsqueda con él
    data class VariosPorTitulo(val titulo: String) : EscaneoUiState

    // La búsqueda falló o no encontró nada; sinConexion indica que fue por falta de internet
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : EscaneoUiState
}

package com.example.heroscan.ui.scan

import com.example.heroscan.model.Comic

// Estados posibles de la pantalla de escaneo.
sealed interface EscaneoUiState {
    // La cámara está buscando un código de barras
    object Escaneando : EscaneoUiState

    // Se detectó un código y se está consultando la API
    object Buscando : EscaneoUiState

    // El código corresponde a un solo cómic
    data class Encontrado(val comic: Comic) : EscaneoUiState

    // El código corresponde a varios cómics: se guarda el código para abrir la búsqueda con él
    data class VariosResultados(val codigo: String) : EscaneoUiState

    // La búsqueda falló o no encontró nada
    data class Error(val mensaje: String) : EscaneoUiState
}

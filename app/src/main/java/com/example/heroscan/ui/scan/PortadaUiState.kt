package com.example.heroscan.ui.scan

import com.example.heroscan.model.Comic

// Estados posibles de la pantalla de búsqueda por portada.
sealed interface PortadaUiState {
    // Todavía no se envió ninguna imagen (o ya se terminó de procesar la anterior)
    object Inicial : PortadaUiState

    // Se envió la portada y se está esperando a la IA y a Metron
    object Buscando : PortadaUiState

    // La portada corresponde a un solo cómic
    data class Encontrado(val comic: Comic) : PortadaUiState

    // La portada corresponde a varios cómics: se guarda el título para abrir la búsqueda con él
    data class VariosResultados(val titulo: String) : PortadaUiState

    // La IA no reconoció la portada, Metron no encontró nada o hubo un error de conexión
    data class Error(val mensaje: String) : PortadaUiState
}

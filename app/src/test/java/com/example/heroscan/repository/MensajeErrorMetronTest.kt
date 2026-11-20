package com.example.heroscan.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Tabla de casos de mensajeErrorMetron: cada fila es un código HTTP y el mensaje que debe dar.
@RunWith(Parameterized::class)
class MensajeErrorMetronTest(private val codigo: Int, private val esperado: String) {

    companion object {
        // Filas de la tabla: código HTTP → mensaje esperado (copiado de ComicRepository.kt).
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: codigo {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf(401, "Falta configurar el token de Metron o no es válido"),
            arrayOf(403, "Falta configurar el token de Metron o no es válido"),
            arrayOf(404, "No se encontró el cómic en Metron"),
            arrayOf(429, "Demasiadas búsquedas seguidas. Espera un minuto e intenta de nuevo."),
            arrayOf(499, "Error al consultar Metron (código 499)"),
            arrayOf(500, "Metron no está respondiendo. Intenta más tarde."),
            arrayOf(599, "Metron no está respondiendo. Intenta más tarde."),
            arrayOf(600, "Error al consultar Metron (código 600)"),
            arrayOf(418, "Error al consultar Metron (código 418)")
        )
    }

    // Comprueba que cada código HTTP da el mensaje de su fila.
    @Test
    fun cadaCodigoDaElMensajeDeSuFila() {
        assertEquals(esperado, mensajeErrorMetron(codigo))
    }
}

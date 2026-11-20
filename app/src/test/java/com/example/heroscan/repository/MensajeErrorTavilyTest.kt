package com.example.heroscan.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Tabla de casos de mensajeErrorTavily: cada fila es un código HTTP y el mensaje que debe dar.
@RunWith(Parameterized::class)
class MensajeErrorTavilyTest(private val codigo: Int, private val esperado: String) {

    companion object {
        // Filas de la tabla: código HTTP → mensaje esperado (copiado de ComicRepository.kt).
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: codigo {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf(401, "Falta configurar la clave de Tavily o no es válida"),
            arrayOf(403, "Error al buscar en la web (código 403)"),
            arrayOf(429, "Demasiadas búsquedas web seguidas. Espera un momento e intenta de nuevo."),
            arrayOf(431, "Error al buscar en la web (código 431)"),
            arrayOf(432, "Se acabaron las búsquedas web gratis de este mes"),
            arrayOf(433, "Se acabaron las búsquedas web gratis de este mes"),
            arrayOf(434, "Error al buscar en la web (código 434)"),
            arrayOf(499, "Error al buscar en la web (código 499)"),
            arrayOf(500, "El buscador web no está respondiendo. Intenta más tarde."),
            arrayOf(599, "El buscador web no está respondiendo. Intenta más tarde."),
            arrayOf(600, "Error al buscar en la web (código 600)"),
            arrayOf(418, "Error al buscar en la web (código 418)")
        )
    }

    // Comprueba que cada código HTTP da el mensaje de su fila.
    @Test
    fun cadaCodigoDaElMensajeDeSuFila() {
        assertEquals(esperado, mensajeErrorTavily(codigo))
    }
}

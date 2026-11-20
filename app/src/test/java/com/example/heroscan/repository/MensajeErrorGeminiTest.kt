package com.example.heroscan.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Tabla de casos de mensajeErrorGemini: cada fila es un código HTTP y el mensaje que debe dar.
@RunWith(Parameterized::class)
class MensajeErrorGeminiTest(private val codigo: Int, private val esperado: String) {

    companion object {
        // Filas de la tabla: código HTTP → mensaje esperado (copiado de ComicRepository.kt).
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: codigo {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf(400, "Petición inválida a la IA"),
            arrayOf(403, "Clave de Gemini inválida o sin permisos"),
            arrayOf(404, "Modelo de Gemini no encontrado"),
            arrayOf(429, "Límite de uso de la IA alcanzado, intenta más tarde"),
            arrayOf(503, "El servicio de la IA está saturado, intenta más tarde"),
            arrayOf(401, "Error al consultar la IA (código 401)"),
            arrayOf(500, "Error al consultar la IA (código 500)"),
            arrayOf(418, "Error al consultar la IA (código 418)")
        )
    }

    // Comprueba que cada código HTTP da el mensaje de su fila.
    @Test
    fun cadaCodigoDaElMensajeDeSuFila() {
        assertEquals(esperado, mensajeErrorGemini(codigo))
    }
}

package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Tabla de casos de quitarCercas: cada fila es el texto que llega de la IA y lo que debe quedar para Gson.
@RunWith(Parameterized::class)
class RespuestaIaTest(private val caso: String, private val entrada: String, private val esperado: String) {

    companion object {
        // JSON de ejemplo que debe quedar después de quitar la cerca.
        private const val J = "{\"titulo\":\"Batman\",\"numero\":\"1\"}"

        // Filas: nombre del caso, texto de entrada y texto esperado.
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf("JSON limpio", J, J),
            arrayOf("cerca con json", "```json\n$J\n```", J),
            arrayOf("cerca sola", "```\n$J\n```", J),
            arrayOf("espacios y saltos alrededor de la cerca", "  \n```json\n$J\n```\n  ", J),
            arrayOf("sin cerca con espacios alrededor", "  $J \n", J),
            arrayOf("cerca que no se cierra", "```json\n$J", J),
            arrayOf("texto vacío", "", "")
        )
    }

    // Comprueba que cada fila da el texto esperado.
    @Test
    fun cadaFilaDaElTextoEsperado() {
        assertEquals(esperado, quitarCercas(entrada))
    }
}

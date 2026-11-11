package com.example.heroscan.util

import com.example.heroscan.network.dto.ResultadoTavily
import org.junit.Assert.assertEquals
import org.junit.Test

class TextoBusquedaWebTest {

    // Comprueba que cada resultado queda en su propia línea con el formato "título: resumen".
    @Test
    fun armarTextoUneTituloYResumenPorLinea() {
        val resultados = listOf(
            ResultadoTavily(title = "T1", content = "R1"),
            ResultadoTavily(title = "T2", content = "R2")
        )
        assertEquals("T1: R1\nT2: R2", armarTextoParaIA(resultados))
    }

    // Comprueba que se salta un resultado sin título ni resumen y que una lista vacía da un texto vacío.
    @Test
    fun armarTextoSaltaResultadosVacios() {
        val resultados = listOf(
            ResultadoTavily(title = null, content = null),
            ResultadoTavily(title = "T1", content = "R1")
        )
        assertEquals("T1: R1", armarTextoParaIA(resultados))
        assertEquals("", armarTextoParaIA(emptyList()))
    }

    // Comprueba que un texto más largo que el máximo se recorta a 4000 caracteres.
    @Test
    fun armarTextoRecortaAlMaximo() {
        val resultados = List(10) { ResultadoTavily(title = "T", content = "a".repeat(500)) }
        assertEquals(4000, armarTextoParaIA(resultados).length)
    }

    // Comprueba que el resumen de cada resultado se recorta a 500 caracteres.
    @Test
    fun armarTextoRecortaCadaResumen() {
        val resumen = "a".repeat(500) + "b".repeat(500)
        val resultados = listOf(ResultadoTavily(title = "T", content = resumen))
        assertEquals("T: " + resumen.take(500), armarTextoParaIA(resultados))
    }
}

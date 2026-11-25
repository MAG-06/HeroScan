package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Tabla de casos de normalizarTitulo: entrada y resultado esperado.
@RunWith(Parameterized::class)
class NormalizarTituloTest(private val caso: String, private val entrada: String, private val esperado: String) {

    companion object {
        // Filas: nombre del caso, entrada y resultado esperado.
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf("barra pegada", "Superman/Batman", "Superman / Batman"),
            arrayOf("barra ya separada", "Superman / Batman", "Superman / Batman"),
            arrayOf("barra pegada a un lado", "Superman /Batman", "Superman / Batman"),
            arrayOf("varias barras", "A/B/C", "A / B / C"),
            arrayOf("espacios de más", "  Batman   Beyond ", "Batman Beyond"),
            arrayOf("sin cambios", "Batman", "Batman"),
            arrayOf("vacío", "", "")
        )
    }

    // Comprueba que normalizarTitulo da lo esperado en cada caso de la tabla.
    @Test
    fun cadaFilaDaElTituloEsperado() {
        assertEquals(esperado, normalizarTitulo(entrada))
    }
}

// Tabla de casos de candidatosBusqueda: título, número, serie y la lista de búsquedas esperada en orden.
@RunWith(Parameterized::class)
class CandidatosBusquedaTest(
    private val caso: String,
    private val titulo: String,
    private val numero: String,
    private val serie: String,
    private val esperado: List<CandidatoBusqueda>
) {

    companion object {
        // Filas: nombre del caso, título, número, serie y la lista esperada.
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf("sin número", "Batman", "", "",
                listOf(CandidatoBusqueda("Batman", null))),
            arrayOf("con número", "Batman", "1", "",
                listOf(CandidatoBusqueda("Batman", "1"), CandidatoBusqueda("Batman", null))),
            arrayOf("número con espacios", "Batman", " 1 ", "",
                listOf(CandidatoBusqueda("Batman", "1"), CandidatoBusqueda("Batman", null))),
            arrayOf("serie solo espacios", "Lobo", "3", "  ",
                listOf(CandidatoBusqueda("Lobo", "3"), CandidatoBusqueda("Lobo", null))),
            arrayOf("serie igual al título", "Warlock", "7", "Warlock",
                listOf(CandidatoBusqueda("Warlock", "7"), CandidatoBusqueda("Warlock", null))),
            arrayOf("serie igual con otras mayúsculas", "JLA", "1", "jla",
                listOf(CandidatoBusqueda("JLA", "1"), CandidatoBusqueda("JLA", null))),
            arrayOf("serie distinta", "The Power of Warlock", "7", "Warlock",
                listOf(
                    CandidatoBusqueda("The Power of Warlock", "7"),
                    CandidatoBusqueda("The Power of Warlock", null),
                    CandidatoBusqueda("Warlock", "7"),
                    CandidatoBusqueda("Warlock", null)
                )),
            arrayOf("título con /", "Superman/Batman", "", "",
                listOf(CandidatoBusqueda("Superman / Batman", null))),
            arrayOf("título con :", "Superman/Batman: Supergirl", "", "",
                listOf(CandidatoBusqueda("Superman / Batman: Supergirl", null), CandidatoBusqueda("Superman / Batman", null))),
            arrayOf("serie igual a lo de antes del :", "Superman/Batman: Supergirl", "", "Superman/Batman",
                listOf(CandidatoBusqueda("Superman / Batman: Supergirl", null), CandidatoBusqueda("Superman / Batman", null))),
            arrayOf("título vacío", "", "1", "Lobo",
                listOf(CandidatoBusqueda("Lobo", "1"), CandidatoBusqueda("Lobo", null))),
            arrayOf("todo junto (5)", "The Power of Warlock: Return", "7", "Warlock",
                listOf(
                    CandidatoBusqueda("The Power of Warlock: Return", "7"),
                    CandidatoBusqueda("The Power of Warlock: Return", null),
                    CandidatoBusqueda("Warlock", "7"),
                    CandidatoBusqueda("Warlock", null),
                    CandidatoBusqueda("The Power of Warlock", null)
                ))
        )
    }

    // Comprueba que candidatosBusqueda arma las búsquedas en el orden esperado, sin vacías ni repetidas.
    @Test
    fun cadaFilaDaLasBusquedasEsperadas() {
        assertEquals(esperado, candidatosBusqueda(titulo, numero, serie))
    }
}

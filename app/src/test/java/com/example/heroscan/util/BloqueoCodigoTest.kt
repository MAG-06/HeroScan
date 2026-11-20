package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BloqueoCodigoTest {

    // Repite la cantidad indicada de cuadros sin código, desde el bloqueo dado, y devuelve cómo queda.
    private fun cuadrosSinCodigo(bloqueo: BloqueoCodigo, cantidad: Int): BloqueoCodigo {
        var resultado = bloqueo
        for (i in 1..cantidad) {
            resultado = actualizarBloqueo(resultado, null)
        }
        return resultado
    }

    // Comprueba que sin código bloqueado un cuadro sin código no cambia nada.
    @Test
    fun sinBloqueoYSinCodigoNoCambiaNada() {
        assertEquals(BloqueoCodigo(null, 0), actualizarBloqueo(BloqueoCodigo(null, 0), null))
    }

    // Comprueba que sin código bloqueado un cuadro con un código no cambia nada (no bloquea ese código).
    @Test
    fun sinBloqueoYConCodigoNoCambiaNada() {
        assertEquals(BloqueoCodigo(null, 0), actualizarBloqueo(BloqueoCodigo(null, 0), "A"))
    }

    // Comprueba que un cuadro sin código suma 1 a la cuenta y el código sigue bloqueado.
    @Test
    fun cuadroSinCodigoSumaUno() {
        assertEquals(BloqueoCodigo("A", 1), actualizarBloqueo(BloqueoCodigo("A", 0), null))
    }

    // Comprueba que ver el código bloqueado devuelve la cuenta a 0 y lo deja bloqueado.
    @Test
    fun verElCodigoBloqueadoReiniciaLaCuenta() {
        assertEquals(BloqueoCodigo("A", 0), actualizarBloqueo(BloqueoCodigo("A", 5), "A"))
    }

    // Comprueba que un cuadro menos de los necesarios todavía no libera el código.
    @Test
    fun nMenosUnCuadrosSinCodigoSigueBloqueado() {
        val resultado = cuadrosSinCodigo(BloqueoCodigo("A", 0), CUADROS_PARA_LIBERAR - 1)
        assertEquals(BloqueoCodigo("A", CUADROS_PARA_LIBERAR - 1), resultado)
    }

    // Comprueba que CUADROS_PARA_LIBERAR cuadros seguidos sin código liberan el bloqueo y la cuenta vuelve a 0.
    @Test
    fun nCuadrosSinCodigoLibera() {
        val resultado = cuadrosSinCodigo(BloqueoCodigo("A", 0), CUADROS_PARA_LIBERAR)
        assertEquals(BloqueoCodigo(null, 0), resultado)
    }

    // Comprueba que si ML Kit "parpadea" (ve el código bloqueado en medio), la cuenta empieza de nuevo y no se libera.
    @Test
    fun parpadeoEnMedioNoLibera() {
        var bloqueo = cuadrosSinCodigo(BloqueoCodigo("A", 0), CUADROS_PARA_LIBERAR - 1)
        bloqueo = actualizarBloqueo(bloqueo, "A")
        bloqueo = cuadrosSinCodigo(bloqueo, CUADROS_PARA_LIBERAR - 1)
        assertEquals(BloqueoCodigo("A", CUADROS_PARA_LIBERAR - 1), bloqueo)
    }

    // Comprueba que ver otro código cuenta como no ver el bloqueado (suma 1).
    @Test
    fun otroCodigoCuentaComoNoVerlo() {
        assertEquals(BloqueoCodigo("A", 1), actualizarBloqueo(BloqueoCodigo("A", 0), "B"))
    }

    // Comprueba que después de liberar, otro cuadro sin código deja la cuenta en 0 y sin bloqueo.
    @Test
    fun despuesDeLiberarLaCuentaQuedaEnCero() {
        val liberado = cuadrosSinCodigo(BloqueoCodigo("A", 0), CUADROS_PARA_LIBERAR)
        assertEquals(BloqueoCodigo(null, 0), actualizarBloqueo(liberado, null))
    }
}

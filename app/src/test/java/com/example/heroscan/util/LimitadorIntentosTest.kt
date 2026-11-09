package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LimitadorIntentosTest {

    // Reloj falso: las pruebas cambian "ahora" a mano, lo que permite mover el tiempo sin esperar.
    private var ahora = 0L

    private fun relojFalso(): Long = ahora

    private val limitador = LimitadorIntentos(::relojFalso)

    // Adelanta el reloj falso hasta el momento exacto en que termina el bloqueo (no hace nada si no hay bloqueo).
    private fun esperarFinDelBloqueo() {
        ahora += limitador.segundosDeEspera() * 1000L
    }

    // Registra varios fallos seguidos, esperando a que acabe cada bloqueo para que todos cuenten.
    private fun fallar(veces: Int) {
        for (i in 1..veces) {
            esperarFinDelBloqueo()
            limitador.registrarFallo()
        }
    }

    // Comprueba que un limitador nuevo deja intentar y le quedan 5 intentos.
    @Test
    fun limitadorNuevoDejaIntentar() {
        assertTrue(limitador.puedeIntentar())
        assertEquals(0, limitador.segundosDeEspera())
        assertEquals(5, limitador.intentosRestantesAntesDeBloqueo())
    }

    // Comprueba que el 4.º fallo todavía no bloquea y queda 1 intento.
    @Test
    fun cuartoFalloNoBloquea() {
        fallar(4)
        assertTrue(limitador.puedeIntentar())
        assertEquals(0, limitador.segundosDeEspera())
        assertEquals(1, limitador.intentosRestantesAntesDeBloqueo())
    }

    // Comprueba que el 5.º fallo bloquea 30 segundos y no quedan intentos.
    @Test
    fun quintoFalloBloqueaTreintaSegundos() {
        fallar(5)
        assertFalse(limitador.puedeIntentar())
        assertEquals(30, limitador.segundosDeEspera())
        assertEquals(0, limitador.intentosRestantesAntesDeBloqueo())
    }

    // Comprueba que un milisegundo antes del final sigue bloqueado y falta 1 segundo.
    @Test
    fun justoAntesDelFinalSigueBloqueado() {
        fallar(5)
        ahora = 29_999L
        assertFalse(limitador.puedeIntentar())
        assertEquals(1, limitador.segundosDeEspera())
    }

    // Comprueba que en el milisegundo exacto del final ya se puede intentar.
    @Test
    fun justoAlFinalYaSePuedeIntentar() {
        fallar(5)
        ahora = 30_000L
        assertTrue(limitador.puedeIntentar())
        assertEquals(0, limitador.segundosDeEspera())
    }

    // Comprueba que los segundos de espera se redondean hacia arriba.
    @Test
    fun segundosDeEsperaRedondeanHaciaArriba() {
        fallar(5)
        ahora = 28_999L
        assertEquals(2, limitador.segundosDeEspera())
        ahora = 29_000L
        assertEquals(1, limitador.segundosDeEspera())
    }

    // Comprueba que un fallo durante el bloqueo no cuenta ni alarga la espera.
    @Test
    fun falloDuranteElBloqueoNoCuenta() {
        fallar(5)
        ahora = 10_000L
        limitador.registrarFallo()
        assertEquals(20, limitador.segundosDeEspera())
    }

    // Comprueba que el 6.º fallo, después de acabar la espera, bloquea 60 segundos.
    @Test
    fun sextoFalloBloqueaSesentaSegundos() {
        fallar(6)
        assertEquals(60, limitador.segundosDeEspera())
        assertEquals(0, limitador.intentosRestantesAntesDeBloqueo())
    }

    // Comprueba que el 7.º fallo bloquea 120 segundos.
    @Test
    fun septimoFalloBloqueaCientoVeinteSegundos() {
        fallar(7)
        assertEquals(120, limitador.segundosDeEspera())
    }

    // Comprueba que el 8.º fallo bloquea 240 segundos y el 9.º, 480.
    @Test
    fun octavoYNovenoFalloSiguenDuplicando() {
        fallar(8)
        assertEquals(240, limitador.segundosDeEspera())
        fallar(1)
        assertEquals(480, limitador.segundosDeEspera())
    }

    // Comprueba que el 10.º fallo llega al tope de 15 minutos (900 segundos, no 960).
    @Test
    fun decimoFalloLlegaAlTope() {
        fallar(10)
        assertEquals(900, limitador.segundosDeEspera())
    }

    // Comprueba que el 11.º fallo se queda en el tope de 15 minutos.
    @Test
    fun undecimoFalloSeQuedaEnElTope() {
        fallar(11)
        assertEquals(900, limitador.segundosDeEspera())
    }

    // Comprueba que un inicio de sesión correcto quita el bloqueo y devuelve los 5 intentos.
    @Test
    fun exitoQuitaElBloqueo() {
        fallar(5)
        limitador.registrarExito()
        assertTrue(limitador.puedeIntentar())
        assertEquals(0, limitador.segundosDeEspera())
        assertEquals(5, limitador.intentosRestantesAntesDeBloqueo())
    }

    // Comprueba que después de un éxito la cuenta empieza de nuevo: el 5.º fallo vuelve a bloquear 30 segundos.
    @Test
    fun trasElExitoSeEmpiezaDeNuevo() {
        fallar(5)
        limitador.registrarExito()
        fallar(5)
        assertEquals(30, limitador.segundosDeEspera())
    }

    // Comprueba que un éxito antes del bloqueo también devuelve los 5 intentos.
    @Test
    fun exitoAntesDelBloqueoReiniciaIntentos() {
        fallar(3)
        limitador.registrarExito()
        assertEquals(5, limitador.intentosRestantesAntesDeBloqueo())
    }
}

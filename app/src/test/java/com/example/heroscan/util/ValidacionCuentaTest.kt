package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

// validarCorreo no se prueba aquí porque usa android.util.Patterns, que no funciona en las pruebas locales.
class ValidacionCuentaTest {

    // Comprueba que normalizar quita los espacios de los extremos y deja todo en minúsculas.
    @Test
    fun normalizarRecortaYBajaAMinusculas() {
        assertEquals("ana.perez", normalizarNombreUsuario(" Ana.Perez "))
    }

    // Comprueba que normalizar no quita los espacios internos.
    @Test
    fun normalizarConservaEspaciosInternos() {
        assertEquals("ana perez", normalizarNombreUsuario("ana perez"))
    }

    // Comprueba el límite inferior: 2 caracteres no sirven y se devuelve el mensaje exacto.
    @Test
    fun nombreDeDosCaracteresDaError() {
        assertEquals(
            "El nombre de usuario debe tener de 3 a 20 caracteres: letras, números, _ o .",
            validarNombreUsuario("ab")
        )
    }

    // Comprueba el límite inferior: 3 caracteres son válidos.
    @Test
    fun nombreDeTresCaracteresEsValido() {
        assertNull(validarNombreUsuario("abc"))
    }

    // Comprueba el límite superior: 20 caracteres son válidos.
    @Test
    fun nombreDeVeinteCaracteresEsValido() {
        assertNull(validarNombreUsuario("a".repeat(20)))
    }

    // Comprueba el límite superior: 21 caracteres no sirven.
    @Test
    fun nombreDeVeintiunCaracteresDaError() {
        assertNotNull(validarNombreUsuario("a".repeat(21)))
    }

    // Comprueba que se aceptan letras, números, guion bajo y punto.
    @Test
    fun nombreConCaracteresPermitidosEsValido() {
        assertNull(validarNombreUsuario("ana_perez.99"))
    }

    // Comprueba que un espacio interno no se acepta.
    @Test
    fun nombreConEspacioDaError() {
        assertNotNull(validarNombreUsuario("ana perez"))
    }

    // Comprueba que una tilde no se acepta.
    @Test
    fun nombreConTildeDaError() {
        assertNotNull(validarNombreUsuario("josé"))
    }

    // Comprueba que la ñ no se acepta.
    @Test
    fun nombreConEnieDaError() {
        assertNotNull(validarNombreUsuario("muñoz"))
    }

    // Comprueba que una mayúscula sin normalizar no se acepta.
    @Test
    fun nombreConMayusculaDaError() {
        assertNotNull(validarNombreUsuario("Ana"))
    }

    // Comprueba que un guion no se acepta.
    @Test
    fun nombreConGuionDaError() {
        assertNotNull(validarNombreUsuario("ana-perez"))
    }

    // Comprueba que un nombre vacío no se acepta.
    @Test
    fun nombreVacioDaError() {
        assertNotNull(validarNombreUsuario(""))
    }

    // Comprueba que una contraseña de 7 caracteres no sirve y se devuelve el mensaje exacto.
    @Test
    fun claveDeSieteCaracteresDaError() {
        assertEquals("La contraseña debe tener al menos 8 caracteres.", validarClave("abcdefg"))
    }

    // Comprueba que una contraseña de 8 caracteres es válida.
    @Test
    fun claveDeOchoCaracteresEsValida() {
        assertNull(validarClave("abcdefgh"))
    }
}

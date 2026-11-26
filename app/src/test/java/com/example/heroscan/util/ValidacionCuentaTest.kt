package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Pruebas normales de normalizarNombreUsuario, validarClave y el mensaje de error de validarNombreUsuario.
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

// Tabla de casos de validarNombreUsuario: cada fila es un nombre ya normalizado y si debe ser válido.
@RunWith(Parameterized::class)
class ValidarNombreUsuarioTest(private val caso: String, private val nombre: String, private val valido: Boolean) {

    companion object {
        // Filas: nombre del caso, nombre de usuario y si es válido.
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf("3 caracteres (límite inferior)", "abc", true),
            arrayOf("20 caracteres (límite superior)", "a".repeat(20), true),
            arrayOf("21 caracteres", "a".repeat(21), false),
            arrayOf("letras, números, _ y .", "ana_perez.99", true),
            arrayOf("espacio interno", "ana perez", false),
            arrayOf("tilde", "josé", false),
            arrayOf("ñ", "muñoz", false),
            arrayOf("mayúscula sin normalizar", "Ana", false),
            arrayOf("guion", "ana-perez", false),
            arrayOf("vacío", "", false),
            arrayOf("solo puntos", "...", false),
            arrayOf("solo guiones bajos", "___", false),
            arrayOf("letra y puntos", "a..", true)
        )
    }

    // Comprueba que cada nombre es válido o da error según su fila.
    @Test
    fun cadaNombreEsValidoODaErrorSegunSuFila() {
        assertEquals(valido, validarNombreUsuario(nombre) == null)
    }
}

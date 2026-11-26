package com.example.heroscan.util

import com.example.heroscan.model.TipoCodigo
import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Pruebas de limpiarCodigo: quita guiones, espacios, tabulaciones y saltos de línea.
class LimpiarCodigoTest {

    // Comprueba que limpiarCodigo quita los guiones.
    @Test
    fun limpiarCodigoQuitaGuiones() {
        assertEquals("9780306406157", limpiarCodigo("978-0-306-40615-7"))
    }

    // Comprueba que limpiarCodigo quita los espacios internos y los de los extremos.
    @Test
    fun limpiarCodigoQuitaEspacios() {
        assertEquals("0306406152", limpiarCodigo("  0 306 40615 2 "))
    }

    // Comprueba que limpiarCodigo quita las tabulaciones y los saltos de línea de los extremos (por ejemplo, al pegar un código copiado).
    @Test
    fun limpiarCodigoQuitaTabulacionesYSaltosDeLinea() {
        assertEquals("9780306406157", limpiarCodigo("\t9780306406157\n"))
    }
}

// Tabla de casos de clasificarCodigoEscrito: cada fila es un código escrito a mano y el tipo que debe dar.
@RunWith(Parameterized::class)
class ClasificarCodigoEscritoTest(private val caso: String, private val entrada: String, private val esperado: TipoCodigo) {

    companion object {
        // Filas: nombre del caso, código escrito y tipo esperado.
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf("10 caracteres", "0306406152", TipoCodigo.ISBN_10),
            arrayOf("12 caracteres", "123456789012", TipoCodigo.UPC_A),
            arrayOf("17 caracteres (con suplemento)", "12345678901200111", TipoCodigo.UPC_A),
            arrayOf("13 sin prefijo ISBN", "5901234123457", TipoCodigo.EAN_13),
            arrayOf("13 con 978", "9780306406157", TipoCodigo.ISBN_13),
            arrayOf("13 con 979", "9791234567896", TipoCodigo.ISBN_13),
            arrayOf("12 con 978", "978030640615", TipoCodigo.UPC_A),
            arrayOf("con guiones", "978-0-306-40615-7", TipoCodigo.ISBN_13),
            arrayOf("9 caracteres", "030640615", TipoCodigo.DESCONOCIDO),
            arrayOf("11 caracteres", "03064061521", TipoCodigo.DESCONOCIDO),
            arrayOf("14 caracteres", "59012341234570", TipoCodigo.DESCONOCIDO),
            arrayOf("16 caracteres", "1234567890120011", TipoCodigo.DESCONOCIDO),
            arrayOf("18 caracteres", "123456789012001110", TipoCodigo.DESCONOCIDO),
            arrayOf("vacío", "", TipoCodigo.DESCONOCIDO),
            arrayOf("10 letras", "abcdefghij", TipoCodigo.DESCONOCIDO),
            arrayOf("13 con una letra", "978030640615X", TipoCodigo.DESCONOCIDO)
        )
    }

    // Comprueba que cada código escrito da el tipo de su fila.
    @Test
    fun cadaCodigoEscritoDaElTipoDeSuFila() {
        assertEquals(esperado, clasificarCodigoEscrito(entrada))
    }
}

// Tabla de casos de clasificarCodigoEscaneado: cada fila es un código, el formato que detectó ML Kit y el tipo que debe dar.
@RunWith(Parameterized::class)
class ClasificarCodigoEscaneadoTest(
    private val caso: String,
    private val codigo: String,
    private val formato: Int,
    private val esperado: TipoCodigo
) {

    companion object {
        // Filas: nombre del caso, código, formato de ML Kit y tipo esperado.
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf("EAN-13 con 978", "9780306406157", Barcode.FORMAT_EAN_13, TipoCodigo.ISBN_13),
            arrayOf("EAN-13 con 979", "9791234567896", Barcode.FORMAT_EAN_13, TipoCodigo.ISBN_13),
            arrayOf("EAN-13 sin prefijo ISBN", "5901234123457", Barcode.FORMAT_EAN_13, TipoCodigo.EAN_13),
            arrayOf("UPC-A", "123456789012", Barcode.FORMAT_UPC_A, TipoCodigo.UPC_A),
            arrayOf("UPC-A con 978 (manda el formato)", "978030640615", Barcode.FORMAT_UPC_A, TipoCodigo.UPC_A),
            arrayOf("QR", "123456789012", Barcode.FORMAT_QR_CODE, TipoCodigo.DESCONOCIDO),
            arrayOf("EAN-8", "12345670", Barcode.FORMAT_EAN_8, TipoCodigo.DESCONOCIDO)
        )
    }

    // Comprueba que cada código escaneado da el tipo de su fila.
    @Test
    fun cadaCodigoEscaneadoDaElTipoDeSuFila() {
        assertEquals(esperado, clasificarCodigoEscaneado(codigo, formato))
    }
}

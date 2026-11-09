package com.example.heroscan.util

import com.example.heroscan.model.TipoCodigo
import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Test

class ClasificadorCodigoTest {

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

    // Comprueba que un código escrito de 10 caracteres es ISBN-10.
    @Test
    fun escritoDeDiezEsIsbn10() {
        assertEquals(TipoCodigo.ISBN_10, clasificarCodigoEscrito("0306406152"))
    }

    // Comprueba que un código escrito de 12 caracteres es UPC-A.
    @Test
    fun escritoDeDoceEsUpcA() {
        assertEquals(TipoCodigo.UPC_A, clasificarCodigoEscrito("123456789012"))
    }

    // Comprueba que un código escrito de 17 caracteres (UPC-A con suplemento) es UPC-A.
    @Test
    fun escritoDeDiecisieteEsUpcA() {
        assertEquals(TipoCodigo.UPC_A, clasificarCodigoEscrito("12345678901200111"))
    }

    // Comprueba que un código escrito de 13 caracteres sin prefijo ISBN es EAN-13.
    @Test
    fun escritoDeTreceSinPrefijoEsEan13() {
        assertEquals(TipoCodigo.EAN_13, clasificarCodigoEscrito("5901234123457"))
    }

    // Comprueba que un código escrito de 13 caracteres que empieza por 978 es ISBN-13.
    @Test
    fun escritoDeTreceCon978EsIsbn13() {
        assertEquals(TipoCodigo.ISBN_13, clasificarCodigoEscrito("9780306406157"))
    }

    // Comprueba que un código escrito de 13 caracteres que empieza por 979 es ISBN-13.
    @Test
    fun escritoDeTreceCon979EsIsbn13() {
        assertEquals(TipoCodigo.ISBN_13, clasificarCodigoEscrito("9791234567896"))
    }

    // Comprueba que el prefijo 978 solo importa con 13 caracteres: con 12 sigue siendo UPC-A.
    @Test
    fun escritoDeDoceCon978EsUpcA() {
        assertEquals(TipoCodigo.UPC_A, clasificarCodigoEscrito("978030640615"))
    }

    // Comprueba que los guiones se quitan antes de clasificar el código escrito.
    @Test
    fun escritoConGuionesSeClasificaLimpio() {
        assertEquals(TipoCodigo.ISBN_13, clasificarCodigoEscrito("978-0-306-40615-7"))
    }

    // Comprueba el límite por debajo de 10: con 9 caracteres es desconocido.
    @Test
    fun escritoDeNueveEsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscrito("030640615"))
    }

    // Comprueba el largo entre 10 y 12: con 11 caracteres es desconocido.
    @Test
    fun escritoDeOnceEsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscrito("03064061521"))
    }

    // Comprueba el límite por encima de 13: con 14 caracteres es desconocido.
    @Test
    fun escritoDeCatorceEsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscrito("59012341234570"))
    }

    // Comprueba el límite por debajo de 17: con 16 caracteres es desconocido.
    @Test
    fun escritoDeDieciseisEsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscrito("1234567890120011"))
    }

    // Comprueba el límite por encima de 17: con 18 caracteres es desconocido.
    @Test
    fun escritoDeDieciochoEsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscrito("123456789012001110"))
    }

    // Comprueba que un código escrito vacío es desconocido.
    @Test
    fun escritoVacioEsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscrito(""))
    }

    // Comprueba que un EAN-13 escaneado que empieza por 978 es ISBN-13.
    @Test
    fun escaneadoEan13Con978EsIsbn13() {
        assertEquals(TipoCodigo.ISBN_13, clasificarCodigoEscaneado("9780306406157", Barcode.FORMAT_EAN_13))
    }

    // Comprueba que un EAN-13 escaneado que empieza por 979 es ISBN-13.
    @Test
    fun escaneadoEan13Con979EsIsbn13() {
        assertEquals(TipoCodigo.ISBN_13, clasificarCodigoEscaneado("9791234567896", Barcode.FORMAT_EAN_13))
    }

    // Comprueba que un EAN-13 escaneado sin prefijo ISBN es EAN-13.
    @Test
    fun escaneadoEan13NormalEsEan13() {
        assertEquals(TipoCodigo.EAN_13, clasificarCodigoEscaneado("5901234123457", Barcode.FORMAT_EAN_13))
    }

    // Comprueba que un UPC-A escaneado es UPC-A.
    @Test
    fun escaneadoUpcAEsUpcA() {
        assertEquals(TipoCodigo.UPC_A, clasificarCodigoEscaneado("123456789012", Barcode.FORMAT_UPC_A))
    }

    // Comprueba que al escanear manda el formato: un UPC-A que empieza por 978 sigue siendo UPC-A.
    @Test
    fun escaneadoUpcACon978SigueSiendoUpcA() {
        assertEquals(TipoCodigo.UPC_A, clasificarCodigoEscaneado("978030640615", Barcode.FORMAT_UPC_A))
    }

    // Comprueba que un código QR escaneado es desconocido.
    @Test
    fun escaneadoQrEsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscaneado("123456789012", Barcode.FORMAT_QR_CODE))
    }

    // Comprueba que un EAN-8 escaneado es desconocido.
    @Test
    fun escaneadoEan8EsDesconocido() {
        assertEquals(TipoCodigo.DESCONOCIDO, clasificarCodigoEscaneado("12345670", Barcode.FORMAT_EAN_8))
    }
}

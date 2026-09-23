package com.example.heroscan.util

import com.example.heroscan.model.TipoCodigo
import com.google.mlkit.vision.barcode.common.Barcode

// Funciones para identificar qué tipo de código (UPC-A, EAN-13, ISBN, ISSN) es un código de barras.

// Quita espacios y guiones de un código para dejar solo sus dígitos.
fun limpiarCodigo(codigo: String): String {
    return codigo.trim().replace("-", "").replace(" ", "")
}

// Identifica el tipo de un código escrito a mano según su longitud y su prefijo.
fun clasificarCodigoEscrito(codigo: String): TipoCodigo {
    val codigoLimpio = limpiarCodigo(codigo)

    return when {
        codigoLimpio.length == 13 &&
                (codigoLimpio.startsWith("978") || codigoLimpio.startsWith("979")) -> TipoCodigo.ISBN_13

        codigoLimpio.length == 8 -> TipoCodigo.ISSN

        codigoLimpio.length == 12 -> TipoCodigo.UPC_A

        codigoLimpio.length == 17 -> TipoCodigo.UPC_A  // UPC-A + suplemento de 5 dígitos

        codigoLimpio.length == 13 -> TipoCodigo.EAN_13

        codigoLimpio.length == 10 -> TipoCodigo.ISBN_10

        else -> TipoCodigo.DESCONOCIDO
    }
}

// Identifica el tipo de un código leído con la cámara, usando el formato que detectó ML Kit y su prefijo.
fun clasificarCodigoEscaneado(codigo: String, formato: Int): TipoCodigo {
    return when {
        formato == Barcode.FORMAT_EAN_13 &&
                (codigo.startsWith("978") || codigo.startsWith("979")) -> TipoCodigo.ISBN_13

        formato == Barcode.FORMAT_EAN_13 && codigo.startsWith("977") -> TipoCodigo.ISSN

        formato == Barcode.FORMAT_EAN_13 -> TipoCodigo.EAN_13

        formato == Barcode.FORMAT_UPC_A -> TipoCodigo.UPC_A

        else -> TipoCodigo.DESCONOCIDO
    }
}

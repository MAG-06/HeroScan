package com.example.heroscan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.heroscan.model.Comic

// Identifica el tipo de código ingresado por texto (UPC-A, EAN-13, ISBN, ISSN) según su longitud y prefijo.
class SearchViewModel : ViewModel() {

    fun clasificarCodigoTexto(code: String): String {
        val codigo = code.trim().replace("-", "").replace(" ", "")

        return when {
            codigo.length == 13 &&
                    (codigo.startsWith("978") || codigo.startsWith("979")) -> "ISBN-13"

            codigo.length == 8 -> "ISSN"

            codigo.length == 12 -> "UPC-A"

            codigo.length == 17 -> "UPC-A"  // UPC-A + suplemento de 5 dígitos

            codigo.length == 13 -> "EAN-13"

            codigo.length == 10 -> "ISBN-10"

            else -> "Desconocido"
        }
    }
}
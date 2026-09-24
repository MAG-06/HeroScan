package com.example.heroscan.model

// Tipos de código que la app sabe reconocer. "etiqueta" es el texto que se muestra en pantalla.
enum class TipoCodigo(val etiqueta: String) {
    UPC_A("UPC-A"),
    EAN_13("EAN-13"),
    ISBN_13("ISBN-13"),
    ISBN_10("ISBN-10"),
    DESCONOCIDO("Desconocido")
}

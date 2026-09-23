package com.example.heroscan.model

// Versión reducida de un cómic. Se usa para mostrar la lista cuando una búsqueda devuelve varios resultados.
data class ComicResumen(
    val id: Int,
    val titulo: String,
    val numero: String,
    val fechaPortada: String,
    val portadaUrl: String
)

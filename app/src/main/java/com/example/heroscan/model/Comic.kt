package com.example.heroscan.model

// Modelo principal del cómic que usa la app para mostrar su información en pantalla.
data class Comic(
    val id: String,
    val titulo: String,
    val numero: String,
    val editorial: String,
    val fechaPublicacion: String,
    val descripcion: String,
    val personajes: List<Personaje>,
    val creadores: List<String>,
    val codigoBarras: String,
    val tipoCodigo: TipoCodigo,
    val portadaUrl: String,
    val fuente: FuenteComic
)

// API de la que se obtuvo la información del cómic.
enum class FuenteComic { METRON }

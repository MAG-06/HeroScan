package com.example.heroscan.model

// Modelo principal del cómic que usa la app para mostrar su información en pantalla.
data class Comic(
    val id: String,
    val title: String,
    val issueNumber: String,
    val publisher: String,
    val releaseDate: String,
    val description: String,
    val characters: List<ComicCharacter>,
    val creators: List<String>,
    val barcode: String,
    val scanType: String,
    val coverUrl: String
)

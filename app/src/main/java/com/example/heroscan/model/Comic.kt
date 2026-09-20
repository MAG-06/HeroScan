package com.example.heroscan.model

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

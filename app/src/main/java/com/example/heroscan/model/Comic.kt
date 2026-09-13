package com.example.heroscan.model

data class Creator(
    val role: String,
    val name: String
)

data class Comic(
    val id: String,
    val title: String,
    val issueNumber: String,
    val publisher: String,
    val releaseDate: String,
    val description: String,
    val characters: List<String>,
    val creators: List<Creator>,
    val barcode: String,
    val scanType: String
)

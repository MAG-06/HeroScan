package com.example.heroscan.network.mapper

// Convierte el detalle de un cómic de Metron (DTO) en el modelo Comic de la app.

import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicCharacter
import com.example.heroscan.model.dto.MetronIssueDetail

fun MetronIssueDetail.toComic(
    scanType: String,
    characterDetails: List<ComicCharacter>,
    translatedDescription: String
): Comic {
    return Comic(
        id = id.toString(),
        title = series.name,
        issueNumber = number,
        publisher = publisher.name,
        releaseDate = cover_date ?: "Desconocida",
        description = translatedDescription,
        characters = characterDetails,
        creators = credits.map { it.creator },
        barcode = upc ?: "",
        scanType = scanType,
        coverUrl = image ?: ""
    )
}
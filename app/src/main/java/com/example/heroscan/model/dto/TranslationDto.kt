package com.example.heroscan.model.dto

// DTOs que reflejan la respuesta de la API de traducción MyMemory.
data class TranslationResponse(
    val responseData: TranslationData
)

data class TranslationData(
    val translatedText: String
)
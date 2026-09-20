package com.example.heroscan.model.dto

data class TranslationResponse(
    val responseData: TranslationData
)

data class TranslationData(
    val translatedText: String
)
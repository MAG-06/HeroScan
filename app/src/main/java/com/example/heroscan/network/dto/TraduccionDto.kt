package com.example.heroscan.network.dto

// DTOs que reflejan la respuesta de la API de traducción MyMemory.
// Los nombres de los campos se dejan en inglés porque deben coincidir con las claves del JSON.
data class RespuestaTraduccion(
    val responseData: DatosTraduccion
)

data class DatosTraduccion(
    val translatedText: String
)

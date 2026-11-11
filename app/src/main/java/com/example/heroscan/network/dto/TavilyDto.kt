package com.example.heroscan.network.dto

// DTOs que reflejan el JSON de la API de Tavily (search). Los campos llevan el nombre exacto del JSON, por eso algunos tienen guion bajo.

// Petición: el texto a buscar, la profundidad ("basic" gasta 1 crédito), cuántos resultados devolver y en qué páginas buscar.
data class PeticionTavily(
    val query: String,
    val search_depth: String,
    val max_results: Int,
    val include_domains: List<String>
)

// Respuesta de Tavily: la lista de páginas encontradas. Puede venir null si no llega.
data class RespuestaTavily(
    val results: List<ResultadoTavily>?
)

// Una página encontrada: su título y un resumen de su contenido.
data class ResultadoTavily(
    val title: String?,
    val content: String?
)

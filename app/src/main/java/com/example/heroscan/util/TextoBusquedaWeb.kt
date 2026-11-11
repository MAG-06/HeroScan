package com.example.heroscan.util

import com.example.heroscan.network.dto.ResultadoTavily

// Máximo de caracteres que se le mandan a Gemini, para que la petición no sea muy grande.
private const val MAXIMO_CARACTERES_TEXTO_IA = 4000

// Máximo de caracteres de cada resumen, para que un resultado largo no deje fuera a los demás.
private const val MAXIMO_CARACTERES_POR_RESUMEN = 500

// Une los resultados de la búsqueda web en un solo texto (una línea "título: resumen" por resultado), con cada resumen recortado y el total recortado al máximo.
fun armarTextoParaIA(resultados: List<ResultadoTavily>): String {
    val lineas = resultados
        .map { "${it.title?.trim() ?: ""}: ${it.content?.trim()?.take(MAXIMO_CARACTERES_POR_RESUMEN) ?: ""}" }
        .filter { it != ": " }
    return lineas.joinToString("\n").take(MAXIMO_CARACTERES_TEXTO_IA)
}

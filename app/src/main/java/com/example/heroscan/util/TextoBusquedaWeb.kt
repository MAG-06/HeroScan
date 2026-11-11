package com.example.heroscan.util

import com.example.heroscan.network.dto.ResultadoTavily

// Máximo de caracteres que se le mandan a Gemini, para que la petición no sea muy grande.
private const val MAXIMO_CARACTERES_TEXTO_IA = 4000

// Máximo de caracteres de cada resumen, para que un resultado largo no deje fuera a los demás.
private const val MAXIMO_CARACTERES_POR_RESUMEN = 500

// Une los resultados de la búsqueda web en un solo texto (una línea "título: resumen" por resultado), con cada resumen recortado y el total recortado al máximo. Les quita los < y > con quitarMarcas.
fun armarTextoParaIA(resultados: List<ResultadoTavily>): String {
    val lineas = resultados
        .map { "${quitarMarcas(it.title ?: "").trim()}: ${quitarMarcas(it.content ?: "").trim().take(MAXIMO_CARACTERES_POR_RESUMEN)}" }
        .filter { it != ": " }
    return lineas.joinToString("\n").take(MAXIMO_CARACTERES_TEXTO_IA)
}

// Cambia los < y > por espacios, para que el texto de una página no pueda cerrar la marca <resultados> que se le manda a Gemini.
private fun quitarMarcas(texto: String): String {
    return texto.replace("<", " ").replace(">", " ")
}

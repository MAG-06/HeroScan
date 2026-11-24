package com.example.heroscan.util

// Cerca de código Markdown (tres acentos graves) con la que a veces la IA envuelve el JSON.
private const val CERCA = "```"

// Quita la cerca de código alrededor del JSON que devuelve la IA (con o sin "json"); si no la tiene, lo devuelve sin espacios alrededor. Como en la guía LLM.
fun quitarCercas(texto: String): String {
    val limpio = texto.trim()
    if (!limpio.startsWith(CERCA)) return limpio
    return limpio.removePrefix(CERCA + "json").removePrefix(CERCA).removeSuffix(CERCA).trim()
}

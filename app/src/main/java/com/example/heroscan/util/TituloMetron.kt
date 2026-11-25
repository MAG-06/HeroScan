package com.example.heroscan.util

// Una búsqueda posible en Metron: el texto del título y el número (null = buscar sin número).
data class CandidatoBusqueda(val texto: String, val numero: String?)

// Deja el título como lo entiende Metron: separa la "/" con espacios y deja un solo espacio entre palabras.
fun normalizarTitulo(texto: String): String {
    return texto.replace("/", " / ")
        .split(" ")
        .filter { it.isNotEmpty() }
        .joinToString(" ")
}

// Arma, en orden, las búsquedas que se prueban en Metron con lo que dio la IA: título con y sin número,
// serie con y sin número, y lo de antes de los dos puntos. Salta las vacías y las repetidas (máximo 5).
fun candidatosBusqueda(titulo: String, numero: String, serie: String): List<CandidatoBusqueda> {
    val tituloLimpio = normalizarTitulo(titulo)
    val serieLimpia = normalizarTitulo(serie)
    val numeroLimpio = numero.trim()
    // Si no hay ":", substringBefore devuelve el título completo y ese candidato se quita por repetido.
    val antesDeDosPuntos = normalizarTitulo(tituloLimpio.substringBefore(":"))

    val posibles = listOf(
        CandidatoBusqueda(tituloLimpio, numeroLimpio),
        CandidatoBusqueda(tituloLimpio, null),
        CandidatoBusqueda(serieLimpia, numeroLimpio),
        CandidatoBusqueda(serieLimpia, null),
        CandidatoBusqueda(antesDeDosPuntos, null)
    )

    val candidatos = mutableListOf<CandidatoBusqueda>()
    for (posible in posibles) {
        if (posible.texto.isEmpty()) continue
        if (posible.numero != null && posible.numero.isEmpty()) continue
        if (yaEsta(candidatos, posible)) continue
        candidatos.add(posible)
    }
    return candidatos
}

// Dice si ya hay un candidato con el mismo texto (sin mirar mayúsculas) y el mismo número, para no repetir la petición.
private fun yaEsta(candidatos: List<CandidatoBusqueda>, posible: CandidatoBusqueda): Boolean {
    for (candidato in candidatos) {
        if (candidato.texto.equals(posible.texto, ignoreCase = true) && candidato.numero == posible.numero) return true
    }
    return false
}

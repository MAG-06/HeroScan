package com.example.heroscan.network.dto

// DTOs que reflejan el JSON de la API de Metron (búsqueda por UPC, detalle del cómic y personajes).
// Los nombres de los campos se dejan en inglés porque deben coincidir exactamente con las claves del JSON.

// === LISTA (paso 1: buscar por UPC) ===

data class RespuestaListaMetron(
    val count: Int,
    val results: List<ComicListaMetron>
)

data class ComicListaMetron(
    val id: Int,
    val series: SerieMetron,
    val number: String,
    val cover_date: String?,
    val image: String?,
    val store_date: String?
)

// === DETALLE (paso 2: obtener info completa) ===

data class DetalleComicMetron(
    val id: Int,
    val publisher: ReferenciaMetron,
    val series: SerieMetron,
    val number: String,
    val cover_date: String?,
    val desc: String?,
    val image: String?,
    val upc: String?,
    val credits: List<CreditoMetron>,
    val characters: List<PersonajeMetron>
)

data class ReferenciaMetron(
    val id: Int,
    val name: String
)

data class SerieMetron(
    val id: Int,
    val name: String
)

data class CreditoMetron(
    val id: Int,
    val creator: String
)

data class PersonajeMetron(
    val id: Int,
    val name: String,
    val image: String?
)

// === BÚSQUEDA DE PERSONAJES ===

// Cada personaje de la lista trae los mismos campos que PersonajeMetron, por eso se reutiliza
data class RespuestaPersonajesMetron(
    val count: Int,
    val results: List<PersonajeMetron>
)

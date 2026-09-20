package com.example.heroscan.model.dto

// === LISTA (paso 1: buscar por UPC) ===

data class MetronListResponse(
    val count: Int,
    val results: List<MetronIssueListItem>
)

data class MetronIssueListItem(
    val id: Int
)

// === DETALLE (paso 2: obtener info completa) ===

data class MetronIssueDetail(
    val id: Int,
    val publisher: MetronReference,
    val series: MetronSeriesSummary,
    val number: String,
    val cover_date: String?,
    val desc: String?,
    val image: String?,
    val upc: String?,
    val credits: List<MetronCredit>,
    val characters: List<MetronCharacter>
)

data class MetronReference(
    val id: Int,
    val name: String
)

data class MetronSeriesSummary(
    val id: Int,
    val name: String
)

data class MetronCredit(
    val id: Int,
    val creator: String
)

data class MetronCharacter(
    val id: Int,
    val name: String,
    val image: String?
)


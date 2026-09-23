package com.example.heroscan.repository

import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicCharacter
import com.example.heroscan.network.RetrofitClient
import com.example.heroscan.network.mapper.toComic
import com.example.heroscan.network.api.MetronApi
import com.example.heroscan.network.api.TranslationApi
import com.example.heroscan.model.dto.MetronIssueListItem

// Busca un cómic por código de barras en Metron, traduce su descripción, obtiene sus personajes y devuelve un Comic.
sealed interface SearchResult {
    data class Single(val comic: Comic) : SearchResult
    data class Multiple(val results: List<MetronIssueListItem>) : SearchResult
}

class SearchRepository {

    private val metronApi = RetrofitClient.metronApi
    private val translationApi = RetrofitClient.translationApi

    suspend fun searchByCode(code: String, scanType: String): SearchResult {
        return when (scanType) {
            "UPC-A" -> searchByUpc(code, scanType)
            "EAN-13" -> searchByUpc(code.take(12), scanType)
            else -> throw Exception("Tipo de código no soportado aún")
        }
    }

    private suspend fun searchByUpc(upc: String, scanType: String): SearchResult {
        val cleanUpc = upc.trim().replace("-", "").replace(" ", "")

        // 17 dígitos identifican un número exacto; 12 dígitos identifican la serie
        val listResponse = if (cleanUpc.length == 17) {
            metronApi.searchByExactUpc(cleanUpc)
        } else {
            metronApi.searchByUpc(cleanUpc.take(12))
        }

        if (listResponse.results.isEmpty()) {
            throw Exception("No se encontró ningún cómic con ese código")
        }

        // Si hay un solo resultado, directo al detalle
        if (listResponse.count == 1) {
            val comic = getComicDetail(listResponse.results.first().id, scanType)
            return SearchResult.Single(comic)
        }

        // Si hay varios, devolver la lista para que el usuario elija
        return SearchResult.Multiple(listResponse.results)
    }

    // Función pública para cuando el usuario elige de la lista
    suspend fun getComicDetail(issueId: Int, scanType: String): Comic {
        val detail = metronApi.getIssueDetail(issueId)

        val descriptionEs = try {
            val original = detail.desc ?: "Sin descripción"
            if (original.length > 500) {
                original
            } else {
                val response = translationApi.translate(original)
                val translated = response.responseData.translatedText
                if (translated.contains("QUERY LENGTH LIMIT", ignoreCase = true)) {
                    original
                } else {
                    translated
                }
            }
        } catch (e: Exception) {
            detail.desc ?: "Sin descripción"
        }

        val characterDetails = detail.characters.take(5).map { character ->
            try {
                val charDetail = metronApi.getCharacterDetail(character.id)
                ComicCharacter(
                    name = charDetail.name,
                    imageUrl = charDetail.image ?: ""
                )
            } catch (e: Exception) {
                ComicCharacter(name = character.name, imageUrl = "")
            }
        }

        return detail.toComic(scanType, characterDetails, descriptionEs)
    }
}
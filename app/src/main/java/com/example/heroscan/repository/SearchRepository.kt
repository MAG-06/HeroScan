package com.example.heroscan.repository

import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicCharacter
import com.example.heroscan.network.RetrofitClient
import com.example.heroscan.network.mapper.toComic

class SearchRepository {

    private val metronApi = RetrofitClient.metronApi

    suspend fun searchByCode(code: String, scanType: String): Comic {
        return when (scanType) {
            "UPC-A"   -> searchByUpc(code, scanType)
            "EAN-13"  -> searchByUpc(code.take(12), scanType)
            else      -> throw Exception("Tipo de código no soportado aún")
        }
    }

    private val translationApi = RetrofitClient.translationApi

    private suspend fun searchByUpc(upc: String, scanType: String): Comic {
        val cleanUpc = upc.take(12)
        val listResponse = metronApi.searchByUpc(cleanUpc)

        if (listResponse.results.isEmpty()) {
            throw Exception("No se encontró ningún cómic con ese código")
        }

        val issueId = listResponse.results.first().id
        val detail = metronApi.getIssueDetail(issueId)

        // Traducir descripción
        val descriptionEs = try {
            val original = detail.desc ?: "Sin descripción"
            if (original.length > 500) {
                original  // muy largo para traducir, deja el original
            } else {
                val response = translationApi.translate(original)
                val translated = response.responseData.translatedText
                if (translated.contains("QUERY LENGTH LIMIT", ignoreCase = true)) {
                    original  // MyMemory devolvió error, deja el original
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
package com.example.heroscan.network.api

import com.example.heroscan.network.dto.RespuestaTraduccion
import retrofit2.http.GET
import retrofit2.http.Query

// Endpoint de la API de traducción MyMemory.
interface TraduccionApi {

    // Traduce un texto. Por defecto traduce de inglés a español ("en|es").
    @GET("get")
    suspend fun traducir(
        @Query("q") texto: String,
        @Query("langpair") idiomas: String = "en|es"
    ): RespuestaTraduccion
}

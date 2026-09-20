package com.example.heroscan.network.api

import com.example.heroscan.model.dto.TranslationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApi {

    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String = "en|es"
    ): TranslationResponse
}
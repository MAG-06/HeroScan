package com.example.heroscan.network.api
import com.example.heroscan.model.dto.MetronListResponse
import com.example.heroscan.model.dto.MetronIssueDetail
import com.example.heroscan.model.dto.MetronCharacter
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MetronApi {

    // Buscar por UPC (prefijo, para códigos de 12 dígitos)
    @GET("api/issue/")
    suspend fun searchByUpc(
        @Query("upc_starts_with") upc: String
    ): MetronListResponse

    // Obtener detalle completo por ID
    @GET("api/issue/{id}/")
    suspend fun getIssueDetail(
        @Path("id") id: Int
    ): MetronIssueDetail

    @GET("api/character/{id}/")
    suspend fun getCharacterDetail(
        @Path("id") id: Int
    ): MetronCharacter
}
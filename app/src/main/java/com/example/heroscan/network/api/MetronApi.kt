package com.example.heroscan.network.api

import com.example.heroscan.network.dto.DetalleComicMetron
import com.example.heroscan.network.dto.PersonajeMetron
import com.example.heroscan.network.dto.RespuestaListaMetron
import com.example.heroscan.network.dto.RespuestaPersonajesMetron
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints de la API de Metron que usa la app.
interface MetronApi {

    // Busca los cómics cuyo UPC empieza con el código dado (12 dígitos: identifica la serie).
    @GET("api/issue/")
    suspend fun buscarPorUpc(@Query("upc_starts_with") upc: String): RespuestaListaMetron

    // Busca el cómic con el UPC exacto (17 dígitos: UPC + suplemento, identifica un número concreto).
    @GET("api/issue/")
    suspend fun buscarPorUpcExacto(@Query("upc") upc: String): RespuestaListaMetron

    // Obtiene toda la información de un cómic a partir de su id en Metron.
    @GET("api/issue/{id}/")
    suspend fun obtenerDetalleComic(@Path("id") id: Int): DetalleComicMetron

    // Obtiene la información de un personaje (incluida su imagen) a partir de su id en Metron.
    @GET("api/character/{id}/")
    suspend fun obtenerDetallePersonaje(@Path("id") id: Int): PersonajeMetron

    // Busca cómics cuyo nombre de serie contenga el texto dado.
    @GET("api/issue/")
    suspend fun buscarPorTitulo(@Query("series_name") titulo: String): RespuestaListaMetron

    // Busca personajes por nombre.
    @GET("api/character/")
    suspend fun buscarPersonaje(@Query("name") nombre: String): RespuestaPersonajesMetron

    // Obtiene los issues de un personaje específico.
    @GET("api/character/{id}/issue_list/")
    suspend fun obtenerIssuesPersonaje(@Path("id") id: Int): RespuestaListaMetron
}

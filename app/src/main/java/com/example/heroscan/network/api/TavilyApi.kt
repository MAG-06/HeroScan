package com.example.heroscan.network.api

import com.example.heroscan.network.dto.PeticionTavily
import com.example.heroscan.network.dto.RespuestaTavily
import retrofit2.http.Body
import retrofit2.http.POST

// Endpoint de la API de Tavily que usa la app para buscar en la web el ISBN de un cómic.
interface TavilyApi {

    // Envía la búsqueda (el texto, la profundidad y cuántos resultados) y devuelve las páginas encontradas.
    @POST("search")
    suspend fun buscarEnLaWeb(@Body peticion: PeticionTavily): RespuestaTavily
}

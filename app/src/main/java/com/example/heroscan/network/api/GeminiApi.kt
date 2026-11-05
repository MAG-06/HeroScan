package com.example.heroscan.network.api

import com.example.heroscan.network.dto.PeticionGemini
import com.example.heroscan.network.dto.RespuestaGemini
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

// Endpoint de la API de Gemini que usa la app para reconocer la portada de un cómic.
interface GeminiApi {

    // Envía la petición (instrucciones + imagen) al modelo indicado y devuelve lo que generó.
    // El modelo va en la URL con @Path; la petición va en el cuerpo (JSON) con @Body.
    @POST("v1beta/models/{modelo}:generateContent")
    suspend fun generarContenido(
        @Path("modelo") modelo: String,
        @Body peticion: PeticionGemini
    ): RespuestaGemini
}

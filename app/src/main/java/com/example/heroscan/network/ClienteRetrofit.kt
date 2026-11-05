package com.example.heroscan.network

import com.example.heroscan.BuildConfig
import com.example.heroscan.network.api.GeminiApi
import com.example.heroscan.network.api.MetronApi
import com.example.heroscan.network.api.TraduccionApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Configura las instancias de Retrofit (Metron con token de autenticación, MyMemory y Gemini con su clave en el header x-goog-api-key) y expone sus APIs.
object ClienteRetrofit {

    private const val URL_BASE_METRON = "https://metron.cloud/"
    private const val URL_BASE_TRADUCCION = "https://api.mymemory.translated.net/"

    // === METRON ===

    // Cliente HTTP que agrega el token de autenticación a cada petición que se hace a Metron.
    private val clienteMetron: OkHttpClient =
        OkHttpClient.Builder().addInterceptor(Interceptor { cadena ->

            val peticion = cadena.request().newBuilder().header(
                "Authorization",
                "Bearer ${BuildConfig.METRON_TOKEN}"
            )
                .build()
            cadena.proceed(peticion)
        })
            .build()

    private val retrofitMetron: Retrofit =
        Retrofit.Builder().baseUrl(URL_BASE_METRON).client(clienteMetron)
            .addConverterFactory(GsonConverterFactory.create()).build()

    val metronApi: MetronApi = retrofitMetron.create(MetronApi::class.java)

    // === TRADUCCIÓN (MyMemory) ===

    private val retrofitTraduccion: Retrofit =
        Retrofit.Builder().baseUrl(URL_BASE_TRADUCCION)
            .addConverterFactory(GsonConverterFactory.create()).build()

    val traduccionApi: TraduccionApi = retrofitTraduccion.create(TraduccionApi::class.java)

    // === GEMINI ===

    // Dirección base de la API de Gemini (Google).
    private const val URL_BASE_GEMINI = "https://generativelanguage.googleapis.com/"

    // Cliente HTTP que agrega la clave de Gemini a cada petición (header x-goog-api-key).
    // El tiempo de lectura se sube a 30 s porque analizar una imagen puede tardar más que los 10 s por defecto.
    private val clienteGemini: OkHttpClient =
        OkHttpClient.Builder().addInterceptor(Interceptor { cadena ->

            val peticion = cadena.request().newBuilder().header(
                "x-goog-api-key",
                BuildConfig.GEMINI_API_KEY
            )
                .build()
            cadena.proceed(peticion)
        })
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    private val retrofitGemini: Retrofit =
        Retrofit.Builder().baseUrl(URL_BASE_GEMINI).client(clienteGemini)
            .addConverterFactory(GsonConverterFactory.create()).build()

    val geminiApi: GeminiApi = retrofitGemini.create(GeminiApi::class.java)

}
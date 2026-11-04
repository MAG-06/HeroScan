package com.example.heroscan.network

import com.example.heroscan.BuildConfig
import com.example.heroscan.network.api.MetronApi
import com.example.heroscan.network.api.TraduccionApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Configura las instancias de Retrofit (Metron con token de autenticación y MyMemory) y expone sus APIs.
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

}
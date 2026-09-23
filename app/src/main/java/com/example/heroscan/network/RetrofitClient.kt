package com.example.heroscan.network

import com.example.heroscan.network.api.MetronApi
import com.example.heroscan.network.api.TranslationApi
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Configura las instancias de Retrofit (Metron con token de autenticación y MyMemory) y expone sus APIs.
object RetrofitClient {

    private const val METRON_BASE_URL = "https://metron.cloud/"


    private val metronClient: OkHttpClient =
        OkHttpClient.Builder().addInterceptor(Interceptor { chain ->

            val request = chain.request().newBuilder().header(
                "Authorization",
                "Bearer fa983adb4227b326ebb5eef1b847875f345b27ce2d7bcc91e5068e17d3e6b255"
            )
                .build()
            chain.proceed(request)
        })
            .build()

    private val metronRetrofit: Retrofit =
        Retrofit.Builder().baseUrl(METRON_BASE_URL).client(metronClient)
            .addConverterFactory(GsonConverterFactory.create()).build()

    val metronApi: MetronApi = metronRetrofit.create(MetronApi::class.java)

    // === TRANSLATION (MyMemory) ===
    private val translationRetrofit: Retrofit =
        Retrofit.Builder().baseUrl("https://api.mymemory.translated.net/")
            .addConverterFactory(GsonConverterFactory.create()).build()

    val translationApi: TranslationApi = translationRetrofit.create(TranslationApi::class.java)
}
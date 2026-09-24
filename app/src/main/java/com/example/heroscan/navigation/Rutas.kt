package com.example.heroscan.navigation

import com.example.heroscan.model.Comic
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

// Nombres de las rutas de navegación de la app.
object Rutas {
    const val INICIO = "inicio"
    const val ESCANEO = "escaneo"
    const val ESCANEO_PORTADA = "escaneoPortada"
    // La búsqueda puede recibir un código opcional (cuando se abre desde el escáner con varios resultados)
    const val ARGUMENTO_CODIGO = "codigo"
    const val BUSQUEDA = "busqueda?$ARGUMENTO_CODIGO={$ARGUMENTO_CODIGO}"
    const val ARGUMENTO_COMIC = "comicJson"
    const val DETALLE_COMIC = "detalleComic/{$ARGUMENTO_COMIC}"
}

private val gson = Gson()

// Crea la ruta a la búsqueda. Sin código abre la pantalla vacía; con código, la pantalla lo busca al abrirse.
fun crearRutaBusqueda(codigo: String? = null): String {
    return if (codigo == null) "busqueda" else "busqueda?${Rutas.ARGUMENTO_CODIGO}=$codigo"
}

// Crea la ruta al detalle convirtiendo el cómic a JSON (codificado para que sea válido dentro de la ruta).
fun crearRutaDetalleComic(comic: Comic): String {
    val comicJson = URLEncoder.encode(gson.toJson(comic), "UTF-8")
    return "detalleComic/$comicJson"
}

// Reconstruye el cómic a partir del JSON recibido como argumento en la ruta de detalle.
fun obtenerComicDeRuta(comicJson: String): Comic {
    val comicJsonDecodificado = URLDecoder.decode(comicJson, "UTF-8")
    return gson.fromJson(comicJsonDecodificado, Comic::class.java)
}

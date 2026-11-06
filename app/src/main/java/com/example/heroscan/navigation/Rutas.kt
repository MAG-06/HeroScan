package com.example.heroscan.navigation

import android.net.Uri
import com.example.heroscan.model.Comic
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

// Nombres de las rutas de navegación de la app.
object Rutas {
    // Pantalla de Acceso (iniciar sesión / crear cuenta). Es la primera si no hay sesión.
    const val ACCESO = "acceso"
    // Pantalla principal. Es la base de la pila cuando hay sesión.
    const val INICIO = "inicio"
    // Pestaña Perfil (correo de la cuenta y cerrar sesión).
    const val PERFIL = "perfil"
    const val ESCANEO = "escaneo"
    const val ESCANEO_PORTADA = "escaneoPortada"
    // La búsqueda puede recibir un código opcional (cuando se abre desde el escáner con varios resultados)
    const val ARGUMENTO_CODIGO = "codigo"
    // La búsqueda también puede recibir un título opcional (cuando se abre desde la búsqueda por portada con varios resultados)
    const val ARGUMENTO_TITULO = "titulo"
    // Ruta de la búsqueda con sus dos argumentos opcionales (código y título)
    const val BUSQUEDA = "busqueda?$ARGUMENTO_CODIGO={$ARGUMENTO_CODIGO}&$ARGUMENTO_TITULO={$ARGUMENTO_TITULO}"
    const val ARGUMENTO_COMIC = "comicJson"
    const val DETALLE_COMIC = "detalleComic/{$ARGUMENTO_COMIC}"
}

private val gson = Gson()

// Crea la ruta a la búsqueda. Sin nada abre la pantalla vacía; con código o título, la pantalla lo busca al abrirse.
// Solo agrega a la ruta los argumentos que no son null. El título se codifica con Uri.encode porque puede
// traer espacios, "&" o ":", que romperían la ruta; Navigation lo decodifica solo al leerlo.
fun crearRutaBusqueda(codigo: String? = null, titulo: String? = null): String {
    var ruta = "busqueda"
    // El primer argumento va después de "?" y los siguientes después de "&"
    var separador = "?"

    if (codigo != null) {
        ruta += "$separador${Rutas.ARGUMENTO_CODIGO}=$codigo"
        separador = "&"
    }

    if (titulo != null) {
        ruta += "$separador${Rutas.ARGUMENTO_TITULO}=${Uri.encode(titulo)}"
    }

    return ruta
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

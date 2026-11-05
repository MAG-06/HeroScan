package com.example.heroscan.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

// Tamaño máximo (en píxeles) del lado mayor de la imagen que se manda a Gemini.
// Alcanza para reconocer la portada y mantiene la petición pequeña y rápida.
private const val LADO_MAXIMO_PX = 1024

// Calidad JPEG (de 0 a 100) con la que se comprime la imagen.
// 80 casi no se nota a la vista y pesa bastante menos que 100.
private const val CALIDAD_JPEG = 80

// Convierte la imagen elegida de la galería (su Uri) en un texto Base64 listo para mandar a Gemini.
// Antes la reduce a máximo 1024 px en el lado mayor y la comprime en JPEG para que no pese tanto.
// Recibe un Context porque necesita el contentResolver para abrir la imagen a partir de su Uri.
fun convertirImagenABase64(context: Context, uri: Uri): String {
    val resolvedor = context.contentResolver

    // Paso 1: leer solo el tamaño de la imagen, sin cargarla en memoria
    val opcionesTamano = BitmapFactory.Options()
    opcionesTamano.inJustDecodeBounds = true
    val streamTamano = resolvedor.openInputStream(uri) ?: throw Exception("No se pudo leer la imagen")
    streamTamano.use { stream ->
        BitmapFactory.decodeStream(stream, null, opcionesTamano)
    }

    val ancho = opcionesTamano.outWidth
    val alto = opcionesTamano.outHeight
    if (ancho <= 0 || alto <= 0) {
        throw Exception("No se pudo leer la imagen")
    }

    // Paso 2: calcular cuánto achicarla al decodificar (siempre potencia de 2: 1, 2, 4, 8...),
    // sin bajar del lado máximo, y decodificarla ya pequeña abriendo el stream otra vez
    var muestreo = 1
    while (maxOf(ancho, alto) / (muestreo * 2) >= LADO_MAXIMO_PX) {
        muestreo *= 2
    }

    val opcionesDecodificar = BitmapFactory.Options()
    opcionesDecodificar.inSampleSize = muestreo
    val streamImagen = resolvedor.openInputStream(uri) ?: throw Exception("No se pudo leer la imagen")
    val imagenMuestreada = streamImagen.use { stream ->
        BitmapFactory.decodeStream(stream, null, opcionesDecodificar)
    } ?: throw Exception("No se pudo leer la imagen")

    // Paso 3: si todavía pasa del lado máximo, escalarla manteniendo la proporción
    val ladoMayor = maxOf(imagenMuestreada.width, imagenMuestreada.height)
    val imagenFinal = if (ladoMayor > LADO_MAXIMO_PX) {
        val escala = LADO_MAXIMO_PX.toFloat() / ladoMayor
        val anchoFinal = maxOf(1, (imagenMuestreada.width * escala).toInt())
        val altoFinal = maxOf(1, (imagenMuestreada.height * escala).toInt())
        Bitmap.createScaledBitmap(imagenMuestreada, anchoFinal, altoFinal, true)
    } else {
        imagenMuestreada
    }

    // Paso 4: comprimir en JPEG y pasar los bytes a texto Base64 (sin saltos de línea)
    val bytes = ByteArrayOutputStream().use { salida ->
        imagenFinal.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        salida.toByteArray()
    }

    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

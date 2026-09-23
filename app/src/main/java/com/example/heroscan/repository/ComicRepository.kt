package com.example.heroscan.repository

import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.Personaje
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.network.ClienteRetrofit
import com.example.heroscan.network.dto.PersonajeMetron
import com.example.heroscan.network.mapper.aComic
import com.example.heroscan.network.mapper.aComicResumen
import com.example.heroscan.util.limpiarCodigo

// Resultado de una búsqueda: un solo cómic (ya con todo su detalle) o varios para que el usuario elija.
sealed interface ResultadoBusqueda {
    data class Unico(val comic: Comic) : ResultadoBusqueda
    data class Varios(val resultados: List<ComicResumen>) : ResultadoBusqueda
}

// Obtiene la información de los cómics desde Metron y traduce su descripción con MyMemory.
class ComicRepository {

    private val metronApi = ClienteRetrofit.metronApi
    private val traduccionApi = ClienteRetrofit.traduccionApi

    // Busca un cómic por su código de barras. Por ahora solo se soportan UPC-A y EAN-13.
    suspend fun buscarPorCodigo(codigo: String, tipoCodigo: TipoCodigo): ResultadoBusqueda {
        val codigoLimpio = limpiarCodigo(codigo)

        return when (tipoCodigo) {
            TipoCodigo.UPC_A -> buscarPorUpc(codigoLimpio, tipoCodigo)
            TipoCodigo.EAN_13 -> buscarPorUpc(codigoLimpio.take(12), tipoCodigo)
            else -> throw Exception("Tipo de código no soportado aún")
        }
    }

    // Consulta Metron por UPC. Con 17 dígitos busca el número exacto; con 12 busca todos los números de la serie.
    private suspend fun buscarPorUpc(upc: String, tipoCodigo: TipoCodigo): ResultadoBusqueda {
        val respuesta = if (upc.length == 17) {
            metronApi.buscarPorUpcExacto(upc)
        } else {
            metronApi.buscarPorUpc(upc.take(12))
        }

        if (respuesta.results.isEmpty()) {
            throw Exception("No se encontró ningún cómic con ese código")
        }

        // Si hay un solo resultado, se trae su detalle directamente
        if (respuesta.count == 1) {
            val comic = obtenerDetalleComic(respuesta.results.first().id, tipoCodigo)
            return ResultadoBusqueda.Unico(comic)
        }

        // Si hay varios, se devuelve la lista para que el usuario elija
        return ResultadoBusqueda.Varios(respuesta.results.map { it.aComicResumen() })
    }

    // Obtiene el detalle completo de un cómic: sus datos, la descripción traducida y sus personajes.
    // Es pública porque también se usa cuando el usuario elige un cómic de la lista de resultados.
    suspend fun obtenerDetalleComic(idComic: Int, tipoCodigo: TipoCodigo): Comic {
        val detalle = metronApi.obtenerDetalleComic(idComic)
        val descripcion = traducirDescripcion(detalle.desc ?: "Sin descripción")
        val personajes = obtenerPersonajes(detalle.characters)

        return detalle.aComic(tipoCodigo, personajes, descripcion)
    }

    // Traduce la descripción al español. Si es muy larga o la traducción falla, devuelve el texto original.
    private suspend fun traducirDescripcion(textoOriginal: String): String {
        if (textoOriginal.length > 500) {
            return textoOriginal
        }

        return try {
            val textoTraducido = traduccionApi.traducir(textoOriginal).responseData.translatedText
            if (textoTraducido.contains("QUERY LENGTH LIMIT", ignoreCase = true)) {
                textoOriginal
            } else {
                textoTraducido
            }
        } catch (e: Exception) {
            textoOriginal
        }
    }

    // Consulta hasta 5 personajes para obtener su imagen. Si falla alguno, se usa solo su nombre.
    private suspend fun obtenerPersonajes(personajesMetron: List<PersonajeMetron>): List<Personaje> {
        return personajesMetron.take(5).map { personaje ->
            try {
                val detallePersonaje = metronApi.obtenerDetallePersonaje(personaje.id)
                Personaje(
                    nombre = detallePersonaje.name,
                    imagenUrl = detallePersonaje.image ?: ""
                )
            } catch (e: Exception) {
                Personaje(nombre = personaje.name, imagenUrl = "")
            }
        }
    }
}

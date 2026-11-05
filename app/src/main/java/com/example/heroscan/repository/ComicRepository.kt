package com.example.heroscan.repository

import com.example.heroscan.BuildConfig
import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.Personaje
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.network.api.GeminiApi
import com.example.heroscan.network.api.MetronApi
import com.example.heroscan.network.api.TraduccionApi
import com.example.heroscan.network.dto.ConfiguracionGemini
import com.example.heroscan.network.dto.ContenidoGemini
import com.example.heroscan.network.dto.DatosEnLineaGemini
import com.example.heroscan.network.dto.DatosPortadaGemini
import com.example.heroscan.network.dto.EsquemaGemini
import com.example.heroscan.network.dto.ParteGemini
import com.example.heroscan.network.dto.PeticionGemini
import com.example.heroscan.network.dto.PersonajeMetron
import com.example.heroscan.network.dto.RespuestaListaMetron
import com.example.heroscan.network.mapper.aComic
import com.example.heroscan.network.mapper.aComicResumen
import com.example.heroscan.util.limpiarCodigo
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException

// Resultado de una búsqueda: un solo cómic (ya con todo su detalle) o varios para que el usuario elija.
sealed interface ResultadoBusqueda {
    data class Unico(val comic: Comic) : ResultadoBusqueda
    data class Varios(val resultados: List<ComicResumen>) : ResultadoBusqueda
}

// Resultado de buscar por portada: el título que reconoció la IA y lo que encontró Metron con él.
// Se guarda el título porque, si hay varios resultados, la pantalla de búsqueda se abre con ese título.
data class ResultadoPortada(val titulo: String, val resultado: ResultadoBusqueda)

// Instrucciones que se le mandan a Gemini junto con la imagen de la portada.
// Es una constante de este archivo (como un "private static final" de Java): no cambia nunca.
private const val INSTRUCCIONES_PORTADA =
    "Identifica el cómic de esta portada. " +
        "titulo: nombre de la serie tal como se publicó originalmente en inglés (edición de EE. UU.), sin número ni año. " +
        "numero: solo el número de la edición (por ejemplo \"1\"); vacío si no se ve. " +
        "editorial: la editorial de la edición de la portada. " +
        "Si la imagen no es una portada de cómic o no lo reconoces, devuelve los tres campos vacíos."

// Obtiene la información de los cómics desde Metron y traduce su descripción con MyMemory.
// También reconoce portadas con Gemini y busca en Metron el cómic que encontró. Recibe las APIs por constructor.
class ComicRepository(
    private val metronApi: MetronApi,
    private val traduccionApi: TraduccionApi,
    private val geminiApi: GeminiApi
) {

    // Se usa para leer por segunda vez el JSON que Gemini devuelve como texto.
    private val gson = Gson()

    // Busca un cómic por su código de barras. Por ahora solo se soportan UPC-A y EAN-13.
    suspend fun buscarPorCodigo(codigo: String, tipoCodigo: TipoCodigo): ResultadoBusqueda {
        val codigoLimpio = limpiarCodigo(codigo)

        return when (tipoCodigo) {
            TipoCodigo.UPC_A -> buscarPorUpc(codigoLimpio, tipoCodigo)
            TipoCodigo.EAN_13 -> buscarPorUpc(codigoLimpio.take(12), tipoCodigo)
            else -> throw Exception("Tipo de código ${tipoCodigo.etiqueta} no soportado aún")
        }
    }

    // Consulta Metron por UPC. Con 17 dígitos busca el número exacto; con 12 busca todos los números de la serie.
    private suspend fun buscarPorUpc(upc: String, tipoCodigo: TipoCodigo): ResultadoBusqueda {
        val respuesta = if (upc.length == 17) {
            metronApi.buscarPorUpcExacto(upc)
        } else {
            metronApi.buscarPorUpc(upc.take(12))
        }

        return procesarRespuesta(respuesta, tipoCodigo, "No se encontró ningún cómic con ese código")
    }

    // Decide qué devolver según la respuesta de Metron: error si está vacía,
    // el detalle si hay un solo cómic, o la lista si hay varios.
    // La usan todas las búsquedas (código, título y personaje) para no repetir esta lógica.
    private suspend fun procesarRespuesta(
        respuesta: RespuestaListaMetron,
        tipoCodigo: TipoCodigo,
        mensajeSinResultados: String
    ): ResultadoBusqueda {
        if (respuesta.results.isEmpty()) {
            throw Exception(mensajeSinResultados)
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

    // Busca cómics por título de serie en Metron. Si se pasa un número, busca solo ese número de la serie.
    suspend fun buscarPorTitulo(titulo: String, numero: String? = null): ResultadoBusqueda {
        val respuesta = metronApi.buscarPorTitulo(titulo, numero)
        return procesarRespuesta(respuesta, TipoCodigo.DESCONOCIDO, "No se encontró ningún cómic con ese título")
    }

    // Busca los cómics en los que aparece un personaje: primero busca el personaje por nombre
    // y luego trae la lista de cómics del primero que coincida.
    suspend fun buscarPorPersonaje(personaje: String): ResultadoBusqueda {
        // Paso 1: buscar el personaje por nombre
        val personajes = metronApi.buscarPersonaje(personaje)

        if (personajes.results.isEmpty()) {
            throw Exception("No se encontró ningún personaje con ese nombre")
        }

        // Tomar el primer personaje que coincida
        val personajeId = personajes.results.first().id

        // Paso 2: obtener sus issues
        val respuesta = metronApi.obtenerIssuesPersonaje(personajeId)
        return procesarRespuesta(respuesta, TipoCodigo.DESCONOCIDO, "No se encontraron cómics para ese personaje")
    }

    // Busca un cómic a partir de la foto de su portada (en Base64): Gemini reconoce el título y el número,
    // y luego se buscan en Metron. Si con el número no hay resultados, se reintenta solo con el título.
    suspend fun buscarPorPortada(imagenBase64: String): ResultadoPortada {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            throw Exception("Falta configurar la clave de Gemini")
        }

        val datos = identificarPortada(imagenBase64)

        val titulo = datos.titulo?.trim() ?: ""
        val numero = datos.numero?.trim() ?: ""

        if (titulo.isEmpty()) {
            throw Exception("No se reconoció el cómic en la imagen")
        }

        val resultado = try {
            buscarPorTitulo(titulo, numero.ifBlank { null })
        } catch (e: Exception) {
            // Si había número, se prueba solo con el título; si no, el error se deja pasar
            if (numero.isNotBlank()) {
                buscarPorTitulo(titulo)
            } else {
                throw e
            }
        }

        return ResultadoPortada(titulo, resultado)
    }

    // Manda la portada a Gemini y devuelve el título, número y editorial que reconoció.
    // Gemini devuelve el JSON pedido como texto dentro de la respuesta, por eso se lee una segunda vez con Gson.
    private suspend fun identificarPortada(imagenBase64: String): DatosPortadaGemini {
        val peticion = PeticionGemini(
            contents = listOf(
                ContenidoGemini(
                    parts = listOf(
                        ParteGemini(text = INSTRUCCIONES_PORTADA),
                        ParteGemini(inlineData = DatosEnLineaGemini(mimeType = "image/jpeg", data = imagenBase64))
                    )
                )
            ),
            generationConfig = ConfiguracionGemini(
                responseMimeType = "application/json",
                responseSchema = EsquemaGemini(
                    type = "OBJECT",
                    properties = mapOf(
                        "titulo" to EsquemaGemini(type = "STRING"),
                        "numero" to EsquemaGemini(type = "STRING"),
                        "editorial" to EsquemaGemini(type = "STRING")
                    ),
                    required = listOf("titulo", "numero", "editorial")
                )
            )
        )

        val respuesta = try {
            geminiApi.generarContenido(BuildConfig.GEMINI_MODEL, peticion)
        } catch (e: HttpException) {
            throw Exception(mensajeErrorGemini(e.code()))
        }

        // Cada paso puede venir vacío, por eso se usa ?. en toda la cadena
        val texto = respuesta.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("La IA no pudo leer la portada")

        // Segunda lectura: el texto es un JSON con titulo, numero y editorial
        val datos = try {
            gson.fromJson(texto, DatosPortadaGemini::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }

        return datos ?: throw Exception("La IA no pudo leer la portada")
    }

    // Convierte el código de error HTTP de Gemini en un mensaje en español para el usuario.
    private fun mensajeErrorGemini(codigo: Int): String {
        return when (codigo) {
            400 -> "Petición inválida a la IA"
            403 -> "Clave de Gemini inválida o sin permisos"
            404 -> "Modelo de Gemini no encontrado"
            429 -> "Límite de uso de la IA alcanzado, intenta más tarde"
            503 -> "El servicio de la IA está saturado, intenta más tarde"
            else -> "Error al consultar la IA (código $codigo)"
        }
    }
}

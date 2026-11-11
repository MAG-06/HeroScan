package com.example.heroscan.repository

import com.example.heroscan.BuildConfig
import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.Personaje
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.network.api.GeminiApi
import com.example.heroscan.network.api.MetronApi
import com.example.heroscan.network.api.TavilyApi
import com.example.heroscan.network.api.TraduccionApi
import com.example.heroscan.network.dto.ConfiguracionGemini
import com.example.heroscan.network.dto.ContenidoGemini
import com.example.heroscan.network.dto.DatosEnLineaGemini
import com.example.heroscan.network.dto.DatosPortadaGemini
import com.example.heroscan.network.dto.EsquemaGemini
import com.example.heroscan.network.dto.ParteGemini
import com.example.heroscan.network.dto.PeticionGemini
import com.example.heroscan.network.dto.PersonajeMetron
import com.example.heroscan.network.dto.PeticionTavily
import com.example.heroscan.network.dto.RespuestaListaMetron
import com.example.heroscan.network.mapper.aComic
import com.example.heroscan.network.mapper.aComicResumen
import com.example.heroscan.util.armarTextoParaIA
import com.example.heroscan.util.limpiarCodigo
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import kotlin.coroutines.cancellation.CancellationException

// Resultado de una búsqueda: un solo cómic (su id y su tipo de código, para abrir el detalle) o varios para que el usuario elija.
sealed interface ResultadoBusqueda {
    data class Unico(val idComic: Int, val tipoCodigo: TipoCodigo) : ResultadoBusqueda
    data class Varios(val resultados: List<ComicResumen>) : ResultadoBusqueda
}

// Resultado de buscar por portada o por ISBN en la web: el título que reconoció la IA y lo que encontró Metron con él.
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

// Instrucciones que se le mandan a Gemini junto con los resultados de buscar el ISBN en la web.
private const val INSTRUCCIONES_CODIGO_WEB =
    "Estos son resultados de buscar en la web el código de barras (ISBN) de un cómic. Pueden estar en español u otro idioma. " +
        "Algunas páginas pueden mezclar el cómic con otro libro que tiene el mismo código: usa solo la información del cómic. " +
        "titulo: nombre ORIGINAL en inglés de la serie o novela gráfica, tal como se publicó en EE. UU., sin número ni año. " +
        "numero: solo el número de la edición original si el libro es un único número; vacío si es un tomo que reúne varios números o si no se sabe. " +
        "editorial: la editorial de la edición original en inglés; vacío si no se sabe. " +
        "Si los resultados no hablan de un cómic o no lo reconoces, devuelve los tres campos vacíos."

// Mensaje cuando un error no trae texto propio. Lo usan los ViewModels de escaneo, búsqueda, portada y detalle.
const val MENSAJE_ERROR_DESCONOCIDO = "Error desconocido"

// Mensaje cuando no hay internet o Metron tarda demasiado. Lo usan escaneo, búsqueda y detalle.
const val MENSAJE_SIN_CONEXION_COMICS = "No hay conexión o el servidor tardó demasiado. Intenta de nuevo."

// Texto cuando falta la clave de Gemini (lo usan buscar por portada y buscar un ISBN en la web).
private const val MENSAJE_FALTA_CLAVE_GEMINI = "Falta configurar la clave de Gemini"

// Páginas donde se busca un ISBN-13: tiendas y catálogos de cómics y libros de Colombia y España, más AbeBooks (internacional). Buscando en toda la web, Tavily trae listas de libros que no tienen que ver.
// Para buscar en más páginas (por ejemplo, de otros países), agrega su dominio a esta lista, sin "https://" ni "www.".
private val DOMINIOS_BUSQUEDA_WEB = listOf(
    "mercadolibre.com.co",
    "whakoom.com",
    "tebeosfera.com",
    "todocoleccion.net",
    "abebooks.com",
    "iberlibro.com",
    "casadellibro.com",
    "buscalibre.com.co"
)

// Obtiene la información de los cómics desde Metron y traduce su descripción con MyMemory.
// También reconoce portadas con Gemini y busca en la web (Tavily) los ISBN-13; en los dos casos busca en Metron el cómic que encontró. Recibe las APIs por constructor.
class ComicRepository(
    private val metronApi: MetronApi,
    private val traduccionApi: TraduccionApi,
    private val geminiApi: GeminiApi,
    private val tavilyApi: TavilyApi
) {

    // Se usa para leer por segunda vez el JSON que Gemini devuelve como texto.
    private val gson = Gson()

    // Busca un cómic por su código de barras en Metron (UPC-A y EAN-13). Los ISBN-13 se buscan con buscarCodigoEnLaWeb.
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
        val respuesta = try {
            if (upc.length == 17) {
                metronApi.buscarPorUpcExacto(upc)
            } else {
                metronApi.buscarPorUpc(upc.take(12))
            }
        } catch (e: HttpException) {
            // Si Metron responde con un error HTTP, se cambia por un mensaje en español
            throw Exception(mensajeErrorMetron(e.code()))
        }

        return procesarRespuesta(respuesta, tipoCodigo, "No se encontró ningún cómic con ese código")
    }

    // Decide qué devolver según la respuesta de Metron: error si está vacía,
    // el id si hay un solo cómic, o la lista si hay varios.
    // La usan todas las búsquedas (código, título y personaje) para no repetir esta lógica.
    private suspend fun procesarRespuesta(
        respuesta: RespuestaListaMetron,
        tipoCodigo: TipoCodigo,
        mensajeSinResultados: String
    ): ResultadoBusqueda {
        if (respuesta.results.isEmpty()) {
            throw Exception(mensajeSinResultados)
        }

        // Si hay un solo resultado, se devuelve su id: el detalle lo busca al abrirse
        if (respuesta.count == 1) {
            return ResultadoBusqueda.Unico(respuesta.results.first().id, tipoCodigo)
        }

        // Si hay varios, se devuelve la lista para que el usuario elija
        return ResultadoBusqueda.Varios(respuesta.results.map { it.aComicResumen() })
    }

    // Obtiene el detalle completo de un cómic: sus datos, la descripción traducida y sus personajes.
    // La usa DetalleViewModel al abrir la pantalla de detalle.
    suspend fun obtenerDetalleComic(idComic: Int, tipoCodigo: TipoCodigo): Comic {
        val detalle = try {
            metronApi.obtenerDetalleComic(idComic)
        } catch (e: HttpException) {
            // Si Metron responde con un error HTTP, se cambia por un mensaje en español
            throw Exception(mensajeErrorMetron(e.code()))
        }
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
        } catch (e: CancellationException) {
            throw e
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Personaje(nombre = personaje.name, imagenUrl = "")
            }
        }
    }

    // Busca cómics por título de serie en Metron. Si se pasa un número, busca solo ese número de la serie.
    suspend fun buscarPorTitulo(titulo: String, numero: String? = null): ResultadoBusqueda {
        val respuesta = try {
            metronApi.buscarPorTitulo(titulo, numero)
        } catch (e: HttpException) {
            // Si Metron responde con un error HTTP, se cambia por un mensaje en español
            throw Exception(mensajeErrorMetron(e.code()))
        }
        return procesarRespuesta(respuesta, TipoCodigo.DESCONOCIDO, "No se encontró ningún cómic con ese título")
    }

    // Busca los cómics en los que aparece un personaje: primero busca el personaje por nombre
    // y luego trae la lista de cómics del primero que coincida.
    suspend fun buscarPorPersonaje(personaje: String): ResultadoBusqueda {
        // Paso 1: buscar el personaje por nombre
        val personajes = try {
            metronApi.buscarPersonaje(personaje)
        } catch (e: HttpException) {
            // Si Metron responde con un error HTTP, se cambia por un mensaje en español
            throw Exception(mensajeErrorMetron(e.code()))
        }

        if (personajes.results.isEmpty()) {
            throw Exception("No se encontró ningún personaje con ese nombre")
        }

        // Tomar el primer personaje que coincida
        val personajeId = personajes.results.first().id

        // Paso 2: obtener sus issues
        val respuesta = try {
            metronApi.obtenerIssuesPersonaje(personajeId)
        } catch (e: HttpException) {
            // Si Metron responde con un error HTTP, se cambia por un mensaje en español
            throw Exception(mensajeErrorMetron(e.code()))
        }
        return procesarRespuesta(respuesta, TipoCodigo.DESCONOCIDO, "No se encontraron cómics para ese personaje")
    }

    // Busca un cómic a partir de la foto de su portada (en Base64): Gemini reconoce el título y el número,
    // y luego se buscan en Metron. Si con el número no hay resultados, se reintenta solo con el título.
    suspend fun buscarPorPortada(imagenBase64: String): ResultadoPortada {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            throw Exception(MENSAJE_FALTA_CLAVE_GEMINI)
        }

        val datos = identificarPortada(imagenBase64)

        val titulo = datos.titulo?.trim() ?: ""
        val numero = datos.numero?.trim() ?: ""

        if (titulo.isEmpty()) {
            throw Exception("No se reconoció el cómic en la imagen")
        }

        val resultado = try {
            buscarPorTitulo(titulo, numero.ifBlank { null })
        } catch (e: CancellationException) {
            throw e
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

    // Busca un ISBN-13 en la web: Tavily trae las páginas, Gemini reconoce el título original y Metron lo confirma.
    // Revisa el token de Metron y las claves de Tavily y Gemini antes de cualquier petición, para no gastar una búsqueda web que después no se pueda usar.
    suspend fun buscarCodigoEnLaWeb(codigo: String): ResultadoPortada {
        if (BuildConfig.METRON_TOKEN.isBlank()) {
            throw Exception("Falta configurar el token de Metron o no es válido")
        }
        if (BuildConfig.TAVILY_API_KEY.isBlank()) {
            throw Exception("Falta configurar la clave de Tavily")
        }
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            throw Exception(MENSAJE_FALTA_CLAVE_GEMINI)
        }

        val respuestaWeb = try {
            tavilyApi.buscarEnLaWeb(
                PeticionTavily(
                    query = limpiarCodigo(codigo),
                    search_depth = "basic",
                    max_results = 5,
                    include_domains = DOMINIOS_BUSQUEDA_WEB
                )
            )
        } catch (e: HttpException) {
            throw Exception(mensajeErrorTavily(e.code()))
        }

        val textoResultados = armarTextoParaIA(respuestaWeb.results ?: emptyList())
        if (textoResultados.isBlank()) {
            throw Exception("No se encontró el cómic en la web")
        }

        val datos = identificarDesdeTexto(textoResultados)

        val titulo = datos.titulo?.trim() ?: ""
        val numero = datos.numero?.trim() ?: ""

        if (titulo.isEmpty()) {
            throw Exception("No se encontró el cómic en la web")
        }

        val resultado = try {
            buscarPorTitulo(titulo, numero.ifBlank { null })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Si había número, se prueba solo con el título; si no, el error se deja pasar
            if (numero.isNotBlank()) {
                buscarPorTitulo(titulo)
            } else {
                throw e
            }
        }

        val resultadoConTipo = when (resultado) {
            is ResultadoBusqueda.Unico -> ResultadoBusqueda.Unico(resultado.idComic, TipoCodigo.ISBN_13)
            is ResultadoBusqueda.Varios -> resultado
        }

        return ResultadoPortada(titulo, resultadoConTipo)
    }

    // Manda la portada a Gemini y devuelve el título, número y editorial que reconoció.
    private suspend fun identificarPortada(imagenBase64: String): DatosPortadaGemini {
        return pedirDatosAGemini(
            partes = listOf(
                ParteGemini(text = INSTRUCCIONES_PORTADA),
                ParteGemini(inlineData = DatosEnLineaGemini(mimeType = "image/jpeg", data = imagenBase64))
            ),
            mensajeIlegible = "La IA no pudo leer la portada"
        )
    }

    // Manda a Gemini los resultados de la búsqueda web, entre <resultados> y </resultados> para separarlos de las instrucciones, y devuelve el título original, número y editorial que reconoció.
    private suspend fun identificarDesdeTexto(textoResultados: String): DatosPortadaGemini {
        return pedirDatosAGemini(
            partes = listOf(
                ParteGemini(text = INSTRUCCIONES_CODIGO_WEB),
                ParteGemini(text = "<resultados>\n$textoResultados\n</resultados>")
            ),
            mensajeIlegible = "La IA no pudo identificar el cómic"
        )
    }

    // Manda a Gemini las partes indicadas pidiendo un JSON con titulo, numero y editorial, y lo lee con Gson.
    // Si la respuesta no se puede leer, lanza mensajeIlegible (cada búsqueda tiene el suyo).
    private suspend fun pedirDatosAGemini(partes: List<ParteGemini>, mensajeIlegible: String): DatosPortadaGemini {
        val peticion = PeticionGemini(
            contents = listOf(
                ContenidoGemini(parts = partes)
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
            ?: throw Exception(mensajeIlegible)

        // Segunda lectura: el texto es un JSON con titulo, numero y editorial
        val datos = try {
            gson.fromJson(texto, DatosPortadaGemini::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }

        return datos ?: throw Exception(mensajeIlegible)
    }

    // Traduce el código de error HTTP de Metron a un mensaje en español (401/403 token, 404 no encontrado, 429 demasiadas peticiones, 500 o más servidor caído).
    private fun mensajeErrorMetron(codigo: Int): String {
        return when (codigo) {
            401, 403 -> "Falta configurar el token de Metron o no es válido"
            404 -> "No se encontró el cómic en Metron"
            429 -> "Demasiadas búsquedas seguidas. Espera un minuto e intenta de nuevo."
            in 500..599 -> "Metron no está respondiendo. Intenta más tarde."
            else -> "Error al consultar Metron (código $codigo)"
        }
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

    // Convierte el código de error HTTP de Tavily en un mensaje en español para el usuario.
    private fun mensajeErrorTavily(codigo: Int): String {
        return when (codigo) {
            401 -> "Falta configurar la clave de Tavily o no es válida"
            429 -> "Demasiadas búsquedas web seguidas. Espera un momento e intenta de nuevo."
            432, 433 -> "Se acabaron las búsquedas web gratis de este mes"
            in 500..599 -> "El buscador web no está respondiendo. Intenta más tarde."
            else -> "Error al buscar en la web (código $codigo)"
        }
    }
}

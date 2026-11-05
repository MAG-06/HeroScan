package com.example.heroscan.network.dto

// DTOs que reflejan el JSON de la API de Gemini (generateContent).
// Los nombres de los campos se dejan en inglés y en camelCase porque deben coincidir con las claves del JSON.
// Los campos opcionales valen null por defecto: Gson no escribe los null, así no se mandan claves vacías.

// === PETICIÓN ===

// Petición completa: el contenido a analizar (texto + imagen) y la configuración de la respuesta.
data class PeticionGemini(
    val contents: List<ContenidoGemini>,
    val generationConfig: ConfiguracionGemini
)

// Un mensaje formado por varias partes. Se usa en la petición y también en la respuesta.
data class ContenidoGemini(
    val parts: List<ParteGemini>
)

// Una parte del mensaje: puede ser texto o una imagen en línea (solo se llena uno de los dos).
data class ParteGemini(
    val text: String? = null,
    val inlineData: DatosEnLineaGemini? = null
)

// Imagen enviada dentro de la petición: su tipo (por ejemplo "image/jpeg") y sus bytes en Base64.
data class DatosEnLineaGemini(
    val mimeType: String,
    val data: String
)

// Pide que la respuesta sea JSON y que siga el esquema indicado.
data class ConfiguracionGemini(
    val responseMimeType: String,
    val responseSchema: EsquemaGemini
)

// Describe la forma del JSON esperado: tipo ("OBJECT", "STRING"...), sus propiedades y cuáles son obligatorias.
data class EsquemaGemini(
    val type: String,
    val properties: Map<String, EsquemaGemini>? = null,
    val required: List<String>? = null
)

// === RESPUESTA ===

// Respuesta de Gemini: una lista de candidatos (normalmente uno). Puede venir null si la API no generó nada.
data class RespuestaGemini(
    val candidates: List<CandidatoGemini>?
)

// Un candidato de respuesta; su contenido trae el texto generado en parts[0].text.
data class CandidatoGemini(
    val content: ContenidoGemini?
)

// JSON que Gemini devuelve como texto dentro de parts[0].text y que se lee una segunda vez con Gson.
// Los campos son String? porque Gson deja en null lo que no llega.
data class DatosPortadaGemini(
    val titulo: String?,
    val numero: String?,
    val editorial: String?
)

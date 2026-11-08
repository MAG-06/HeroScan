package com.example.heroscan.navigation

import com.example.heroscan.model.TipoCodigo
import kotlinx.serialization.Serializable

// Rutas tipadas de la app: cada pantalla es un tipo @Serializable. Si falta un argumento o tiene el tipo equivocado, la app no compila.

// Pantalla de Acceso (iniciar sesión / crear cuenta). Es la primera si no hay sesión.
@Serializable
object RutaAcceso

// Pantalla principal. Es la base de la pila cuando hay sesión.
@Serializable
object RutaInicio

// Pestaña Perfil (correo de la cuenta y cerrar sesión).
@Serializable
object RutaPerfil

// Pantalla de escaneo con la cámara.
@Serializable
object RutaEscaneo

// Pantalla de búsqueda por foto de la portada.
@Serializable
object RutaEscaneoPortada

// Búsqueda con dos argumentos opcionales: el código (desde el escáner con varios resultados) y el título (desde la búsqueda por portada con varios resultados). Sin ninguno, se abre vacía. Navigation codifica el título solo.
@Serializable
data class RutaBusqueda(val codigo: String? = null, val titulo: String? = null)

// Detalle del cómic: lleva su id en Metron y su tipo de código; el detalle busca el cómic al abrirse.
@Serializable
data class RutaDetalle(val idComic: Int, val tipoCodigo: TipoCodigo)

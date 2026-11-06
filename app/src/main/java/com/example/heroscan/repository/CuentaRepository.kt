package com.example.heroscan.repository

import com.example.heroscan.model.Usuario
import com.example.heroscan.util.MENSAJE_CORREO_INVALIDO
import com.example.heroscan.util.normalizarNombreUsuario
import com.example.heroscan.util.validarClave
import com.example.heroscan.util.validarCorreo
import com.example.heroscan.util.validarNombreUsuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

// Nombres de las colecciones de Firestore.
private const val COLECCION_USUARIOS = "usuarios"
private const val COLECCION_NOMBRES_USUARIO = "nombresUsuario"

// Mensajes de error que se muestran al usuario.
private const val MENSAJE_NOMBRE_EN_USO = "Ese nombre de usuario ya está en uso."
private const val MENSAJE_CORREO_EN_USO = "Ese correo ya tiene una cuenta."
// Es pública porque AccesoViewModel la usa para contar los fallos en el limitador de intentos.
const val MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos."
private const val MENSAJE_CLAVE_REQUISITOS = "La contraseña no cumple los requisitos de seguridad."
private const val MENSAJE_SIN_CONEXION = "Sin conexión. Revisa tu internet e inténtalo de nuevo."
private const val MENSAJE_MUCHOS_INTENTOS = "Demasiados intentos. Espera un momento e inténtalo de nuevo."
private const val MENSAJE_GENERICO = "No se pudo completar la operación. Inténtalo de nuevo."

// Maneja la cuenta del usuario con Firebase: registro, inicio y cierre de sesión y recuperación de contraseña.
// Recibe Firebase Authentication y Firestore por constructor. Si algo falla lanza Exception("mensaje en español").
//
// Nota para quien viene de Java:
// - "suspend" marca una función que puede esperar (por ejemplo, a la red) sin congelar la pantalla.
//   Solo se puede llamar desde otra función suspend o desde una corrutina (por ejemplo, viewModelScope.launch).
// - Firebase devuelve un Task (como un "resultado futuro"). ".await()" espera a que ese Task termine
//   y devuelve su resultado; si el Task falló, lanza la excepción de Firebase.
class CuentaRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // Devuelve el usuario con la sesión iniciada, o null si no hay sesión. No usa la red.
    fun usuarioActual(): Usuario? {
        val usuarioFirebase = auth.currentUser ?: return null
        return Usuario(usuarioFirebase.uid, usuarioFirebase.email ?: "")
    }

    // Crea una cuenta nueva y guarda su perfil en Firestore. Si todo sale bien, la sesión queda iniciada.
    // Pasos: validar datos → revisar que el nombre esté libre → crear la cuenta → guardar perfil y nombre juntos.
    suspend fun registrar(nombreUsuario: String, correo: String, clave: String): Usuario {
        val nombre = normalizarNombreUsuario(nombreUsuario)
        val correoLimpio = correo.trim()

        // 1. Validar el formato antes de usar la red.
        val errorNombre = validarNombreUsuario(nombre)
        if (errorNombre != null) throw Exception(errorNombre)
        val errorCorreo = validarCorreo(correoLimpio)
        if (errorCorreo != null) throw Exception(errorCorreo)
        val errorClave = validarClave(clave)
        if (errorClave != null) throw Exception(errorClave)

        // 2. Revisar que nadie tenga ese nombre. Source.SERVER obliga a leer del servidor y no del caché del celular.
        val nombreEnUso = try {
            firestore.collection(COLECCION_NOMBRES_USUARIO).document(nombre)
                .get(Source.SERVER).await().exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducirError(e)
        }
        if (nombreEnUso) throw Exception(MENSAJE_NOMBRE_EN_USO)

        // 3. Crear la cuenta en Firebase Authentication (la contraseña la guarda Firebase, nunca nosotros).
        val usuarioFirebase = try {
            auth.createUserWithEmailAndPassword(correoLimpio, clave).await().user
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducirError(e)
        }
        if (usuarioFirebase == null) throw Exception(MENSAJE_GENERICO)

        // 4. Guardar el nombre y el perfil en UN batch: un grupo de escrituras que se guardan todas juntas
        // o no se guarda ninguna. Así nunca queda un nombre reservado sin perfil, o al revés.
        val batch = firestore.batch()
        batch.set(
            firestore.collection(COLECCION_NOMBRES_USUARIO).document(nombre),
            hashMapOf("uid" to usuarioFirebase.uid)
        )
        batch.set(
            firestore.collection(COLECCION_USUARIOS).document(usuarioFirebase.uid),
            hashMapOf(
                "nombreUsuario" to nombre,
                "correo" to correoLimpio,
                "codigos" to emptyList<String>(),
                "fechaRegistro" to FieldValue.serverTimestamp()
            )
        )
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 5. Si el batch falló, se borra la cuenta recién creada para que el usuario pueda volver a intentarlo.
            borrarCuentaRecienCreada(usuarioFirebase)
            // PERMISSION_DENIED: lo más probable es que otra persona tomó el nombre en el mismo instante y las reglas
            // no dejan crearlo de nuevo. Con las reglas actuales es el único caso esperado.
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                throw Exception(MENSAJE_NOMBRE_EN_USO)
            }
            throw traducirError(e)
        }

        return Usuario(usuarioFirebase.uid, correoLimpio)
    }

    // Inicia sesión con correo y contraseña. Si la cuenta no existe o la clave es mala, da el mismo mensaje.
    suspend fun iniciarSesion(correo: String, clave: String): Usuario {
        val correoLimpio = correo.trim()

        val errorCorreo = validarCorreo(correoLimpio)
        if (errorCorreo != null) throw Exception(errorCorreo)
        if (clave.isEmpty()) throw Exception(MENSAJE_CREDENCIALES)

        val usuarioFirebase = try {
            auth.signInWithEmailAndPassword(correoLimpio, clave).await().user
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducirError(e)
        }
        if (usuarioFirebase == null) throw Exception(MENSAJE_GENERICO)

        return Usuario(usuarioFirebase.uid, usuarioFirebase.email ?: correoLimpio)
    }

    // Cierra la sesión en este celular. No usa la red.
    fun cerrarSesion() {
        auth.signOut()
    }

    // Pide a Firebase que mande un correo para cambiar la contraseña.
    // Responde igual exista o no la cuenta (Firebase no avisa si el correo no está registrado,
    // porque está activa la protección contra enumeración de correos).
    suspend fun enviarRecuperacion(correo: String) {
        val correoLimpio = correo.trim()

        val errorCorreo = validarCorreo(correoLimpio)
        if (errorCorreo != null) throw Exception(errorCorreo)

        try {
            auth.sendPasswordResetEmail(correoLimpio).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw traducirError(e)
        }
    }

    // Borra la cuenta recién creada cuando no se pudo guardar su perfil.
    // Si el borrado también falla, al menos cierra la sesión para no dejarla a medias.
    private suspend fun borrarCuentaRecienCreada(usuarioFirebase: FirebaseUser) {
        try {
            usuarioFirebase.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            auth.signOut()
        }
    }

    // Convierte una excepción de Firebase en una Exception con un mensaje en español para el usuario.
    // El orden importa: FirebaseAuthWeakPasswordException es un caso particular de
    // FirebaseAuthInvalidCredentialsException, y todas las excepciones de Auth son casos particulares de FirebaseAuthException.
    private fun traducirError(e: Exception): Exception {
        val mensaje = when (e) {
            is FirebaseNetworkException -> MENSAJE_SIN_CONEXION
            is FirebaseTooManyRequestsException -> MENSAJE_MUCHOS_INTENTOS
            is FirebaseAuthWeakPasswordException -> MENSAJE_CLAVE_REQUISITOS
            is FirebaseAuthUserCollisionException -> MENSAJE_CORREO_EN_USO
            is FirebaseAuthInvalidUserException -> MENSAJE_CREDENCIALES
            is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> MENSAJE_CORREO_INVALIDO
                "ERROR_PASSWORD_DOES_NOT_MEET_REQUIREMENTS" -> MENSAJE_CLAVE_REQUISITOS
                else -> MENSAJE_CREDENCIALES
            }
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_PASSWORD_DOES_NOT_MEET_REQUIREMENTS" -> MENSAJE_CLAVE_REQUISITOS
                else -> MENSAJE_GENERICO
            }
            is FirebaseFirestoreException -> when (e.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE -> MENSAJE_SIN_CONEXION
                else -> MENSAJE_GENERICO
            }
            else -> MENSAJE_GENERICO
        }
        return Exception(mensaje)
    }
}

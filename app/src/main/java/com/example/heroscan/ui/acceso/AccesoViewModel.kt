package com.example.heroscan.ui.acceso

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.repository.CuentaRepository
import com.example.heroscan.repository.MENSAJE_CREDENCIALES
import com.example.heroscan.util.LimitadorIntentos
import com.example.heroscan.util.evaluarClave
import com.example.heroscan.util.validarClave
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

// Mensajes que revisa el propio ViewModel, sin usar la red.
private const val MENSAJE_CLAVE_VACIA = "Escribe tu contraseña."
// Es pública porque PantallaAcceso también la usa en el aviso en vivo de "Confirmar contraseña".
const val MENSAJE_CLAVES_DISTINTAS = "Las contraseñas no coinciden."
private const val MENSAJE_CLAVE_DEBIL = "La contraseña es muy débil. Sigue los consejos del medidor."
private const val MENSAJE_DESCONOCIDO = "Error desconocido"

// Cada cuánto se actualiza la cuenta regresiva del bloqueo.
private const val UN_SEGUNDO_MS = 1000L

// Maneja la pantalla de Acceso: iniciar sesión, crear cuenta y recuperar contraseña.
// Llama a CuentaRepository y guarda el estado para que la pantalla lo dibuje. No recibe Context.
// Las contraseñas solo pasan por las funciones como parámetro: nunca se guardan en el estado ni en Log.
class AccesoViewModel : ViewModel() {

    private val repositorio = CuentaRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    // Cuenta los inicios de sesión fallidos. Vive mientras viva este ViewModel (al girar el celular se conserva).
    private val limitador = LimitadorIntentos()

    // Corrutina de la cuenta regresiva. Un Job es como un "hilo liviano" que se puede cancelar.
    private var trabajoCuentaRegresiva: Job? = null

    var uiState: AccesoUiState by mutableStateOf(AccesoUiState.Normal)
        private set

    var estadoRecuperacion: RecuperacionUiState by mutableStateOf(RecuperacionUiState.Cerrado)
        private set

    var modo: ModoAcceso by mutableStateOf(ModoAcceso.INICIAR_SESION)
        private set

    // true si el error actual es "Correo o contraseña incorrectos.": la pantalla pinta en rojo correo y contraseña.
    // "as?" intenta convertir uiState a Error y da null si no lo es (como un instanceof + cast de Java, sin excepción).
    val errorDeCredenciales: Boolean
        get() = (uiState as? AccesoUiState.Error)?.mensaje == MENSAJE_CREDENCIALES

    // Cambia entre Iniciar sesión y Crear cuenta. Quita el error que hubiera, pero no quita un bloqueo.
    fun cambiarModo(nuevoModo: ModoAcceso) {
        modo = nuevoModo
        limpiarError()
    }

    // Inicia sesión. Solo el error MENSAJE_CREDENCIALES cuenta como fallo en el limitador.
    fun iniciarSesion(correo: String, clave: String) {
        if (estaOcupado()) return

        // Clave vacía: se avisa sin llamar a Firebase y sin contar el fallo.
        if (clave.isEmpty()) {
            uiState = AccesoUiState.Error(MENSAJE_CLAVE_VACIA)
            return
        }

        uiState = AccesoUiState.Cargando
        viewModelScope.launch {
            try {
                val usuario = repositorio.iniciarSesion(correo, clave)
                limitador.registrarExito()
                uiState = AccesoUiState.SesionIniciada(usuario)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e.message == MENSAJE_CREDENCIALES) {
                    limitador.registrarFallo()
                }
                if (limitador.puedeIntentar()) {
                    uiState = AccesoUiState.Error(e.message ?: MENSAJE_DESCONOCIDO)
                } else {
                    iniciarCuentaRegresiva()
                }
            }
        }
    }

    // Crea la cuenta. Antes de usar la red revisa: que las contraseñas coincidan, el largo mínimo y la fortaleza.
    // El nombre de usuario y el correo los valida el Repository. Aquí no se usa el limitador.
    fun registrar(nombreUsuario: String, correo: String, clave: String, confirmacion: String) {
        if (estaOcupado()) return

        if (clave != confirmacion) {
            uiState = AccesoUiState.Error(MENSAJE_CLAVES_DISTINTAS)
            return
        }

        val errorClave = validarClave(clave)
        if (errorClave != null) {
            uiState = AccesoUiState.Error(errorClave)
            return
        }

        if (!evaluarClave(clave, correo).esAceptable) {
            uiState = AccesoUiState.Error(MENSAJE_CLAVE_DEBIL)
            return
        }

        uiState = AccesoUiState.Cargando
        viewModelScope.launch {
            try {
                val usuario = repositorio.registrar(nombreUsuario, correo, clave)
                uiState = AccesoUiState.SesionIniciada(usuario)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = AccesoUiState.Error(e.message ?: MENSAJE_DESCONOCIDO)
            }
        }
    }

    // Cierra la tarjeta de error y deja el formulario listo. Si está bloqueado o cargando, no hace nada.
    fun limpiarError() {
        if (uiState is AccesoUiState.Error) {
            uiState = AccesoUiState.Normal
        }
    }

    // Abre el diálogo "Recuperar contraseña" (también sirve para volver a intentarlo después de un error).
    fun abrirRecuperacion() {
        estadoRecuperacion = RecuperacionUiState.Abierto
    }

    // Cierra el diálogo "Recuperar contraseña".
    fun cerrarRecuperacion() {
        estadoRecuperacion = RecuperacionUiState.Cerrado
    }

    // Pide el correo de recuperación. Si sale bien, el diálogo muestra siempre el mismo texto, exista o no la cuenta.
    fun enviarRecuperacion(correo: String) {
        if (estadoRecuperacion is RecuperacionUiState.Enviando) return

        estadoRecuperacion = RecuperacionUiState.Enviando
        viewModelScope.launch {
            try {
                repositorio.enviarRecuperacion(correo)
                // Solo se cambia si el diálogo sigue esperando (el usuario pudo cerrarlo mientras tanto).
                if (estadoRecuperacion is RecuperacionUiState.Enviando) {
                    estadoRecuperacion = RecuperacionUiState.Enviado
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (estadoRecuperacion is RecuperacionUiState.Enviando) {
                    estadoRecuperacion = RecuperacionUiState.Error(e.message ?: MENSAJE_DESCONOCIDO)
                }
            }
        }
    }

    // Indica si hay que ignorar el toque del botón: ya se está esperando a Firebase o hay un bloqueo.
    private fun estaOcupado(): Boolean {
        return uiState is AccesoUiState.Cargando || uiState is AccesoUiState.Bloqueado
    }

    // Muestra la cuenta regresiva del bloqueo: cada segundo actualiza los segundos que faltan y al terminar vuelve a Normal.
    // "delay" espera sin congelar la pantalla. La corrutina vive en viewModelScope, así que se cancela sola
    // cuando se destruye el ViewModel.
    private fun iniciarCuentaRegresiva() {
        trabajoCuentaRegresiva?.cancel()
        trabajoCuentaRegresiva = viewModelScope.launch {
            while (!limitador.puedeIntentar()) {
                uiState = AccesoUiState.Bloqueado(limitador.segundosDeEspera())
                delay(UN_SEGUNDO_MS)
            }
            uiState = AccesoUiState.Normal
        }
    }
}

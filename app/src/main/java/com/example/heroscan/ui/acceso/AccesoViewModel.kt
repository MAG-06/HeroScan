package com.example.heroscan.ui.acceso

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.repository.CuentaRepository
import com.example.heroscan.repository.MENSAJE_CREDENCIALES
import com.example.heroscan.util.AlmacenIntentos
import com.example.heroscan.util.LimitadorIntentos
import com.example.heroscan.util.evaluarClave
import com.example.heroscan.util.validarClave
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
// Llama a CuentaRepository y guarda el estado para que la pantalla lo dibuje. Recibe la Application solo para AlmacenIntentos.
// Las contraseñas solo pasan por las funciones como parámetro: nunca se guardan en el estado ni en Log.
class AccesoViewModel(app: Application) : AndroidViewModel(app) {

    private val repositorio = CuentaRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    // Cuenta los inicios de sesión fallidos. Sus valores se guardan con AlmacenIntentos, así que siguen aunque se cierre la app.
    private val limitador = LimitadorIntentos()

    // Guarda en el teléfono los valores del limitador para que el bloqueo siga aunque se cierre la app.
    private val almacen = AlmacenIntentos(app)

    // Corrutina de la cuenta regresiva. Un Job es como un "hilo liviano" que se puede cancelar.
    private var trabajoCuentaRegresiva: Job? = null

    // Empieza en Cargando hasta leer lo guardado, para que no se pueda intentar antes.
    private val _uiState = MutableStateFlow<AccesoUiState>(AccesoUiState.Cargando)
    val uiState: StateFlow<AccesoUiState> = _uiState

    private val _estadoRecuperacion = MutableStateFlow<RecuperacionUiState>(RecuperacionUiState.Cerrado)
    val estadoRecuperacion: StateFlow<RecuperacionUiState> = _estadoRecuperacion

    private val _modo = MutableStateFlow<ModoAcceso>(ModoAcceso.INICIAR_SESION)
    val modo: StateFlow<ModoAcceso> = _modo

    // true si el error actual es "Correo o contraseña incorrectos.": la pantalla pinta en rojo correo y contraseña.
    // "as?" intenta convertir el valor actual de uiState a Error y da null si no lo es (como un instanceof + cast de Java, sin excepción).
    val errorDeCredenciales: Boolean
        get() = (_uiState.value as? AccesoUiState.Error)?.mensaje == MENSAJE_CREDENCIALES

    // Al crearse, lee los fallos y el bloqueo guardados. Si sigue bloqueado, arranca la cuenta regresiva; si no, deja la pantalla lista.
    init {
        viewModelScope.launch {
            try {
                val fallos = almacen.fallosSeguidos.first()
                val hastaMs = almacen.bloqueadoHastaMs.first()
                limitador.restaurar(fallos, hastaMs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Si no se pudo leer, se empieza con 0 fallos.
            }
            if (limitador.puedeIntentar()) {
                _uiState.value = AccesoUiState.Normal
            } else {
                iniciarCuentaRegresiva()
            }
        }
    }

    // Cambia entre Iniciar sesión y Crear cuenta. Quita el error que hubiera, pero no quita un bloqueo.
    fun cambiarModo(nuevoModo: ModoAcceso) {
        _modo.value = nuevoModo
        limpiarError()
    }

    // Inicia sesión. Solo el error MENSAJE_CREDENCIALES cuenta como fallo en el limitador.
    fun iniciarSesion(correo: String, clave: String) {
        if (estaOcupado()) return

        // Clave vacía: se avisa sin llamar a Firebase y sin contar el fallo.
        if (clave.isEmpty()) {
            _uiState.value = AccesoUiState.Error(MENSAJE_CLAVE_VACIA)
            return
        }

        _uiState.value = AccesoUiState.Cargando
        viewModelScope.launch {
            try {
                val usuario = repositorio.iniciarSesion(correo, clave)
                limitador.registrarExito()
                guardarLimitador()
                _uiState.value = AccesoUiState.SesionIniciada(usuario)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e.message == MENSAJE_CREDENCIALES) {
                    limitador.registrarFallo()
                    guardarLimitador()
                }
                if (limitador.puedeIntentar()) {
                    _uiState.value = AccesoUiState.Error(e.message ?: MENSAJE_DESCONOCIDO)
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
            _uiState.value = AccesoUiState.Error(MENSAJE_CLAVES_DISTINTAS)
            return
        }

        val errorClave = validarClave(clave)
        if (errorClave != null) {
            _uiState.value = AccesoUiState.Error(errorClave)
            return
        }

        if (!evaluarClave(clave, correo).esAceptable) {
            _uiState.value = AccesoUiState.Error(MENSAJE_CLAVE_DEBIL)
            return
        }

        _uiState.value = AccesoUiState.Cargando
        viewModelScope.launch {
            try {
                val usuario = repositorio.registrar(nombreUsuario, correo, clave)
                _uiState.value = AccesoUiState.SesionIniciada(usuario)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = AccesoUiState.Error(e.message ?: MENSAJE_DESCONOCIDO)
            }
        }
    }

    // Cierra la tarjeta de error y deja el formulario listo. Si está bloqueado o cargando, no hace nada.
    fun limpiarError() {
        if (_uiState.value is AccesoUiState.Error) {
            _uiState.value = AccesoUiState.Normal
        }
    }

    // Abre el diálogo "Recuperar contraseña" (también sirve para volver a intentarlo después de un error).
    fun abrirRecuperacion() {
        _estadoRecuperacion.value = RecuperacionUiState.Abierto
    }

    // Cierra el diálogo "Recuperar contraseña".
    fun cerrarRecuperacion() {
        _estadoRecuperacion.value = RecuperacionUiState.Cerrado
    }

    // Pide el correo de recuperación. Si sale bien, el diálogo muestra siempre el mismo texto, exista o no la cuenta.
    fun enviarRecuperacion(correo: String) {
        if (_estadoRecuperacion.value is RecuperacionUiState.Enviando) return

        _estadoRecuperacion.value = RecuperacionUiState.Enviando
        viewModelScope.launch {
            try {
                repositorio.enviarRecuperacion(correo)
                // Solo se cambia si el diálogo sigue esperando (el usuario pudo cerrarlo mientras tanto).
                if (_estadoRecuperacion.value is RecuperacionUiState.Enviando) {
                    _estadoRecuperacion.value = RecuperacionUiState.Enviado
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_estadoRecuperacion.value is RecuperacionUiState.Enviando) {
                    _estadoRecuperacion.value = RecuperacionUiState.Error(e.message ?: MENSAJE_DESCONOCIDO)
                }
            }
        }
    }

    // Indica si hay que ignorar el toque del botón: se está leyendo lo guardado, se espera a Firebase o hay un bloqueo.
    private fun estaOcupado(): Boolean {
        return _uiState.value is AccesoUiState.Cargando || _uiState.value is AccesoUiState.Bloqueado
    }

    // Guarda en el teléfono los fallos y el bloqueo actuales del limitador.
    private suspend fun guardarLimitador() {
        try {
            almacen.guardar(limitador.fallosSeguidos, limitador.bloqueadoHastaMs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Si no se pudo guardar, el limitador sigue funcionando en memoria.
        }
    }

    // Muestra la cuenta regresiva del bloqueo: cada segundo actualiza los segundos que faltan y al terminar vuelve a Normal.
    // "delay" espera sin congelar la pantalla. La corrutina vive en viewModelScope, así que se cancela sola
    // cuando se destruye el ViewModel.
    private fun iniciarCuentaRegresiva() {
        trabajoCuentaRegresiva?.cancel()
        trabajoCuentaRegresiva = viewModelScope.launch {
            while (!limitador.puedeIntentar()) {
                _uiState.value = AccesoUiState.Bloqueado(limitador.segundosDeEspera())
                delay(UN_SEGUNDO_MS)
            }
            _uiState.value = AccesoUiState.Normal
        }
    }
}

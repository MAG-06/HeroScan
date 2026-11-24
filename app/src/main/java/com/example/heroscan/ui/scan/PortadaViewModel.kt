package com.example.heroscan.ui.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.network.ClienteRetrofit
import com.example.heroscan.repository.ComicRepository
import com.example.heroscan.repository.MENSAJE_ERROR_DESCONOCIDO
import com.example.heroscan.repository.ResultadoBusqueda
import com.example.heroscan.util.avisarComicEncontrado
import com.example.heroscan.util.avisarVariosComics
import com.example.heroscan.util.quitarAvisos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

// Maneja la pantalla de búsqueda por portada: manda la imagen (ya en Base64) al repositorio,
// que la reconoce con Gemini y la busca en Metron, y guarda el estado para que la pantalla lo muestre.
// Es AndroidViewModel: recibe la Application para poder avisar con una notificación si la búsqueda termina con la app fuera de la vista.
// La imagen la sigue convirtiendo la pantalla antes, y solo le pasa el texto Base64.
class PortadaViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repositorio = ComicRepository(ClienteRetrofit.metronApi, ClienteRetrofit.traduccionApi, ClienteRetrofit.geminiApi, ClienteRetrofit.tavilyApi)

    private val _uiState = MutableStateFlow<PortadaUiState>(PortadaUiState.Inicial)
    val uiState: StateFlow<PortadaUiState> = _uiState

    // Última imagen enviada. Se guarda para poder reenviarla con el botón REINTENTAR.
    private var ultimaImagenBase64: String? = null

    // true mientras la pantalla se ve. La pantalla lo cambia en ON_START y ON_STOP.
    private var appVisible = true

    // La pantalla avisa si se ve o no (ON_START / ON_STOP).
    // Al volver a verse quita los avisos que quedaron (por ejemplo, "varios" si el usuario volvió con el ícono).
    fun cambiarVisibilidad(visible: Boolean) {
        appVisible = visible
        if (visible) quitarAvisos(app)
    }

    // Envía la portada al repositorio y actualiza el estado según lo que encuentre.
    fun enviarPortada(imagenBase64: String) {
        ultimaImagenBase64 = imagenBase64
        _uiState.value = PortadaUiState.Buscando

        viewModelScope.launch {
            var tituloIa = ""
            _uiState.value = try {
                val resultadoPortada = repositorio.buscarPorPortada(imagenBase64)
                tituloIa = resultadoPortada.titulo
                when (val resultado = resultadoPortada.resultado) {
                    is ResultadoBusqueda.Unico -> PortadaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> PortadaUiState.VariosResultados(resultadoPortada.titulo)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                PortadaUiState.Error("No hay conexión o la IA tardó demasiado. Intenta de nuevo.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                PortadaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
            avisarSiNoSeVe(tituloIa)
        }
    }

    // Si la búsqueda terminó con la app fuera de la vista, avisa con una notificación (uno o varios). Los errores no avisan.
    private fun avisarSiNoSeVe(tituloIa: String) {
        if (appVisible) return
        val estado = _uiState.value
        when (estado) {
            is PortadaUiState.Encontrado -> avisarComicEncontrado(app, estado.idComic, estado.tipoCodigo, tituloIa)
            is PortadaUiState.VariosResultados -> avisarVariosComics(app)
            else -> {}
        }
    }

    // Se llama desde la acción REINTENTAR del Snackbar: vuelve a enviar la última imagen.
    fun reintentar() {
        val imagen = ultimaImagenBase64
        if (imagen != null) {
            enviarPortada(imagen)
        } else {
            _uiState.value = PortadaUiState.Inicial
        }
    }

    // Se llama después de navegar con un resultado o al cerrar el error: deja la pantalla lista para otro envío.
    fun prepararNuevaBusqueda() {
        _uiState.value = PortadaUiState.Inicial
        quitarAvisos(app)
    }
}

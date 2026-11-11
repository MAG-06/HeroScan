package com.example.heroscan.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.network.ClienteRetrofit
import com.example.heroscan.repository.ComicRepository
import com.example.heroscan.repository.MENSAJE_ERROR_DESCONOCIDO
import com.example.heroscan.repository.ResultadoBusqueda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

// Maneja la pantalla de búsqueda por portada: manda la imagen (ya en Base64) al repositorio,
// que la reconoce con Gemini y la busca en Metron, y guarda el estado para que la pantalla lo muestre.
// No recibe Context: la pantalla convierte la imagen antes y solo le pasa el texto Base64.
class PortadaViewModel : ViewModel() {

    private val repositorio = ComicRepository(ClienteRetrofit.metronApi, ClienteRetrofit.traduccionApi, ClienteRetrofit.geminiApi, ClienteRetrofit.tavilyApi)

    private val _uiState = MutableStateFlow<PortadaUiState>(PortadaUiState.Inicial)
    val uiState: StateFlow<PortadaUiState> = _uiState

    // Última imagen enviada. Se guarda para poder reenviarla con el botón REINTENTAR.
    private var ultimaImagenBase64: String? = null

    // Envía la portada al repositorio y actualiza el estado según lo que encuentre.
    fun enviarPortada(imagenBase64: String) {
        ultimaImagenBase64 = imagenBase64
        _uiState.value = PortadaUiState.Buscando

        viewModelScope.launch {
            _uiState.value = try {
                val resultadoPortada = repositorio.buscarPorPortada(imagenBase64)
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
    }
}

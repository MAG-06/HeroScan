package com.example.heroscan.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.heroscan.navigation.RutaDetalle
import com.example.heroscan.network.ClienteRetrofit
import com.example.heroscan.repository.ComicRepository
import com.example.heroscan.repository.MENSAJE_ERROR_DESCONOCIDO
import com.example.heroscan.repository.MENSAJE_SIN_CONEXION_COMICS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// ViewModel del detalle: recibe por la ruta el id del cómic y su tipo de código, y le pide el detalle a Metron.
class DetalleViewModel(estadoGuardado: SavedStateHandle) : ViewModel() {

    // Ruta del detalle, ya tipada: trae el id del cómic y su tipo de código
    private val ruta = estadoGuardado.toRoute<RutaDetalle>()

    // Id del cómic en Metron, leído de la ruta
    private val idComic = ruta.idComic

    // Tipo de código con el que se encontró el cómic, leído de la ruta
    private val tipoCodigo = ruta.tipoCodigo

    // Repositorio que consulta Metron y traduce la descripción
    private val repositorio = ComicRepository(ClienteRetrofit.metronApi, ClienteRetrofit.traduccionApi, ClienteRetrofit.geminiApi)

    // Estado que dibuja la pantalla: cargando, listo o error
    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)
    val uiState: StateFlow<DetalleUiState> = _uiState

    // "init" se ejecuta una sola vez, cuando se crea el ViewModel: pide el cómic al abrir la pantalla.
    init {
        cargarComic()
    }

    // Pide el detalle del cómic. Se llama al abrir la pantalla y otra vez con REINTENTAR.
    fun cargarComic() {
        _uiState.value = DetalleUiState.Cargando

        viewModelScope.launch {
            _uiState.value = try {
                val comic = repositorio.obtenerDetalleComic(idComic, tipoCodigo)
                DetalleUiState.Listo(comic)
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                DetalleUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: Exception) {
                DetalleUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }
}

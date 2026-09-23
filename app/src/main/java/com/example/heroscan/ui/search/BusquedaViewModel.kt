package com.example.heroscan.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.repository.ComicRepository
import com.example.heroscan.repository.ResultadoBusqueda
import com.example.heroscan.util.clasificarCodigoEscrito
import kotlinx.coroutines.launch

// Maneja la pantalla de búsqueda: guarda el texto y el filtro, clasifica el código y pide los cómics al repositorio.
class BusquedaViewModel : ViewModel() {

    private val repositorio = ComicRepository()

    // Filtros que se muestran debajo de la barra de búsqueda
    val filtros = listOf("TODO", "TÍTULO", "PERSONAJE", "CÓDIGO")

    var uiState: BusquedaUiState by mutableStateOf(BusquedaUiState.Inicial)
        private set

    var textoBusqueda by mutableStateOf("")
        private set

    var filtroSeleccionado by mutableStateOf(filtros.first())
        private set

    // Tipo del último código buscado. Se guarda para usarlo cuando el usuario elige un cómic de la lista,
    // así no importa si después cambió el texto de la barra de búsqueda.
    private var tipoCodigoBuscado = TipoCodigo.DESCONOCIDO

    // Actualiza el texto escrito en la barra de búsqueda.
    fun cambiarTextoBusqueda(texto: String) {
        textoBusqueda = texto
    }

    // Cambia el filtro seleccionado.
    fun seleccionarFiltro(filtro: String) {
        filtroSeleccionado = filtro
    }

    // Clasifica el texto escrito y, si es un código reconocible, busca el cómic en el repositorio.
    fun buscar() {
        val tipoCodigo = clasificarCodigoEscrito(textoBusqueda)
        if (tipoCodigo == TipoCodigo.DESCONOCIDO) return

        tipoCodigoBuscado = tipoCodigo
        uiState = BusquedaUiState.Cargando

        viewModelScope.launch {
            uiState = try {
                when (val resultado = repositorio.buscarPorCodigo(textoBusqueda, tipoCodigo)) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.comic)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Se llama cuando el usuario elige un cómic de la lista de resultados: trae su detalle completo.
    fun seleccionarComic(idComic: Int) {
        uiState = BusquedaUiState.Cargando

        viewModelScope.launch {
            uiState = try {
                val comic = repositorio.obtenerDetalleComic(idComic, tipoCodigoBuscado)
                BusquedaUiState.Encontrado(comic)
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Vuelve al estado inicial. Se usa después de navegar al detalle del cómic.
    fun reiniciarEstado() {
        uiState = BusquedaUiState.Inicial
    }
}

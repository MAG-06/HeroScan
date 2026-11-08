package com.example.heroscan.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.navigation.RutaBusqueda
import com.example.heroscan.network.ClienteRetrofit
import com.example.heroscan.repository.ComicRepository
import com.example.heroscan.repository.MENSAJE_ERROR_DESCONOCIDO
import com.example.heroscan.repository.MENSAJE_SIN_CONEXION_COMICS
import com.example.heroscan.repository.ResultadoBusqueda
import com.example.heroscan.util.clasificarCodigoEscrito
import com.example.heroscan.util.limpiarCodigo
import kotlinx.coroutines.launch
import java.io.IOException

// Maneja la pantalla de búsqueda: guarda el texto y el filtro, decide qué tipo de búsqueda hacer
// y pide los cómics al repositorio.
// Recibe un SavedStateHandle: Navigation Compose se lo entrega automáticamente con los argumentos de la ruta.
class BusquedaViewModel(estadoGuardado: SavedStateHandle) : ViewModel() {

    private val repositorio = ComicRepository(ClienteRetrofit.metronApi, ClienteRetrofit.traduccionApi, ClienteRetrofit.geminiApi)

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

    // Última lista de resultados de la que el usuario eligió un cómic.
    // Se guarda para volver a mostrarla cuando regresa del detalle.
    private var ultimosResultados: List<ComicResumen> = emptyList()

    // "init" se ejecuta una sola vez, cuando se crea el ViewModel.
    // Si la pantalla se abrió desde el escáner, la ruta trae un código y se busca automáticamente.
    // Si se abrió desde la búsqueda por portada, trae un título: se pone el filtro TÍTULO y se busca.
    // El código tiene prioridad: si llegan los dos, se busca por código.
    init {
        val ruta = estadoGuardado.toRoute<RutaBusqueda>()
        if (ruta.codigo != null) {
            textoBusqueda = ruta.codigo
            buscarPorCodigo()
        } else if (ruta.titulo != null) {
            textoBusqueda = ruta.titulo
            filtroSeleccionado = "TÍTULO"
            buscarPorTitulo()
        }
    }

    // Actualiza el texto escrito en la barra de búsqueda.
    fun cambiarTextoBusqueda(texto: String) {
        textoBusqueda = texto
    }

    // Cambia el filtro seleccionado.
    fun seleccionarFiltro(filtro: String) {
        filtroSeleccionado = filtro
    }

    // Lanza una búsqueda nueva según el filtro seleccionado.
    fun buscar() {
        if (textoBusqueda.isBlank()) return

        // Es una búsqueda nueva: se olvida la lista anterior
        ultimosResultados = emptyList()

        when (filtroSeleccionado) {
            "TÍTULO" -> buscarPorTitulo()
            "PERSONAJE" -> buscarPorPersonaje()
            "CÓDIGO" -> buscarPorCodigo()
            "TODO" -> buscarGeneral()
        }
    }

    // Busca cómics cuyo título de serie contenga el texto escrito.
    private fun buscarPorTitulo() {
        // No viene de un código de barras, así que el detalle no debe mostrar un tipo de código viejo
        tipoCodigoBuscado = TipoCodigo.DESCONOCIDO
        uiState = BusquedaUiState.Cargando

        viewModelScope.launch {
            uiState = try {
                when (val resultado = repositorio.buscarPorTitulo(textoBusqueda)) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                BusquedaUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Clasifica el texto como código de barras y, si es reconocible, busca el cómic por ese código.
    private fun buscarPorCodigo() {

        val tipoCodigo = clasificarCodigoEscrito(textoBusqueda)

        // Si el código no tiene una longitud válida, se avisa al usuario en vez de no hacer nada
        if (tipoCodigo == TipoCodigo.DESCONOCIDO) {
            uiState = BusquedaUiState.Error("Código no válido: debe tener 12, 13 o 17 dígitos")
            return
        }

        tipoCodigoBuscado = tipoCodigo
        uiState = BusquedaUiState.Cargando

        viewModelScope.launch {
            uiState = try {
                when (val resultado = repositorio.buscarPorCodigo(textoBusqueda, tipoCodigo)) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                BusquedaUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Busca los cómics en los que aparece el personaje escrito.
    private fun buscarPorPersonaje() {
        // No viene de un código de barras, así que el detalle no debe mostrar un tipo de código viejo
        tipoCodigoBuscado = TipoCodigo.DESCONOCIDO
        uiState = BusquedaUiState.Cargando

        viewModelScope.launch {
            uiState = try {
                when (val resultado = repositorio.buscarPorPersonaje(textoBusqueda)) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                BusquedaUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Filtro "TODO": si el texto son solo dígitos se busca como código; si tiene letras, como título.
    private fun buscarGeneral() {
        // Se quitan espacios y guiones antes de revisar, para que "76194-134..." se reconozca como código
        val textoLimpio = limpiarCodigo(textoBusqueda)

        // Si son solo dígitos, es un código de barras
        if (textoLimpio.all { it.isDigit() }) {
            buscarPorCodigo()
            return
        }

        // Si tiene letras, buscar por título
        buscarPorTitulo()
    }

    // Abre el detalle del cómic elegido de la lista: guarda la lista para mostrarla al volver y navega con su id.
    fun seleccionarComic(idComic: Int) {
        // Guardar la lista actual para volver a mostrarla al regresar del detalle
        val estadoActual = uiState
        if (estadoActual is BusquedaUiState.VariosResultados) {
            ultimosResultados = estadoActual.resultados
        }

        uiState = BusquedaUiState.Encontrado(idComic, tipoCodigoBuscado)
    }

    // Se llama después de navegar al detalle. Si el cómic se eligió de una lista, se vuelve a mostrar
    // esa lista (para verla al regresar); si no, la pantalla vuelve a su estado inicial.
    fun reiniciarEstado() {
        uiState = if (ultimosResultados.isNotEmpty()) {
            BusquedaUiState.VariosResultados(ultimosResultados)
        } else {
            BusquedaUiState.Inicial
        }
    }
}

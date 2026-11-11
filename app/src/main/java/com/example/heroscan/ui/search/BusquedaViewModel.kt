package com.example.heroscan.ui.search

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

// Maneja la pantalla de búsqueda: guarda el texto y el filtro, decide qué tipo de búsqueda hacer
// y pide los cómics al repositorio.
// Recibe un SavedStateHandle: Navigation Compose se lo entrega automáticamente con los argumentos de la ruta, y aquí también se guardan el texto y el filtro.
class BusquedaViewModel(private val estadoGuardado: SavedStateHandle) : ViewModel() {

    private val repositorio = ComicRepository(ClienteRetrofit.metronApi, ClienteRetrofit.traduccionApi, ClienteRetrofit.geminiApi, ClienteRetrofit.tavilyApi)

    // Filtros que se muestran debajo de la barra de búsqueda
    val filtros = listOf("TODO", "TÍTULO", "PERSONAJE", "CÓDIGO")

    private val _uiState = MutableStateFlow<BusquedaUiState>(BusquedaUiState.Inicial)
    val uiState: StateFlow<BusquedaUiState> = _uiState

    // El texto y el filtro empiezan con lo guardado en el SavedStateHandle (si no hay nada, con su valor inicial): así sobreviven a que Android cierre la app en segundo plano.
    private val _textoBusqueda = MutableStateFlow<String>(estadoGuardado.get<String>(CLAVE_TEXTO) ?: "")
    val textoBusqueda: StateFlow<String> = _textoBusqueda

    private val _filtroSeleccionado = MutableStateFlow<String>(estadoGuardado.get<String>(CLAVE_FILTRO) ?: filtros.first())
    val filtroSeleccionado: StateFlow<String> = _filtroSeleccionado

    // Tipo del último código buscado. Se guarda para usarlo cuando el usuario elige un cómic de la lista,
    // así no importa si después cambió el texto de la barra de búsqueda.
    private var tipoCodigoBuscado = TipoCodigo.DESCONOCIDO

    // Última lista de resultados de la que el usuario eligió un cómic.
    // Se guarda para volver a mostrarla cuando regresa del detalle.
    private var ultimosResultados: List<ComicResumen> = emptyList()

    // "init" se ejecuta una sola vez, cuando se crea el ViewModel.
    // Si ya hay un texto guardado, Android cerró la app en segundo plano: se deja lo guardado y no se busca, para no gastar peticiones a Metron.
    // Si no, y la pantalla se abrió desde el escáner, la ruta trae un código y se busca automáticamente.
    // Si se abrió desde la búsqueda por portada o del escáner con un ISBN-13, trae un título: se pone el filtro TÍTULO y se busca.
    // El código tiene prioridad: si llegan los dos, se busca por código.
    init {
        val ruta = estadoGuardado.toRoute<RutaBusqueda>()
        if (estadoGuardado.get<String>(CLAVE_TEXTO) == null) {
            if (ruta.codigo != null) {
                cambiarTextoBusqueda(ruta.codigo)
                buscarPorCodigo()
            } else if (ruta.titulo != null) {
                cambiarTextoBusqueda(ruta.titulo)
                seleccionarFiltro("TÍTULO")
                buscarPorTitulo()
            }
        }
    }

    // Actualiza el texto escrito en la barra de búsqueda y lo copia al SavedStateHandle.
    fun cambiarTextoBusqueda(texto: String) {
        _textoBusqueda.value = texto
        estadoGuardado[CLAVE_TEXTO] = texto
    }

    // Cambia el filtro seleccionado y lo copia al SavedStateHandle.
    fun seleccionarFiltro(filtro: String) {
        _filtroSeleccionado.value = filtro
        estadoGuardado[CLAVE_FILTRO] = filtro
    }

    // Lanza una búsqueda nueva según el filtro seleccionado.
    fun buscar() {
        if (_textoBusqueda.value.isBlank()) return

        // Es una búsqueda nueva: se olvida la lista anterior
        ultimosResultados = emptyList()

        when (_filtroSeleccionado.value) {
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
        _uiState.value = BusquedaUiState.Cargando

        viewModelScope.launch {
            _uiState.value = try {
                when (val resultado = repositorio.buscarPorTitulo(_textoBusqueda.value)) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                BusquedaUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Clasifica el texto como código de barras y, si es reconocible, busca el cómic por ese código. Los ISBN-13 se buscan en la web.
    private fun buscarPorCodigo() {

        val tipoCodigo = clasificarCodigoEscrito(_textoBusqueda.value)

        // Si el código no tiene una longitud válida, se avisa al usuario en vez de no hacer nada
        if (tipoCodigo == TipoCodigo.DESCONOCIDO) {
            _uiState.value = BusquedaUiState.Error("Código no válido: debe tener 12, 13 o 17 dígitos")
            return
        }

        tipoCodigoBuscado = tipoCodigo

        if (tipoCodigo == TipoCodigo.ISBN_13) {
            buscarEnLaWeb()
            return
        }

        _uiState.value = BusquedaUiState.Cargando

        viewModelScope.launch {
            _uiState.value = try {
                when (val resultado = repositorio.buscarPorCodigo(_textoBusqueda.value, tipoCodigo)) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                BusquedaUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Busca en la web el ISBN-13 detectado (o escrito) y actualiza el estado con lo que encuentre.
    private fun buscarEnLaWeb() {
        _uiState.value = BusquedaUiState.CargandoEnLaWeb

        viewModelScope.launch {
            _uiState.value = try {
                when (val resultado = repositorio.buscarCodigoEnLaWeb(_textoBusqueda.value).resultado) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                BusquedaUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Busca los cómics en los que aparece el personaje escrito.
    private fun buscarPorPersonaje() {
        // No viene de un código de barras, así que el detalle no debe mostrar un tipo de código viejo
        tipoCodigoBuscado = TipoCodigo.DESCONOCIDO
        _uiState.value = BusquedaUiState.Cargando

        viewModelScope.launch {
            _uiState.value = try {
                when (val resultado = repositorio.buscarPorPersonaje(_textoBusqueda.value)) {
                    is ResultadoBusqueda.Unico -> BusquedaUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> BusquedaUiState.VariosResultados(resultado.resultados)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                BusquedaUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                BusquedaUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Filtro "TODO": si el texto son solo dígitos se busca como código; si tiene letras, como título.
    private fun buscarGeneral() {
        // Se quitan espacios y guiones antes de revisar, para que "76194-134..." se reconozca como código
        val textoLimpio = limpiarCodigo(_textoBusqueda.value)

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
        val estadoActual = _uiState.value
        if (estadoActual is BusquedaUiState.VariosResultados) {
            ultimosResultados = estadoActual.resultados
        }

        _uiState.value = BusquedaUiState.Encontrado(idComic, tipoCodigoBuscado)
    }

    // Se llama después de navegar al detalle. Si el cómic se eligió de una lista, se vuelve a mostrar
    // esa lista (para verla al regresar); si no, la pantalla vuelve a su estado inicial.
    fun reiniciarEstado() {
        _uiState.value = if (ultimosResultados.isNotEmpty()) {
            BusquedaUiState.VariosResultados(ultimosResultados)
        } else {
            BusquedaUiState.Inicial
        }
    }

    // Nombres con los que se guardan el texto y el filtro en el SavedStateHandle.
    private companion object {
        const val CLAVE_TEXTO = "busqueda_texto"
        const val CLAVE_FILTRO = "busqueda_filtro"
    }
}

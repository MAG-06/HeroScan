package com.example.heroscan.ui.scan

import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.network.ClienteRetrofit
import com.example.heroscan.repository.ComicRepository
import com.example.heroscan.repository.MENSAJE_ERROR_DESCONOCIDO
import com.example.heroscan.repository.MENSAJE_SIN_CONEXION_COMICS
import com.example.heroscan.repository.ResultadoBusqueda
import com.example.heroscan.util.BloqueoCodigo
import com.example.heroscan.util.actualizarBloqueo
import com.example.heroscan.util.clasificarCodigoEscaneado
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.cancellation.CancellationException

// Controla la cámara (linterna), analiza cada frame con ML Kit para detectar códigos de barras
// y, cuando detecta uno, busca el cómic en el repositorio.
class EscaneoViewModel : ViewModel() {

    private val repositorio = ComicRepository(ClienteRetrofit.metronApi, ClienteRetrofit.traduccionApi, ClienteRetrofit.geminiApi, ClienteRetrofit.tavilyApi)

    private val _uiState = MutableStateFlow<EscaneoUiState>(EscaneoUiState.Escaneando)
    val uiState: StateFlow<EscaneoUiState> = _uiState

    private val _codigoDetectado = MutableStateFlow<String?>(null)
    val codigoDetectado: StateFlow<String?> = _codigoDetectado

    private val _tipoCodigoDetectado = MutableStateFlow<TipoCodigo?>(null)
    val tipoCodigoDetectado: StateFlow<TipoCodigo?> = _tipoCodigoDetectado

    private val _linternaEncendida = MutableStateFlow<Boolean>(false)
    val linternaEncendida: StateFlow<Boolean> = _linternaEncendida

    // Último código que se buscó. No se vuelve a buscar hasta que la cámara pase unos cuadros seguidos sin verlo, para que al volver con la cámara todavía apuntando al mismo cómic no se abra solo otra vez.
    private var bloqueo = BloqueoCodigo(codigoBloqueado = null, cuadrosSinVerlo = 0)

    private var camara: Camera? = null

    // Lector de ML Kit configurado solo para los formatos que usan los cómics (EAN-13 y UPC-A)
    private val lectorCodigos: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A
            )
            .build()
    )

    // Hilo aparte donde se analizan los frames, para no bloquear la interfaz
    val ejecutorAnalisis: ExecutorService = Executors.newSingleThreadExecutor()

    // Guarda la cámara cuando CameraX termina de iniciarla, para poder controlar la linterna.
    // Una cámara recién iniciada tiene la linterna apagada, así que el botón también vuelve a apagado.
    fun guardarCamara(camara: Camera) {
        this.camara = camara
        _linternaEncendida.value = false
    }

    // Enciende o apaga la linterna de la cámara.
    fun alternarLinterna() {
        val camaraActual = camara ?: return
        val nuevoEstado = !_linternaEncendida.value
        camaraActual.cameraControl.enableTorch(nuevoEstado)
        _linternaEncendida.value = nuevoEstado
    }

    // Deja el botón de la linterna en apagado. Se llama cuando la pantalla deja de verse, porque al cerrarse la cámara la linterna se apaga sola.
    fun apagarLinterna() {
        _linternaEncendida.value = false
    }

    // Analiza un frame de la cámara con ML Kit. Si encuentra un código nuevo mientras se está escaneando,
    // guarda su valor y su tipo, y lanza la búsqueda del cómic.
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    fun analizarFrame(imageProxy: ImageProxy) {
        val imagenCamara = imageProxy.image

        if (imagenCamara != null) {
            val imagenEntrada =
                InputImage.fromMediaImage(imagenCamara, imageProxy.imageInfo.rotationDegrees)
            lectorCodigos.process(imagenEntrada)
                .addOnSuccessListener { codigos ->
                    val primerCodigo = codigos.firstOrNull()
                    val valor = primerCodigo?.rawValue
                    val formato = primerCodigo?.format

                    // Actualiza el bloqueo con lo que vio este cuadro (lo libera si la cámara lleva un rato sin ver ese código).
                    bloqueo = actualizarBloqueo(bloqueo, valor)

                    // Solo se acepta un código si la pantalla está escaneando (no buscando ni mostrando un error)
                    // y si no es el código bloqueado
                    if (valor != null && formato != null &&
                        valor != bloqueo.codigoBloqueado &&
                        _uiState.value is EscaneoUiState.Escaneando
                    ) {
                        val tipo = clasificarCodigoEscaneado(valor, formato)
                        _codigoDetectado.value = valor
                        _tipoCodigoDetectado.value = tipo
                        buscarComic(valor, tipo)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    // Busca en el repositorio el cómic que corresponde al código detectado.
    private fun buscarComic(codigo: String, tipo: TipoCodigo) {
        if (tipo == TipoCodigo.ISBN_13) {
            buscarEnLaWeb(codigo)
            return
        }

        _uiState.value = EscaneoUiState.Buscando

        viewModelScope.launch {
            _uiState.value = try {
                when (val resultado = repositorio.buscarPorCodigo(codigo, tipo)) {
                    is ResultadoBusqueda.Unico -> EscaneoUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> EscaneoUiState.VariosResultados(codigo)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                EscaneoUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EscaneoUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Busca en la web el ISBN-13 detectado (o escrito) y actualiza el estado con lo que encuentre.
    private fun buscarEnLaWeb(codigo: String) {
        _uiState.value = EscaneoUiState.BuscandoEnLaWeb

        viewModelScope.launch {
            _uiState.value = try {
                val resultadoWeb = repositorio.buscarCodigoEnLaWeb(codigo)
                when (val resultado = resultadoWeb.resultado) {
                    is ResultadoBusqueda.Unico -> EscaneoUiState.Encontrado(resultado.idComic, resultado.tipoCodigo)
                    is ResultadoBusqueda.Varios -> EscaneoUiState.VariosPorTitulo(resultadoWeb.titulo)
                }
            } catch (e: IOException) {
                // IOException: no hay internet o la respuesta tardó más que el tiempo máximo
                EscaneoUiState.Error(MENSAJE_SIN_CONEXION_COMICS, sinConexion = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EscaneoUiState.Error(e.message ?: MENSAJE_ERROR_DESCONOCIDO)
            }
        }
    }

    // Se llama después de navegar con un resultado (detalle o lista): recuerda el código para no
    // volver a buscarlo en cuanto el usuario regrese hasta que la cámara deje de verlo un momento, y deja la cámara lista para escanear otro.
    fun prepararNuevoEscaneo() {
        bloqueo = BloqueoCodigo(_codigoDetectado.value, 0)
        _codigoDetectado.value = null
        _tipoCodigoDetectado.value = null
        _uiState.value = EscaneoUiState.Escaneando
    }

    // Se llama desde el botón REINTENTAR después de un error: vuelve a escanear y permite leer
    // otra vez el mismo código (por ejemplo, si falló por no tener internet).
    fun reintentar() {
        bloqueo = BloqueoCodigo(null, 0)
        _codigoDetectado.value = null
        _tipoCodigoDetectado.value = null
        _uiState.value = EscaneoUiState.Escaneando
    }

    // Se ejecuta cuando el ViewModel se destruye: libera el hilo de análisis y el lector de ML Kit.
    override fun onCleared() {
        super.onCleared()
        ejecutorAnalisis.shutdown()
        lectorCodigos.close()
    }
}

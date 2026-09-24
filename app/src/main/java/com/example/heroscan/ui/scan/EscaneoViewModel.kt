package com.example.heroscan.ui.scan

import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.repository.ComicRepository
import com.example.heroscan.repository.ResultadoBusqueda
import com.example.heroscan.util.clasificarCodigoEscaneado
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Controla la cámara (linterna), analiza cada frame con ML Kit para detectar códigos de barras
// y, cuando detecta uno, busca el cómic en el repositorio.
class EscaneoViewModel : ViewModel() {

    private val repositorio = ComicRepository()

    var uiState: EscaneoUiState by mutableStateOf(EscaneoUiState.Escaneando)
        private set

    var codigoDetectado by mutableStateOf<String?>(null)
        private set

    var tipoCodigoDetectado by mutableStateOf<TipoCodigo?>(null)
        private set

    var linternaEncendida by mutableStateOf(false)
        private set

    // Último código que ya se buscó con éxito. Se ignora para que, al volver del detalle con la cámara
    // todavía apuntando al mismo cómic, no se vuelva a abrir el detalle solo.
    private var codigoYaProcesado: String? = null

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
    fun guardarCamara(camara: Camera) {
        this.camara = camara
    }

    // Enciende o apaga la linterna de la cámara.
    fun alternarLinterna() {
        val camaraActual = camara ?: return
        val nuevoEstado = !linternaEncendida
        camaraActual.cameraControl.enableTorch(nuevoEstado)
        linternaEncendida = nuevoEstado
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

                    // Solo se acepta un código si la pantalla está escaneando (no buscando ni mostrando un error)
                    // y si no es el mismo que ya se encontró antes
                    if (valor != null && formato != null &&
                        valor != codigoYaProcesado &&
                        uiState is EscaneoUiState.Escaneando
                    ) {
                        val tipo = clasificarCodigoEscaneado(valor, formato)
                        codigoDetectado = valor
                        tipoCodigoDetectado = tipo
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
        uiState = EscaneoUiState.Buscando

        viewModelScope.launch {
            uiState = try {
                when (val resultado = repositorio.buscarPorCodigo(codigo, tipo)) {
                    is ResultadoBusqueda.Unico -> EscaneoUiState.Encontrado(resultado.comic)
                    is ResultadoBusqueda.Varios -> EscaneoUiState.VariosResultados(codigo)
                }
            } catch (e: Exception) {
                EscaneoUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Se llama después de navegar con un resultado (detalle o lista): recuerda el código para no
    // volver a buscarlo en cuanto el usuario regrese, y deja la cámara lista para escanear otro.
    fun prepararNuevoEscaneo() {
        codigoYaProcesado = codigoDetectado
        codigoDetectado = null
        tipoCodigoDetectado = null
        uiState = EscaneoUiState.Escaneando
    }

    // Se llama desde el botón REINTENTAR después de un error: vuelve a escanear y permite leer
    // otra vez el mismo código (por ejemplo, si falló por no tener internet).
    fun reintentar() {
        codigoYaProcesado = null
        codigoDetectado = null
        tipoCodigoDetectado = null
        uiState = EscaneoUiState.Escaneando
    }

    // Se ejecuta cuando el ViewModel se destruye: libera el hilo de análisis y el lector de ML Kit.
    override fun onCleared() {
        super.onCleared()
        ejecutorAnalisis.shutdown()
        lectorCodigos.close()
    }
}

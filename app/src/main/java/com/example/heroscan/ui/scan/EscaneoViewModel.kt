package com.example.heroscan.ui.scan

import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.util.clasificarCodigoEscaneado
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Controla la cámara (linterna) y analiza cada frame con ML Kit para detectar códigos de barras.
class EscaneoViewModel : ViewModel() {

    var codigoDetectado by mutableStateOf<String?>(null)
        private set

    var tipoCodigoDetectado by mutableStateOf<TipoCodigo?>(null)
        private set

    var linternaEncendida by mutableStateOf(false)
        private set

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

    // Analiza un frame de la cámara con ML Kit. Si encuentra un código, guarda su valor y su tipo.
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    fun analizarFrame(imageProxy: ImageProxy) {
        val imagenCamara = imageProxy.image
        if (imagenCamara != null) {
            val imagenEntrada = InputImage.fromMediaImage(imagenCamara, imageProxy.imageInfo.rotationDegrees)
            lectorCodigos.process(imagenEntrada)
                .addOnSuccessListener { codigos ->
                    val primerCodigo = codigos.firstOrNull()
                    val valor = primerCodigo?.rawValue
                    val formato = primerCodigo?.format
                    if (valor != null && formato != null) {
                        codigoDetectado = valor
                        tipoCodigoDetectado = clasificarCodigoEscaneado(valor, formato)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    // Se ejecuta cuando el ViewModel se destruye: libera el hilo de análisis y el lector de ML Kit.
    override fun onCleared() {
        super.onCleared()
        ejecutorAnalisis.shutdown()
        lectorCodigos.close()
    }
}

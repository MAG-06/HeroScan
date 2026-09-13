package com.example.heroscan.ui.scan

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import androidx.camera.core.Camera

class ScanViewModel : ViewModel() {

    var detectedCode by mutableStateOf<String?>(null)
        private set

    var detectedFormat by mutableStateOf<Int?>(null)
        private set

    var isFlashOn by mutableStateOf(false)
        private set

    private var camera: Camera? = null

    val detectedTypeName: String?
        get() {
            val code = detectedCode
            val format = detectedFormat
            return if (code != null && format != null) clasificarCodigo(code, format) else null
        }

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A
            )
            .build()
    )

    val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun onCameraReady(camera: Camera) {
        this.camera = camera
    }

    fun toggleFlash() {
        val currentCamera = camera ?: return
        val nuevoEstado = !isFlashOn
        currentCamera.cameraControl.enableTorch(nuevoEstado)
        isFlashOn = nuevoEstado
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    fun analyzeFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val barcode = barcodes.firstOrNull()
                    val code = barcode?.rawValue
                    val format = barcode?.format
                    if (code != null && format != null) {
                        detectedCode = code
                        detectedFormat = format
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun clasificarCodigo(code: String, format: Int): String = when {
        format == Barcode.FORMAT_EAN_13 && (code.startsWith("978") || code.startsWith("979")) -> "ISBN-13"
        format == Barcode.FORMAT_EAN_13 && code.startsWith("977") -> "ISSN"
        format == Barcode.FORMAT_EAN_13 -> "EAN-13"
        format == Barcode.FORMAT_UPC_A -> "UPC-A"
        else -> "Desconocido"
    }

    override fun onCleared() {
        super.onCleared()
        analysisExecutor.shutdown()
        scanner.close()
    }
}
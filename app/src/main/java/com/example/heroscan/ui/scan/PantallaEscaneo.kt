package com.example.heroscan.ui.scan

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heroscan.ui.components.AreaEscaneo
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.components.BotonCircular
import com.example.heroscan.ui.components.PanelInferior
import com.example.heroscan.ui.components.Visor
import com.example.heroscan.ui.theme.AmbarCodigo
import com.example.heroscan.ui.theme.VerdeDetectado

// Pantalla de escaneo: muestra la cámara en vivo (CameraX) y el código de barras detectado.
@Composable
fun PantallaEscaneo(
    alVolver: () -> Unit = {},
    viewModel: EscaneoViewModel = viewModel()
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            BarraSuperior(
                titulo = "ESCANEAR CÓMIC",
                alVolver = alVolver,
                iconoDerecho = Icons.Filled.QrCodeScanner,
                colorIconoDerecho = Color.Black,
                fondoIconoDerecho = MaterialTheme.colorScheme.primary
            )

            AreaCamara(
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )

            ContenidoInferiorEscaneo(
                codigoDetectado = viewModel.codigoDetectado,
                tipoDetectado = viewModel.tipoCodigoDetectado?.etiqueta,
                linternaEncendida = viewModel.linternaEncendida,
                alPulsarLinterna = viewModel::alternarLinterna
            )
        }
    }
}

// Área superior con la vista previa de la cámara y el visor encima.
@Composable
private fun AreaCamara(viewModel: EscaneoViewModel, modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AreaEscaneo(modifier = modifier) {
        // AndroidView permite usar una vista clásica de Android (PreviewView) dentro de Compose
        AndroidView(
            modifier = Modifier
                .size(width = 270.dp, height = 210.dp)
                .clip(RoundedCornerShape(12.dp)),
            factory = { context ->
                val vistaPrevia = PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                iniciarCamara(context, lifecycleOwner, vistaPrevia, viewModel)
                vistaPrevia
            }
        )

        Visor(mostrarLineaEscaneo = true, modifier = Modifier.size(width = 280.dp, height = 220.dp))
    }
}

// Enciende la cámara trasera con CameraX: muestra la imagen en la vista previa y manda cada frame
// al ViewModel para que ML Kit busque códigos de barras.
private fun iniciarCamara(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    vistaPrevia: PreviewView,
    viewModel: EscaneoViewModel
) {
    val proveedorCamaraFuturo = ProcessCameraProvider.getInstance(context)

    proveedorCamaraFuturo.addListener({
        val proveedorCamara = proveedorCamaraFuturo.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(vistaPrevia.surfaceProvider)
        }

        val analisisImagen = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analisis ->
                analisis.setAnalyzer(viewModel.ejecutorAnalisis) { imageProxy ->
                    viewModel.analizarFrame(imageProxy)
                }
            }

        proveedorCamara.unbindAll()
        val camara = proveedorCamara.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analisisImagen
        )
        viewModel.guardarCamara(camara)
    }, ContextCompat.getMainExecutor(context))
}

// Panel inferior: muestra instrucciones o el código detectado, y el botón de la linterna.
@Composable
private fun ContenidoInferiorEscaneo(
    codigoDetectado: String?,
    tipoDetectado: String?,
    linternaEncendida: Boolean,
    alPulsarLinterna: () -> Unit
) {
    PanelInferior {

        Spacer(modifier = Modifier.height(12.dp))

        if (codigoDetectado == null) {
            Text(
                text = "APUNTA AL CÓDIGO DE BARRAS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Coloca el código dentro del área de escaneo para buscar automáticamente en la base de datos de cómics.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        } else {
            SeccionCodigoDetectado(codigo = codigoDetectado, tipo = tipoDetectado ?: "Desconocido")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BotonCircular(
                icono = Icons.Filled.FlashOn,
                texto = "LINTERNA",
                colorIcono = if (linternaEncendida) Color.Black else Color.White,
                colorFondo = if (linternaEncendida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                relleno = true,
                alPulsar = alPulsarLinterna
            )
        }
    }
}

// Muestra la etiqueta "CÓDIGO DETECTADO" junto con el código leído y su tipo.
@Composable
private fun SeccionCodigoDetectado(codigo: String, tipo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(width = 1.dp, color = VerdeDetectado, shape = RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = VerdeDetectado,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CÓDIGO DETECTADO",
                color = VerdeDetectado,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        CajaValorDetectado(texto = codigo)
        CajaValorDetectado(texto = tipo)
    }
}

// Caja con fondo oscuro que muestra un valor detectado (el código o su tipo) en letras grandes.
@Composable
private fun CajaValorDetectado(texto: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = AmbarCodigo,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
    }
}

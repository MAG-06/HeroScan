package com.example.heroscan.ui.scan

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heroscan.model.Comic
import com.example.heroscan.ui.components.AreaEscaneo
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.components.BotonCircular
import com.example.heroscan.ui.components.PanelInferior
import com.example.heroscan.ui.components.Visor
import com.example.heroscan.util.vibrarDispositivo

// Pantalla de escaneo: muestra la cámara en vivo (CameraX), el estado de la búsqueda
// y navega cuando el ViewModel encuentra el cómic.
@Composable
fun PantallaEscaneo(
    alVolver: () -> Unit = {},
    alEncontrarComic: (Comic) -> Unit = {},
    alVariosResultados: (String) -> Unit = {},
    escaneoViewModel: EscaneoViewModel = viewModel()
) {
    val uiState = escaneoViewModel.uiState
    val context = LocalContext.current

    // Cuando el ViewModel termina de buscar: vibra, navega y deja la cámara lista para otro escaneo
    LaunchedEffect(uiState) {
        when (uiState) {
            is EscaneoUiState.Encontrado -> {
                vibrarDispositivo(context)
                alEncontrarComic(uiState.comic)
                escaneoViewModel.prepararNuevoEscaneo()
            }

            is EscaneoUiState.VariosResultados -> {
                vibrarDispositivo(context)
                alVariosResultados(uiState.codigo)
                escaneoViewModel.prepararNuevoEscaneo()
            }

            else -> {}
        }
    }

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
                viewModel = escaneoViewModel,
                modifier = Modifier.weight(1f)
            )

            when (uiState) {
                is EscaneoUiState.Buscando -> {
                    PanelInferior {
                        Spacer(modifier = Modifier.height(20.dp))
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Buscando cómic...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                is EscaneoUiState.Error -> {
                    PanelInferior {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontró el cómic",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.mensaje,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "REINTENTAR",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = escaneoViewModel::reintentar)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                else -> {
                    ContenidoInferiorEscaneo(
                        codigoDetectado = escaneoViewModel.codigoDetectado,
                        tipoDetectado = escaneoViewModel.tipoCodigoDetectado?.etiqueta,
                        linternaEncendida = escaneoViewModel.linternaEncendida,
                        alPulsarLinterna = escaneoViewModel::alternarLinterna
                    )
                }
            }
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

        Visor(
            mostrarLineaEscaneo = true,
            modifier = Modifier.size(width = 280.dp, height = 220.dp)
        )
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
            /*SeccionCodigoDetectado(
                codigo = codigoDetectado,
                tipo = tipoDetectado ?: "Desconocido"
            )*/
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





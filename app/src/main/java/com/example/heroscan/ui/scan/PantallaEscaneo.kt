package com.example.heroscan.ui.scan

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.ui.components.AreaEscaneo
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.components.BotonCircular
import com.example.heroscan.ui.components.PanelInferior
import com.example.heroscan.ui.components.Visor
import com.example.heroscan.ui.theme.TemaHeroScan
import com.example.heroscan.util.vibrarDispositivo

// Pantalla de escaneo: muestra la cámara en vivo (CameraX), el estado de la búsqueda
// y navega cuando el ViewModel encuentra el cómic.
// Crea (o recibe) el ViewModel, vibra y navega al encontrar, y le pasa todo a ContenidoEscaneo (con la cámara como parámetro), que solo dibuja.
@Composable
fun PantallaEscaneo(
    alVolver: () -> Unit = {},
    alEncontrarComic: (Int, TipoCodigo) -> Unit = { _, _ -> },
    alVariosResultados: (String) -> Unit = {},
    alVariosPorTitulo: (String) -> Unit = {},
    escaneoViewModel: EscaneoViewModel = viewModel()
) {
    val uiState by escaneoViewModel.uiState.collectAsStateWithLifecycle()
    val codigoDetectado by escaneoViewModel.codigoDetectado.collectAsStateWithLifecycle()
    val tipoCodigoDetectado by escaneoViewModel.tipoCodigoDetectado.collectAsStateWithLifecycle()
    val linternaEncendida by escaneoViewModel.linternaEncendida.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cuando el ViewModel termina de buscar: vibra, navega y deja la cámara lista para otro escaneo
    LaunchedEffect(uiState) {
        // "val estado" guarda una copia fija de uiState para que Kotlin deje leer sus datos después del "is" (como en la guía Consumo API parte 1).
        when (val estado = uiState) {
            is EscaneoUiState.Encontrado -> {
                vibrarDispositivo(context)
                alEncontrarComic(estado.idComic, estado.tipoCodigo)
                escaneoViewModel.prepararNuevoEscaneo()
            }

            is EscaneoUiState.VariosResultados -> {
                vibrarDispositivo(context)
                alVariosResultados(estado.codigo)
                escaneoViewModel.prepararNuevoEscaneo()
            }

            is EscaneoUiState.VariosPorTitulo -> {
                vibrarDispositivo(context)
                alVariosPorTitulo(estado.titulo)
                escaneoViewModel.prepararNuevoEscaneo()
            }

            else -> {}
        }
    }

    // Si la pantalla deja de verse (app al fondo, otra pantalla o girar), la cámara se cierra y apaga la linterna: el botón también vuelve a apagado.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        escaneoViewModel.apagarLinterna()
    }

    ContenidoEscaneo(
        uiState = uiState,
        codigoDetectado = codigoDetectado,
        tipoDetectado = tipoCodigoDetectado?.etiqueta,
        linternaEncendida = linternaEncendida,
        areaCamara = { modificador -> AreaCamara(viewModel = escaneoViewModel, modifier = modificador) },
        alVolver = alVolver,
        alPulsarLinterna = escaneoViewModel::alternarLinterna,
        alReintentar = escaneoViewModel::reintentar
    )
}

// Dibuja Escaneo con lo que recibe; no tiene ViewModel, así las previews pueden mostrarla con datos de ejemplo.
@Composable
private fun ContenidoEscaneo(
    uiState: EscaneoUiState,
    codigoDetectado: String?,
    tipoDetectado: String?,
    linternaEncendida: Boolean,
    areaCamara: @Composable (Modifier) -> Unit,
    alVolver: () -> Unit = {},
    alPulsarLinterna: () -> Unit = {},
    alReintentar: () -> Unit = {}
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
                colorIconoDerecho = MaterialTheme.colorScheme.onPrimary,
                fondoIconoDerecho = MaterialTheme.colorScheme.primary
            )

            areaCamara(Modifier.weight(1f))

            when (uiState) {
                is EscaneoUiState.Buscando, is EscaneoUiState.BuscandoEnLaWeb -> {
                    PanelInferior {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState is EscaneoUiState.BuscandoEnLaWeb) "Buscando en la web..." else "Buscando cómic...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                is EscaneoUiState.Error -> {
                    PanelInferior {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.sinConexion) "Problema de conexión" else "No se encontró el cómic",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.mensaje,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // TextButton (como en la guía MercadoLocal): el texto se ve igual, pero el área para tocarlo es de 48 dp.
                        TextButton(onClick = alReintentar) {
                            Text(
                                text = "REINTENTAR",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                else -> {
                    ContenidoInferiorEscaneo(
                        codigoDetectado = codigoDetectado,
                        tipoDetectado = tipoDetectado,
                        linternaEncendida = linternaEncendida,
                        alPulsarLinterna = alPulsarLinterna
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

        Spacer(modifier = Modifier.height(8.dp))

        if (codigoDetectado == null) {
            Text(
                text = "APUNTA AL CÓDIGO DE BARRAS",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Coloca el código dentro del área de escaneo para buscar automáticamente en la base de datos de cómics.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            /*SeccionCodigoDetectado(
                codigo = codigoDetectado,
                tipo = tipoDetectado ?: "Desconocido"
            )*/
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BotonCircular(
                icono = Icons.Filled.FlashOn,
                texto = "LINTERNA",
                colorIcono = if (linternaEncendida) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                colorFondo = if (linternaEncendida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                relleno = true,
                alPulsar = alPulsarLinterna
            )
        }
    }
}

// ---------- Previews (solo para ver la pantalla en Android Studio; no se usan en la app) ----------

// Solo para las previews: el área de la cámara con el visor, pero sin cámara (la cámara no se puede dibujar en Android Studio).
@Composable
private fun AreaCamaraEjemplo(modifier: Modifier = Modifier) {
    AreaEscaneo(modifier = modifier) {
        Visor(
            mostrarLineaEscaneo = true,
            modifier = Modifier.size(width = 280.dp, height = 220.dp)
        )
    }
}

// Frame "Escaneo · Apunta al código": la cámara buscando, todavía sin código detectado.
@androidx.compose.ui.tooling.preview.Preview(name = "Escaneo · Apunta al código", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewEscaneoApuntaAlCodigo() {
    TemaHeroScan {
        ContenidoEscaneo(
            uiState = EscaneoUiState.Escaneando,
            codigoDetectado = null,
            tipoDetectado = null,
            linternaEncendida = false,
            areaCamara = { modificador -> AreaCamaraEjemplo(modificador) }
        )
    }
}

// Frame "Escaneo · Buscando": se detectó un código y se está consultando Metron.
@androidx.compose.ui.tooling.preview.Preview(name = "Escaneo · Buscando", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewEscaneoBuscando() {
    TemaHeroScan {
        ContenidoEscaneo(
            uiState = EscaneoUiState.Buscando,
            codigoDetectado = null,
            tipoDetectado = null,
            linternaEncendida = false,
            areaCamara = { modificador -> AreaCamaraEjemplo(modificador) }
        )
    }
}

// Frame "Escaneo · Buscando en la web": se detectó un ISBN-13 y se está buscando en la web.
@androidx.compose.ui.tooling.preview.Preview(name = "Escaneo · Buscando en la web", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewEscaneoBuscandoEnLaWeb() {
    TemaHeroScan {
        ContenidoEscaneo(
            uiState = EscaneoUiState.BuscandoEnLaWeb,
            codigoDetectado = null,
            tipoDetectado = null,
            linternaEncendida = false,
            areaCamara = { modificador -> AreaCamaraEjemplo(modificador) }
        )
    }
}

// Frame "Escaneo · Error · No encontrado": el código no corresponde a ningún cómic.
@androidx.compose.ui.tooling.preview.Preview(name = "Escaneo · Error · No encontrado", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewEscaneoErrorNoEncontrado() {
    TemaHeroScan {
        ContenidoEscaneo(
            uiState = EscaneoUiState.Error("No se encontró ningún cómic con ese código"),
            codigoDetectado = null,
            tipoDetectado = null,
            linternaEncendida = false,
            areaCamara = { modificador -> AreaCamaraEjemplo(modificador) }
        )
    }
}

// Frame "Escaneo · Error · Sin conexión": no hay internet o el servidor tardó demasiado, con REINTENTAR.
@androidx.compose.ui.tooling.preview.Preview(name = "Escaneo · Error · Sin conexión", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewEscaneoErrorSinConexion() {
    TemaHeroScan {
        ContenidoEscaneo(
            uiState = EscaneoUiState.Error("No hay conexión o el servidor tardó demasiado. Intenta de nuevo.", sinConexion = true),
            codigoDetectado = null,
            tipoDetectado = null,
            linternaEncendida = false,
            areaCamara = { modificador -> AreaCamaraEjemplo(modificador) }
        )
    }
}





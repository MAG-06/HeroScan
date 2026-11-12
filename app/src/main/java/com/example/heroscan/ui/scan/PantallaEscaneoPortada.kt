package com.example.heroscan.ui.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.ui.components.AreaEscaneo
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.components.BotonCircular
import com.example.heroscan.ui.components.PanelInferior
import com.example.heroscan.ui.components.Visor
import com.example.heroscan.ui.theme.CapaOscura
import com.example.heroscan.ui.theme.TemaHeroScan
import com.example.heroscan.util.convertirImagenABase64
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Pantalla para escanear la portada del cómic a partir de una imagen elegida de la galería.
// Al pulsar ENVIAR convierte la imagen a Base64 fuera del hilo principal, se la pasa al ViewModel y navega cuando hay resultado.
// Crea (o recibe) el ViewModel, guarda la imagen elegida, abre la galería y muestra el aviso (Snackbar), y le pasa todo a ContenidoPortada, que solo dibuja.
@Composable
fun PantallaEscaneoPortada(
    alVolver: () -> Unit = {},
    alEncontrarComic: (Int, TipoCodigo) -> Unit = { _, _ -> },
    alVariosResultados: (String) -> Unit = {},
    portadaViewModel: PortadaViewModel = viewModel()
) {
    val uiState by portadaViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // rememberSaveable: la imagen elegida sigue ahí al girar el celular y al volver del detalle o de la lista (solo se guarda su Uri, no la imagen).
    var imagenSeleccionada by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Abre la galería y guarda la imagen que elija el usuario
    val selectorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenSeleccionada = uri
    }

    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()
    // true mientras la imagen se convierte a Base64; evita mandarla dos veces si se toca ENVIAR de nuevo.
    var convirtiendo by remember { mutableStateOf(false) }

    // Cuando el ViewModel termina de buscar: navega con el resultado, o muestra el error con REINTENTAR
    LaunchedEffect(uiState) {
        // "val estado" guarda una copia fija de uiState para que Kotlin deje leer sus datos después del "is" (como en la guía Consumo API parte 1).
        when (val estado = uiState) {
            is PortadaUiState.Encontrado -> {
                alEncontrarComic(estado.idComic, estado.tipoCodigo)
                portadaViewModel.prepararNuevaBusqueda()
            }

            is PortadaUiState.VariosResultados -> {
                alVariosResultados(estado.titulo)
                portadaViewModel.prepararNuevaBusqueda()
            }

            is PortadaUiState.Error -> {
                val resultado = estadoSnackbar.showSnackbar(
                    message = estado.mensaje,
                    actionLabel = "REINTENTAR",
                    duration = SnackbarDuration.Long
                )
                if (resultado == SnackbarResult.ActionPerformed) {
                    portadaViewModel.reintentar()
                } else {
                    portadaViewModel.prepararNuevaBusqueda()
                }
            }

            else -> {}
        }
    }

    ContenidoPortada(
        imagenSeleccionada = imagenSeleccionada,
        buscando = uiState is PortadaUiState.Buscando || convirtiendo,
        estadoSnackbar = estadoSnackbar,
        alVolver = alVolver,
        alPulsarGaleria = { selectorGaleria.launch("image/*") },
        // Convierte la imagen a Base64 fuera del hilo principal y la manda al ViewModel; si no se puede leer, avisa con un Snackbar.
        alPulsarEnviar = {
            val imagen = imagenSeleccionada
            if (imagen != null && !convirtiendo) {
                convirtiendo = true
                alcanceCorrutina.launch {
                    try {
                        // Dispatchers.IO: leer y comprimir la imagen se hace en otro hilo para que la pantalla no se trabe.
                        val imagenBase64 = withContext(Dispatchers.IO) {
                            convertirImagenABase64(context, imagen)
                        }
                        portadaViewModel.enviarPortada(imagenBase64)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Se apaga antes del Snackbar porque showSnackbar espera hasta que el aviso se cierre (si no, ENVIAR seguiría cargando).
                        convirtiendo = false
                        // Texto fijo: si Android ya no deja leer la imagen (por ejemplo, después de cerrar la app en segundo plano), su error viene en inglés.
                        estadoSnackbar.showSnackbar("No se pudo leer la imagen")
                    } finally {
                        convirtiendo = false
                    }
                }
            }
        }
    )
}

// Dibuja Portada con lo que recibe; no tiene ViewModel, así las previews pueden mostrarla con datos de ejemplo.
@Composable
private fun ContenidoPortada(
    imagenSeleccionada: Uri?,
    buscando: Boolean,
    estadoSnackbar: SnackbarHostState = remember { SnackbarHostState() },
    alVolver: () -> Unit = {},
    alPulsarGaleria: () -> Unit = {},
    alPulsarEnviar: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                BarraSuperior(
                    titulo = "ESCANEAR COMIC",
                    alVolver = alVolver,
                    iconoDerecho = Icons.Filled.QrCodeScanner,
                    colorIconoDerecho = MaterialTheme.colorScheme.onPrimary,
                    fondoIconoDerecho = MaterialTheme.colorScheme.primary
                )

                AreaImagenPortada(
                    imagenSeleccionada = imagenSeleccionada,
                    modifier = Modifier.weight(1f)
                )

                PanelInferiorPortada(
                    hayImagen = imagenSeleccionada != null,
                    buscando = buscando,
                    alPulsarGaleria = alPulsarGaleria,
                    alPulsarEnviar = alPulsarEnviar
                )
            }

            SnackbarHost(
                hostState = estadoSnackbar,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// Área superior que muestra la imagen elegida dentro del visor, o un aviso si todavía no hay imagen.
@Composable
private fun AreaImagenPortada(imagenSeleccionada: Uri?, modifier: Modifier = Modifier) {
    AreaEscaneo(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CapaOscura)
        )

        Box(
            modifier = Modifier.size(width = 240.dp, height = 320.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imagenSeleccionada != null) {
                // Coil carga la imagen directamente desde su Uri
                AsyncImage(
                    model = imagenSeleccionada,
                    contentDescription = "Portada seleccionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Text(
                    text = "SIN IMAGEN SELECCIONADA",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Visor(mostrarLineaEscaneo = false)
        }
    }
}

// Panel inferior con el aviso de la IA, el botón ENVIAR (activo solo si hay imagen) y el botón para abrir la galería.
// Mientras se convierte la imagen o se busca, ENVIAR queda deshabilitado y muestra un indicador de carga en vez del texto.
@Composable
private fun PanelInferiorPortada(
    hayImagen: Boolean,
    buscando: Boolean,
    alPulsarGaleria: () -> Unit,
    alPulsarEnviar: () -> Unit
) {
    PanelInferior {
        Text(
            text = "SUBE LA IMAGEN",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "La IA reconoce la portada y puede equivocarse. No subas fotos con datos personales.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = alPulsarEnviar,
            enabled = hayImagen && !buscando,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (buscando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "ENVIAR",
                    color = if (hayImagen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BotonCircular(
            icono = Icons.Filled.Smartphone,
            texto = "GALERIA",
            alPulsar = alPulsarGaleria
        )
    }
}

// ---------- Previews (solo para ver la pantalla en Android Studio; no se usan en la app) ----------

// Uri de ejemplo: solo sirve para que haya "imagen elegida" (ENVIAR habilitado); Coil no la carga en la preview.
private val imagenEjemplo = Uri.parse("content://heroscan/preview/portada")

// Frame "Portada · Sin imagen": todavía no se eligió imagen, ENVIAR deshabilitado.
@Preview(name = "Portada · Sin imagen", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewPortadaSinImagen() {
    TemaHeroScan {
        ContenidoPortada(imagenSeleccionada = null, buscando = false)
    }
}

// Frame "Portada · Con imagen": ya hay imagen elegida, ENVIAR habilitado.
@Preview(name = "Portada · Con imagen", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewPortadaConImagen() {
    TemaHeroScan {
        ContenidoPortada(imagenSeleccionada = imagenEjemplo, buscando = false)
    }
}

// Frame "Portada · Buscando": se envió la imagen y ENVIAR muestra el indicador de carga.
@Preview(name = "Portada · Buscando", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewPortadaBuscando() {
    TemaHeroScan {
        ContenidoPortada(imagenSeleccionada = imagenEjemplo, buscando = true)
    }
}

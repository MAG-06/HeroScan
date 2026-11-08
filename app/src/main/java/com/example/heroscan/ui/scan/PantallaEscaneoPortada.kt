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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

// Pantalla para escanear la portada del cómic a partir de una imagen elegida de la galería.
// Al pulsar ENVIAR convierte la imagen a Base64, se la pasa al ViewModel y navega cuando hay resultado.
// Crea (o recibe) el ViewModel, guarda la imagen elegida, abre la galería y muestra el aviso (Snackbar), y le pasa todo a ContenidoPortada, que solo dibuja.
@Composable
fun PantallaEscaneoPortada(
    alVolver: () -> Unit = {},
    alEncontrarComic: (Int, TipoCodigo) -> Unit = { _, _ -> },
    alVariosResultados: (String) -> Unit = {},
    portadaViewModel: PortadaViewModel = viewModel()
) {
    val uiState = portadaViewModel.uiState
    val context = LocalContext.current

    var imagenSeleccionada by remember { mutableStateOf<Uri?>(null) }

    // Abre la galería y guarda la imagen que elija el usuario
    val selectorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenSeleccionada = uri
    }

    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()

    // Cuando el ViewModel termina de buscar: navega con el resultado, o muestra el error con REINTENTAR
    LaunchedEffect(uiState) {
        when (uiState) {
            is PortadaUiState.Encontrado -> {
                alEncontrarComic(uiState.idComic, uiState.tipoCodigo)
                portadaViewModel.prepararNuevaBusqueda()
            }

            is PortadaUiState.VariosResultados -> {
                alVariosResultados(uiState.titulo)
                portadaViewModel.prepararNuevaBusqueda()
            }

            is PortadaUiState.Error -> {
                val resultado = estadoSnackbar.showSnackbar(
                    message = uiState.mensaje,
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
        buscando = uiState is PortadaUiState.Buscando,
        estadoSnackbar = estadoSnackbar,
        alVolver = alVolver,
        alPulsarGaleria = { selectorGaleria.launch("image/*") },
        // Convierte la imagen a Base64 y la manda al ViewModel; si no se puede leer, avisa con un Snackbar
        alPulsarEnviar = {
            val imagen = imagenSeleccionada
            if (imagen != null) {
                try {
                    val imagenBase64 = convertirImagenABase64(context, imagen)
                    portadaViewModel.enviarPortada(imagenBase64)
                } catch (e: Exception) {
                    alcanceCorrutina.launch {
                        estadoSnackbar.showSnackbar(e.message ?: "No se pudo leer la imagen")
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
                    colorIconoDerecho = Color.Black,
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
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Visor(mostrarLineaEscaneo = false)
        }
    }
}

// Panel inferior con el botón ENVIAR (activo solo si hay imagen) y el botón para abrir la galería.
// Mientras se busca, ENVIAR queda deshabilitado y muestra un indicador de carga en vez del texto.
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
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
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
                    color = if (hayImagen) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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

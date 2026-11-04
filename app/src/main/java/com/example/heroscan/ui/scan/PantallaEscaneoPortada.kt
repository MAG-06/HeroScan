package com.example.heroscan.ui.scan

import android.Manifest
import android.net.Uri
import android.os.Build
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.heroscan.ui.components.AreaEscaneo
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.components.BotonCircular
import com.example.heroscan.ui.components.PanelInferior
import com.example.heroscan.ui.components.Visor
import com.example.heroscan.ui.theme.CapaOscura
import kotlinx.coroutines.launch

// Pantalla para escanear la portada del cómic a partir de una imagen elegida de la galería.
@Composable
fun PantallaEscaneoPortada(alVolver: () -> Unit = {}) {

    var imagenSeleccionada by remember { mutableStateOf<Uri?>(null) }

    // Abre la galería y guarda la imagen que elija el usuario
    val selectorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenSeleccionada = uri
    }

    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()

    // Desde Android 13 el permiso para leer fotos cambió de nombre
    val permisoGaleria = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Pide el permiso de galería: si lo conceden abre la galería, si no muestra un aviso
    val solicitudPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { permisoConcedido ->
        if (permisoConcedido) {
            selectorGaleria.launch("image/*")
        } else {
            alcanceCorrutina.launch {
                estadoSnackbar.showSnackbar(
                    "Se necesita permiso para acceder a las fotos"
                )
            }
        }
    }

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
                    alPulsarGaleria = { solicitudPermiso.launch(permisoGaleria) },
                    // Pendiente: la búsqueda por portada todavía no está implementada
                    alPulsarEnviar = { }
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
@Composable
private fun PanelInferiorPortada(
    hayImagen: Boolean,
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
            enabled = hayImagen,
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
            Text(
                text = "ENVIAR",
                color = if (hayImagen) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BotonCircular(
            icono = Icons.Filled.Smartphone,
            texto = "GALERIA",
            alPulsar = alPulsarGaleria
        )
    }
}

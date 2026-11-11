package com.example.heroscan.ui.home

import android.hardware.Sensor
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.heroscan.R
import com.example.heroscan.ui.components.BarraNavegacionInferior
import com.example.heroscan.ui.components.BloqueTituloPestana
import com.example.heroscan.ui.components.BotonAccionPrincipal
import com.example.heroscan.ui.components.LogoHeroScan
import com.example.heroscan.ui.components.PestanaInferior
import com.example.heroscan.ui.components.mostrarAvisoPermisoCamara
import com.example.heroscan.ui.components.rememberPedirPermisoCamara
import com.example.heroscan.ui.theme.CapaOscura
import com.example.heroscan.ui.theme.TemaHeroScan
import com.example.heroscan.util.sensorFlow
import com.example.heroscan.util.tieneSensor
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// Pantalla principal: permite ir a escanear (pidiendo antes el permiso de cámara) o a las otras formas de búsqueda y, desde la barra inferior, a Perfil.
// Guarda el permiso de cámara, el aviso (Snackbar) y el sensor de agitar, y le pasa todo a ContenidoInicio, que solo dibuja.
@Composable
fun PantallaInicio(
    alEscanear: () -> Unit = {},
    alBuscarPorPortada: () -> Unit = {},
    alBuscarPorTexto: () -> Unit = {},
    alIrAPerfil: () -> Unit = {}
) {

    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()
    val contexto = LocalContext.current
    // El chip "O AGITA EL TELÉFONO" solo se muestra si el teléfono tiene acelerómetro (sin él, agitar no hace nada).
    val hayAcelerometro = remember { contexto.tieneSensor(Sensor.TYPE_ACCELEROMETER) }

    // Pide el permiso de cámara: si lo conceden va a escanear, si no muestra un aviso
    val pedirPermisoCamara = rememberPedirPermisoCamara(
        alConceder = alEscanear,
        alRechazar = {
            alcanceCorrutina.launch {
                mostrarAvisoPermisoCamara(estadoSnackbar, contexto)
            }
        }
    )

    DetectorSacudida(alSacudir = pedirPermisoCamara)

    ContenidoInicio(
        estadoSnackbar = estadoSnackbar,
        alEscanear = pedirPermisoCamara,
        alBuscarPorPortada = alBuscarPorPortada,
        alBuscarPorTexto = alBuscarPorTexto,
        alIrAPerfil = alIrAPerfil,
        mostrarPistaAgitar = hayAcelerometro
    )
}

// Dibuja Inicio con lo que recibe; no tiene ViewModel, así las previews pueden mostrarla con datos de ejemplo.
@Composable
private fun ContenidoInicio(
    estadoSnackbar: SnackbarHostState = remember { SnackbarHostState() },
    alEscanear: () -> Unit = {},
    alBuscarPorPortada: () -> Unit = {},
    alBuscarPorTexto: () -> Unit = {},
    alIrAPerfil: () -> Unit = {},
    mostrarPistaAgitar: Boolean = true
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {

        Box(modifier = Modifier.fillMaxSize()) {

            Column {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {

                    LogoHeroScan()
                    Spacer(modifier = Modifier.height(16.dp))
                    BloqueTituloPestana(
                        titulo = "¡ENCUENTRA TU PRÓXIMO CÓMIC!",
                        subtitulo = "Escanea, busca y descubre."
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    TarjetaEscaneo(alEscanear = alEscanear, mostrarPistaAgitar = mostrarPistaAgitar)

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "OTRAS FORMAS DE BUSCAR",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TarjetaOpcionBusqueda(
                            modifier = Modifier.weight(1f),
                            icono = Icons.Filled.Search,
                            colorIcono = MaterialTheme.colorScheme.primary,
                            titulo = "BUSCAR POR TEXTO",
                            descripcion = "Busca por título, personaje o código.",
                            alPulsar = alBuscarPorTexto
                        )
                        TarjetaOpcionBusqueda(
                            modifier = Modifier.weight(1f),
                            icono = Icons.Filled.Image,
                            colorIcono = MaterialTheme.colorScheme.secondary,
                            titulo = "BUSCAR POR PORTADA",
                            descripcion = "Usa una foto de la portada para encontrar el cómic.",
                            alPulsar = alBuscarPorPortada
                        )
                    }

                }

                BarraNavegacionInferior(
                    pestanaActiva = PestanaInferior.INICIO,
                    alEscanear = alEscanear,
                    alBuscar = alBuscarPorTexto,
                    alIrAPerfil = alIrAPerfil
                )
            }

            SnackbarHost(
                hostState = estadoSnackbar,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// Tarjeta principal con imagen de fondo y el botón "ESCANEAR AHORA". El chip "O AGITA EL TELÉFONO" solo aparece si mostrarPistaAgitar es true.
@Composable
private fun TarjetaEscaneo(alEscanear: () -> Unit, mostrarPistaAgitar: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Capa 1: la imagen, de fondo completo
        Image(
            painter = painterResource(id = R.drawable.imageback_scan_card),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Capa 2: oscurecido semitransparente para que se lea el texto
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(CapaOscura)
        )

        // Capa 3: todo el contenido (ícono, textos, botón), con el margen interno de 16dp
        Box(modifier = Modifier.padding(16.dp)) {

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CÁMARA INTELIGENTE",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ESCANEAR CÓMIC",
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Apunta al código de barras del cómic",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )

                // El chip solo aparece si el teléfono tiene acelerómetro; sin él, el botón queda a 16 dp del subtítulo.
                if (mostrarPistaAgitar) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PistaAgitar()
                }
                Spacer(modifier = Modifier.height(16.dp))

                BotonAccionPrincipal(
                    icono = Icons.Filled.PhotoCamera,
                    texto = "ESCANEAR AHORA",
                    alPulsar = alEscanear
                )
            }
        }
    }
}

// Tarjeta pequeña para una forma alternativa de búsqueda (por texto o por portada).
@Composable
private fun TarjetaOpcionBusqueda(
    modifier: Modifier = Modifier,
    icono: ImageVector,
    colorIcono: Color,
    titulo: String,
    descripcion: String,
    alPulsar: () -> Unit,
    habilitada: Boolean = true
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface, disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        enabled = habilitada,
        onClick = alPulsar
    ) {
        val colorContenido = if (habilitada) colorIcono else MaterialTheme.colorScheme.onSurfaceVariant

        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorIcono.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = colorContenido, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = titulo, color = if (habilitada) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Fuerza mínima (en m/s²) para contar una sacudida. En reposo el acelerómetro marca ~9,8 por la gravedad,
// así que 18 equivale a casi el doble: hay que agitar con ganas, no basta con mover el teléfono.
private const val UMBRAL_SACUDIDA = 18f

// Tiempo mínimo (en milisegundos) entre dos sacudidas, para no abrir el escáner dos veces seguidas.
private const val TIEMPO_ENTRE_SACUDIDAS = 1000L

// Escucha el acelerómetro mientras Inicio está activa y llama a alSacudir cuando el teléfono se agita.
// Está en un composable aparte para que solo él se redibuje con cada lectura, no toda la pantalla.
// minActiveState = RESUMED apaga el sensor si Inicio no está al frente (por ejemplo, con el diálogo del permiso abierto o al ir a Escaneo).
@Composable
private fun DetectorSacudida(alSacudir: () -> Unit) {
    val context = LocalContext.current

    val lectura by remember { context.sensorFlow(Sensor.TYPE_ACCELEROMETER) }
        .collectAsStateWithLifecycle(
            initialValue = floatArrayOf(0f, 0f, 0f),
            minActiveState = Lifecycle.State.RESUMED
        )

    var ultimaSacudida by remember { mutableLongStateOf(0L) }

    // Se ejecuta con cada lectura nueva: calcula la fuerza total (x, y, z) y decide si fue una sacudida
    LaunchedEffect(lectura) {
        val magnitud = sqrt(lectura[0] * lectura[0] + lectura[1] * lectura[1] + lectura[2] * lectura[2])
        val ahora = System.currentTimeMillis()

        if (magnitud > UMBRAL_SACUDIDA && ahora - ultimaSacudida > TIEMPO_ENTRE_SACUDIDAS) {
            ultimaSacudida = ahora
            alSacudir()
        }
    }
}

// Pista pequeña tipo chip ("O AGITA EL TELÉFONO") dentro de la tarjeta de escaneo.
// Solo es visual: avisa que agitar el teléfono también abre el escáner (lo detecta DetectorSacudida). Solo se muestra si el teléfono tiene acelerómetro.
@Composable
private fun PistaAgitar() {
    Row(
        modifier = Modifier
            .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Vibration,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "O AGITA EL TELÉFONO",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp
        )
    }
}

// ---------- Previews (solo para ver la pantalla en Android Studio; no se usan en la app) ----------

// Frame "Inicio": logo, tarjeta de escaneo, las dos formas de buscar y la barra inferior con INICIO activa.
@Preview(name = "Inicio", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewInicio() {
    TemaHeroScan {
        ContenidoInicio()
    }
}

// Frame "Inicio · Sin acelerómetro": igual que Inicio, pero sin el chip "O AGITA EL TELÉFONO".
@Preview(name = "Inicio · Sin acelerómetro", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewInicioSinAcelerometro() {
    TemaHeroScan {
        ContenidoInicio(mostrarPistaAgitar = false)
    }
}

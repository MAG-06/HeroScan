package com.example.heroscan.ui.home

import android.Manifest
import android.hardware.Sensor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.heroscan.R
import com.example.heroscan.ui.components.BotonAccionPrincipal
import com.example.heroscan.ui.theme.CapaOscura
import com.example.heroscan.util.sensorFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// Pantalla principal: permite ir a escanear (pidiendo antes el permiso de cámara) o a las otras formas de búsqueda.
@Composable
fun PantallaInicio(
    alEscanear: () -> Unit = {},
    alBuscarPorPortada: () -> Unit = {},
    alBuscarPorTexto: () -> Unit = {}
) {

    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()

    // Pide el permiso de cámara: si lo conceden va a escanear, si no muestra un aviso
    val solicitudPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { permisoConcedido ->
        if (permisoConcedido) {
            alEscanear()
        } else {
            alcanceCorrutina.launch {
                estadoSnackbar.showSnackbar(
                    "Se necesita permiso de cámara para escanear cómics"
                )
            }
        }
    }

    val pedirPermisoCamara: () -> Unit = {
        solicitudPermisoCamara.launch(Manifest.permission.CAMERA)
    }

    DetectorSacudida(alSacudir = pedirPermisoCamara)

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {

        Box(modifier = Modifier.fillMaxSize()) {

            Column {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {

                    EncabezadoInicio()
                    Spacer(modifier = Modifier.height(20.dp))
                    BloqueTituloInicio()
                    Spacer(modifier = Modifier.height(20.dp))

                    TarjetaEscaneo(alEscanear = pedirPermisoCamara)

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "OTRAS FORMAS DE BUSCAR",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                BarraNavegacionInferior(alEscanear = pedirPermisoCamara, alBuscarPorTexto = alBuscarPorTexto)
            }

            SnackbarHost(
                hostState = estadoSnackbar,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// Logo de la app ("HERO" + "SCAN") en la parte superior.
@Composable
private fun EncabezadoInicio() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "HERO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "SCAN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

// Título grande y subtítulo de bienvenida.
@Composable
private fun BloqueTituloInicio() {
    Text(
        text = "¡ENCUENTRA TU PRÓXIMO CÓMIC!",
        color = Color.White,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 30.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "Escanea, busca y descubre.",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 15.sp
    )
}

// Tarjeta principal con imagen de fondo y el botón "ESCANEAR AHORA".
@Composable
private fun TarjetaEscaneo(alEscanear: () -> Unit) {
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

        // Capa 3: todo el contenido (ícono, textos, botón), con el margen interno de 20dp
        Box(modifier = Modifier.padding(20.dp)) {

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CÁMARA INTELIGENTE",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ESCANEAR CÓMIC",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Apunta a la portada o código de barras",
                    color = Color.White,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                PistaAgitar()
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

        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorIcono.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = colorContenido, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = titulo, color = if (habilitada) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

// Barra de navegación inferior. Avisa hacia arriba cuando se pulsa ESCANEAR o BÚSQUEDA.
@Composable
private fun BarraNavegacionInferior(alEscanear: () -> Unit = {}, alBuscarPorTexto: () -> Unit = {}) {

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, windowInsets = WindowInsets(0, 0, 0, 0)) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("INICIO") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.background
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = alEscanear,
            icon = { Icon(Icons.Filled.CenterFocusStrong, contentDescription = null) },
            label = { Text("ESCANEAR") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = alBuscarPorTexto,
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("BÚSQUEDA") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
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
// Solo es visual: avisa que agitar el teléfono también abre el escáner (lo detecta DetectorSacudida).
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
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp
        )
    }
}

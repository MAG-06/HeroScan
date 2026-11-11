package com.example.heroscan.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heroscan.R
import com.example.heroscan.ui.theme.FondoCamara
import com.example.heroscan.ui.theme.GrisInactivo
import com.example.heroscan.ui.theme.TextoSobreCian

// Componentes reutilizables de la UI: logo, barra superior, botones, visor de escaneo, paneles, barra inferior, título de pestaña, diálogos y permiso de cámara.

// Logo de la app ("HERO" + "SCAN"). No tiene padding propio: lo pone cada pantalla. Lo usan Inicio, Acceso y Perfil.
@Composable
fun LogoHeroScan() {
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
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "HERO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "SCAN", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

// Botón cuadrado con la flecha de "volver". Solo lo usa la barra superior.
@Composable
private fun BotonVolver(alPulsar: () -> Unit) {
    IconButton(
        onClick = alPulsar,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = Color.White
        )
    }
}

// Barra superior de las pantallas: botón volver a la izquierda, título al centro e ícono a la derecha.
@Composable
fun BarraSuperior(
    titulo: String,
    alVolver: () -> Unit,
    iconoDerecho: ImageVector,
    colorIconoDerecho: Color = Color.White,
    fondoIconoDerecho: Color = MaterialTheme.colorScheme.surface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotonVolver(alPulsar = alVolver)

        Text(
            text = titulo,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(fondoIconoDerecho),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconoDerecho,
                contentDescription = null,
                tint = colorIconoDerecho,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Botón principal de acción con forma de píldora, ícono y texto. Se ve gris cuando está deshabilitado.
@Composable
fun BotonAccionPrincipal(
    icono: ImageVector,
    texto: String,
    alPulsar: () -> Unit,
    habilitado: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = alPulsar,
        enabled = habilitado,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val colorContenido = if (habilitado) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        Icon(imageVector = icono, contentDescription = null, tint = colorContenido)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, color = colorContenido, fontWeight = FontWeight.Bold)
    }
}

// Botón circular con un ícono y un texto debajo. Puede ir relleno de color o solo con borde.
@Composable
fun BotonCircular(
    icono: ImageVector,
    texto: String,
    alPulsar: () -> Unit,
    relleno: Boolean = false,
    colorIcono: Color = MaterialTheme.colorScheme.primary,
    colorFondo: Color = MaterialTheme.colorScheme.surface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = alPulsar)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (relleno) {
                        Modifier.background(colorFondo)
                    } else {
                        Modifier.border(width = 2.dp, color = colorIcono, shape = CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                tint = colorIcono,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Dibuja las 4 esquinas del visor de escaneo y, si se pide, una línea horizontal en el centro.
@Composable
fun Visor(mostrarLineaEscaneo: Boolean = false, modifier: Modifier = Modifier) {
    val colorEsquinas = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        val largoEsquina = 30.dp.toPx()
        val grosorLinea = 4.dp.toPx()
        val ancho = size.width
        val alto = size.height

        // Dibuja una esquina: una línea horizontal y una vertical que salen del mismo punto
        fun dibujarEsquina(punto: Offset, finHorizontal: Offset, finVertical: Offset) {
            drawLine(colorEsquinas, punto, finHorizontal, grosorLinea, StrokeCap.Round)
            drawLine(colorEsquinas, punto, finVertical, grosorLinea, StrokeCap.Round)
        }

        // Superior izquierda
        dibujarEsquina(Offset(0f, 0f), Offset(largoEsquina, 0f), Offset(0f, largoEsquina))
        // Superior derecha
        dibujarEsquina(Offset(ancho, 0f), Offset(ancho - largoEsquina, 0f), Offset(ancho, largoEsquina))
        // Inferior izquierda
        dibujarEsquina(Offset(0f, alto), Offset(largoEsquina, alto), Offset(0f, alto - largoEsquina))
        // Inferior derecha
        dibujarEsquina(Offset(ancho, alto), Offset(ancho - largoEsquina, alto), Offset(ancho, alto - largoEsquina))

        // La línea de escaneo: solo si mostrarLineaEscaneo es true
        if (mostrarLineaEscaneo) {
            drawLine(
                color = colorEsquinas.copy(alpha = 0.7f),
                start = Offset(0f, alto / 2),
                end = Offset(ancho, alto / 2),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

// Panel inferior con esquinas superiores redondeadas donde van los textos y botones de las pantallas de escaneo.
@Composable
fun PanelInferior(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = contenido
    )
}

// Área superior de las pantallas de escaneo: pone la imagen de fondo de HeroScan y centra el contenido encima.
@Composable
fun AreaEscaneo(modifier: Modifier = Modifier, contenido: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(FondoCamara),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.imageback_scan_card),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        contenido()
    }
}

// Título grande y subtítulo de una pestaña (por ejemplo, Inicio y Perfil). Las medidas son las de Inicio.
@Composable
fun BloqueTituloPestana(titulo: String, subtitulo: String) {
    Text(
        text = titulo,
        color = Color.White,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 30.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = subtitulo,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 15.sp
    )
}

// Las 4 pestañas de la barra inferior. Cada pantalla con barra dice cuál es la suya para que se vea marcada.
enum class PestanaInferior {
    INICIO,
    ESCANEAR,
    BUSQUEDA,
    PERFIL
}

// Barra de navegación inferior con 4 pestañas (la usan Inicio y Perfil). Marca en cian la pestaña activa
// y avisa hacia arriba cuando se pulsa cada una; quien la usa decide a dónde ir.
@Composable
fun BarraNavegacionInferior(
    pestanaActiva: PestanaInferior,
    alIrAInicio: () -> Unit = {},
    alEscanear: () -> Unit = {},
    alBuscar: () -> Unit = {},
    alIrAPerfil: () -> Unit = {}
) {
    // Los mismos colores para todas: la activa en cian sobre un indicador del color de fondo, las demás en gris.
    val colores = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = MaterialTheme.colorScheme.background
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, windowInsets = WindowInsets(0, 0, 0, 0)) {
        NavigationBarItem(
            selected = pestanaActiva == PestanaInferior.INICIO,
            onClick = alIrAInicio,
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("INICIO") },
            colors = colores
        )
        NavigationBarItem(
            selected = pestanaActiva == PestanaInferior.ESCANEAR,
            onClick = alEscanear,
            icon = { Icon(Icons.Filled.CenterFocusStrong, contentDescription = null) },
            label = { Text("ESCANEAR") },
            colors = colores
        )
        NavigationBarItem(
            selected = pestanaActiva == PestanaInferior.BUSQUEDA,
            onClick = alBuscar,
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("BÚSQUEDA") },
            colors = colores
        )
        NavigationBarItem(
            selected = pestanaActiva == PestanaInferior.PERFIL,
            onClick = alIrAPerfil,
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            label = { Text("PERFIL") },
            colors = colores
        )
    }
}

// Aviso que muestran Inicio y Perfil cuando se niega el permiso de cámara.
const val MENSAJE_PERMISO_CAMARA = "Se necesita permiso de cámara para escanear cómics"

// Prepara la petición del permiso de cámara y devuelve la acción que la lanza.
// Cuando el usuario responde llama a alConceder (si lo dio) o a alRechazar (si lo negó).
// Empieza con "remember" porque, como remember, guarda algo entre redibujos: el lanzador del permiso.
@Composable
fun rememberPedirPermisoCamara(alConceder: () -> Unit, alRechazar: () -> Unit): () -> Unit {
    val solicitudPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { permisoConcedido ->
        if (permisoConcedido) {
            alConceder()
        } else {
            alRechazar()
        }
    }

    return { solicitudPermisoCamara.launch(Manifest.permission.CAMERA) }
}

// Avisa que falta el permiso de cámara, con la acción AJUSTES para abrir los ajustes de HeroScan.
// Hace falta porque, si el usuario niega el permiso varias veces, Android deja de mostrar el diálogo.
// Si ya hay un aviso en pantalla no agrega otro, para que no se acumulen al tocar o agitar varias veces.
suspend fun mostrarAvisoPermisoCamara(estadoSnackbar: SnackbarHostState, contexto: Context) {
    if (estadoSnackbar.currentSnackbarData != null) return

    val resultado = estadoSnackbar.showSnackbar(
        message = MENSAJE_PERMISO_CAMARA,
        actionLabel = "AJUSTES",
        duration = SnackbarDuration.Long
    )

    if (resultado == SnackbarResult.ActionPerformed) {
        contexto.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
        )
    }
}

// Capa de diálogo: oscurece toda la pantalla y pone encima una tarjeta centrada con el contenido que se le pase.
// La usan el diálogo de recuperar contraseña (Acceso) y el de cerrar sesión (Perfil).
@Composable
fun CapaDialogo(contenido: @Composable ColumnScope.() -> Unit) {
    val forma = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
            // Un clickable que no hace nada (y sin efecto visual) para que los toques no lleguen a la pantalla de atrás.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 352.dp)
                .fillMaxWidth()
                .shadow(elevation = 16.dp, shape = forma)
                .clip(forma)
                .background(MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = GrisInactivo, shape = forma)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = contenido
        )
    }
}

// Botón de los diálogos (48 dp, esquinas de 14 dp, texto 14 sp Bold). Relleno = cian con texto oscuro;
// sin relleno = transparente con texto gris. Si cargando es true muestra un indicador y no responde al toque.
@Composable
fun BotonDialogo(
    texto: String,
    alPulsar: () -> Unit,
    relleno: Boolean,
    cargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (!cargando) alPulsar()
        },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (relleno) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (relleno) TextoSobreCian else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.height(48.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = TextoSobreCian,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = texto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// Indicador de carga con un texto debajo. Lo usan Búsqueda ("Buscando cómic...") y Detalle ("Cargando cómic...").
@Composable
fun ContenidoCargando(texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

// Mensaje de error de Búsqueda y Detalle. Si falló por falta de internet (sinConexion), el título dice "Problema de conexión". Si recibe alReintentar, muestra el botón REINTENTAR (Detalle); si no, no lo muestra (Búsqueda).
@Composable
fun ContenidoError(
    mensaje: String,
    sinConexion: Boolean,
    modifier: Modifier = Modifier,
    alReintentar: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (sinConexion) "Problema de conexión" else "No se encontró el cómic",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        if (alReintentar != null) {
            Spacer(modifier = Modifier.height(16.dp))
            // TextButton (como en la guía MercadoLocal): el texto se ve igual, pero el área para tocarlo es de 48 dp.
            TextButton(onClick = alReintentar) {
                Text(
                    text = "REINTENTAR",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

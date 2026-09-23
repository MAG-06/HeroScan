package com.example.heroscan.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heroscan.R
import com.example.heroscan.ui.theme.FondoCamara

// Componentes reutilizables de la UI: barra superior, botones, visor de escaneo y paneles.

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
            .padding(horizontal = 12.dp, vertical = 12.dp),
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
                modifier = Modifier.size(20.dp)
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
        Spacer(modifier = Modifier.height(6.dp))
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
            .padding(horizontal = 24.dp, vertical = 20.dp),
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

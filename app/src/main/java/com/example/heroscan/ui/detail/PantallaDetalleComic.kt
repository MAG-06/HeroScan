package com.example.heroscan.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.heroscan.model.Comic
import com.example.heroscan.model.Personaje
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.theme.AmarilloSeccion
import com.example.heroscan.ui.theme.AmbarCodigo
import com.example.heroscan.ui.theme.FondoPortadaVacia
import com.example.heroscan.ui.theme.RojoSeccion

// Pantalla de detalle: muestra portada, título, créditos, datos y personajes del cómic encontrado.
@Composable
fun PantallaDetalleComic(
    comic: Comic,
    alVolver: () -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            BarraSuperior(
                titulo = "DETALLE DEL CÓMIC",
                alVolver = alVolver,
                iconoDerecho = Icons.Filled.Bookmark,
                colorIconoDerecho = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                PortadaComic(portadaUrl = comic.portadaUrl)

                Spacer(modifier = Modifier.height(16.dp))

                BloqueTituloComic(
                    titulo = comic.titulo,
                    numero = comic.numero,
                    editorial = comic.editorial,
                    fechaPublicacion = comic.fechaPublicacion
                )

                Spacer(modifier = Modifier.height(20.dp))

                SeccionInformacion(
                    icono = Icons.AutoMirrored.Filled.MenuBook,
                    colorIcono = MaterialTheme.colorScheme.primary,
                    titulo = "SOBRE ESTE CÓMIC"
                ) {
                    Text(
                        text = comic.descripcion,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (comic.personajes.isNotEmpty()) {
                    SeccionInformacion(
                        icono = Icons.Filled.Star,
                        colorIcono = AmarilloSeccion,
                        titulo = "PERSONAJES"
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            comic.personajes.forEach { personaje ->
                                AvatarPersonaje(personaje = personaje)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (comic.creadores.isNotEmpty()) {
                    SeccionCreditos(creadores = comic.creadores)
                    Spacer(modifier = Modifier.height(20.dp))
                }

                SeccionInformacion(
                    icono = Icons.Filled.Sell,
                    colorIcono = MaterialTheme.colorScheme.secondary,
                    titulo = "IDENTIFICACIÓN"
                ) {
                    TarjetaInformacion {
                        FilaEtiquetaValor("Código", comic.codigoBarras, AmbarCodigo)
                        FilaEtiquetaValor("Tipo", comic.tipoCodigo.etiqueta, AmbarCodigo)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// Muestra la portada del cómic. Si no tiene, muestra un degradado con el ícono de un libro.
@Composable
private fun PortadaComic(portadaUrl: String) {
    if (portadaUrl.isNotEmpty()) {
        AsyncImage(
            model = portadaUrl,
            contentDescription = "Portada del cómic",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, FondoPortadaVacia))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

// Título del cómic con su número a la derecha, y debajo la editorial y la fecha.
@Composable
private fun BloqueTituloComic(
    titulo: String,
    numero: String,
    editorial: String,
    fechaPublicacion: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "#$numero",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "$editorial · $fechaPublicacion",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.sp
    )
}

// Lista de creadores. Muestra 3 al inicio y permite expandir para ver todos.
@Composable
private fun SeccionCreditos(creadores: List<String>) {
    var expandido by remember { mutableStateOf(false) }

    SeccionInformacion(
        icono = Icons.Filled.Edit,
        colorIcono = RojoSeccion,
        titulo = "CRÉDITOS"
    ) {
        TarjetaInformacion {
            val creadoresVisibles = if (expandido) creadores else creadores.take(3)

            creadoresVisibles.forEach { nombre ->
                Text(
                    text = nombre,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (creadores.size > 3) {
                Text(
                    text = if (expandido) "Ver menos" else "Ver todos (${creadores.size})",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { expandido = !expandido }
                )
            }
        }
    }
}

// Sección con un encabezado (ícono + título de color) y el contenido que recibe debajo.
@Composable
private fun SeccionInformacion(
    icono: ImageVector,
    colorIcono: Color,
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = titulo,
                color = colorIcono,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        contenido()
    }
}

// Tarjeta con fondo oscuro y esquinas redondeadas que agrupa información.
@Composable
private fun TarjetaInformacion(contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        contenido()
    }
}

// Fila con una etiqueta a la izquierda y su valor a la derecha (por ejemplo "Código   123456").
@Composable
private fun FilaEtiquetaValor(etiqueta: String, valor: String, colorValor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(text = valor, color = colorValor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

// Foto circular del personaje con su nombre debajo. Si no tiene foto, muestra su inicial.
@Composable
private fun AvatarPersonaje(personaje: Personaje) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
        if (personaje.imagenUrl.isNotEmpty()) {
            AsyncImage(
                model = personaje.imagenUrl,
                contentDescription = personaje.nombre,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = personaje.nombre.take(1),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = personaje.nombre,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

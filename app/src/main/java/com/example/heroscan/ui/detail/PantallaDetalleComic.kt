package com.example.heroscan.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.heroscan.model.Comic
import com.example.heroscan.model.FuenteComic
import com.example.heroscan.model.Personaje
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.components.ContenidoCargando
import com.example.heroscan.ui.components.ContenidoError
import com.example.heroscan.ui.theme.AmarilloSeccion
import com.example.heroscan.ui.theme.AmbarCodigo
import com.example.heroscan.ui.theme.FondoPortadaVacia
import com.example.heroscan.ui.theme.RojoSeccion
import com.example.heroscan.ui.theme.TemaHeroScan

// Pantalla de detalle: crea el DetalleViewModel y le pasa todo a ContenidoDetalle, que solo dibuja.
@Composable
fun PantallaDetalleComic(
    alVolver: () -> Unit = {},
    detalleViewModel: DetalleViewModel = viewModel()
) {
    val uiState by detalleViewModel.uiState.collectAsStateWithLifecycle()

    ContenidoDetalle(
        uiState = uiState,
        alVolver = alVolver,
        alReintentar = detalleViewModel::cargarComic
    )
}

// Dibuja el detalle con lo que recibe (cargando, error o el cómic); no tiene ViewModel, así las previews pueden mostrarla con datos de ejemplo.
@Composable
private fun ContenidoDetalle(
    uiState: DetalleUiState,
    alVolver: () -> Unit = {},
    alReintentar: () -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            BarraSuperior(
                titulo = "DETALLE DEL CÓMIC",
                alVolver = alVolver,
                iconoDerecho = Icons.Filled.Bookmark,
                colorIconoDerecho = MaterialTheme.colorScheme.primary
            )

            when (uiState) {
                is DetalleUiState.Cargando -> {
                    ContenidoCargando(texto = "Cargando cómic...", modifier = Modifier.weight(1f))
                }
                is DetalleUiState.Error -> {
                    ContenidoError(
                        mensaje = uiState.mensaje,
                        sinConexion = uiState.sinConexion,
                        modifier = Modifier.weight(1f),
                        alReintentar = alReintentar
                    )
                }
                is DetalleUiState.Listo -> {
                    val comic = uiState.comic

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        PortadaComic(portadaUrl = comic.portadaUrl)

                        Spacer(modifier = Modifier.height(16.dp))

                        BloqueTituloComic(
                            titulo = comic.titulo,
                            numero = comic.numero,
                            editorial = comic.editorial,
                            fechaPublicacion = comic.fechaPublicacion
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        SeccionInformacion(
                            icono = Icons.AutoMirrored.Filled.MenuBook,
                            colorIcono = MaterialTheme.colorScheme.primary,
                            titulo = "SOBRE ESTE CÓMIC"
                        ) {
                            Text(
                                text = comic.descripcion,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleSmall,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (comic.creadores.isNotEmpty()) {
                            SeccionCreditos(creadores = comic.creadores)
                            Spacer(modifier = Modifier.height(16.dp))
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

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
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
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
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
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "#$numero",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "$editorial · $fechaPublicacion",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall
    )
}

// Lista de creadores. Muestra 3 al inicio y permite expandir para ver todos.
@Composable
private fun SeccionCreditos(creadores: List<String>) {
    // rememberSaveable: "Ver todos" sigue abierto si se gira el celular.
    var expandido by rememberSaveable { mutableStateOf(false) }

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
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (creadores.size > 3) {
                // TextButton (como en la guía MercadoLocal): el texto se ve igual, pero el área para tocarlo es de 48 dp.
                TextButton(
                    onClick = { expandido = !expandido },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expandido) "Ver menos" else "Ver todos (${creadores.size})",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, letterSpacing = 0.1.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = titulo,
                color = colorIcono,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        contenido()
    }
}

// Fila con una etiqueta a la izquierda y su valor a la derecha (por ejemplo "Código   123456").
@Composable
private fun FilaEtiquetaValor(etiqueta: String, valor: String, colorValor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleSmall)
        Text(text = valor, color = colorValor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = personaje.nombre,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// ---------- Previews (solo para ver la pantalla en Android Studio; no se usan en la app) ----------

// Cómic de ejemplo con todos los datos: descripción, 3 personajes y 5 creadores (así se ve "Ver todos").
private val comicEjemplo = Comic(
    id = "1",
    titulo = "Batman",
    numero = "1",
    editorial = "DC Comics",
    fechaPublicacion = "2016-06-15",
    descripcion = "Bruce Wayne patrulla Gotham mientras un nuevo héroe aparece en la ciudad.",
    personajes = listOf(Personaje("Batman", ""), Personaje("Gotham Girl", ""), Personaje("Alfred Pennyworth", "")),
    creadores = listOf("Tom King", "David Finch", "Matt Banning", "Jordie Bellaire", "John Workman"),
    codigoBarras = "76194134182700111",
    tipoCodigo = TipoCodigo.UPC_A,
    portadaUrl = "",
    fuente = FuenteComic.METRON
)

// El mismo cómic sin personajes ni creadores, y con la descripción que pone el Repository cuando Metron no trae una.
private val comicSinDatos = comicEjemplo.copy(descripcion = "Sin descripción", personajes = emptyList(), creadores = emptyList())

// Frame "Detalle · Completo": portada vacía (degradado), descripción, personajes, créditos e identificación.
@Preview(name = "Detalle · Completo", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewDetalleCompleto() {
    TemaHeroScan {
        ContenidoDetalle(uiState = DetalleUiState.Listo(comicEjemplo))
    }
}

// Frame "Detalle · Sin datos": sin personajes ni créditos; solo descripción e identificación.
@Preview(name = "Detalle · Sin datos", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewDetalleSinDatos() {
    TemaHeroScan {
        ContenidoDetalle(uiState = DetalleUiState.Listo(comicSinDatos))
    }
}

// Frame "Detalle · Cargando": mientras se pide el cómic a Metron.
@Preview(name = "Detalle · Cargando", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewDetalleCargando() {
    TemaHeroScan {
        ContenidoDetalle(uiState = DetalleUiState.Cargando)
    }
}

// Frame "Detalle · Error": falló la carga del cómic, con REINTENTAR.
@Preview(name = "Detalle · Error", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewDetalleError() {
    TemaHeroScan {
        ContenidoDetalle(uiState = DetalleUiState.Error("Error desconocido"))
    }
}

// Frame "Detalle · Error · Sin conexión": sin internet, con REINTENTAR.
@Preview(name = "Detalle · Error · Sin conexión", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewDetalleErrorSinConexion() {
    TemaHeroScan {
        ContenidoDetalle(uiState = DetalleUiState.Error("No hay conexión o el servidor tardó demasiado. Intenta de nuevo.", sinConexion = true))
    }
}

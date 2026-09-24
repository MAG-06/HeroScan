package com.example.heroscan.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.heroscan.model.Comic
import com.example.heroscan.model.ComicResumen
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.util.vibrarDispositivo

// Pantalla de búsqueda por código, titulo o personaje escrito: muestra carga, error o resultados, y vibra cuando encuentra el cómic.
@Composable
fun PantallaBusqueda(
    alVolver: () -> Unit = {},
    alEncontrarComic: (Comic) -> Unit = {},
    busquedaViewModel: BusquedaViewModel = viewModel()
) {
    val uiState = busquedaViewModel.uiState
    val context = LocalContext.current

    // Cuando se encuentra el cómic: vibra, navega al detalle y reinicia el estado de la búsqueda
    LaunchedEffect(uiState) {
        if (uiState is BusquedaUiState.Encontrado) {
            vibrarDispositivo(context)
            alEncontrarComic(uiState.comic)
            busquedaViewModel.reiniciarEstado()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            BarraSuperior(
                titulo = "BUSCAR CÓMIC",
                alVolver = alVolver,
                iconoDerecho = Icons.Filled.CenterFocusStrong,
                colorIconoDerecho = Color.White,
                fondoIconoDerecho = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoBusqueda(
                texto = busquedaViewModel.textoBusqueda,
                alCambiarTexto = busquedaViewModel::cambiarTextoBusqueda,
                alBuscar = busquedaViewModel::buscar
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                busquedaViewModel.filtros.forEach { filtro ->
                    ChipFiltro(
                        texto = filtro,
                        seleccionado = busquedaViewModel.filtroSeleccionado == filtro,
                        alPulsar = { busquedaViewModel.seleccionarFiltro(filtro) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lo que se muestra depende del estado de la búsqueda
            when (uiState) {
                is BusquedaUiState.Inicial -> {
                    ContenidoBusquedaVacia(modifier = Modifier.weight(1f))
                }
                is BusquedaUiState.Cargando -> {
                    ContenidoCargando(modifier = Modifier.weight(1f))
                }
                is BusquedaUiState.Error -> {
                    ContenidoError(
                        mensaje = uiState.mensaje,
                        modifier = Modifier.weight(1f)
                    )
                }
                is BusquedaUiState.VariosResultados -> {
                    ContenidoVariosResultados(
                        resultados = uiState.resultados,
                        alSeleccionar = busquedaViewModel::seleccionarComic,
                        modifier = Modifier.weight(1f)
                    )
                }
                is BusquedaUiState.Encontrado -> {
                    // No muestra nada: el LaunchedEffect ya se encarga de navegar al detalle
                }
            }
        }
    }
}

// Barra de texto para escribir el código. Al pulsar "buscar" en el teclado se lanza la búsqueda.
@Composable
private fun CampoBusqueda(
    texto: String,
    alCambiarTexto: (String) -> Unit,
    alBuscar: () -> Unit
) {
    OutlinedTextField(
        value = texto,
        onValueChange = alCambiarTexto,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { alBuscar() }),
        placeholder = {
            Text(
                text = "Busca por título, personaje o código...",
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { alCambiarTexto("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

// Chip de filtro: relleno de color si está seleccionado, solo con borde si no.
@Composable
private fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    alPulsar: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (seleccionado) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(50)
                    )
                }
            )
            .clickable(onClick = alPulsar)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Contenido que se muestra antes de buscar: ícono grande y un texto de ayuda.
@Composable
private fun ContenidoBusquedaVacia(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DESCUBRE TU PRÓXIMO\nCÓMIC",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Busca por título, personaje, ISBN, UPC o código de barra EAN para comenzar la sincronización.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// Indicador de carga mientras se busca el cómic.
@Composable
private fun ContenidoCargando(modifier: Modifier = Modifier) {
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
            text = "Buscando cómic...",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

// Mensaje que se muestra cuando la búsqueda falla o no encuentra nada.
@Composable
private fun ContenidoError(mensaje: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No se encontró el cómic",
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
    }
}

// Lista de cómics cuando la búsqueda devuelve varios resultados. Al tocar uno se pide su detalle.
@Composable
private fun ContenidoVariosResultados(
    resultados: List<ComicResumen>,
    alSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Se encontraron ${resultados.size} resultados",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(resultados) { comic ->
                TarjetaResultadoComic(comic = comic, alPulsar = { alSeleccionar(comic.id) })
            }
        }
    }
}

// Tarjeta de un resultado: miniatura de la portada, título con número y fecha.
@Composable
private fun TarjetaResultadoComic(
    comic: ComicResumen,
    alPulsar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = alPulsar)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (comic.portadaUrl.isNotEmpty()) {
            AsyncImage(
                model = comic.portadaUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "${comic.titulo} #${comic.numero}",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (comic.fechaPortada.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comic.fechaPortada,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

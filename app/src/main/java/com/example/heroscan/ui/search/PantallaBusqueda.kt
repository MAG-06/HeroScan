package com.example.heroscan.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.heroscan.model.ComicResumen
import com.example.heroscan.model.TipoCodigo
import com.example.heroscan.ui.components.BarraSuperior
import com.example.heroscan.ui.components.ContenidoCargando
import com.example.heroscan.ui.components.ContenidoError
import com.example.heroscan.ui.theme.TemaHeroScan
import com.example.heroscan.util.vibrarDispositivo

// Pantalla de búsqueda por código, titulo o personaje escrito: muestra carga, error o resultados, y vibra cuando encuentra el cómic.
// Crea (o recibe) el ViewModel, vibra y navega al encontrar, y le pasa todo a ContenidoBusqueda, que solo dibuja.
@Composable
fun PantallaBusqueda(
    alVolver: () -> Unit = {},
    alEncontrarComic: (Int, TipoCodigo) -> Unit = { _, _ -> },
    busquedaViewModel: BusquedaViewModel = viewModel()
) {
    val uiState by busquedaViewModel.uiState.collectAsStateWithLifecycle()
    val textoBusqueda by busquedaViewModel.textoBusqueda.collectAsStateWithLifecycle()
    val filtroSeleccionado by busquedaViewModel.filtroSeleccionado.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cuando se encuentra el cómic: vibra, navega al detalle y reinicia el estado de la búsqueda
    LaunchedEffect(uiState) {
        // "val estado" guarda una copia fija de uiState para que Kotlin deje leer sus datos después del "is" (como en la guía Consumo API parte 1).
        when (val estado = uiState) {
            is BusquedaUiState.Encontrado -> {
                vibrarDispositivo(context)
                alEncontrarComic(estado.idComic, estado.tipoCodigo)
                busquedaViewModel.reiniciarEstado()
            }

            else -> {}
        }
    }

    ContenidoBusqueda(
        uiState = uiState,
        textoBusqueda = textoBusqueda,
        filtros = busquedaViewModel.filtros,
        filtroSeleccionado = filtroSeleccionado,
        alVolver = alVolver,
        alCambiarTexto = busquedaViewModel::cambiarTextoBusqueda,
        alBuscar = busquedaViewModel::buscar,
        alSeleccionarFiltro = busquedaViewModel::seleccionarFiltro,
        alSeleccionarComic = busquedaViewModel::seleccionarComic
    )
}

// Dibuja Búsqueda con lo que recibe; no tiene ViewModel, así las previews pueden mostrarla con datos de ejemplo.
@Composable
private fun ContenidoBusqueda(
    uiState: BusquedaUiState,
    textoBusqueda: String,
    filtros: List<String>,
    filtroSeleccionado: String,
    alVolver: () -> Unit = {},
    alCambiarTexto: (String) -> Unit = {},
    alBuscar: () -> Unit = {},
    alSeleccionarFiltro: (String) -> Unit = {},
    alSeleccionarComic: (Int) -> Unit = {}
) {
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
                texto = textoBusqueda,
                alCambiarTexto = alCambiarTexto,
                alBuscar = alBuscar
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtros.forEach { filtro ->
                    ChipFiltro(
                        texto = filtro,
                        seleccionado = filtroSeleccionado == filtro,
                        alPulsar = { alSeleccionarFiltro(filtro) }
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
                    ContenidoCargando(texto = "Buscando cómic...", modifier = Modifier.weight(1f))
                }
                is BusquedaUiState.Error -> {
                    ContenidoError(
                        mensaje = uiState.mensaje,
                        sinConexion = uiState.sinConexion,
                        modifier = Modifier.weight(1f)
                    )
                }
                is BusquedaUiState.VariosResultados -> {
                    ContenidoVariosResultados(
                        resultados = uiState.resultados,
                        alSeleccionar = alSeleccionarComic,
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
                style = MaterialTheme.typography.titleMedium
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
            .padding(horizontal = 16.dp)
    )
}

// Chip de filtro: FilterChip de Material 3 (como en la guía MercadoLocal). Trae el área táctil de 48 dp y le avisa a TalkBack si está seleccionado; se ve como antes: cian si está elegido, solo con borde si no.
@Composable
private fun ChipFiltro(
    texto: String,
    seleccionado: Boolean,
    alPulsar: () -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = alPulsar,
        label = {
            Text(
                text = texto,
                color = if (seleccionado) Color.Black else Color.White,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, letterSpacing = 0.1.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = seleccionado,
            borderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedBorderColor = Color.Transparent,
            borderWidth = 1.dp
        )
    )
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Busca por título, personaje, UPC o código de barras EAN para encontrar tu cómic.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// Lista de cómics cuando la búsqueda devuelve varios resultados. Al tocar uno se abre su detalle.
@Composable
private fun ContenidoVariosResultados(
    resultados: List<ComicResumen>,
    alSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Se encontraron ${resultados.size} resultados",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // key: le dice a Compose qué fila es cuál por el id del cómic, no por su posición en la lista.
            items(resultados, key = { it.id }) { comic ->
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
            .padding(8.dp),
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
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "${comic.titulo} #${comic.numero}",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (comic.fechaPortada.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = comic.fechaPortada,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ---------- Previews (solo para ver la pantalla en Android Studio; no se usan en la app) ----------

// Los mismos filtros que tiene BusquedaViewModel.
private val filtrosEjemplo = listOf("TODO", "TÍTULO", "PERSONAJE", "CÓDIGO")

// Tres cómics de ejemplo para la lista de resultados (sin portada, así se ve el ícono del libro).
private val resultadosEjemplo = listOf(
    ComicResumen(1, "Batman", "1", "2016-06-15", ""),
    ComicResumen(2, "Batman", "2", "2016-07-06", ""),
    ComicResumen(3, "Batman", "3", "2016-07-20", "")
)

// Frame "Búsqueda · Vacía": antes de buscar, con el campo vacío y el filtro TODO.
@Preview(name = "Búsqueda · Vacía", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewBusquedaVacia() {
    TemaHeroScan {
        ContenidoBusqueda(
            uiState = BusquedaUiState.Inicial,
            textoBusqueda = "",
            filtros = filtrosEjemplo,
            filtroSeleccionado = "TODO"
        )
    }
}

// Frame "Búsqueda · Cargando": buscando "Batman" por título.
@Preview(name = "Búsqueda · Cargando", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewBusquedaCargando() {
    TemaHeroScan {
        ContenidoBusqueda(
            uiState = BusquedaUiState.Cargando,
            textoBusqueda = "Batman",
            filtros = filtrosEjemplo,
            filtroSeleccionado = "TÍTULO"
        )
    }
}

// Frame "Búsqueda · Error · No encontrado": el título buscado no existe.
@Preview(name = "Búsqueda · Error · No encontrado", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewBusquedaErrorNoEncontrado() {
    TemaHeroScan {
        ContenidoBusqueda(
            uiState = BusquedaUiState.Error("No se encontró ningún cómic con ese título"),
            textoBusqueda = "zzqqxx",
            filtros = filtrosEjemplo,
            filtroSeleccionado = "TÍTULO"
        )
    }
}

// Frame "Búsqueda · Error · Sin conexión": no hay internet o el servidor tardó demasiado.
@Preview(name = "Búsqueda · Error · Sin conexión", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewBusquedaErrorSinConexion() {
    TemaHeroScan {
        ContenidoBusqueda(
            uiState = BusquedaUiState.Error("No hay conexión o el servidor tardó demasiado. Intenta de nuevo.", sinConexion = true),
            textoBusqueda = "Batman",
            filtros = filtrosEjemplo,
            filtroSeleccionado = "TÍTULO"
        )
    }
}

// Frame "Búsqueda · Resultados": la lista con los 3 cómics de ejemplo.
@Preview(name = "Búsqueda · Resultados", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewBusquedaResultados() {
    TemaHeroScan {
        ContenidoBusqueda(
            uiState = BusquedaUiState.VariosResultados(resultadosEjemplo),
            textoBusqueda = "Batman",
            filtros = filtrosEjemplo,
            filtroSeleccionado = "TÍTULO"
        )
    }
}

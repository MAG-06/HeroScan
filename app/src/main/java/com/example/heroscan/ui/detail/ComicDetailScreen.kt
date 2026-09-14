package com.example.heroscan.ui.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.heroscan.model.Comic
import com.example.heroscan.model.MockComics
import com.example.heroscan.ui.theme.CodeAmber
import com.example.heroscan.ui.theme.SectionRed
import com.example.heroscan.ui.theme.SectionYellow
import com.example.heroscan.ui.components.AppTopBar
import com.example.heroscan.ui.components.PrimaryActionButton


@Composable
fun ComicDetailScreen(
    comicId: String,
    onBackClick: () -> Unit = {},
    onScanAnotherClick: () -> Unit = {}
) {
    val comic = MockComics.getById(comicId)

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        if (comic == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No se encontró el cómic.", color = Color.White)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                AppTopBar(
                    title = "DETALLE DEL CÓMIC",
                    onBackClick = onBackClick,
                    trailingIcon = Icons.Filled.Bookmark,
                    trailingIconTint = MaterialTheme.colorScheme.primary
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    ComicCover()

                    Spacer(modifier = Modifier.height(16.dp))

                    ComicTitleBlock(comic)

                    Spacer(modifier = Modifier.height(20.dp))

                    InfoSection(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconColor = MaterialTheme.colorScheme.primary,
                        title = "SOBRE ESTE CÓMIC"
                    ) {
                        Text(
                            text = comic.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    InfoSection(
                        icon = Icons.Filled.Star,
                        iconColor = SectionYellow,
                        title = "PERSONAJES"
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            comic.characters.forEach { name ->
                                CharacterAvatar(name = name)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    InfoSection(
                        icon = Icons.Filled.Edit,
                        iconColor = SectionRed,
                        title = "CREADORES"
                    ) {
                        InfoCard {
                            comic.creators.forEach { creator ->
                                LabelValueRow(label = creator.role, value = creator.name, valueColor = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    InfoSection(
                        icon = Icons.Filled.Sell,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        title = "IDENTIFICACIÓN"
                    ) {
                        InfoCard {
                            LabelValueRow(label = "Código de barra", value = comic.barcode, valueColor = CodeAmber)
                            LabelValueRow(label = "Tipo de escaneo", value = comic.scanType, valueColor = CodeAmber)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                PrimaryActionButton(
                    icon = Icons.Filled.PhotoCamera,
                    label = "ESCANEAR OTRO CÓMIC",
                    onClick = onScanAnotherClick,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}


// placeholder de la portada, todavia no hay imagen real de ningun comic,
// esto se reemplaza por la portada real cuando se conecte la api
@Composable
private fun ComicCover() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, Color(0xFF1A1830)))
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

// titulo, numero de edicion en la insignia, y editorial + fecha debajo
@Composable
private fun ComicTitleBlock(comic: Comic) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = comic.title,
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
                text = comic.issueNumber,
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "${comic.publisher} • ${comic.releaseDate}",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 13.sp
    )
}

// bloque reutilizable para cada seccion: icono + titulo del color del icono, y el contenido debajo
@Composable
private fun InfoSection(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = iconColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

// tarjeta oscura reutilizada por creadores e identificacion
@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}

// una fila etiqueta-valor, usada tanto en creadores como en identificacion
@Composable
private fun LabelValueRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

// circulo con la inicial del personaje, todavia no hay imagenes reales de personajes
@Composable
private fun CharacterAvatar(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}




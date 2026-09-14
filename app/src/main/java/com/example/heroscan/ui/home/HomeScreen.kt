package com.example.heroscan.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.heroscan.R
import androidx.compose.foundation.layout.WindowInsets
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxSize
import com.example.heroscan.ui.components.PrimaryActionButton

@Composable
fun HomeScreen(
    onScanClick: () -> Unit = {},
    onComicClick: (String) -> Unit = {},
    onPortadaClick: () -> Unit = {},
    onTextoClick: () -> Unit = {}
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {

        isGranted ->
        if (isGranted) {
            onScanClick()
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Se necesita permiso de cámara para escanear cómics"
                )
            }
        }
    }

    val requestCameraPermission: () -> Unit = {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {

        Box(modifier = Modifier.fillMaxSize()) {

            Column {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {

                    HomeHeader()
                    Spacer(modifier = Modifier.height(20.dp))
                    HomeTitleBlock()
                    Spacer(modifier = Modifier.height(20.dp))

                    ScanCard(requestCameraPermission)

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
                        SearchOptionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Search,
                            iconColor = MaterialTheme.colorScheme.primary,
                            title = "BUSCAR POR TEXTO",
                            description = "Busca por título, personaje o editorial.",
                            onClick =  onTextoClick ,
                            true
                        )
                        SearchOptionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Filled.Image,
                            iconColor = MaterialTheme.colorScheme.secondary,
                            title = "BUSCAR POR PORTADA",
                            description = "Usa una foto de la portada para encontrar el cómic.",
                            onClick = onPortadaClick,
                            true
                        )
                    }

                }

                HomeBottomNavBar(onScanClick = requestCameraPermission)
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun HomeHeader() {
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

@Composable
private fun HomeTitleBlock() {
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

@Composable
private fun ScanCard(onScanClick: () -> Unit) {
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

        // Capa 2: oscurecido semitransparente para legibilidad
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0x73121826))
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

                Spacer(modifier = Modifier.height(50.dp))

                PrimaryActionButton(
                    icon = Icons.Filled.PhotoCamera,
                    label = "ESCANEAR AHORA",
                    onClick = onScanClick
                )
            }
        }
    }
}

@Composable
private fun SearchOptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface, disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        onClick = onClick
    ) {
        val contentColor = if (enabled) iconColor else MaterialTheme.colorScheme.onSurfaceVariant

        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun HomeBottomNavBar(onScanClick: () -> Unit = {}) {

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
            onClick = onScanClick,
            icon = { Icon(Icons.Filled.CenterFocusStrong, contentDescription = null) },
            label = { Text("ESCANEAR") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text("BÚSQUEDA") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
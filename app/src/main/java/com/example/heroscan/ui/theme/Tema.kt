package com.example.heroscan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de colores de HeroScan (solo oscuro, sin dynamic color).
private val EsquemaColoresHeroScan = darkColorScheme(
    primary = Cian,
    secondary = Magenta,
    background = Fondo,
    surface = Superficie,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = TextoSecundario
)

// Aplica los colores y la tipografía de HeroScan a todo el contenido que recibe.
@Composable
fun TemaHeroScan(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaColoresHeroScan,
        typography = Tipografia,
        content = contenido
    )
}

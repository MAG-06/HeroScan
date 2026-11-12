package com.example.heroscan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Esquema de colores de HeroScan (solo oscuro, sin dynamic color). Las pantallas toman sus colores de aquí.
private val EsquemaColoresHeroScan = darkColorScheme(
    primary = Cian,
    secondary = Magenta,
    // Texto e íconos que van encima del cian (primary) y del magenta (secondary)
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    background = Fondo,
    surface = Superficie,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = TextoSecundario,
    // Colores del Snackbar: fondo gris azulado (se separa del fondo de la app), texto blanco y la acción (REINTENTAR, AJUSTES) en cian
    inverseSurface = BordeCampo,
    inverseOnSurface = Color.White,
    inversePrimary = Cian,
    // Rojo de error (bordes de campos, tarjeta de error y avisos de la pantalla de Acceso)
    error = RojoSeccion
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

package com.example.heroscan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tema Material 3 de HeroScan (solo oscuro, sin dynamic color).
private val HeroScanColorScheme = darkColorScheme(
    primary = Cyan,
    secondary = Magenta,
    background = Background,
    surface = Surface,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = TextSecondary
)

@Composable
fun HeroScanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HeroScanColorScheme,
        typography = Typography,
        content = content
    )
}
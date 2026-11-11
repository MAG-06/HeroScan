package com.example.heroscan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Estilo base: el mismo bodyLarge de siempre (peso Normal, alto de línea 24 sp, espacio 0.5 sp). Los estilos nuevos lo copian cambiando solo el tamaño, para que los textos se vean igual que cuando el tamaño se escribía en cada Text.
private val estiloBase = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

// Escala de tipografía de HeroScan: un estilo por cada tamaño de letra de la app. bodyMedium, labelLarge y labelMedium quedan como los de Material porque los usan el Snackbar, los botones y la barra inferior.
val Tipografia = Typography(
    displayLarge = estiloBase.copy(fontSize = 36.sp),
    displayMedium = estiloBase.copy(fontSize = 26.sp),
    displaySmall = estiloBase.copy(fontSize = 24.sp),
    headlineLarge = estiloBase.copy(fontSize = 22.sp),
    headlineMedium = estiloBase.copy(fontSize = 20.sp),
    headlineSmall = estiloBase.copy(fontSize = 18.sp),
    titleLarge = estiloBase.copy(fontSize = 15.sp),
    titleMedium = estiloBase.copy(fontSize = 14.sp),
    titleSmall = estiloBase.copy(fontSize = 13.sp),
    bodySmall = estiloBase.copy(fontSize = 12.sp),
    labelSmall = estiloBase.copy(fontSize = 11.sp),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

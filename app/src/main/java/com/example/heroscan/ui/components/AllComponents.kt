package com.example.heroscan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap

// Componentes reutilizables de la UI: barra superior, botones, visor de escaneo y panel inferior.

// Botón circular de "volver" reutilizado en Scan y en Detalle del cómic
@Composable
fun BackIconButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = Color.White
        )
    }
}

// Barra superior reutilizada: volver a la izquierda, título al centro, ícono a la derecha
@Composable
fun AppTopBar(
    title: String,
    onBackClick: () -> Unit,
    trailingIcon: ImageVector,
    trailingIconTint: Color = Color.White,
    trailingIconBackground: Color = MaterialTheme.colorScheme.surface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackIconButton(onClick = onBackClick)

        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(trailingIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = trailingIconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Botón principal de acción (píldora + ícono + texto), ahora con soporte de estado "enabled"
@Composable
fun PrimaryActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val contentColor = if (enabled) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = contentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CircleActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    filled: Boolean = false,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (filled) {
                        Modifier.background(backgroundColor)
                    } else {
                        Modifier.border(width = 2.dp, color = iconColor, shape = CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Viewfinder(showScanLine: Boolean = false, modifier: Modifier = Modifier) {
    val cornerColor = MaterialTheme.colorScheme.primary


    Canvas(modifier = modifier.fillMaxSize()) {
        val cornerLength = 30.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val w = size.width
        val h = size.height

        drawLine(
            cornerColor,
            Offset(0f, 0f),
            Offset(cornerLength, 0f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            cornerColor,
            Offset(0f, 0f),
            Offset(0f, cornerLength),
            strokeWidth,
            StrokeCap.Round
        )

        drawLine(
            cornerColor,
            Offset(w, 0f),
            Offset(w - cornerLength, 0f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            cornerColor,
            Offset(w, 0f),
            Offset(w, cornerLength),
            strokeWidth,
            StrokeCap.Round
        )

        drawLine(
            cornerColor,
            Offset(0f, h),
            Offset(cornerLength, h),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            cornerColor,
            Offset(0f, h),
            Offset(0f, h - cornerLength),
            strokeWidth,
            StrokeCap.Round
        )

        drawLine(
            cornerColor,
            Offset(w, h),
            Offset(w - cornerLength, h),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            cornerColor,
            Offset(w, h),
            Offset(w, h - cornerLength),
            strokeWidth,
            StrokeCap.Round
        )


        // La linea de escaneo: solo si showScanLine es true
        if (showScanLine) {
            drawLine(
                color = cornerColor.copy(alpha = 0.7f),
                start = Offset(0f, h / 2),
                end = Offset(w, h / 2),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}


@Composable
fun BottomPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
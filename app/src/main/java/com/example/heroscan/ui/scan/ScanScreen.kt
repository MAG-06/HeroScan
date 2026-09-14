package com.example.heroscan.ui.scan

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heroscan.R
import com.example.heroscan.ui.theme.CodeAmber
import com.example.heroscan.ui.theme.DetectedGreen
import com.example.heroscan.ui.components.AppTopBar
import com.example.heroscan.ui.components.Viewfinder
import com.example.heroscan.ui.components.CircleActionButton
import com.example.heroscan.ui.components.BottomPanel

@Composable
fun ScanScreen(
    onBackClick: () -> Unit = {},
    viewModel: ScanViewModel = viewModel()
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            AppTopBar(
                title = "ESCANEAR CÓMIC",
                onBackClick = onBackClick,
                trailingIcon = Icons.Filled.QrCodeScanner,
                trailingIconTint = Color.Black,
                trailingIconBackground = MaterialTheme.colorScheme.primary
            )

            CameraArea(
                modifier = Modifier.weight(1f),
                viewModel = viewModel
            )
            ScanBottomContent(
                detectedCode = viewModel.detectedCode,
                detectedType = viewModel.detectedTypeName,
                isFlashOn = viewModel.isFlashOn,
                onFlashClick = viewModel::toggleFlash
            )
        }
    }
}


@Composable
private fun CameraArea(modifier: Modifier = Modifier, viewModel: ScanViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF080B12)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.imageback_scan_card),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        AndroidView(
            modifier = Modifier
                .size(width = 270.dp, height = 210.dp)
                .clip(RoundedCornerShape(12.dp)),
            factory = { ctx ->

                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {

                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(viewModel.analysisExecutor) { imageProxy ->
                                viewModel.analyzeFrame(imageProxy)
                            }
                        }

                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    viewModel.onCameraReady(camera)
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        Viewfinder(showScanLine = true, modifier = Modifier.size(width = 280.dp, height = 220.dp))
    }
}


@Composable
private fun ScanBottomContent(
    detectedCode: String? = null,
    detectedType: String? = null,
    isFlashOn: Boolean = false,
    onFlashClick: () -> Unit = {}
) {
    BottomPanel {

        Spacer(modifier = Modifier.height(12.dp))

        if (detectedCode == null) {
            Text(
                text = "APUNTA AL CÓDIGO DE BARRAS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Coloca el código dentro del área de escaneo para buscar automáticamente en la base de datos de cómics.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        } else {
            DetectedCodeSection(code = detectedCode, tipo = detectedType ?: "Desconocido")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircleActionButton(
                icon = Icons.Filled.FlashOn,
                label = "LINTERNA",
                iconColor = if (isFlashOn) Color.Black else Color.White,
                backgroundColor = if (isFlashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                filled = true,
                onClick = onFlashClick
            )
        }
    }
}

@Composable
private fun DetectedCodeSection(code: String, tipo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(width = 1.dp, color = DetectedGreen, shape = RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = DetectedGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CÓDIGO DETECTADO",
                color = DetectedGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code,
                color = CodeAmber,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tipo,
                color = CodeAmber,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }
    }
}


package com.example.heroscan.ui.perfil

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heroscan.ui.components.BarraNavegacionInferior
import com.example.heroscan.ui.components.BloqueTituloPestana
import com.example.heroscan.ui.components.BotonDialogo
import com.example.heroscan.ui.components.CapaDialogo
import com.example.heroscan.ui.components.LogoHeroScan
import com.example.heroscan.ui.components.PestanaInferior
import com.example.heroscan.ui.components.mostrarAvisoPermisoCamara
import com.example.heroscan.ui.components.rememberPedirPermisoCamara
import com.example.heroscan.ui.theme.TemaHeroScan
import kotlinx.coroutines.launch

// Pantalla de Perfil: muestra el correo de la cuenta y permite cerrar la sesión (con un diálogo para confirmar).
// Crea (o recibe) el ViewModel, guarda si el diálogo está abierto y le pasa todo a ContenidoPerfil, que solo dibuja.
@Composable
fun PantallaPerfil(
    alCerrarSesion: () -> Unit = {},
    alIrAInicio: () -> Unit = {},
    alEscanear: () -> Unit = {},
    alBuscar: () -> Unit = {},
    perfilViewModel: PerfilViewModel = viewModel()
) {
    // rememberSaveable: el diálogo sigue abierto si se gira el celular.
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    val estadoSnackbar = remember { SnackbarHostState() }
    val alcanceCorrutina = rememberCoroutineScope()
    val contexto = LocalContext.current

    // Pide el permiso de cámara: si lo conceden va a escanear, si no muestra el mismo aviso que Inicio.
    val pedirPermisoCamara = rememberPedirPermisoCamara(
        alConceder = alEscanear,
        alRechazar = {
            alcanceCorrutina.launch {
                mostrarAvisoPermisoCamara(estadoSnackbar, contexto)
            }
        }
    )

    // Con el diálogo abierto, "atrás" lo cierra. Sin diálogo queda desactivado y "atrás" vuelve a Inicio como siempre.
    BackHandler(enabled = mostrarDialogo) {
        mostrarDialogo = false
    }

    ContenidoPerfil(
        correo = perfilViewModel.correo,
        mostrarDialogo = mostrarDialogo,
        estadoSnackbar = estadoSnackbar,
        alPulsarCerrarSesion = { mostrarDialogo = true },
        alCancelarDialogo = { mostrarDialogo = false },
        alConfirmarCierre = {
            perfilViewModel.cerrarSesion()
            alCerrarSesion()
        },
        alIrAInicio = alIrAInicio,
        alEscanear = pedirPermisoCamara,
        alBuscar = alBuscar
    )
}

// Dibuja Perfil con lo que recibe; no tiene ViewModel, así las previews pueden mostrarlo con datos de ejemplo.
// Capas: encabezado + tarjeta (con scroll), la barra inferior abajo, el aviso (Snackbar) y, encima de todo, el diálogo.
@Composable
private fun ContenidoPerfil(
    correo: String,
    mostrarDialogo: Boolean,
    estadoSnackbar: SnackbarHostState = remember { SnackbarHostState() },
    alPulsarCerrarSesion: () -> Unit = {},
    alCancelarDialogo: () -> Unit = {},
    alConfirmarCierre: () -> Unit = {},
    alIrAInicio: () -> Unit = {},
    alEscanear: () -> Unit = {},
    alBuscar: () -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {

        Box(modifier = Modifier.fillMaxSize()) {

            Column {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {

                    LogoHeroScan()
                    Spacer(modifier = Modifier.height(20.dp))
                    BloqueTituloPestana(titulo = "PERFIL", subtitulo = "Gestiona tu cuenta y sesión.")
                    Spacer(modifier = Modifier.height(12.dp))

                    TarjetaPerfil(correo = correo, alPulsarCerrarSesion = alPulsarCerrarSesion)
                }

                BarraNavegacionInferior(
                    pestanaActiva = PestanaInferior.PERFIL,
                    alIrAInicio = alIrAInicio,
                    alEscanear = alEscanear,
                    alBuscar = alBuscar
                )
            }

            SnackbarHost(
                hostState = estadoSnackbar,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (mostrarDialogo) {
                DialogoCerrarSesion(alCancelar = alCancelarDialogo, alConfirmar = alConfirmarCierre)
            }
        }
    }
}

// Tarjeta de la cuenta: avatar, correo y el botón CERRAR SESIÓN, con 24 dp entre cada uno.
@Composable
private fun TarjetaPerfil(correo: String, alPulsarCerrarSesion: () -> Unit) {
    val forma = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = forma)
            .padding(24.dp),
        // Todo alineado a la izquierda, como en Figma. El botón igual ocupa el ancho completo.
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Avatar: círculo de 88 dp con el ícono de persona en cian, relleno cian al 15 % y borde cian al 30 %.
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .border(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        // El correo y su explicación, con 8 dp entre ellos. El correo va en 18 sp para que uno normal quepa en una línea;
        // si es muy largo pasa a la siguiente línea (no se corta).
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = correo,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Correo asociado a tu cuenta.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Start
            )
        }

        BotonCerrarSesion(alPulsar = alPulsarCerrarSesion)
    }
}

// Botón "CERRAR SESIÓN" de la tarjeta: ancho completo, 48 dp, sin relleno, con borde y texto magenta.
@Composable
private fun BotonCerrarSesion(alPulsar: () -> Unit) {
    OutlinedButton(
        onClick = alPulsar,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.secondary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "CERRAR SESIÓN", fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// Diálogo "¿Cerrar sesión?" sobre Perfil. CANCELAR lo cierra; CERRAR confirma.
@Composable
private fun DialogoCerrarSesion(alCancelar: () -> Unit, alConfirmar: () -> Unit) {
    CapaDialogo {
        // Mismo estilo de título que el diálogo de recuperar contraseña.
        Text(
            text = "¿Cerrar sesión?",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 1.1.em
        )

        Text(
            text = "Tendrás que volver a ingresar tu correo y contraseña.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 1.5.em
        )

        // Los pesos 136f y 160f salen de Figma: CANCELAR mide 136 dp y el botón de confirmar (CERRAR) 160 dp, con 8 dp entre ellos.
        // Con weight el ancho disponible se reparte en esa misma proporción (la acción principal es la más ancha).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonDialogo(
                texto = "CANCELAR",
                alPulsar = alCancelar,
                relleno = false,
                modifier = Modifier.weight(136f)
            )
            BotonDialogo(
                texto = "CERRAR",
                alPulsar = alConfirmar,
                relleno = true,
                modifier = Modifier.weight(160f)
            )
        }
    }
}

// ---------- Previews (solo para ver la pantalla en Android Studio; no se usan en la app) ----------

// Frame "Perfil · cerrar sesión": la tarjeta con el correo y el botón CERRAR SESIÓN.
@Preview(name = "Perfil", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewPerfil() {
    TemaHeroScan {
        ContenidoPerfil(correo = "mag@correo.com", mostrarDialogo = false)
    }
}

// Frame "Perfil · confirmación de cierre de sesión": el diálogo abierto sobre Perfil.
@Preview(name = "Perfil · Cerrar sesión", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewPerfilCerrarSesion() {
    TemaHeroScan {
        ContenidoPerfil(correo = "mag@correo.com", mostrarDialogo = true)
    }
}

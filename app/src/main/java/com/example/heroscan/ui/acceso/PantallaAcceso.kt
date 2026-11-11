package com.example.heroscan.ui.acceso

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.heroscan.ui.components.BotonDialogo
import com.example.heroscan.ui.components.CapaDialogo
import com.example.heroscan.ui.components.LogoHeroScan
import com.example.heroscan.ui.theme.BordeCampo
import com.example.heroscan.ui.theme.GrisInactivo
import com.example.heroscan.ui.theme.TemaHeroScan
import com.example.heroscan.ui.theme.TextoDeshabilitado
import com.example.heroscan.ui.theme.TextoSobreCian
import com.example.heroscan.util.EvaluacionClave
import com.example.heroscan.util.evaluarClave

// Cantidad de segmentos de la barra del medidor de fortaleza (uno por nivel: Muy débil, Débil, Aceptable, Fuerte).
private const val SEGMENTOS_MEDIDOR = 4

// Pantalla de Acceso: iniciar sesión o crear cuenta.
// Crea (o recibe) el ViewModel, guarda lo que el usuario escribe y le pasa todo a ContenidoAcceso, que solo dibuja.
// Cuando la sesión queda iniciada llama a alIniciarSesion, que lleva a Inicio (lo conecta NavegacionHeroScan).
@Composable
fun PantallaAcceso(
    alIniciarSesion: () -> Unit = {},
    accesoViewModel: AccesoViewModel = viewModel()
) {
    // El nombre de usuario y el correo pueden sobrevivir a girar el celular (rememberSaveable).
    var nombreUsuario by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    // Las contraseñas usan remember (NUNCA rememberSaveable) para que no queden en el estado guardado de Android.
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    val uiState by accesoViewModel.uiState.collectAsStateWithLifecycle()
    val modo by accesoViewModel.modo.collectAsStateWithLifecycle()
    val estadoRecuperacion by accesoViewModel.estadoRecuperacion.collectAsStateWithLifecycle()

    // Cuando uiState cambia a SesionIniciada se avisa una sola vez a quien abrió la pantalla.
    LaunchedEffect(uiState) {
        if (uiState is AccesoUiState.SesionIniciada) {
            alIniciarSesion()
        }
    }

    // Cambia de modo (Iniciar sesión ↔ Crear cuenta) y vacía las contraseñas. El correo se conserva.
    val alCambiarModo: (ModoAcceso) -> Unit = { nuevoModo ->
        clave = ""
        confirmacion = ""
        accesoViewModel.cambiarModo(nuevoModo)
    }

    // BackHandler intercepta el botón "atrás" del celular mientras enabled sea true.
    // Se activa con el diálogo abierto o en Crear cuenta, y entonces "atrás":
    // 1) con el diálogo abierto, lo cierra (en cualquier estado);
    // 2) en Crear cuenta mientras se crea la cuenta (Cargando), no hace nada: así no cambia de modo
    //    ni cierra la app a mitad del registro;
    // 3) en Crear cuenta en cualquier otro estado, vuelve a Iniciar sesión.
    // En Iniciar sesión sin diálogo queda desactivado y "atrás" funciona como siempre.
    val dialogoAbierto = estadoRecuperacion !is RecuperacionUiState.Cerrado
    BackHandler(enabled = dialogoAbierto || modo == ModoAcceso.CREAR_CUENTA) {
        if (dialogoAbierto) {
            accesoViewModel.cerrarRecuperacion()
        } else if (uiState is AccesoUiState.Cargando) {
            // No hace nada a propósito: se espera a que termine el registro.
        } else {
            alCambiarModo(ModoAcceso.INICIAR_SESION)
        }
    }

    ContenidoAcceso(
        modo = modo,
        uiState = uiState,
        errorDeCredenciales = accesoViewModel.errorDeCredenciales,
        correo = correo,
        clave = clave,
        estadoRecuperacion = estadoRecuperacion,
        nombreUsuario = nombreUsuario,
        confirmacion = confirmacion,
        alCambiarCorreo = { nuevoCorreo ->
            correo = nuevoCorreo
            accesoViewModel.limpiarError()
        },
        alCambiarClave = { nuevaClave ->
            clave = nuevaClave
            accesoViewModel.limpiarError()
        },
        alCambiarNombreUsuario = { nuevoNombre ->
            nombreUsuario = nuevoNombre
            accesoViewModel.limpiarError()
        },
        alCambiarConfirmacion = { nuevaConfirmacion ->
            confirmacion = nuevaConfirmacion
            accesoViewModel.limpiarError()
        },
        alEntrar = { accesoViewModel.iniciarSesion(correo, clave) },
        alCrearCuenta = { accesoViewModel.registrar(nombreUsuario, correo, clave, confirmacion) },
        alCambiarModo = alCambiarModo,
        alOlvidarClave = { accesoViewModel.abrirRecuperacion() },
        alCerrarRecuperacion = { accesoViewModel.cerrarRecuperacion() },
        alEnviarRecuperacion = { correoRecuperacion -> accesoViewModel.enviarRecuperacion(correoRecuperacion) }
    )
}

// Dibuja la pantalla de Acceso con el estado que recibe. No conoce al ViewModel: por eso las previews
// pueden mostrar cada estado con datos de mentira.
@Composable
private fun ContenidoAcceso(
    modo: ModoAcceso,
    uiState: AccesoUiState,
    errorDeCredenciales: Boolean,
    correo: String,
    clave: String,
    estadoRecuperacion: RecuperacionUiState = RecuperacionUiState.Cerrado,
    nombreUsuario: String = "",
    confirmacion: String = "",
    alCambiarCorreo: (String) -> Unit = {},
    alCambiarClave: (String) -> Unit = {},
    alCambiarNombreUsuario: (String) -> Unit = {},
    alCambiarConfirmacion: (String) -> Unit = {},
    alEntrar: () -> Unit = {},
    alCrearCuenta: () -> Unit = {},
    alCambiarModo: (ModoAcceso) -> Unit = {},
    alOlvidarClave: () -> Unit = {},
    alCerrarRecuperacion: () -> Unit = {},
    alEnviarRecuperacion: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // imePadding va antes del scroll: achica el área visible cuando sale el teclado y así no tapa los campos.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)) {
                LogoHeroScan()
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(72.dp))

                if (modo == ModoAcceso.INICIAR_SESION) {
                    BloqueTituloAcceso(titulo = "INICIAR SESIÓN", subtitulo = "Entra para escanear tus cómics.")
                    Spacer(modifier = Modifier.height(32.dp))
                    FormularioIniciarSesion(
                        uiState = uiState,
                        errorDeCredenciales = errorDeCredenciales,
                        correo = correo,
                        clave = clave,
                        alCambiarCorreo = alCambiarCorreo,
                        alCambiarClave = alCambiarClave,
                        alEntrar = alEntrar,
                        alIrACrearCuenta = { alCambiarModo(ModoAcceso.CREAR_CUENTA) },
                        alOlvidarClave = alOlvidarClave
                    )
                } else {
                    BloqueTituloAcceso(titulo = "CREAR CUENTA", subtitulo = "Tu próxima aventura empieza aquí.")
                    Spacer(modifier = Modifier.height(32.dp))
                    FormularioCrearCuenta(
                        uiState = uiState,
                        nombreUsuario = nombreUsuario,
                        correo = correo,
                        clave = clave,
                        confirmacion = confirmacion,
                        alCambiarNombreUsuario = alCambiarNombreUsuario,
                        alCambiarCorreo = alCambiarCorreo,
                        alCambiarClave = alCambiarClave,
                        alCambiarConfirmacion = alCambiarConfirmacion,
                        alCrearCuenta = alCrearCuenta,
                        alIrAIniciarSesion = { alCambiarModo(ModoAcceso.INICIAR_SESION) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // El diálogo va fuera de la columna con scroll, encima de todo, para que no se desplace con el contenido.
        if (estadoRecuperacion !is RecuperacionUiState.Cerrado) {
            DialogoRecuperacion(
                estado = estadoRecuperacion,
                correoInicial = correo,
                alCerrar = alCerrarRecuperacion,
                alEnviar = alEnviarRecuperacion
            )
        }
    }
}

// Formulario de Iniciar sesión: correo, contraseña, "¿Olvidaste tu contraseña?", aviso de error o bloqueo,
// botón ENTRAR y enlace a Crear cuenta. Deja 24 dp entre cada elemento.
@Composable
private fun FormularioIniciarSesion(
    uiState: AccesoUiState,
    errorDeCredenciales: Boolean,
    correo: String,
    clave: String,
    alCambiarCorreo: (String) -> Unit,
    alCambiarClave: (String) -> Unit,
    alEntrar: () -> Unit,
    alIrACrearCuenta: () -> Unit,
    alOlvidarClave: () -> Unit
) {
    val cargando = uiState is AccesoUiState.Cargando

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Si el error es de credenciales se pintan en rojo los DOS campos, para no revelar cuál falló.
        CampoAcceso(
            etiqueta = "Correo electrónico",
            valor = correo,
            alCambiar = alCambiarCorreo,
            icono = Icons.Outlined.Email,
            tipoTeclado = KeyboardType.Email,
            conError = errorDeCredenciales,
            habilitado = !cargando
        )

        CampoAcceso(
            etiqueta = "Contraseña",
            valor = clave,
            alCambiar = alCambiarClave,
            icono = Icons.Outlined.Lock,
            tipoTeclado = KeyboardType.Password,
            esClave = true,
            conError = errorDeCredenciales,
            habilitado = !cargando
        )

        // TextButton (como en la guía MercadoLocal): el texto se ve igual, pero el área para tocarlo es de 48 dp.
        TextButton(
            onClick = alOlvidarClave,
            enabled = !cargando,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }

        // Entre el enlace y el botón va la tarjeta de error o el texto de bloqueo, según el estado.
        if (uiState is AccesoUiState.Error) {
            TarjetaErrorAcceso(mensaje = uiState.mensaje)
        }
        if (uiState is AccesoUiState.Bloqueado) {
            TextoBloqueo(segundos = uiState.segundosRestantes)
        }

        BotonAcceso(
            texto = "ENTRAR",
            alPulsar = alEntrar,
            habilitado = uiState !is AccesoUiState.Bloqueado,
            cargando = cargando
        )

        EnlaceInferiorAcceso(
            pregunta = "¿No tienes cuenta? ",
            accion = "Regístrate",
            alPulsar = alIrACrearCuenta,
            habilitado = !cargando
        )
    }
}

// Formulario de Crear cuenta: nombre de usuario, correo, contraseña con medidor, confirmación,
// aviso de error, botón CREAR CUENTA y enlace a Iniciar sesión. Deja 24 dp entre cada elemento.
@Composable
private fun FormularioCrearCuenta(
    uiState: AccesoUiState,
    nombreUsuario: String,
    correo: String,
    clave: String,
    confirmacion: String,
    alCambiarNombreUsuario: (String) -> Unit,
    alCambiarCorreo: (String) -> Unit,
    alCambiarClave: (String) -> Unit,
    alCambiarConfirmacion: (String) -> Unit,
    alCrearCuenta: () -> Unit,
    alIrAIniciarSesion: () -> Unit
) {
    val cargando = uiState is AccesoUiState.Cargando
    // Revisión en vivo: solo cuando ya se escribió algo en la confirmación.
    val clavesNoCoinciden = confirmacion.isNotEmpty() && confirmacion != clave

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        CampoAcceso(
            etiqueta = "Nombre de usuario",
            valor = nombreUsuario,
            alCambiar = alCambiarNombreUsuario,
            icono = Icons.Outlined.Person,
            tipoTeclado = KeyboardType.Text,
            habilitado = !cargando,
            textoAyuda = "3 a 20 caracteres: letras, números, _ o ."
        )

        CampoAcceso(
            etiqueta = "Correo electrónico",
            valor = correo,
            alCambiar = alCambiarCorreo,
            icono = Icons.Outlined.Email,
            tipoTeclado = KeyboardType.Email,
            habilitado = !cargando
        )

        // La contraseña y su medidor van juntos, con 8 dp entre ellos (no 24).
        Column {
            CampoAcceso(
                etiqueta = "Contraseña",
                valor = clave,
                alCambiar = alCambiarClave,
                icono = Icons.Outlined.Lock,
                tipoTeclado = KeyboardType.Password,
                esClave = true,
                habilitado = !cargando
            )
            if (clave.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                // Se vuelve a evaluar en cada tecla: evaluarClave es rápida y no usa la red.
                MedidorFortaleza(evaluacion = evaluarClave(clave, correo))
            }
        }

        CampoAcceso(
            etiqueta = "Confirmar contraseña",
            valor = confirmacion,
            alCambiar = alCambiarConfirmacion,
            icono = Icons.Outlined.Lock,
            tipoTeclado = KeyboardType.Password,
            esClave = true,
            conError = clavesNoCoinciden,
            habilitado = !cargando,
            textoError = if (clavesNoCoinciden) MENSAJE_CLAVES_DISTINTAS else null
        )

        // Misma posición que en Iniciar sesión: entre el último campo y el botón.
        if (uiState is AccesoUiState.Error) {
            TarjetaErrorAcceso(mensaje = uiState.mensaje)
        }
        // Solo pasa si se vino desde Iniciar sesión con el bloqueo activo: se avisa por qué el botón está gris.
        if (uiState is AccesoUiState.Bloqueado) {
            TextoBloqueo(segundos = uiState.segundosRestantes)
        }

        BotonAcceso(
            texto = "CREAR CUENTA",
            alPulsar = alCrearCuenta,
            habilitado = !clavesNoCoinciden && uiState !is AccesoUiState.Bloqueado,
            cargando = cargando
        )

        EnlaceInferiorAcceso(
            pregunta = "¿Ya tienes cuenta? ",
            accion = "Inicia sesión",
            alPulsar = alIrAIniciarSesion,
            habilitado = !cargando
        )
    }
}

// Medidor de fortaleza: barra de 4 segmentos (se pintan nivel + 1), el texto "Fortaleza: …" y los consejos.
// Rojo para Muy débil y Débil; cian para Aceptable y Fuerte.
@Composable
private fun MedidorFortaleza(evaluacion: EvaluacionClave) {
    val colorNivel = if (evaluacion.esAceptable) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val segmentosPintados = evaluacion.nivel.nivel + 1

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (segmento in 1..SEGMENTOS_MEDIDOR) {
                // weight(1f) reparte el ancho en partes iguales entre los 4 segmentos.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (segmento <= segmentosPintados) colorNivel else GrisInactivo)
                )
            }
        }

        Text(
            text = "Fortaleza: ${evaluacion.nivel.etiqueta} (≈ ${evaluacion.bits} bits)",
            color = colorNivel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        // Si es Fuerte no hay consejos y no se agrega nada (así no queda un espacio vacío de 8 dp).
        if (evaluacion.consejos.isNotEmpty()) {
            Column {
                for (consejo in evaluacion.consejos) {
                    Text(
                        text = consejo,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 1.5.em
                    )
                }
            }
        }
    }
}

// Título grande de la pantalla (36 sp, Black) y subtítulo gris, con 8 dp entre los dos.
@Composable
private fun BloqueTituloAcceso(titulo: String, subtitulo: String) {
    Text(
        text = titulo,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Black,
        lineHeight = 1.05.em,
        letterSpacing = (-0.5).sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = subtitulo,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.titleMedium,
        lineHeight = 1.5.em
    )
}

// Campo del formulario con su etiqueta encima. Si esClave es true oculta el texto y muestra el "ojo"
// para verlo; si conError es true el borde se pinta de rojo. Debajo puede llevar un texto de ayuda (gris)
// o un texto de error (rojo), a 8 dp del campo.
@Composable
private fun CampoAcceso(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    icono: ImageVector,
    tipoTeclado: KeyboardType,
    esClave: Boolean = false,
    conError: Boolean = false,
    habilitado: Boolean = true,
    textoAyuda: String? = null,
    textoError: String? = null
) {
    // Si la contraseña se ve o no. Es solo visual, por eso vive aquí y no en el ViewModel.
    var claveVisible by remember { mutableStateOf(false) }
    val claveOculta = esClave && !claveVisible

    Column {
        Text(
            text = etiqueta,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            enabled = habilitado,
            singleLine = true,
            isError = conError,
            // Con la clave oculta los puntos van separados 2 sp, como en el diseño.
            textStyle = TextStyle(
                fontSize = 14.sp,
                letterSpacing = if (claveOculta) 2.sp else TextUnit.Unspecified
            ),
            leadingIcon = {
                Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(24.dp))
            },
            // Solo los campos de contraseña tienen el ojo; en los demás no se pone nada (null).
            trailingIcon = if (esClave) {
                {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            imageVector = if (claveVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                null
            },
            visualTransformation = if (claveOculta) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = BordeCampo,
                disabledBorderColor = BordeCampo,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error,
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        if (textoAyuda != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = textoAyuda,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 1.5.em
            )
        }
        if (textoError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = textoError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 1.5.em
            )
        }
    }
}

// Tarjeta roja con un ícono de alerta y el mensaje de error.
@Composable
private fun TarjetaErrorAcceso(mensaje: String) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.06f))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f), shape = forma)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            lineHeight = 1.5.em
        )
    }
}

// Texto rojo que avisa cuántos segundos faltan para poder volver a intentar.
@Composable
private fun TextoBloqueo(segundos: Int) {
    Text(
        text = "Demasiados intentos. Reintenta en $segundos s.",
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.titleMedium,
        lineHeight = 1.5.em
    )
}

// Botón principal de Acceso (cian, esquinas de 14 dp). Si cargando es true muestra un indicador al lado
// del texto, se sigue viendo cian y no responde al toque. Deshabilitado se ve gris.
@Composable
private fun BotonAcceso(
    texto: String,
    alPulsar: () -> Unit,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    Button(
        onClick = {
            if (!cargando) alPulsar()
        },
        enabled = habilitado,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = TextoSobreCian,
            disabledContainerColor = GrisInactivo,
            disabledContentColor = TextoDeshabilitado
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = TextoSobreCian,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.headlineMedium.copy(lineHeight = 20.sp),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

// Enlace centrado de abajo: una pregunta en gris y la acción en cian (solo la acción se puede tocar).
@Composable
private fun EnlaceInferiorAcceso(
    pregunta: String,
    accion: String,
    alPulsar: () -> Unit,
    habilitado: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = pregunta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            lineHeight = 1.5.em
        )
        // TextButton (como en la guía MercadoLocal): el texto se ve igual, pero el área para tocarlo es de 48 dp.
        TextButton(
            onClick = alPulsar,
            enabled = habilitado,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = accion,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 1.5.em
            )
        }
    }
}

// Diálogo "Recuperar contraseña": capa oscura sobre toda la pantalla y una tarjeta centrada.
// El campo de correo tiene su propio estado y empieza con el correo que se escribió en el formulario.
@Composable
private fun DialogoRecuperacion(
    estado: RecuperacionUiState,
    correoInicial: String,
    alCerrar: () -> Unit,
    alEnviar: (String) -> Unit
) {
    // remember guarda el correo mientras el diálogo está abierto; al cerrarlo y abrirlo vuelve a empezar con correoInicial.
    var correoRecuperacion by remember { mutableStateOf(correoInicial) }
    val enviando = estado is RecuperacionUiState.Enviando
    val enviado = estado is RecuperacionUiState.Enviado

    // CapaDialogo (en ComponentesComunes) pone la capa oscura y la tarjeta; aquí solo va lo de adentro.
    CapaDialogo {
        Text(
            text = "Recuperar contraseña",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 1.1.em
        )

        // Ya enviado, el campo no hace falta: solo queda el texto de confirmación y CERRAR.
        if (!enviado) {
            CampoAcceso(
                etiqueta = "Correo electrónico",
                valor = correoRecuperacion,
                alCambiar = { nuevoCorreo -> correoRecuperacion = nuevoCorreo },
                icono = Icons.Outlined.Email,
                tipoTeclado = KeyboardType.Email,
                habilitado = !enviando
            )
        }

        Text(
            text = if (enviado) {
                "Listo. Si el correo tiene una cuenta, te llegará un enlace."
            } else {
                "Si el correo tiene una cuenta, te llegará un enlace."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            lineHeight = 1.5.em
        )

        if (estado is RecuperacionUiState.Error) {
            Text(
                text = estado.mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
                lineHeight = 1.5.em
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (enviado) {
                BotonDialogo(
                    texto = "CERRAR",
                    alPulsar = alCerrar,
                    relleno = true,
                    modifier = Modifier.weight(1f)
                )
            } else {
                BotonDialogo(
                    texto = "CANCELAR",
                    alPulsar = alCerrar,
                    relleno = false,
                    modifier = Modifier.weight(1f)
                )
                BotonDialogo(
                    texto = "ENVIAR",
                    alPulsar = { alEnviar(correoRecuperacion) },
                    relleno = true,
                    cargando = enviando,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ---------- Previews (solo para ver la pantalla en Android Studio; no se usan en la app) ----------

// Frame "Acceso · Iniciar sesión": formulario vacío.
@Preview(name = "Acceso · Iniciar sesión", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewIniciarSesion() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Normal,
            errorDeCredenciales = false,
            correo = "",
            clave = ""
        )
    }
}

// Frame "Acceso · Error": credenciales incorrectas, los dos campos en rojo y la tarjeta de error.
@Preview(name = "Acceso · Error", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewError() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Error("Correo o contraseña incorrectos."),
            errorDeCredenciales = true,
            correo = "mag@correo.com",
            clave = "clave12345"
        )
    }
}

// Frame "Acceso · Bloqueado": texto de bloqueo y botón deshabilitado.
@Preview(name = "Acceso · Bloqueado", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewBloqueado() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Bloqueado(segundosRestantes = 27),
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "clave12345"
        )
    }
}

// Frame "Acceso · Cargando": indicador dentro del botón y campos deshabilitados.
@Preview(name = "Acceso · Cargando", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewCargando() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Cargando,
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "clave12345"
        )
    }
}

// Frame "Acceso · Crear cuenta": datos escritos y el medidor en Aceptable (3 de 4 segmentos, cian).
@Preview(name = "Acceso · Crear cuenta", widthDp = 390, heightDp = 1000)
@Composable
private fun PreviewCrearCuenta() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.CREAR_CUENTA,
            uiState = AccesoUiState.Normal,
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "Colombia2024!",
            nombreUsuario = "mag_06",
            confirmacion = "Colombia2024!"
        )
    }
}

// Frame "Acceso · Crear cuenta · Error": tarjeta de error entre la confirmación y el botón.
@Preview(name = "Acceso · Crear cuenta · Error", widthDp = 390, heightDp = 1000)
@Composable
private fun PreviewCrearCuentaError() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.CREAR_CUENTA,
            uiState = AccesoUiState.Error("Ese nombre de usuario ya está en uso."),
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "Colombia2024!",
            nombreUsuario = "mag_06",
            confirmacion = "Colombia2024!"
        )
    }
}

// Confirmación distinta: borde rojo, "Las contraseñas no coinciden." y botón deshabilitado.
@Preview(name = "Acceso · No coinciden", widthDp = 390, heightDp = 1000)
@Composable
private fun PreviewNoCoinciden() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.CREAR_CUENTA,
            uiState = AccesoUiState.Normal,
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "Colombia2024!",
            nombreUsuario = "mag_06",
            confirmacion = "Colombia2023!"
        )
    }
}

// Frame "Acceso · Recuperar contraseña": diálogo abierto con el correo del login.
@Preview(name = "Acceso · Recuperar contraseña", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecuperar() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Normal,
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "",
            estadoRecuperacion = RecuperacionUiState.Abierto
        )
    }
}

// Diálogo enviando: indicador en ENVIAR.
@Preview(name = "Acceso · Recuperar contraseña · Enviando", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecuperarEnviando() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Normal,
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "",
            estadoRecuperacion = RecuperacionUiState.Enviando
        )
    }
}

// Diálogo enviado: texto "Listo…" y un solo botón CERRAR.
@Preview(name = "Acceso · Recuperar contraseña · Enviado", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecuperarEnviado() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Normal,
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "",
            estadoRecuperacion = RecuperacionUiState.Enviado
        )
    }
}

// Diálogo con error (por ejemplo, sin conexión): mensaje rojo dentro del diálogo.
@Preview(name = "Acceso · Recuperar contraseña · Error", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewRecuperarError() {
    TemaHeroScan {
        ContenidoAcceso(
            modo = ModoAcceso.INICIAR_SESION,
            uiState = AccesoUiState.Normal,
            errorDeCredenciales = false,
            correo = "mag@correo.com",
            clave = "",
            estadoRecuperacion = RecuperacionUiState.Error("Sin conexión. Revisa tu internet e inténtalo de nuevo.")
        )
    }
}

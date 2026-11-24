package com.example.heroscan.ui.acceso

import com.example.heroscan.model.Usuario

// Modo de la pantalla de Acceso: qué formulario se muestra.
enum class ModoAcceso {
    INICIAR_SESION,
    CREAR_CUENTA
}

// Estados del formulario principal (botón Iniciar sesión / Crear cuenta). Ninguno guarda la contraseña.
sealed interface AccesoUiState {
    // Formulario listo para usarse
    data object Normal : AccesoUiState

    // Se están leyendo los intentos guardados (al abrir la pantalla) o se está esperando a Firebase: la pantalla muestra el indicador dentro del botón
    data object Cargando : AccesoUiState

    // Algo falló: la pantalla muestra una tarjeta con el mensaje
    data class Error(val mensaje: String) : AccesoUiState

    // Demasiados intentos fallidos: la pantalla muestra "Demasiados intentos. Reintenta en N s." y deshabilita el botón
    data class Bloqueado(val segundosRestantes: Int) : AccesoUiState

    // Se inició sesión o se creó la cuenta: la pantalla navega al inicio
    data class SesionIniciada(val usuario: Usuario) : AccesoUiState
}

// Estados del diálogo "Recuperar contraseña". Van aparte porque el diálogo se muestra encima del formulario.
sealed interface RecuperacionUiState {
    // Diálogo cerrado
    data object Cerrado : RecuperacionUiState

    // Diálogo abierto, esperando que el usuario escriba su correo
    data object Abierto : RecuperacionUiState

    // Se está pidiendo el correo de recuperación a Firebase
    data object Enviando : RecuperacionUiState

    // Pedido enviado: la pantalla muestra "Listo. Si el correo tiene una cuenta, te llegará un enlace."
    data object Enviado : RecuperacionUiState

    // Error de formato o de red. Nunca dice si la cuenta existe.
    data class Error(val mensaje: String) : RecuperacionUiState
}

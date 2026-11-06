package com.example.heroscan.util

import android.util.Patterns

// Funciones para revisar el formato del nombre de usuario, el correo y la contraseña antes de llamar a Firebase.
// Las funciones validar… devuelven null si el dato es válido, o el mensaje de error si no lo es.

// Nombre de usuario: de 3 a 20 caracteres, solo letras a-z, números, "_" y ".".
private val FORMATO_NOMBRE_USUARIO = Regex("^[a-z0-9_.]{3,20}$")

// Largo mínimo de la contraseña (el mismo que exige la consola de Firebase).
// También la usa EvaluadorClave para que el largo mínimo exista en un solo lugar.
const val LARGO_MINIMO_CLAVE = 8

// Mensaje cuando el correo no tiene forma de correo. También lo usa CuentaRepository cuando Firebase rechaza el correo.
const val MENSAJE_CORREO_INVALIDO = "El correo no es válido."

// Deja el nombre de usuario como se guarda: sin espacios al inicio o al final y en minúsculas.
fun normalizarNombreUsuario(nombreUsuario: String): String {
    return nombreUsuario.trim().lowercase()
}

// Revisa que el nombre de usuario (ya normalizado) cumpla el formato.
fun validarNombreUsuario(nombreUsuario: String): String? {
    if (!FORMATO_NOMBRE_USUARIO.matches(nombreUsuario)) {
        return "El nombre de usuario debe tener de 3 a 20 caracteres: letras, números, _ o ."
    }
    return null
}

// Revisa que el correo tenga forma de correo (algo@algo.algo), usando el patrón que trae Android.
fun validarCorreo(correo: String): String? {
    if (!Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()) {
        return MENSAJE_CORREO_INVALIDO
    }
    return null
}

// Revisa que la contraseña tenga al menos 8 caracteres. No la modifica ni la guarda.
fun validarClave(clave: String): String? {
    if (clave.length < LARGO_MINIMO_CLAVE) {
        return "La contraseña debe tener al menos 8 caracteres."
    }
    return null
}

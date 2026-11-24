package com.example.heroscan.util

// Estados de un permiso en tiempo de ejecución (como en la guía Segundo plano y notificaciones). La pantalla hace algo distinto en cada uno.
sealed interface EstadoPermiso {
    // El usuario ya dio el permiso: no hay nada que pedir.
    data object Concedido : EstadoPermiso

    // Todavía no se ha preguntado: se puede mostrar el diálogo del sistema.
    data object PuedePedir : EstadoPermiso

    // Lo negó una vez: antes de pedirlo otra vez hay que explicar para qué sirve.
    data object DebeExplicar : EstadoPermiso

    // Lo negó para siempre, o esta versión de Android no muestra el diálogo: solo se puede activar en Ajustes.
    data object BloqueadoEnAjustes : EstadoPermiso
}

// Decide el estado del permiso sin usar nada de Android, para poder probarla con pruebas unitarias.
// versionQuePide es la versión de Android desde la que el permiso se pide con un diálogo (33 para notificaciones).
fun decidirEstadoPermiso(
    concedido: Boolean,
    debeExplicar: Boolean,
    yaPregunto: Boolean,
    versionAndroid: Int,
    versionQuePide: Int
): EstadoPermiso {
    return when {
        concedido -> EstadoPermiso.Concedido
        versionAndroid < versionQuePide -> EstadoPermiso.BloqueadoEnAjustes
        debeExplicar -> EstadoPermiso.DebeExplicar
        !yaPregunto -> EstadoPermiso.PuedePedir
        else -> EstadoPermiso.BloqueadoEnAjustes
    }
}

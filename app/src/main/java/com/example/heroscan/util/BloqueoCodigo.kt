package com.example.heroscan.util

// Cuántos cuadros seguidos sin ver el código bloqueado hacen falta para liberarlo (más o menos 1 segundo; se calibra en el celular).
const val CUADROS_PARA_LIBERAR = 15

// Código que no se vuelve a buscar y cuántos cuadros seguidos lleva la cámara sin verlo.
data class BloqueoCodigo(val codigoBloqueado: String?, val cuadrosSinVerlo: Int)

// Regla de cada cuadro: si ve el código bloqueado, la cuenta vuelve a 0; si no lo ve, suma 1 y al llegar a CUADROS_PARA_LIBERAR lo libera. Sin bloqueo no cambia nada.
fun actualizarBloqueo(bloqueo: BloqueoCodigo, codigoVisto: String?): BloqueoCodigo {
    if (bloqueo.codigoBloqueado == null) return bloqueo
    if (codigoVisto == bloqueo.codigoBloqueado) return BloqueoCodigo(bloqueo.codigoBloqueado, 0)
    val cuadros = bloqueo.cuadrosSinVerlo + 1
    if (cuadros >= CUADROS_PARA_LIBERAR) return BloqueoCodigo(null, 0)
    return BloqueoCodigo(bloqueo.codigoBloqueado, cuadros)
}

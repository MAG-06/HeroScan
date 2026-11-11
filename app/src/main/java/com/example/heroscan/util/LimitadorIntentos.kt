package com.example.heroscan.util

// Número de fallo seguido que activa el primer bloqueo: el 5.º (los fallos 1 a 4 no bloquean).
private const val FALLO_QUE_BLOQUEA = 5

// Primera espera: 30 segundos. Cada fallo adicional la duplica, hasta 15 minutos.
private const val ESPERA_INICIAL_MS = 30_000L
private const val ESPERA_MAXIMA_MS = 15 * 60_000L

// Cuenta los inicios de sesión fallidos seguidos y bloquea un tiempo cuando hay demasiados.
// Es una clase pura (sin Android): no guarda nada; AccesoViewModel guarda sus valores con AlmacenIntentos y los restaura al abrir.
//
// Nota para quien viene de Java: "() -> Long" es un tipo función, es decir, "una función sin parámetros
// que devuelve un Long" (parecido a un Supplier<Long> de Java). "System::currentTimeMillis" es una referencia
// a esa función de Java, y es el valor por defecto: el reloj real. En una prueba se puede pasar un reloj falso.
class LimitadorIntentos(private val reloj: () -> Long = System::currentTimeMillis) {

    // Fallos seguidos. Se puede leer desde afuera, pero solo esta clase lo cambia.
    var fallosSeguidos = 0
        private set

    // Momento (en milisegundos) hasta el que está bloqueado. 0 = sin bloqueo.
    var bloqueadoHastaMs = 0L
        private set

    // Indica si se puede intentar ahora (no hay bloqueo o ya terminó).
    fun puedeIntentar(): Boolean {
        return reloj() >= bloqueadoHastaMs
    }

    // Devuelve cuántos segundos faltan para poder intentar, redondeando hacia arriba (0 si ya se puede).
    fun segundosDeEspera(): Int {
        val restanteMs = bloqueadoHastaMs - reloj()
        if (restanteMs <= 0L) return 0
        return ((restanteMs + 999L) / 1000L).toInt()
    }

    // Suma un fallo y, desde el 5.º, bloquea. Si ya está bloqueado no suma (el intento ni siquiera debería hacerse).
    fun registrarFallo() {
        if (!puedeIntentar()) return
        fallosSeguidos++
        if (fallosSeguidos >= FALLO_QUE_BLOQUEA) {
            bloqueadoHastaMs = reloj() + calcularEsperaMs()
        }
    }

    // Inicio de sesión correcto: vuelve a 0 fallos y quita el bloqueo.
    fun registrarExito() {
        fallosSeguidos = 0
        bloqueadoHastaMs = 0L
    }

    // Cuántos fallos faltan para el bloqueo (el último de ellos ya bloquea). 0 si ya se llegó.
    fun intentosRestantesAntesDeBloqueo(): Int {
        val restantes = FALLO_QUE_BLOQUEA - fallosSeguidos
        if (restantes < 0) return 0
        return restantes
    }

    // Pone los fallos y la hora del bloqueo que se habían guardado (al volver a abrir la app). La espera nunca pasa de 15 minutos, por si el reloj del teléfono se atrasó.
    fun restaurar(fallos: Int, hastaMs: Long) {
        fallosSeguidos = fallos
        bloqueadoHastaMs = minOf(hastaMs, reloj() + ESPERA_MAXIMA_MS)
    }

    // Calcula la espera según los fallos: 30 s en el 5.º, 60 s en el 6.º, 120 s en el 7.º… sin pasar de 15 min.
    private fun calcularEsperaMs(): Long {
        var espera = ESPERA_INICIAL_MS
        val duplicaciones = fallosSeguidos - FALLO_QUE_BLOQUEA
        for (i in 1..duplicaciones) {
            espera *= 2
            if (espera >= ESPERA_MAXIMA_MS) return ESPERA_MAXIMA_MS
        }
        return espera
    }
}

package com.example.heroscan.util

import kotlin.math.log2

// Evalúa qué tan fuerte es una contraseña para mostrar el medidor al crear la cuenta.
// Es una función pura: no usa la red ni Android, no guarda la contraseña y siempre da el mismo resultado
// para los mismos datos. No exige mayúsculas, números ni símbolos (recomendación de NIST): premia el largo.

// Nivel de fortaleza: un texto para mostrar y un número de 0 a 3 para comparar y pintar el medidor.
enum class NivelClave(val etiqueta: String, val nivel: Int) {
    MUY_DEBIL("Muy débil", 0),
    DEBIL("Débil", 1),
    ACEPTABLE("Aceptable", 2),
    FUERTE("Fuerte", 3)
}

// Resultado de evaluar una contraseña: su nivel, los bits estimados (redondeados hacia abajo) y consejos para mejorarla.
data class EvaluacionClave(
    val nivel: NivelClave,
    val bits: Int,
    val consejos: List<String>
) {
    // Para registrarse la contraseña debe ser Aceptable o Fuerte.
    // "get()" hace que se calcule cada vez que se lee, como un getter de Java sin campo propio.
    val esAceptable: Boolean
        get() = nivel.nivel >= NivelClave.ACEPTABLE.nivel
}

// Contraseñas muy usadas: si la clave es una de estas, se considera casi nula (máximo 10 bits).
private val CLAVES_COMUNES = setOf(
    "123456", "1234567", "12345678", "123456789", "1234567890", "111111", "000000", "123123",
    "password", "qwerty", "abc123", "iloveyou", "admin", "colombia", "colombia123", "contraseña"
)

// Filas del teclado: 4 caracteres seguidos de una fila (como "qwer", "asdf" o "7890") cuentan como secuencia.
private val FILAS_TECLADO = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm", "1234567890")

// Patrón "palabra + 1 a 4 números + símbolo opcional", por ejemplo Colombia2024!
private val PATRON_PALABRA_NUMEROS = Regex("^[A-Za-zñÑáéíóúÁÉÍÓÚ]+[0-9]{1,4}[^A-Za-z0-9]?$")

// Tamaño de cada grupo de caracteres para calcular los bits.
private const val ALFABETO_MINUSCULAS = 26
private const val ALFABETO_MAYUSCULAS = 26
private const val ALFABETO_DIGITOS = 10
private const val ALFABETO_SIMBOLOS = 33

// Penalizaciones y límites.
private const val BITS_MAXIMOS_CLAVE_COMUN = 10.0
private const val PENALIZACION_CORREO = 20.0
private const val PENALIZACION_SECUENCIA = 10.0
private const val FACTOR_PENALIZACION = 0.6
private const val LARGO_SECUENCIA = 4
private const val LARGO_MINIMO_PARTE_CORREO = 3

// Umbrales de bits para cada nivel.
private const val UMBRAL_DEBIL = 28.0
private const val UMBRAL_ACEPTABLE = 45.0
private const val UMBRAL_FUERTE = 60.0

// Consejos que se muestran debajo del medidor.
private const val CONSEJO_LARGO = "Usa al menos 8 caracteres."
private const val CONSEJO_COMUN = "Es una de las contraseñas más usadas."
private const val CONSEJO_CORREO = "No uses tu correo dentro de la contraseña."
private const val CONSEJO_SECUENCIA = "Evita secuencias como 1234, abcd o qwer."
private const val CONSEJO_REPETICIONES = "Evita repetir tantos caracteres."
private const val CONSEJO_PATRON = "Evita el patrón palabra + números + símbolo."
private const val CONSEJO_FRASE = "Una frase de varias palabras es más larga y más fácil de recordar."

// Evalúa la contraseña. El correo es opcional: si se pasa, se penaliza usar su parte antes de la @.
fun evaluarClave(clave: String, correo: String = ""): EvaluacionClave {
    val consejos = mutableListOf<String>()
    val claveMinusculas = clave.lowercase()

    // Bits base = largo × log2(tamaño del alfabeto usado).
    var bits = calcularBitsBase(clave)

    if (clave.length < LARGO_MINIMO_CLAVE) {
        consejos.add(CONSEJO_LARGO)
    }

    if (claveMinusculas in CLAVES_COMUNES) {
        if (bits > BITS_MAXIMOS_CLAVE_COMUN) bits = BITS_MAXIMOS_CLAVE_COMUN
        consejos.add(CONSEJO_COMUN)
    }

    val parteCorreo = correo.trim().substringBefore("@").lowercase()
    if (parteCorreo.length >= LARGO_MINIMO_PARTE_CORREO && claveMinusculas.contains(parteCorreo)) {
        bits -= PENALIZACION_CORREO
        consejos.add(CONSEJO_CORREO)
    }

    val secuencias = contarSecuencias(claveMinusculas)
    if (secuencias > 0) {
        bits -= PENALIZACION_SECUENCIA * secuencias
        consejos.add(CONSEJO_SECUENCIA)
    }

    if (tieneDemasiadasRepeticiones(clave)) {
        bits *= FACTOR_PENALIZACION
        consejos.add(CONSEJO_REPETICIONES)
    }

    if (PATRON_PALABRA_NUMEROS.matches(clave)) {
        bits *= FACTOR_PENALIZACION
        consejos.add(CONSEJO_PATRON)
    }

    if (bits < 0.0) bits = 0.0

    var nivel = when {
        bits < UMBRAL_DEBIL -> NivelClave.MUY_DEBIL
        bits < UMBRAL_ACEPTABLE -> NivelClave.DEBIL
        bits < UMBRAL_FUERTE -> NivelClave.ACEPTABLE
        else -> NivelClave.FUERTE
    }

    // Con menos de 8 caracteres nunca pasa de Débil.
    if (clave.length < LARGO_MINIMO_CLAVE && nivel.nivel > NivelClave.DEBIL.nivel) {
        nivel = NivelClave.DEBIL
    }

    if (nivel != NivelClave.FUERTE) {
        consejos.add(CONSEJO_FRASE)
    }

    return EvaluacionClave(nivel, bits.toInt(), consejos)
}

// Calcula largo × log2(alfabeto), sumando el tamaño de cada grupo de caracteres que aparece en la clave.
// Cualquier carácter que no sea a-z, A-Z ni 0-9 (incluye ñ, tildes y espacio) cuenta como símbolo.
private fun calcularBitsBase(clave: String): Double {
    var tieneMinusculas = false
    var tieneMayusculas = false
    var tieneDigitos = false
    var tieneSimbolos = false

    for (caracter in clave) {
        when (caracter) {
            in 'a'..'z' -> tieneMinusculas = true
            in 'A'..'Z' -> tieneMayusculas = true
            in '0'..'9' -> tieneDigitos = true
            else -> tieneSimbolos = true
        }
    }

    var alfabeto = 0
    if (tieneMinusculas) alfabeto += ALFABETO_MINUSCULAS
    if (tieneMayusculas) alfabeto += ALFABETO_MAYUSCULAS
    if (tieneDigitos) alfabeto += ALFABETO_DIGITOS
    if (tieneSimbolos) alfabeto += ALFABETO_SIMBOLOS

    if (alfabeto == 0) return 0.0
    return clave.length * log2(alfabeto.toDouble())
}

// Cuenta cuántos bloques de 4 caracteres son secuencias, sin contar bloques solapados:
// cuando encuentra uno, salta esos 4 caracteres y sigue buscando después.
private fun contarSecuencias(claveMinusculas: String): Int {
    var cantidad = 0
    var inicio = 0
    while (inicio + LARGO_SECUENCIA <= claveMinusculas.length) {
        val bloque = claveMinusculas.substring(inicio, inicio + LARGO_SECUENCIA)
        if (esSecuencia(bloque)) {
            cantidad++
            inicio += LARGO_SECUENCIA
        } else {
            inicio++
        }
    }
    return cantidad
}

// Indica si el bloque sube o baja de uno en uno (1234, dcba) o sigue una fila del teclado, al derecho o al revés.
private fun esSecuencia(bloque: String): Boolean {
    var sube = true
    var baja = true
    for (i in 1 until bloque.length) {
        val diferencia = bloque[i].code - bloque[i - 1].code
        if (diferencia != 1) sube = false
        if (diferencia != -1) baja = false
    }
    if (sube || baja) return true

    for (fila in FILAS_TECLADO) {
        if (fila.contains(bloque) || fila.reversed().contains(bloque)) return true
    }
    return false
}

// Indica si la clave repite demasiado: caracteres distintos × 3 menor que el largo (por ejemplo "aaaaaaaa").
private fun tieneDemasiadasRepeticiones(clave: String): Boolean {
    val distintos = clave.toSet().size
    return distintos * 3 < clave.length
}

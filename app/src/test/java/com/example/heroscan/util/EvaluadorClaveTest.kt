package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluadorClaveTest {

    // Clave de 12 minúsculas sin secuencias: da Aceptable con 56 bits si no hay penalizaciones.
    private val claveDeDoceMinusculas = "xkvmqzrtwpgh"

    // Revisa juntos el nivel y los bits de una evaluación.
    private fun revisarNivelYBits(nivelEsperado: NivelClave, bitsEsperados: Int, evaluacion: EvaluacionClave) {
        assertEquals(nivelEsperado, evaluacion.nivel)
        assertEquals(bitsEsperados, evaluacion.bits)
    }

    // Comprueba que una contraseña vacía es Muy débil, con 0 bits, y no alcanza para crear la cuenta.
    @Test
    fun claveVaciaEsMuyDebil() {
        val evaluacion = evaluarClave("")
        revisarNivelYBits(NivelClave.MUY_DEBIL, 0, evaluacion)
        assertFalse(evaluacion.esAceptable)
    }

    // Comprueba que una contraseña común queda en máximo 10 bits y muestra su consejo.
    @Test
    fun claveComunQuedaEnDiezBitsConConsejo() {
        val evaluacion = evaluarClave("password")
        revisarNivelYBits(NivelClave.MUY_DEBIL, 10, evaluacion)
        assertTrue(evaluacion.consejos.contains("Es una de las contraseñas más usadas."))
    }

    // Comprueba que una contraseña común se reconoce aunque tenga mayúsculas.
    @Test
    fun claveComunConMayusculasSeReconoce() {
        revisarNivelYBits(NivelClave.MUY_DEBIL, 10, evaluarClave("PassWord"))
    }

    // Comprueba que los bits no bajan de 0 con una clave común que además tiene 2 secuencias.
    @Test
    fun bitsNoBajanDeCero() {
        revisarNivelYBits(NivelClave.MUY_DEBIL, 0, evaluarClave("12345678"))
    }

    // Comprueba el umbral de 28 bits por debajo: 5 minúsculas son Muy débil.
    @Test
    fun debajoDeVeintiochoBitsEsMuyDebil() {
        revisarNivelYBits(NivelClave.MUY_DEBIL, 23, evaluarClave("xkvmq"))
    }

    // Comprueba el umbral de 28 bits por encima: 6 minúsculas son Débil.
    @Test
    fun encimaDeVeintiochoBitsEsDebil() {
        revisarNivelYBits(NivelClave.DEBIL, 28, evaluarClave("xkvmqz"))
    }

    // Comprueba el umbral de 45 bits por debajo: Débil no alcanza para crear la cuenta.
    @Test
    fun debajoDeCuarentaYCincoBitsNoEsAceptable() {
        val evaluacion = evaluarClave("xkvmqzrtw")
        revisarNivelYBits(NivelClave.DEBIL, 42, evaluacion)
        assertFalse(evaluacion.esAceptable)
    }

    // Comprueba el umbral de 45 bits por encima: Aceptable sí alcanza para crear la cuenta.
    @Test
    fun encimaDeCuarentaYCincoBitsEsAceptable() {
        val evaluacion = evaluarClave("xkvmqzrtwp")
        revisarNivelYBits(NivelClave.ACEPTABLE, 47, evaluacion)
        assertTrue(evaluacion.esAceptable)
    }

    // Comprueba el umbral de 60 bits por debajo: 12 minúsculas siguen siendo Aceptable.
    @Test
    fun debajoDeSesentaBitsSigueAceptable() {
        revisarNivelYBits(NivelClave.ACEPTABLE, 56, evaluarClave(claveDeDoceMinusculas))
    }

    // Comprueba el umbral de 60 bits por encima: 13 minúsculas son Fuerte y no hay consejos.
    @Test
    fun encimaDeSesentaBitsEsFuerteSinConsejos() {
        val evaluacion = evaluarClave("xkvmqzrtwpghd")
        revisarNivelYBits(NivelClave.FUERTE, 61, evaluacion)
        assertTrue(evaluacion.consejos.isEmpty())
    }

    // Comprueba que con 7 caracteres el nivel nunca pasa de Débil, aunque los bits den para más.
    @Test
    fun sieteCaracteresTienenTopeEnDebil() {
        revisarNivelYBits(NivelClave.DEBIL, 45, evaluarClave("Xk9#mQ2"))
    }

    // Comprueba que con 8 caracteres ya no se aplica el tope de Débil.
    @Test
    fun ochoCaracteresNoTienenTope() {
        revisarNivelYBits(NivelClave.ACEPTABLE, 52, evaluarClave("Xk9#mQ2z"))
    }

    // Comprueba que usar la parte del correo antes de la @ resta bits.
    @Test
    fun claveConElCorreoSePenaliza() {
        revisarNivelYBits(NivelClave.DEBIL, 36, evaluarClave(claveDeDoceMinusculas, "xkvmq@correo.com"))
    }

    // Comprueba el límite: una parte del correo de 2 caracteres no penaliza.
    @Test
    fun parteDeCorreoDeDosNoPenaliza() {
        revisarNivelYBits(NivelClave.ACEPTABLE, 56, evaluarClave(claveDeDoceMinusculas, "xk@correo.com"))
    }

    // Comprueba el límite: una parte del correo de 3 caracteres sí penaliza.
    @Test
    fun parteDeCorreoDeTresPenaliza() {
        revisarNivelYBits(NivelClave.DEBIL, 36, evaluarClave(claveDeDoceMinusculas, "xkv@correo.com"))
    }

    // Comprueba que una secuencia que sube resta bits.
    @Test
    fun secuenciaQueSubeSePenaliza() {
        revisarNivelYBits(NivelClave.ACEPTABLE, 52, evaluarClave("1234xkvmqzrt"))
    }

    // Comprueba, para comparar con la anterior, que la misma clave sin secuencia no se penaliza.
    @Test
    fun mismaClaveSinSecuenciaNoSePenaliza() {
        revisarNivelYBits(NivelClave.FUERTE, 62, evaluarClave("1834xkvmqzrt"))
    }

    // Comprueba que una secuencia que baja resta bits.
    @Test
    fun secuenciaQueBajaSePenaliza() {
        revisarNivelYBits(NivelClave.ACEPTABLE, 46, evaluarClave("dcbaxkvmqzrt"))
    }

    // Comprueba que cuatro teclas seguidas de una fila del teclado cuentan como secuencia.
    @Test
    fun filaDelTecladoSePenaliza() {
        revisarNivelYBits(NivelClave.ACEPTABLE, 46, evaluarClave("qwerxkvmzpgh"))
    }

    // Comprueba que dos secuencias restan el doble.
    @Test
    fun dosSecuenciasRestanElDoble() {
        revisarNivelYBits(NivelClave.DEBIL, 42, evaluarClave("12345678xkvm"))
    }

    // Comprueba que repetir el mismo carácter reduce los bits.
    @Test
    fun todoRepetidoSePenaliza() {
        revisarNivelYBits(NivelClave.MUY_DEBIL, 22, evaluarClave("aaaaaaaa"))
    }

    // Comprueba el límite de repeticiones: 3 caracteres distintos en un largo de 9 no penalizan.
    @Test
    fun repeticionEnElLimiteNoPenaliza() {
        revisarNivelYBits(NivelClave.DEBIL, 42, evaluarClave("xkvxkvxkv"))
    }

    // Comprueba el límite de repeticiones: 3 caracteres distintos en un largo de 10 sí penalizan.
    @Test
    fun repeticionPasadoElLimitePenaliza() {
        revisarNivelYBits(NivelClave.DEBIL, 28, evaluarClave("xkvxkvxkvx"))
    }

    // Comprueba la limitación documentada: Colombia2024! da Aceptable, con el consejo del patrón.
    @Test
    fun patronConSimboloDaAceptableConConsejo() {
        val evaluacion = evaluarClave("Colombia2024!")
        revisarNivelYBits(NivelClave.ACEPTABLE, 51, evaluacion)
        assertTrue(evaluacion.esAceptable)
        assertTrue(evaluacion.consejos.contains("Evita el patrón palabra + números + símbolo."))
    }

    // Comprueba que el mismo patrón sin símbolo se queda en Débil y no alcanza para crear la cuenta.
    @Test
    fun patronSinSimboloNoEsAceptable() {
        val evaluacion = evaluarClave("Colombia2024")
        revisarNivelYBits(NivelClave.DEBIL, 42, evaluacion)
        assertFalse(evaluacion.esAceptable)
    }

    // Comprueba que una frase larga solo en minúsculas es Fuerte: se premia el largo.
    @Test
    fun fraseEnMinusculasEsFuerte() {
        revisarNivelYBits(NivelClave.FUERTE, 147, evaluarClave("caballo verde corre lejos"))
    }
}

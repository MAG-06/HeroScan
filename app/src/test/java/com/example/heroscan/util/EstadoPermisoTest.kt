package com.example.heroscan.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Tabla de casos de decidirEstadoPermiso: cada fila son las entradas y el estado que debe dar.
@RunWith(Parameterized::class)
class EstadoPermisoTest(
    private val concedido: Boolean,
    private val debeExplicar: Boolean,
    private val yaPregunto: Boolean,
    private val versionAndroid: Int,
    private val versionQuePide: Int,
    private val esperado: EstadoPermiso
) {

    companion object {
        // Filas de la tabla: concedido, debeExplicar, yaPregunto, versión de Android, versión que pide → estado esperado.
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: concedido={0} explicar={1} pregunto={2} v{3}/{4}")
        fun casos(): List<Array<Any>> = listOf(
            arrayOf(true, false, false, 33, 33, EstadoPermiso.Concedido),
            arrayOf(true, false, false, 30, 33, EstadoPermiso.Concedido),
            arrayOf(false, false, false, 32, 33, EstadoPermiso.BloqueadoEnAjustes),
            arrayOf(false, false, false, 33, 33, EstadoPermiso.PuedePedir),
            arrayOf(false, true, true, 33, 33, EstadoPermiso.DebeExplicar),
            arrayOf(false, false, true, 34, 33, EstadoPermiso.BloqueadoEnAjustes),
            arrayOf(false, true, false, 36, 33, EstadoPermiso.DebeExplicar),
            arrayOf(true, true, true, 37, 33, EstadoPermiso.Concedido),
            arrayOf(false, false, false, 24, 23, EstadoPermiso.PuedePedir)
        )
    }

    // Comprueba que cada fila da el estado esperado.
    @Test
    fun cadaCasoDaElEstadoDeSuFila() {
        assertEquals(esperado, decidirEstadoPermiso(concedido, debeExplicar, yaPregunto, versionAndroid, versionQuePide))
    }
}

package com.example.heroscan.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Convierte las lecturas de un sensor (por ejemplo el acelerómetro) en un Flow.
// Es una "función de extensión": se llama como context.sensorFlow(...), como si fuera un método de Context.
// El oyente se registra solo mientras alguien recolecta el Flow y se quita al dejar de hacerlo (awaitClose),
// así el sensor no queda encendido gastando batería. Si el teléfono no tiene ese sensor, el Flow se cierra sin emitir nada.
fun Context.sensorFlow(tipo: Int, retardo: Int = SensorManager.SENSOR_DELAY_UI): Flow<FloatArray> = callbackFlow {
    val gestorSensores = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor = gestorSensores.getDefaultSensor(tipo)

    if (sensor == null) {
        close()
        return@callbackFlow
    }

    // Objeto anónimo (como una clase anónima en Java) que recibe cada lectura del sensor
    val oyente = object : SensorEventListener {

        // Se llama con cada lectura nueva; se envía una copia porque Android reutiliza el arreglo values
        override fun onSensorChanged(evento: SensorEvent) {
            trySend(evento.values.clone())
        }

        // Obligatorio por la interfaz; aquí no se necesita
        override fun onAccuracyChanged(sensor: Sensor?, precision: Int) {}
    }

    gestorSensores.registerListener(oyente, sensor, retardo)

    awaitClose { gestorSensores.unregisterListener(oyente) }
}

// Dice si el teléfono tiene un sensor de ese tipo (por ejemplo el acelerómetro).
// getDefaultSensor devuelve null cuando el teléfono no tiene ese sensor.
fun Context.tieneSensor(tipo: Int): Boolean {
    val gestorSensores = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    return gestorSensores.getDefaultSensor(tipo) != null
}

package com.example.heroscan.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

// Hace vibrar el celular durante el tiempo indicado (en milisegundos).
// Android 8 (API 26) cambió la forma de vibrar, por eso se revisa la versión del sistema.
fun vibrarDispositivo(context: Context, duracionMs: Long = 200) {
    val vibrador = context.getSystemService(Vibrator::class.java) ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrador.vibrate(VibrationEffect.createOneShot(duracionMs, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrador.vibrate(duracionMs)
    }
}

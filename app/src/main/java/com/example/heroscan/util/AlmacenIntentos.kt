package com.example.heroscan.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Archivo de DataStore donde se guardan los intentos de inicio de sesión. Va a nivel de archivo para que haya uno solo en toda la app.
private val Context.dataStore by preferencesDataStore(name = "intentos_acceso")

// Guarda en el teléfono los fallos seguidos y la hora del bloqueo, para que el bloqueo siga aunque se cierre la app. No guarda el correo ni la contraseña.
class AlmacenIntentos(private val context: Context) {

    // Nombres con los que se guardan los 2 valores.
    private object Claves {
        val FALLOS_SEGUIDOS = intPreferencesKey("fallos_seguidos")
        val BLOQUEADO_HASTA_MS = longPreferencesKey("bloqueado_hasta_ms")
    }

    // Fallos seguidos guardados (0 si no hay nada guardado).
    val fallosSeguidos: Flow<Int> = context.dataStore.data.map { prefs -> prefs[Claves.FALLOS_SEGUIDOS] ?: 0 }

    // Hora (en milisegundos) hasta la que dura el bloqueo guardado (0 = sin bloqueo).
    val bloqueadoHastaMs: Flow<Long> = context.dataStore.data.map { prefs -> prefs[Claves.BLOQUEADO_HASTA_MS] ?: 0L }

    // Guarda los 2 valores juntos. Es suspend: se llama desde una corrutina (viewModelScope.launch).
    suspend fun guardar(fallos: Int, hastaMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[Claves.FALLOS_SEGUIDOS] = fallos
            prefs[Claves.BLOQUEADO_HASTA_MS] = hastaMs
        }
    }
}

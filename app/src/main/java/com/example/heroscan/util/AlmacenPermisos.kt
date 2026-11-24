package com.example.heroscan.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Archivo de DataStore donde se guarda si la app ya pidió permisos. Va a nivel de archivo para que haya uno solo en toda la app.
private val Context.dataStore by preferencesDataStore(name = "permisos")

// Guarda si la app ya mostró el diálogo del permiso de notificaciones. Hace falta porque shouldShowRequestPermissionRationale da false tanto antes de preguntar como después de un rechazo para siempre.
class AlmacenPermisos(private val context: Context) {

    // Nombre con el que se guarda el valor.
    private object Claves {
        val YA_PREGUNTO_NOTIFICACIONES = booleanPreferencesKey("ya_pregunto_notificaciones")
    }

    // Lo guardado. Si el archivo no se puede leer (IOException), se usan datos vacíos: se toma como que no se ha preguntado, en vez de fallar.
    private val preferencias = context.dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    // true si ya se mostró el diálogo de notificaciones (false si no hay nada guardado).
    val yaPreguntoNotificaciones: Flow<Boolean> = preferencias.map { prefs -> prefs[Claves.YA_PREGUNTO_NOTIFICACIONES] ?: false }

    // Guarda que ya se mostró el diálogo de notificaciones. Es suspend: se llama desde una corrutina.
    suspend fun guardarYaPreguntoNotificaciones() {
        context.dataStore.edit { prefs ->
            prefs[Claves.YA_PREGUNTO_NOTIFICACIONES] = true
        }
    }
}

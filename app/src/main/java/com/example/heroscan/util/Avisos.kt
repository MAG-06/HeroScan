package com.example.heroscan.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.heroscan.MainActivity
import com.example.heroscan.R
import com.example.heroscan.model.TipoCodigo

// Id del canal de los avisos. Android lo usa para guardar los ajustes que el usuario le cambie al canal.
private const val ID_CANAL_AVISOS = "busquedas_terminadas"

// Ids fijos de cada tipo de aviso: un aviso nuevo del mismo tipo reemplaza al anterior en vez de sumarse.
private const val ID_AVISO_COMIC_ENCONTRADO = 1
private const val ID_AVISO_VARIOS_COMICS = 2

// Crea el canal de los avisos (Android 8 o más). Importancia ALTA: el aviso baja desde arriba de la pantalla (heads-up).
// Si el canal ya existe, Android no hace nada, así que se puede llamar antes de cada aviso.
private fun crearCanalAvisos(contexto: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(ID_CANAL_AVISOS, "Búsquedas terminadas", NotificationManager.IMPORTANCE_HIGH)
        canal.description = "Avisa cuando termina una búsqueda en la web o por portada mientras no estás en la app."
        NotificationManagerCompat.from(contexto).createNotificationChannel(canal)
    }
}

// Avisa que la búsqueda encontró un cómic. El texto es el título con el que Metron encontró el cómic (o "Toca para ver el detalle" si viene vacío).
// Tocarlo abre el detalle con el enlace heroscan://detalle/{idComic}/{tipoCodigo}, en una tarea nueva (NEW_TASK + CLEAR_TASK).
fun avisarComicEncontrado(contexto: Context, idComic: Int, tipoCodigo: TipoCodigo, tituloEncontrado: String) {
    val texto = if (tituloEncontrado.isBlank()) "Toca para ver el detalle" else tituloEncontrado
    val enlace = Uri.parse("heroscan://detalle/$idComic/${tipoCodigo.name}")
    val intent = Intent(Intent.ACTION_VIEW, enlace, contexto, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    mostrarAviso(contexto, ID_AVISO_COMIC_ENCONTRADO, "Encontramos tu cómic", texto, intent)
}

// Avisa que la búsqueda encontró varios cómics. Tocarlo abre la app igual que su ícono (MAIN + LAUNCHER), en la lista.
fun avisarVariosComics(contexto: Context) {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    intent.setClass(contexto, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    mostrarAviso(contexto, ID_AVISO_VARIOS_COMICS, "Encontramos varios cómics", "Toca para elegir el tuyo en la lista.", intent)
}

// Quita los avisos que hayan quedado en la barra (por ejemplo, si el usuario volvió con el ícono de la app y no tocó el aviso).
fun quitarAvisos(contexto: Context) {
    val gestor = NotificationManagerCompat.from(contexto)
    gestor.cancel(ID_AVISO_COMIC_ENCONTRADO)
    gestor.cancel(ID_AVISO_VARIOS_COMICS)
}

// Arma y muestra un aviso. Si las notificaciones están desactivadas (o el permiso negado), no hace nada y no falla.
private fun mostrarAviso(contexto: Context, id: Int, titulo: String, texto: String, intent: Intent) {
    val gestor = NotificationManagerCompat.from(contexto)
    if (!gestor.areNotificationsEnabled()) return
    // En Android 13 o más también se revisa el permiso POST_NOTIFICATIONS justo antes de notificar.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    crearCanalAvisos(contexto)

    // FLAG_IMMUTABLE: nadie más puede cambiar el intent que se abre al tocar el aviso.
    val alTocar = PendingIntent.getActivity(contexto, id, intent, PendingIntent.FLAG_IMMUTABLE)

    val aviso = NotificationCompat.Builder(contexto, ID_CANAL_AVISOS)
        .setSmallIcon(R.drawable.ic_aviso)
        .setContentTitle(titulo)
        .setContentText(texto)
        // Prioridad ALTA: en Android 7 (sin canales) es lo que hace que el aviso baje desde arriba.
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(alTocar)
        // Se quita solo al tocarlo.
        .setAutoCancel(true)
        .build()

    gestor.notify(id, aviso)
}

package com.example.firstapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.firstapp.views.ListaActivity

object NotificadorTemperatura {

    // Rango seguro para una sala de servidores (ajústalo a tu criterio)
    const val TEMP_MAX = 30.0
    const val TEMP_MIN = 18.0

    private const val CANAL_ID = "alertas_temperatura"

    fun evaluar(context: Context, ubicacion: String, temperatura: Double) {
            // Si el usuario apagó las alertas en Preferencias, no se notifica nada
            val prefs = context.getSharedPreferences("IoT_Prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("alertas_activas", true)) return

        when {
            temperatura > TEMP_MAX -> mostrar(
                context, ubicacion,
                "Temperatura ALTA en $ubicacion",
                "$temperatura °C supera el máximo de $TEMP_MAX °C"
            )
            temperatura < TEMP_MIN -> mostrar(
                context, ubicacion,
                "Temperatura BAJA en $ubicacion",
                "$temperatura °C está bajo el mínimo de $TEMP_MIN °C"
            )
        }
    }

    private fun mostrar(context: Context, ubicacion: String, titulo: String, texto: String) {
        // Desde Android 13 hay que tener el permiso concedido
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        // Desde Android 8 las notificaciones necesitan un canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID, "Alertas de temperatura", NotificationManager.IMPORTANCE_HIGH
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }

        // Al tocar la notificación se abre la lista
        val pendingIntent = PendingIntent.getActivity(
            context, 0, Intent(context, ListaActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Un id por sala: una alerta nueva de la misma sala reemplaza a la anterior
        NotificationManagerCompat.from(context).notify(ubicacion.hashCode(), notificacion)
    }
}
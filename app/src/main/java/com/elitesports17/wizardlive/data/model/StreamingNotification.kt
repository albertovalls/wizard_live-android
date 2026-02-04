package com.elitesports17.wizardlive.data.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elitesports17.wizardlive.R

object StreamingNotification {

    private const val CHANNEL_ID = "streaming_status"
    private const val CHANNEL_NAME = "Estado del streaming"
    private const val NOTIF_ID = 1001

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) != null) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones de streaming (ON/OFF)"
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent: Intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            (PendingIntent.FLAG_UPDATE_CURRENT) or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
    }

    /**
     * Muestra streaming ON con cronómetro dinámico.
     * IMPORTANTE: pásale el startTimeMs REAL del inicio del streaming para que el contador sea correcto.
     */
    fun showStreamingOn(context: Context, startTimeMs: Long = System.currentTimeMillis()) {
        ensureChannel(context)

        // 🔴 Si creas R.drawable.ic_stat_live úsalo aquí.
        // Si no lo tienes, deja ic_stat_notification.
        val smallIconRes = runCatching { R.drawable.ic_stat_notification }.getOrElse { R.drawable.ic_stat_notification }

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIconRes)
            .setContentTitle("🔴 LIVE · Streaming en directo")
            .setContentText("La Wizcam está emitiendo")
            // ⏱️ Cronómetro nativo (se actualiza solo)
            .setWhen(startTimeMs)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            // estilo / comportamiento
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(contentIntent(context))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notif)
    }

    fun showStreamingOff(context: Context) {
        ensureChannel(context)

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("Streaming apagado")
            .setContentText("La emisión se ha detenido.")
            .setOngoing(false)
            .setAutoCancel(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(contentIntent(context))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notif)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIF_ID)
    }
}

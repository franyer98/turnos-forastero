package com.franyer.turnosforastero

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Cada pocas horas, en segundo plano: baja los turnos nuevos y busca versión nueva de la app.
 * Avisa con una notificación cuando hay algo nuevo.
 */
class ActualizacionWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (Datos.bajar(ctx)) {
            notificar(ctx, 4, "Turnos actualizados", "Ya están los turnos nuevos del Hotel El Forastero",
                Intent(ctx, MainActivity::class.java))
        }
        Actualizador.buscar(ctx)?.let { nueva ->
            val prefs = ctx.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
            if (prefs.getLong("version_avisada", 0) != nueva.versionCode) {
                notificar(ctx, 3, "Turnos Forastero ${nueva.versionName} lista", "Toca para instalar la actualización",
                    Intent(ctx, InstalarActivity::class.java))
                prefs.edit().putLong("version_avisada", nueva.versionCode).apply()
            }
        }
        return Result.success()
    }

    companion object {
        private const val CANAL = "novedades"

        fun programar(ctx: Context) {
            val pedido = PeriodicWorkRequestBuilder<ActualizacionWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                "buscar_novedades", ExistingPeriodicWorkPolicy.KEEP, pedido
            )
        }

        fun notificar(ctx: Context, id: Int, titulo: String, texto: String, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(
                    NotificationChannel(CANAL, "Novedades", NotificationManager.IMPORTANCE_DEFAULT)
                )
            }
            val abrir = PendingIntent.getActivity(
                ctx, id, intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val n = NotificationCompat.Builder(ctx, CANAL)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setAutoCancel(true)
                .setContentIntent(abrir)
                .build()
            val nm = NotificationManagerCompat.from(ctx)
            if (nm.areNotificationsEnabled()) {
                @Suppress("MissingPermission")
                nm.notify(id, n)
            }
        }
    }
}

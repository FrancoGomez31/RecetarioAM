package com.app.core.data.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class RecipeReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val recipeId = intent.getStringExtra(EXTRA_RECIPE_ID) ?: return
        val recipeName = intent.getStringExtra(EXTRA_RECIPE_NAME) ?: "tu receta"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificación si estamos en Android 8.0+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de cocina",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos para cocinar recetas planificadas"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la aplicación al tocar la notificación
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_RECIPE_ID, recipeId)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            recipeId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Momento de cocinar!")
            .setContentText("Tenés planificado cocinar: $recipeName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        notificationManager.notify(recipeId.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "cooking_reminders"
        const val EXTRA_RECIPE_ID = "extra_recipe_id"
        const val EXTRA_RECIPE_NAME = "extra_recipe_name"
    }
}

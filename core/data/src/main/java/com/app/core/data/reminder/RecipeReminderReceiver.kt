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

        val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val lang = prefs.getString("pref_language", "SPANISH")
        val isEnglish = lang == "ENGLISH"

        val channelName = if (isEnglish) "Cooking Reminders" else "Recordatorios de cocina"
        val channelDesc = if (isEnglish) "Notices to cook planned recipes" else "Avisos para cocinar recetas planificadas"
        val fallbackRecipe = if (isEnglish) "your recipe" else "tu receta"
        val resolvedRecipeName = if (recipeName == "tu receta") fallbackRecipe else recipeName
        val title = if (isEnglish) "Time to cook!" else "¡Momento de cocinar!"
        val text = if (isEnglish) "You have scheduled to cook: $resolvedRecipeName" else "Tenés planificado cocinar: $resolvedRecipeName"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificación si estamos en Android 8.0+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDesc
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
            .setContentTitle(title)
            .setContentText(text)
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

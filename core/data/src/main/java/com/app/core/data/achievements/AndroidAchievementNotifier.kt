package com.app.core.data.achievements

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.app.core.domain.AchievementId
import com.app.core.domain.AchievementNotifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Muestra la notificación de "Logro desbloqueado" y guarda en el teléfono cuáles ya se avisaron.
 * Igual que los recordatorios, el idioma sale de las preferencias (no del sistema).
 */
@Singleton
class AndroidAchievementNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) : AchievementNotifier {

    private val prefs = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)

    override fun alreadyNotified(): Set<AchievementId> {
        val saved = prefs.getStringSet(KEY_NOTIFIED, emptySet()).orEmpty()
        return AchievementId.entries.filter { it.name in saved }.toSet()
    }

    override fun notifyUnlocked(id: AchievementId) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Sin permiso de notificaciones no mostramos nada ni marcamos el logro como avisado
        if (!manager.areNotificationsEnabled()) return

        val isEnglish = context
            .getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            .getString("pref_language", "SPANISH") == "ENGLISH"

        val channel = NotificationChannel(
            CHANNEL_ID,
            if (isEnglish) "Achievements" else "Logros",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)

        val (name, description) = id.texts(isEnglish)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_big_on)
            .setContentTitle(if (isEnglish) "Achievement unlocked!" else "¡Logro desbloqueado!")
            .setContentText("$name: $description")
            .setAutoCancel(true)
        if (launchIntent != null) {
            builder.setContentIntent(
                PendingIntent.getActivity(
                    context,
                    id.ordinal,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        manager.notify(NOTIFICATION_ID_BASE + id.ordinal, builder.build())

        // Se guarda una copia nueva del conjunto: el que devuelve SharedPreferences no se puede modificar
        val saved = prefs.getStringSet(KEY_NOTIFIED, emptySet()).orEmpty()
        prefs.edit().putStringSet(KEY_NOTIFIED, saved + id.name).apply()
    }

    /** Nombre y descripción del logro, en español o inglés. */
    private fun AchievementId.texts(english: Boolean): Pair<String, String> = when (this) {
        AchievementId.FIRST_RECIPE ->
            if (english) "First recipe" to "You saved your first recipe."
            else "Primera receta" to "Guardaste tu primera receta."
        AchievementId.FULL_NOTEBOOK ->
            if (english) "A full notebook" to "You saved 10 recipes."
            else "Un cuaderno lleno" to "Guardaste 10 recetas."
        AchievementId.WORLD_KITCHENS ->
            if (english) "World kitchens" to "You saved recipes from 5 countries."
            else "Cocinas del mundo" to "Guardaste recetas de 5 países."
        AchievementId.FIRST_ATTEMPT ->
            if (english) "Hands on" to "You logged your first cooking attempt."
            else "Manos a la obra" to "Registraste tu primer intento de cocina."
        AchievementId.LEARNING ->
            if (english) "Learning by cooking" to "You logged 10 cooking attempts."
            else "Aprender cocinando" to "Registraste 10 intentos de cocina."
    }

    private companion object {
        const val CHANNEL_ID = "achievements"
        const val KEY_NOTIFIED = "notified_ids"
        const val NOTIFICATION_ID_BASE = 2000
    }
}
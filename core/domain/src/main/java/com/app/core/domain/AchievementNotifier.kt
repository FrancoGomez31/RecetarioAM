package com.app.core.domain

/** Avisa al usuario (con una notificación) que desbloqueó un logro. El dominio no sabe cómo. */
interface AchievementNotifier {
    /** Logros que ya se avisaron: no se avisan dos veces. */
    fun alreadyNotified(): Set<AchievementId>

    /**
     * Muestra la notificación y recuerda que ya se avisó. Si el usuario no tiene las
     * notificaciones activadas no hace nada, así se avisa más adelante cuando las active.
     */
    fun notifyUnlocked(id: AchievementId)
}
package com.app.core.domain.usecase

import com.app.core.domain.Achievement
import com.app.core.domain.AchievementNotifier
import com.app.core.domain.AppResult
import com.app.core.domain.RecipeRepository
import com.app.core.domain.achievementsFor
import javax.inject.Inject

/**
 * Calcula los logros del usuario y, de paso, avisa con una notificación de los que se
 * acaban de desbloquear. Lo usa la pantalla de Logros y lo llaman los casos de uso que
 * guardan recetas e intentos (para que el aviso llegue justo al conseguirlo).
 */
class GetAchievementsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val notifier: AchievementNotifier
) {
    suspend operator fun invoke(): AppResult<List<Achievement>> {
        return when (val result = recipeRepository.getRecipes()) {
            is AppResult.Success -> {
                val achievements = achievementsFor(result.data)
                val notified = notifier.alreadyNotified()
                achievements
                    .filter { it.isUnlocked && it.id !in notified }
                    .forEach { notifier.notifyUnlocked(it.id) }
                AppResult.Success(achievements)
            }
            is AppResult.Error -> AppResult.Error(result.error)
        }
    }
}
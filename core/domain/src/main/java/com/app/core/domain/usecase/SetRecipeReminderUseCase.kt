package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import com.app.core.domain.ReminderScheduler
import javax.inject.Inject

class SetRecipeReminderUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(recipe: Recipe, triggerAtMillis: Long?): AppResult<Recipe> {
        val updatedRecipe = recipe.copy(reminderEpochMillis = triggerAtMillis)
        val result = recipeRepository.saveRecipe(updatedRecipe)
        if (result is AppResult.Success) {
            if (triggerAtMillis != null) {
                reminderScheduler.scheduleReminder(recipe.id, recipe.name, triggerAtMillis)
            } else {
                reminderScheduler.cancelReminder(recipe.id)
            }
        }
        return result
    }
}

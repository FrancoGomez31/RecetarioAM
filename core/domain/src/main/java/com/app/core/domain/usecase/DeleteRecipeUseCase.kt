package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.RecipeRepository
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = recipeRepository.deleteRecipe(id)
}
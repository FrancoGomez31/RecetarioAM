package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): AppResult<Recipe> {
        val clean = recipe.cleaned()
        val invalidFields = validateRecipe(clean)
        if (invalidFields.isNotEmpty()) {
            return AppResult.Error(DomainError.InvalidRecipe(invalidFields))
        }
        return recipeRepository.saveRecipe(clean)
    }
}
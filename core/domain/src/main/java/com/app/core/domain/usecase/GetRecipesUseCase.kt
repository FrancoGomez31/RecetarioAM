package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import javax.inject.Inject

class GetRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(): AppResult<List<Recipe>> = recipeRepository.getRecipes()
}
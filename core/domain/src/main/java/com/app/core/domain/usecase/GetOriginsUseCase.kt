package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.RecipeOrigin
import com.app.core.domain.RecipeRepository
import com.app.core.domain.groupByOrigin
import javax.inject.Inject

class GetOriginsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(): AppResult<List<RecipeOrigin>> =
        when (val result = recipeRepository.getRecipes()) {
            is AppResult.Success -> AppResult.Success(groupByOrigin(result.data))
            is AppResult.Error -> AppResult.Error(result.error)
        }
}
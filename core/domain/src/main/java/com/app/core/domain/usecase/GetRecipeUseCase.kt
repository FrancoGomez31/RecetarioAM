package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.ExploreCatalog
import com.app.core.domain.LanguagePreference
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import com.app.core.domain.RecipeSearcher
import com.app.core.domain.UserPreferencesRepository
import javax.inject.Inject

class GetRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val recipeSearcher: RecipeSearcher
) {
    suspend operator fun invoke(id: String): AppResult<Recipe> {
        val repositoryResult = recipeRepository.getRecipe(id)
        if (repositoryResult is AppResult.Success) {
            return repositoryResult
        }
        if (id.startsWith("comm_")) {
            val isEnglish = preferencesRepository.language.value == LanguagePreference.ENGLISH
            val catalogRecipe = ExploreCatalog.findRecipe(id, isEnglish)
            if (catalogRecipe != null) {
                return AppResult.Success(catalogRecipe)
            }
        }
        if (id.startsWith("ai_")) {
            val found = recipeSearcher.find(id)
            if (found != null) {
                return AppResult.Success(found)
            }
        }
        return repositoryResult
    }
}
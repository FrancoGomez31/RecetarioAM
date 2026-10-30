package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeSearcher
import com.app.core.domain.UserPreferencesRepository
import javax.inject.Inject

/** Busca recetas por plato o ingrediente en el idioma activo del usuario, de a una página. */
class SearchRecipesUseCase @Inject constructor(
    private val searcher: RecipeSearcher,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(query: String, page: Int = 0): AppResult<List<Recipe>> {
        val currentLang = preferencesRepository.language.value.code
        return searcher.search(query, currentLang, page)
    }
}
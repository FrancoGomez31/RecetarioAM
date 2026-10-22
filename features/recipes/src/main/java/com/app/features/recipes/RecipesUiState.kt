package com.app.features.recipes

import com.app.core.domain.DomainError
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility
import com.app.core.domain.originLabel

enum class RecipeFilter {
    ALL,
    PRIVATE,
    PUBLIC
}

data class RecipesUiState(
    val recipes: List<Recipe> = emptyList(),
    val query: String = "",
    val filter: RecipeFilter = RecipeFilter.ALL,
    val isLoading: Boolean = true,
    val error: DomainError? = null
) {
    val totalCount: Int
        get() = recipes.size

    val privateCount: Int
        get() = recipes.count { it.visibility == Visibility.PRIVATE }

    val publicCount: Int
        get() = recipes.count { it.visibility == Visibility.PUBLIC }

    val countryCount: Int
        get() = recipes.map { it.country }.distinct().size

    val visibleRecipes: List<Recipe>
        get() {
            val byFilter = when (filter) {
                RecipeFilter.ALL -> recipes
                RecipeFilter.PRIVATE -> recipes.filter { it.visibility == Visibility.PRIVATE }
                RecipeFilter.PUBLIC -> recipes.filter { it.visibility == Visibility.PUBLIC }
            }
            val text = query.trim()
            if (text.isEmpty()) return byFilter
            // originLabel junta restaurante, ciudad y país: se busca por cualquiera de los tres.
            return byFilter.filter {
                it.name.contains(text, ignoreCase = true) ||
                        it.originLabel.contains(text, ignoreCase = true)
            }
        }
}
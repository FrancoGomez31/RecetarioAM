package com.app.features.recipes

import com.app.core.domain.Recipe
import com.app.core.domain.Visibility

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
    val errorMessage: String? = null
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
            return byFilter.filter {
                it.name.contains(text, ignoreCase = true) ||
                        it.city.contains(text, ignoreCase = true) ||
                        it.country.contains(text, ignoreCase = true)
            }
        }
}
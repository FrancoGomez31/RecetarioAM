package com.app.features.recipes.explore

import com.app.core.domain.DomainError
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility
import com.app.core.domain.originLabel

enum class ExploreFilter {
    ALL,
    QUICK,       // <= 35 minutos
    TESTED,      // con al menos 1 intento de cocina
    FAMILY       // >= 4 porciones
}

data class ExploreUiState(
    val recipes: List<Recipe> = emptyList(),
    val query: String = "",
    val filter: ExploreFilter = ExploreFilter.ALL,
    val selectedCountry: String? = null,
    val isLoading: Boolean = true,
    val error: DomainError? = null
) {
    val availableCountries: List<String>
        get() = recipes.map { it.country }.filter { it.isNotBlank() }.distinct().sorted()

    val visibleRecipes: List<Recipe>
        get() {
            var list = recipes.filter { it.visibility == Visibility.PUBLIC }

            // Filtro por país si está seleccionado
            if (!selectedCountry.isNullOrBlank()) {
                list = list.filter { it.country.equals(selectedCountry, ignoreCase = true) }
            }

            // Filtro por categoría / chip
            list = when (filter) {
                ExploreFilter.ALL -> list
                ExploreFilter.QUICK -> list.filter { it.prepTimeMinutes in 1..35 }
                ExploreFilter.TESTED -> list.filter { it.attempts > 0 }
                ExploreFilter.FAMILY -> list.filter { it.servings >= 4 }
            }

            // Filtro por texto: busca en nombre, origen (ciudad, restaurante, país) e ingredientes
            val text = query.trim()
            if (text.isEmpty()) return list

            return list.filter { recipe ->
                recipe.name.contains(text, ignoreCase = true) ||
                    recipe.originLabel.contains(text, ignoreCase = true) ||
                    recipe.description.contains(text, ignoreCase = true) ||
                    recipe.ingredients.any { it.name.contains(text, ignoreCase = true) }
            }
        }
}

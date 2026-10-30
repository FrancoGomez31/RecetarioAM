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
    /** Recetas que devolvió la IA para el texto buscado. */
    val aiRecipes: List<Recipe> = emptyList(),
    val query: String = "",
    val filter: ExploreFilter = ExploreFilter.ALL,
    val selectedCountry: String? = null,
    val isLoading: Boolean = true,
    /** True mientras la IA está buscando. */
    val isSearching: Boolean = false,
    /** Cuántas páginas de la IA se cargaron para la búsqueda actual. */
    val aiPage: Int = 0,
    /** True mientras se carga la página siguiente (los resultados ya cargados siguen visibles). */
    val isLoadingMore: Boolean = false,
    /** True si todavía se puede pedir otra página. */
    val canLoadMore: Boolean = false,
    /** Error de la búsqueda con IA (no bloquea los resultados locales). */
    val searchError: DomainError? = null,
    val error: DomainError? = null
) {
    val availableCountries: List<String>
        get() = (recipes + aiRecipes).map { it.country }.filter { it.isNotBlank() }.distinct().sorted()

    /** Aplica el país y el chip elegidos. */
    private fun applyFilters(list: List<Recipe>): List<Recipe> {
        var result = list

        if (!selectedCountry.isNullOrBlank()) {
            result = result.filter { it.country.equals(selectedCountry, ignoreCase = true) }
        }

        return when (filter) {
            ExploreFilter.ALL -> result
            ExploreFilter.QUICK -> result.filter { it.prepTimeMinutes in 1..35 }
            ExploreFilter.TESTED -> result.filter { it.attempts > 0 }
            ExploreFilter.FAMILY -> result.filter { it.servings >= 4 }
        }
    }

    val visibleRecipes: List<Recipe>
        get() {
            val text = query.trim()

            // Primero las recetas locales (catálogo + públicas), filtradas por el texto al instante
            var local = applyFilters(recipes.filter { it.visibility == Visibility.PUBLIC })
            if (text.isNotEmpty()) {
                local = local.filter { recipe ->
                    recipe.name.contains(text, ignoreCase = true) ||
                            recipe.originLabel.contains(text, ignoreCase = true) ||
                            recipe.description.contains(text, ignoreCase = true) ||
                            recipe.ingredients.any { it.name.contains(text, ignoreCase = true) }
                }
            }

            // Después las de la IA (ya vienen filtradas por la búsqueda), sin repetir nombres
            val ai = applyFilters(aiRecipes).filter { found ->
                local.none { it.name.equals(found.name, ignoreCase = true) }
            }

            return (local + ai).distinctBy { it.id }
        }
}
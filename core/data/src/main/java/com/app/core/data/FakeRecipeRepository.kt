package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import com.app.core.domain.Visibility

// Implementación temporal en memoria. Se reemplaza por Firebase más adelante.
class FakeRecipeRepository : RecipeRepository {

    private val recipes = mutableListOf(
        Recipe("1", "Ñoquis al pomodoro", null, "Nápoles", "Italia", 35, 2, Visibility.PUBLIC),
        Recipe("2", "Ramen shoyu", null, "Kioto", "Japón", 50, 1, Visibility.PRIVATE),
        Recipe("3", "Empanadas criollas", null, "Córdoba", "Argentina", 70, 3, Visibility.PUBLIC),
        Recipe("4", "Pizza margherita", null, "Nápoles", "Italia", 90, 0, Visibility.PRIVATE),
        Recipe("5", "Frittata di pasta", null, "Nápoles", "Italia", 25, 1, Visibility.PRIVATE)
    )

    override suspend fun getRecipes(): AppResult<List<Recipe>> {
        return AppResult.Success(recipes.toList())
    }

    override suspend fun getRecipe(id: String): AppResult<Recipe> {
        val recipe = recipes.firstOrNull { it.id == id }
            ?: return AppResult.Error(DomainError.NotFound)
        return AppResult.Success(recipe)
    }

    override suspend fun saveRecipe(recipe: Recipe): AppResult<Recipe> {
        val index = recipes.indexOfFirst { it.id == recipe.id }
        if (index >= 0) recipes[index] = recipe else recipes.add(recipe)
        return AppResult.Success(recipe)
    }

    override suspend fun deleteRecipe(id: String): AppResult<Unit> {
        recipes.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
}
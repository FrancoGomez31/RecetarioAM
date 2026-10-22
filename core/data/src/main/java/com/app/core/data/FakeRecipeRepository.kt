package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import com.app.core.domain.Visibility
import java.util.UUID
import javax.inject.Inject

// Implementación temporal en memoria. Se reemplaza por Firebase más adelante.
class FakeRecipeRepository @Inject constructor() : RecipeRepository {

    private val recipes = mutableListOf(
        Recipe(
            id = "1",
            name = "Ñoquis al pomodoro",
            photoUrl = null,
            city = "Nápoles",
            country = "Italia",
            prepTimeMinutes = 35,
            attempts = 2,
            visibility = Visibility.PUBLIC,
            description = "Un clásico napolitano para cocinar despacio y compartir en casa.",
            servings = 4,
            ingredients = listOf(
                Ingredient("1 kg", "Papas"),
                Ingredient("250 g", "Harina 0000"),
                Ingredient("1", "Huevo"),
                Ingredient("400 g", "Tomate triturado"),
                Ingredient("2 cdas.", "Aceite de oliva"),
                Ingredient("A gusto", "Albahaca y sal")
            ),
            steps = listOf(
                "Herví las papas con piel 20 min. Pelá y pisá mientras estén tibias.",
                "Sumá huevo y harina. Uní sin amasar de más, cortá y marcá con tenedor.",
                "Herví en agua con sal. Retirá cuando suban a la superficie, unos 2 min.",
                "Cociná el tomate con aceite 10 min. Mezclá y agregá albahaca."
            ),
            latitude = 40.8518,
            longitude = 14.2681
        ),
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
        // id vacío = receta nueva: acá se le asigna el id (Firestore hará lo mismo con el suyo).
        val toSave = if (recipe.id.isBlank()) recipe.copy(id = UUID.randomUUID().toString()) else recipe
        val index = recipes.indexOfFirst { it.id == toSave.id }
        if (index >= 0) recipes[index] = toSave else recipes.add(toSave)
        return AppResult.Success(toSave)
    }

    override suspend fun deleteRecipe(id: String): AppResult<Unit> {
        recipes.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
}
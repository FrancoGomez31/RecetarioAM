package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import com.app.core.domain.Visibility
import java.util.UUID
import javax.inject.Inject

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
        Recipe(
            "2", "Ramen shoyu", null, "Kioto", "Japón", 50, 1, Visibility.PRIVATE,
            latitude = 35.0116, longitude = 135.7681
        ),
        Recipe(
            "3", "Empanadas criollas", null, "Córdoba", "Argentina", 70, 3, Visibility.PUBLIC,
            latitude = -31.4201, longitude = -64.1888
        ),
        Recipe(
            "4", "Pizza margherita", null, "Nápoles", "Italia", 90, 0, Visibility.PRIVATE,
            latitude = 40.8518, longitude = 14.2681
        ),
        Recipe(
            "5", "Frittata di pasta", null, "Nápoles", "Italia", 25, 1, Visibility.PRIVATE,
            latitude = 40.8518, longitude = 14.2681
        ),
        Recipe(
            id = "comm_1",
            name = "Tacos al Pastor",
            photoUrl = null,
            city = "Ciudad de México",
            country = "México",
            prepTimeMinutes = 45,
            attempts = 4,
            visibility = Visibility.PUBLIC,
            description = "Carne marinada con achiote y chiles, servida con piña asada y cilantro fresco.",
            servings = 4,
            ingredients = listOf(
                Ingredient("500 g", "Carne de cerdo"),
                Ingredient("2 cdas.", "Pasta de achiote"),
                Ingredient("1/2 taza", "Jugo de naranja agria"),
                Ingredient("1 rodaja", "Piña asada"),
                Ingredient("12 un.", "Tortillas de maíz"),
                Ingredient("A gusto", "Cebolla y cilantro picados")
            ),
            steps = listOf(
                "Marinar el cerdo con el achiote y el jugo de naranja al menos 2 horas.",
                "Cocinar la carne en sartén bien caliente hasta que tome un dorado intenso.",
                "Picar la carne y servir sobre tortillas calientes con piña, cilantro y cebolla."
            ),
            latitude = 19.4326,
            longitude = -99.1332
        ),
        Recipe(
            id = "comm_2",
            name = "Paella Valenciana",
            photoUrl = null,
            city = "Valencia",
            country = "España",
            prepTimeMinutes = 55,
            attempts = 2,
            visibility = Visibility.PUBLIC,
            description = "Auténtica paella tradicional con arroz bomba, azafrán, pollo y judías.",
            servings = 6,
            ingredients = listOf(
                Ingredient("400 g", "Arroz bomba"),
                Ingredient("500 g", "Pollo troceado"),
                Ingredient("150 g", "Judías verdes (bajoqueta)"),
                Ingredient("1 cdta.", "Hebras de azafrán"),
                Ingredient("1 litro", "Caldo de pollo"),
                Ingredient("3 cdas.", "Aceite de oliva virgen extra")
            ),
            steps = listOf(
                "Dorar bien el pollo en la paella con aceite de oliva.",
                "Agregar las judías verdes y sofreír unos minutos.",
                "Añadir el arroz, sofreírlo brevemente y verter el caldo caliente con azafrán.",
                "Cocinar 10 min a fuego vivo y 8 min a fuego suave hasta formar socarrat."
            ),
            latitude = 39.4699,
            longitude = -0.3763
        ),
        Recipe(
            id = "comm_3",
            name = "Croissant de Mantequilla",
            photoUrl = null,
            city = "París",
            country = "Francia",
            prepTimeMinutes = 90,
            attempts = 1,
            visibility = Visibility.PUBLIC,
            description = "Masa hojaldrada crujiente por fuera y tierna y esponjosa por dentro.",
            servings = 8,
            ingredients = listOf(
                Ingredient("500 g", "Harina de trigo"),
                Ingredient("250 g", "Mantequilla seca fría"),
                Ingredient("10 g", "Levadura seca"),
                Ingredient("50 g", "Azúcar"),
                Ingredient("10 g", "Sal"),
                Ingredient("280 ml", "Leche fría")
            ),
            steps = listOf(
                "Amasar harina, levadura, azúcar, sal y leche hasta obtener una masa lisa. Dejar reposar.",
                "Laminar la mantequilla dentro de la masa realizando tres pliegues simples con reposos en frío.",
                "Cortar triángulos, enrollar y fermentar 2 horas a 26°C.",
                "Pintar con huevo y hornear a 190°C durante 18 minutos."
            ),
            latitude = 48.8566,
            longitude = 2.3522
        ),
        Recipe(
            id = "comm_4",
            name = "Ramen Tonkotsu",
            photoUrl = null,
            city = "Fukuoka",
            country = "Japón",
            prepTimeMinutes = 30,
            attempts = 5,
            visibility = Visibility.PUBLIC,
            description = "Fideos artesanales en caldo cremoso con chashu, huevo marinado y cebollino.",
            servings = 2,
            ingredients = listOf(
                Ingredient("200 g", "Fideos de ramen frescos"),
                Ingredient("600 ml", "Caldo concentrado tonkotsu"),
                Ingredient("4 fetas", "Chashu de cerdo"),
                Ingredient("2 un.", "Huevos ajitama marinados"),
                Ingredient("1 lámina", "Alga nori"),
                Ingredient("2 tallos", "Cebollino picado")
            ),
            steps = listOf(
                "Calentar el caldo tonkotsu hasta que esté bien humeante.",
                "Hervir los fideos durante 90 segundos y escurrir rápidamente.",
                "Armar el tazón: base de fideos, caldo caliente, láminas de chashu, huevo cortado al medio y nori."
            ),
            latitude = 33.5902,
            longitude = 130.4017
        )
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

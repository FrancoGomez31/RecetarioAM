package com.app.core.data

import android.content.Context
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class CachedIngredient(
    val quantity: String,
    val name: String
)

@Serializable
private data class CachedRecipe(
    val id: String,
    val name: String,
    val photoUrl: String? = null,
    val city: String = "",
    val country: String = "",
    val prepTimeMinutes: Int = 0,
    val attempts: Int = 0,
    val visibility: String = "PRIVATE",
    val description: String = "",
    val servings: Int = 4,
    val ingredients: List<CachedIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val restaurant: String = "",
    val reminderEpochMillis: Long? = null
)

@Singleton
class LocalRecipeCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val cacheFile: File
        get() = File(context.filesDir, "cached_recipes.json")

    suspend fun save(recipes: List<Recipe>) = withContext(Dispatchers.IO) {
        try {
            val dtoList = recipes.map { it.toCached() }
            val text = json.encodeToString(dtoList)
            cacheFile.writeText(text)
        } catch (_: Exception) {
            // Falla silenciosa si no se puede escribir caché
        }
    }

    suspend fun load(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            if (!cacheFile.exists()) return@withContext emptyList()
            val text = cacheFile.readText()
            if (text.isBlank()) return@withContext emptyList()
            val dtoList = json.decodeFromString<List<CachedRecipe>>(text)
            dtoList.map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveSingle(recipe: Recipe) = withContext(Dispatchers.IO) {
        val current = load().toMutableList()
        val index = current.indexOfFirst { it.id == recipe.id }
        if (index >= 0) {
            current[index] = recipe
        } else {
            current.add(recipe)
        }
        save(current)
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val current = load().filter { it.id != id }
        save(current)
    }

    private fun Recipe.toCached() = CachedRecipe(
        id = id,
        name = name,
        photoUrl = photoUrl,
        city = city,
        country = country,
        prepTimeMinutes = prepTimeMinutes,
        attempts = attempts,
        visibility = visibility.name,
        description = description,
        servings = servings,
        ingredients = ingredients.map { CachedIngredient(it.quantity, it.name) },
        steps = steps,
        latitude = latitude,
        longitude = longitude,
        restaurant = restaurant,
        reminderEpochMillis = reminderEpochMillis
    )

    private fun CachedRecipe.toDomain() = Recipe(
        id = id,
        name = name,
        photoUrl = photoUrl,
        city = city,
        country = country,
        prepTimeMinutes = prepTimeMinutes,
        attempts = attempts,
        visibility = if (visibility == "PUBLIC") Visibility.PUBLIC else Visibility.PRIVATE,
        description = description,
        servings = servings,
        ingredients = ingredients.map { Ingredient(it.quantity, it.name) },
        steps = steps,
        latitude = latitude,
        longitude = longitude,
        restaurant = restaurant,
        reminderEpochMillis = reminderEpochMillis
    )
}

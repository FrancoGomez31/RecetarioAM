package com.app.core.domain

interface RecipeRepository {
    suspend fun getRecipes(): AppResult<List<Recipe>>
    suspend fun getRecipe(id: String): AppResult<Recipe>
    suspend fun saveRecipe(recipe: Recipe): AppResult<Recipe>
    suspend fun deleteRecipe(id: String): AppResult<Unit>
}
package com.app.recetario.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object RecipesDestination : NavKey

@Serializable
data object MapDestination : NavKey

@Serializable
data object ExploreDestination : NavKey

@Serializable
data object AchievementsDestination : NavKey

@Serializable
data object AccountDestination : NavKey

@Serializable
data class RecipeDetailDestination(val recipeId: String) : NavKey

@Serializable
data class RecipeFormDestination(
    val recipeId: String? = null,
    val prefillName: String = "",
    val prefillServings: String = "",
    val prefillPrepTime: String = ""
) : NavKey

@Serializable
data class AttemptDestination(val recipeId: String) : NavKey

@Serializable
data object LinkImportDestination : NavKey

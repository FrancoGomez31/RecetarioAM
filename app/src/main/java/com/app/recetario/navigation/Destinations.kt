package com.app.recetario.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Cada pantalla es una "key" tipada. El back stack es literalmente una lista de estas keys,
// por ejemplo [RecipesDestination, AccountDestination] cuando abrís Mi cuenta.
// @Serializable permite guardar esa lista al rotar la pantalla o si el sistema mata el proceso.

@Serializable
data object RecipesDestination : NavKey

@Serializable
data object MapDestination : NavKey

@Serializable
data object ExploreDestination : NavKey

@Serializable
data object AchievementsDestination : NavKey

// Pantallas que se abren encima de una pestaña (ahí la barra inferior se oculta sola)
@Serializable
data object AccountDestination : NavKey

/** Detalle de una receta: ingredientes, pasos, origen en el mapa y progreso. */
@Serializable
data class RecipeDetailDestination(val recipeId: String) : NavKey

/** Formulario de receta: recipeId == null crea una receta nueva; con id, edita esa receta. */
@Serializable
data class RecipeFormDestination(val recipeId: String? = null) : NavKey

/** Registrar un intento de cocinar una receta: fotos por paso, comparación y nota. */
@Serializable
data class AttemptDestination(val recipeId: String) : NavKey
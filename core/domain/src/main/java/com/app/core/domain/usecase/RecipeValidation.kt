package com.app.core.domain.usecase

import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeField

/**
 * Normaliza lo que escribió el usuario: recorta espacios y descarta ingredientes
 * sin nombre y pasos vacíos (filas que dejó a medio llenar en el formulario).
 */
internal fun Recipe.cleaned(): Recipe = copy(
    name = name.trim(),
    description = description.trim(),
    city = city.trim(),
    country = country.trim(),
    restaurant = restaurant.trim(),
    ingredients = ingredients
        .map { Ingredient(quantity = it.quantity.trim(), name = it.name.trim()) }
        .filter { it.name.isNotEmpty() },
    steps = steps
        .map { it.trim() }
        .filter { it.isNotEmpty() }
)

/**
 * Reglas de negocio de una receta. Se espera una receta ya pasada por [cleaned].
 * Devuelve los campos inválidos; vacío significa que se puede guardar.
 */
internal fun validateRecipe(recipe: Recipe): Set<RecipeField> = buildSet {
    if (recipe.name.isEmpty()) add(RecipeField.NAME)
    if (recipe.servings < 1) add(RecipeField.SERVINGS)
    if (recipe.prepTimeMinutes < 1) add(RecipeField.TIME)
    // El origen puede ser un país, una ciudad o un restaurante: el país es lo mínimo.
    if (recipe.country.isEmpty()) add(RecipeField.ORIGIN)
    if (recipe.ingredients.isEmpty()) add(RecipeField.INGREDIENTS)
    if (recipe.steps.isEmpty()) add(RecipeField.STEPS)
}
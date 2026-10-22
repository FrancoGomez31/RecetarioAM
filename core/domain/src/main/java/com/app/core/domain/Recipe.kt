package com.app.core.domain

data class Recipe(
    /** Vacío = receta nueva todavía sin guardar; quien la guarda le asigna el id. */
    val id: String,
    val name: String,
    val photoUrl: String?,
    val city: String,
    val country: String,
    val prepTimeMinutes: Int,
    val attempts: Int,
    val visibility: Visibility,
    val description: String = "",
    val servings: Int = 4,
    val ingredients: List<Ingredient> = emptyList(),
    /** El orden de la lista es el número de paso (índice 0 = Paso 1). */
    val steps: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** Nombre del restaurante de origen; vacío si el origen es una ciudad o un país. */
    val restaurant: String = ""
)

/** La cantidad es texto libre ("1 kg", "2 cdas.", "A gusto") porque así se escribe en una receta. */
data class Ingredient(
    val quantity: String,
    val name: String
)

/**
 * Origen legible: "Trattoria X, Nápoles, Italia". Salta las partes vacías, así
 * funciona igual si el origen es un restaurante, una ciudad o solo un país.
 */
val Recipe.originLabel: String
    get() = listOf(restaurant, city, country)
        .filter { it.isNotBlank() }
        .joinToString(", ")
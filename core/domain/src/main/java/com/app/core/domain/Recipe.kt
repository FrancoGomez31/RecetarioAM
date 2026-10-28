package com.app.core.domain

data class Recipe(
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
    val steps: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val restaurant: String = "",
    val reminderEpochMillis: Long? = null
)

data class Ingredient(
    val quantity: String,
    val name: String
)

val Recipe.originLabel: String
    get() = listOf(restaurant, city, country)
        .filter { it.isNotBlank() }
        .joinToString(", ")

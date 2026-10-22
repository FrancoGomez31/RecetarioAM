package com.app.core.domain

/** Campos obligatorios de una receta. Sirven para que la UI marque cuál falló. */
enum class RecipeField {
    NAME,
    SERVINGS,
    TIME,
    ORIGIN,
    INGREDIENTS,
    STEPS
}
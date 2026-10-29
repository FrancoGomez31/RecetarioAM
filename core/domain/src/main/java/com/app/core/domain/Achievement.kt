package com.app.core.domain

/** Los logros que se pueden conseguir. El texto de cada uno vive en la UI. */
enum class AchievementId {
    FIRST_RECIPE,
    FULL_NOTEBOOK,
    WORLD_KITCHENS,
    FIRST_ATTEMPT,
    LEARNING
}

/** Un logro con su avance: [current] es lo que lleva el usuario y [target] lo que necesita. */
data class Achievement(
    val id: AchievementId,
    val current: Int,
    val target: Int
) {
    val isUnlocked: Boolean get() = current >= target

    /** Lo que se muestra como avance: nunca pasa de la meta (nada de "12/10"). */
    val shownCurrent: Int get() = minOf(current, target)

    /** De 0f a 1f, para la barra de progreso. */
    val progress: Float get() = shownCurrent.toFloat() / target
}

/** Calcula el avance de todos los logros a partir de las recetas del usuario. */
fun achievementsFor(recipes: List<Recipe>): List<Achievement> {
    val recipeCount = recipes.size
    val countryCount = recipes
        .map { it.country.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .distinct()
        .size
    val attemptCount = recipes.sumOf { it.attempts }

    return listOf(
        Achievement(AchievementId.FIRST_RECIPE, recipeCount, 1),
        Achievement(AchievementId.FULL_NOTEBOOK, recipeCount, 10),
        Achievement(AchievementId.WORLD_KITCHENS, countryCount, 5),
        Achievement(AchievementId.FIRST_ATTEMPT, attemptCount, 1),
        Achievement(AchievementId.LEARNING, attemptCount, 10)
    )
}
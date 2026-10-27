package com.app.core.domain

/**
 * Un punto del atlas: una ciudad (o país) donde nacieron una o más de tus recetas.
 * [recipes] viene ordenada: primero la que más intentos tiene.
 */
data class RecipeOrigin(
    /** Identifica el origen: ciudad y país en minúsculas. Sirve para seleccionarlo. */
    val key: String,
    /** "Nápoles, Italia" */
    val label: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val recipes: List<Recipe>
)

/**
 * Agrupa las recetas por ciudad y país. Las recetas sin coordenadas o sin país no se
 * pueden ubicar en el mapa, así que se dejan afuera. El resultado va de más a menos recetas.
 */
fun groupByOrigin(recipes: List<Recipe>): List<RecipeOrigin> =
    recipes
        .filter { it.latitude != null && it.longitude != null && it.country.isNotBlank() }
        .groupBy { "${it.city.trim().lowercase()}|${it.country.trim().lowercase()}" }
        .map { (key, group) ->
            val first = group.first()
            RecipeOrigin(
                key = key,
                label = listOf(first.city, first.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", "),
                city = first.city.trim(),
                country = first.country.trim(),
                // Si varias recetas son de restaurantes distintos de la misma ciudad, el pin va al centro del grupo.
                latitude = group.mapNotNull { it.latitude }.average(),
                longitude = group.mapNotNull { it.longitude }.average(),
                recipes = group.sortedWith(
                    compareByDescending<Recipe> { it.attempts }.thenBy { it.name }
                )
            )
        }
        .sortedWith(compareByDescending<RecipeOrigin> { it.recipes.size }.thenBy { it.label })
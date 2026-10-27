package com.app.features.map

import com.app.core.domain.DomainError
import com.app.core.domain.GeoLocation
import com.app.core.domain.RecipeOrigin
import com.app.core.domain.UserLocation

enum class LocationStatus {
    /** Todavía no se intentó. */
    IDLE,
    LOADING,
    READY,
    /** El usuario no concedió el permiso de ubicación. */
    PERMISSION_NEEDED,
    FAILED
}

/**
 * Orden de "llevá el mapa acá". El token cambia en cada orden: la pantalla la ejecuta
 * cuando ve un token nuevo (o cuando se vuelve a crear el mapa).
 */
data class FitRequest(
    val token: Long,
    val points: List<GeoLocation>,
    /** Zoom para cuando hay un solo punto. Si es null se usa uno por defecto. */
    val zoom: Double? = null,
    /** Con un solo punto: centrar sin cambiar el zoom actual (al tocar un pin). */
    val keepZoom: Boolean = false
)

data class CountryChip(
    val country: String,
    val recipeCount: Int
)

data class MapUiState(
    val isLoading: Boolean = true,
    val origins: List<RecipeOrigin> = emptyList(),
    /** null = todos los países. */
    val selectedCountry: String? = null,
    val selectedOriginKey: String? = null,
    val userLocation: UserLocation? = null,
    val locationStatus: LocationStatus = LocationStatus.IDLE,
    val locationError: DomainError? = null,
    val fit: FitRequest? = null,
    val error: DomainError? = null
) {
    val countryCount: Int
        get() = origins.map { it.country }.distinct().size

    val originCount: Int
        get() = origins.size

    /** Un chip por país, con la cantidad de recetas; el país con más recetas primero. */
    val countries: List<CountryChip>
        get() = origins
            .groupBy { it.country }
            .map { (country, list) -> CountryChip(country, list.sumOf { it.recipes.size }) }
            .sortedWith(compareByDescending<CountryChip> { it.recipeCount }.thenBy { it.country })

    val visibleOrigins: List<RecipeOrigin>
        get() = selectedCountry
            ?.let { country -> origins.filter { it.country == country } }
            ?: origins

    /** El elegido; si no hay ninguno (o ya no está visible), el que más recetas tiene. */
    val selectedOrigin: RecipeOrigin?
        get() = visibleOrigins.firstOrNull { it.key == selectedOriginKey } ?: visibleOrigins.firstOrNull()

    val otherOrigins: List<RecipeOrigin>
        get() = visibleOrigins.filter { it.key != selectedOrigin?.key }
}
package com.app.features.recipes

import com.app.core.domain.DomainError
import com.app.core.domain.Place
import com.app.core.domain.PlaceType
import com.app.core.domain.UserLocation

/**
 * Orden de "llevá el mapa a este punto". El token cambia en cada orden, así la pantalla
 * distingue una orden nueva de una que ya ejecutó (por ejemplo, después de rotar).
 */
data class MapFocus(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double,
    val token: Long
)

data class PlacePickerUiState(
    val type: PlaceType = PlaceType.CITY,
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<Place> = emptyList(),
    /** true cuando ya se hizo al menos una búsqueda: permite distinguir "sin resultados" de "todavía no busqué". */
    val hasSearched: Boolean = false,
    /** true = se muestra la lista de resultados; false = se muestra el mapa. */
    val showResults: Boolean = false,
    val error: DomainError? = null,
    val selected: Place? = null,
    /** Posición actual del pin (el centro del mapa). Puede diferir del lugar elegido si el usuario lo ajustó. */
    val pinLatitude: Double? = null,
    val pinLongitude: Double? = null,
    val focus: MapFocus? = null,
    /** Dónde está el usuario: sirve para calcular la distancia y para abrir el mapa cerca suyo. Null si no hay permiso. */
    val userLocation: UserLocation? = null
) {
    val canSearch: Boolean
        get() = query.trim().length >= 3 && !isSearching
}

/** Qué tan cerca se acerca el mapa según el nivel elegido. */
internal fun zoomFor(type: PlaceType): Double = when (type) {
    PlaceType.COUNTRY -> 5.0
    PlaceType.CITY -> 11.0
    PlaceType.RESTAURANT -> 17.0
}
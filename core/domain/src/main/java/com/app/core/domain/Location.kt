package com.app.core.domain

data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)

data class PlaceLabel(
    val city: String,
    val country: String
)

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
) {
    val label: String
        get() = listOf(city, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

interface LocationProvider {
    suspend fun getCurrentLocation(): AppResult<GeoLocation>
}

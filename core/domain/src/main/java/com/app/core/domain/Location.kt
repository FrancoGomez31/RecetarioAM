package com.app.core.domain

/** Un punto en el mapa. */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)

/** El nombre legible de un punto: ciudad y país. Cualquiera puede venir vacío. */
data class PlaceLabel(
    val city: String,
    val country: String
)

/** Dónde está el usuario ahora: coordenadas más el nombre del lugar (vacío si no se pudo obtener). */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String
) {
    /** "Buenos Aires, Argentina": salta las partes vacías. */
    val label: String
        get() = listOf(city, country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

/** Contrato para saber dónde está el usuario. El dominio no sabe de GPS ni de Google Play Services. */
interface LocationProvider {
    /** Requiere que el usuario haya concedido el permiso de ubicación; si no, devuelve un error. */
    suspend fun getCurrentLocation(): AppResult<GeoLocation>
}
package com.app.core.domain

/** Nivel de detalle del origen. Coincide con las pestañas País / Ciudad / Restaurante. */
enum class PlaceType {
    COUNTRY,
    CITY,
    RESTAURANT
}

/** Un lugar encontrado en el buscador. [city] y [region] pueden estar vacíos (un país no tiene ciudad). */
data class Place(
    val name: String,
    val city: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val type: PlaceType
)
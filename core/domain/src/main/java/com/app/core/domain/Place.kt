package com.app.core.domain

enum class PlaceType {
    COUNTRY,
    CITY,
    RESTAURANT
}

data class Place(
    val name: String,
    val city: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val type: PlaceType
)

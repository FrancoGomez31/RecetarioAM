package com.app.core.domain

/** Contrato de búsqueda de lugares. El dominio no sabe qué servicio hay detrás. */
interface PlaceRepository {
    suspend fun search(query: String, type: PlaceType): AppResult<List<Place>>

    /** Búsqueda inversa: de unas coordenadas a "ciudad, país". */
    suspend fun describe(latitude: Double, longitude: Double): AppResult<PlaceLabel>
}
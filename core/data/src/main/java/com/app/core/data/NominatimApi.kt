package com.app.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API pública de búsqueda de OpenStreetMap. Sin API key, pero exige identificarse con un
 * User-Agent (lo agrega NetworkModule) y no más de 1 consulta por segundo.
 */
interface NominatimApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        // country | city. Si es null, Retrofit omite el parámetro y busca de todo.
        @Query("featureType") featureType: String? = null,
        @Query("format") format: String = "jsonv2",
        // Pide el desglose (ciudad, provincia, país) en el campo "address"
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("accept-language") language: String = "es"
    ): List<NominatimResultDto>
}

// DTOs: reflejan el JSON tal cual llega. Son detalle de esta capa; el dominio solo ve Place.

@Serializable
data class NominatimResultDto(
    // Nominatim manda las coordenadas como texto
    val lat: String,
    val lon: String,
    val name: String? = null,
    val category: String? = null,
    val type: String? = null,
    val address: NominatimAddressDto? = null
)

@Serializable
data class NominatimAddressDto(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val municipality: String? = null,
    val state: String? = null,
    val country: String? = null,
    @SerialName("country_code") val countryCode: String? = null
)
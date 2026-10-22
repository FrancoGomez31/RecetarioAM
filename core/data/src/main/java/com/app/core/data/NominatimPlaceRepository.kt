package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Place
import com.app.core.domain.PlaceRepository
import com.app.core.domain.PlaceType
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

class NominatimPlaceRepository @Inject constructor(
    private val api: NominatimApi
) : PlaceRepository {

    override suspend fun search(query: String, type: PlaceType): AppResult<List<Place>> {
        return try {
            val results = api.search(
                query = query,
                featureType = when (type) {
                    PlaceType.COUNTRY -> "country"
                    PlaceType.CITY -> "city"
                    // No existe filtro para restaurantes: se piden más y se filtran acá abajo.
                    PlaceType.RESTAURANT -> null
                },
                limit = if (type == PlaceType.RESTAURANT) 20 else 10
            )
            AppResult.Success(results.mapNotNull { it.toPlace(type) })
        } catch (e: CancellationException) {
            // Una búsqueda cancelada (el usuario cambió de pestaña o buscó otra cosa) no es un error.
            throw e
        } catch (e: IOException) {
            AppResult.Error(DomainError.Network)
        } catch (e: HttpException) {
            AppResult.Error(
                DomainError.Unknown("El buscador no está disponible. Probá de nuevo en un momento.")
            )
        } catch (e: Exception) {
            AppResult.Error(DomainError.Unknown())
        }
    }
}

// Categorías de OpenStreetMap que cuentan como "restaurante" para esta app.
private val RESTAURANT_TYPES = setOf(
    "restaurant", "cafe", "fast_food", "bar", "pub", "food_court", "ice_cream", "biergarten"
)

/** Traduce un resultado crudo a un Place. Devuelve null si falta algo esencial o no corresponde al tipo buscado. */
private fun NominatimResultDto.toPlace(placeType: PlaceType): Place? {
    val latitude = lat.toDoubleOrNull() ?: return null
    val longitude = lon.toDoubleOrNull() ?: return null
    val addr = address ?: return null
    val country = addr.country ?: return null
    val locality = addr.city ?: addr.town ?: addr.village ?: addr.municipality
    val region = addr.state.orEmpty()

    return when (placeType) {
        PlaceType.COUNTRY -> Place(
            name = country,
            city = "",
            region = "",
            country = country,
            latitude = latitude,
            longitude = longitude,
            type = placeType
        )

        PlaceType.CITY -> {
            val cityName = name ?: locality ?: return null
            Place(
                name = cityName,
                city = cityName,
                region = region,
                country = country,
                latitude = latitude,
                longitude = longitude,
                type = placeType
            )
        }

        PlaceType.RESTAURANT -> {
            if (category != "amenity" || type !in RESTAURANT_TYPES) return null
            val placeName = name ?: return null
            Place(
                name = placeName,
                city = locality.orEmpty(),
                region = region,
                country = country,
                latitude = latitude,
                longitude = longitude,
                type = placeType
            )
        }
    }
}
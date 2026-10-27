package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.LocationProvider
import com.app.core.domain.PlaceRepository
import com.app.core.domain.UserLocation
import com.app.core.domain.UserLocationCache
import javax.inject.Inject

/**
 * Dónde está el usuario AHORA, con el nombre del lugar ("Buenos Aires, Argentina") cuando se
 * pueda averiguar. Siempre pide una lectura nueva, y deja el resultado en la caché para el
 * resto de las pantallas.
 */
class GetCurrentLocationUseCase @Inject constructor(
    private val locationProvider: LocationProvider,
    private val placeRepository: PlaceRepository,
    private val locationCache: UserLocationCache
) {
    suspend operator fun invoke(): AppResult<UserLocation> {
        val location = when (val result = locationProvider.getCurrentLocation()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return AppResult.Error(result.error)
        }

        // El nombre es un extra: si el servicio de búsqueda falla, igual devolvemos las
        // coordenadas, que alcanzan para calcular distancias.
        val label = when (val result = placeRepository.describe(location.latitude, location.longitude)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> null
        }

        val userLocation = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            city = label?.city.orEmpty(),
            country = label?.country.orEmpty()
        )
        locationCache.save(userLocation)
        return AppResult.Success(userLocation)
    }
}
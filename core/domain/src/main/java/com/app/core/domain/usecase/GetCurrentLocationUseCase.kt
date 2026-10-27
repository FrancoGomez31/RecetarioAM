package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.LocationProvider
import com.app.core.domain.PlaceRepository
import com.app.core.domain.UserLocation
import javax.inject.Inject

/** Dónde está el usuario, con el nombre del lugar ("Buenos Aires, Argentina") cuando se pueda averiguar. */
class GetCurrentLocationUseCase @Inject constructor(
    private val locationProvider: LocationProvider,
    private val placeRepository: PlaceRepository
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

        return AppResult.Success(
            UserLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                city = label?.city.orEmpty(),
                country = label?.country.orEmpty()
            )
        )
    }
}
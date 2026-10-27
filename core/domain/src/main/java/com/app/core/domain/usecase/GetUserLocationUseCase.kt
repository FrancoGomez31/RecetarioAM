package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.UserLocation
import com.app.core.domain.UserLocationCache
import javax.inject.Inject

/**
 * La ubicación del usuario para pantallas que solo la necesitan como referencia (calcular
 * distancias): primero la caché; si está vacía, intenta averiguarla.
 *
 * Devuelve null si no hay permiso o no se pudo obtener: esas pantallas no tienen que explicar
 * el motivo, simplemente no muestran la distancia. El permiso se pide en la pestaña Mapa.
 */
class GetUserLocationUseCase @Inject constructor(
    private val locationCache: UserLocationCache,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) {
    suspend operator fun invoke(): UserLocation? {
        locationCache.get()?.let { return it }
        return when (val result = getCurrentLocationUseCase()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> null
        }
    }
}
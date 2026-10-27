package com.app.core.data

import com.app.core.domain.UserLocation
import com.app.core.domain.UserLocationCache
import javax.inject.Inject

/** Guarda la ubicación solo mientras la app está abierta: no se persiste a propósito. */
class InMemoryUserLocationCache @Inject constructor() : UserLocationCache {

    // @Volatile: varias pantallas (en distintos hilos) la leen y la escriben
    @Volatile
    private var last: UserLocation? = null

    override fun get(): UserLocation? = last

    override fun save(location: UserLocation) {
        last = location
    }
}
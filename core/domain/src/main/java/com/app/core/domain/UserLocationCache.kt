package com.app.core.domain

/**
 * Última ubicación conocida del usuario durante esta sesión. La pestaña Mapa la actualiza;
 * el detalle de receta y el selector de origen la leen para calcular distancias sin volver a
 * pedir el GPS en cada pantalla.
 */
interface UserLocationCache {
    fun get(): UserLocation?
    fun save(location: UserLocation)
}
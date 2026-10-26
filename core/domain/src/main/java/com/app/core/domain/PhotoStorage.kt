package com.app.core.domain

/**
 * Dónde viven las fotos. El dominio solo sabe esto: "guardame esta imagen y dame una
 * referencia permanente". Hoy es almacenamiento local; la nube (Firebase Storage,
 * Cloudinary…) se enchufa cambiando solo la implementación.
 */
interface PhotoStorage {
    /** [sourceUri]: URI temporal de la imagen elegida. Devuelve la referencia permanente. */
    suspend fun save(sourceUri: String): AppResult<String>
}
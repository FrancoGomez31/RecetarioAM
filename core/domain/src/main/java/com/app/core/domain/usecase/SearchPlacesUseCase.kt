package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Place
import com.app.core.domain.PlaceRepository
import com.app.core.domain.PlaceType
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(query: String, type: PlaceType): AppResult<List<Place>> {
        val clean = query.trim()
        // Con menos letras casi todo coincide con todo, y el servicio limita las consultas.
        if (clean.length < MIN_QUERY_LENGTH) return AppResult.Success(emptyList())
        return placeRepository.search(clean, type)
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
    }
}
package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeLinkExtractor
import com.app.core.domain.UserPreferencesRepository
import javax.inject.Inject

/**
 * Extrae una receta a partir de un enlace (video, reel, página).
 * Delega en [RecipeLinkExtractor] que hace el scraping del texto
 * y la llamada a la IA para estructurar ingredientes y pasos en el idioma activo del usuario.
 */
class ExtractRecipeFromLinkUseCase @Inject constructor(
    private val extractor: RecipeLinkExtractor,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(url: String): AppResult<Recipe> {
        val currentLang = preferencesRepository.language.value.code
        return extractor.extract(url, currentLang)
    }
}

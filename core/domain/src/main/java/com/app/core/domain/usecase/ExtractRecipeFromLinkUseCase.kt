package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeLinkExtractor
import javax.inject.Inject

/**
 * Extrae una receta a partir de un enlace (video, reel, página).
 * Delega en [RecipeLinkExtractor] que hace el scraping del texto
 * y la llamada a la IA para estructurar ingredientes y pasos.
 */
class ExtractRecipeFromLinkUseCase @Inject constructor(
    private val extractor: RecipeLinkExtractor
) {
    suspend operator fun invoke(url: String): AppResult<Recipe> = extractor.extract(url)
}

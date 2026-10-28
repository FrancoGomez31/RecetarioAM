package com.app.core.domain

import com.app.core.domain.AppResult

/** Contrato del dominio: extrae una receta estructurada a partir de un enlace URL. */
interface RecipeLinkExtractor {
    suspend fun extract(url: String, languageCode: String = "es"): AppResult<Recipe>
}

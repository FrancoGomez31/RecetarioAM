package com.app.features.recipes

import com.app.core.domain.DomainError

/**
 * El flujo de importar una receta por link tiene 3 pasos:
 *  1. IDLE / INPUT  → el usuario pega el link
 *  2. EXTRACTING    → se llama a Gemini, muestra spinner
 *  3. REVIEW        → se muestran los campos extraídos para que el usuario revise y confirme
 */
sealed interface LinkImportUiState {
    data object Idle : LinkImportUiState

    data object Extracting : LinkImportUiState

    data class Review(
        val name: String = "",
        val servings: String = "",
        val prepTime: String = "",
        val ingredients: List<IngredientInput> = emptyList(),
        val steps: List<StepInput> = emptyList()
    ) : LinkImportUiState

    data class Error(val error: DomainError) : LinkImportUiState
}

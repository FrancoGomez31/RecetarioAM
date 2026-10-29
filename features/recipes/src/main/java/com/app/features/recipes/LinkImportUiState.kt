package com.app.features.recipes

import com.app.core.domain.DomainError
import com.app.core.domain.Recipe

/**
 * El flujo de importar una receta por link tiene 3 pasos:
 *  1. IDLE / INPUT  → el usuario pega el link
 *  2. EXTRACTING    → se llama a Gemini, muestra spinner
 *  3. REVIEW        → se muestran los campos extraídos para que el usuario revise y elija entre Guardar, Editar o Probar otra
 */
sealed interface LinkImportUiState {
    data object Idle : LinkImportUiState

    data object Extracting : LinkImportUiState

    data class Review(
        val recipeDraft: Recipe? = null,
        val name: String = "",
        val description: String = "",
        val servings: String = "",
        val prepTime: String = "",
        val city: String = "",
        val country: String = "",
        val restaurant: String = "",
        val photoUrl: String? = null,
        val ingredients: List<IngredientInput> = emptyList(),
        val steps: List<StepInput> = emptyList(),
        val isSaving: Boolean = false,
        val saved: Boolean = false,
        val saveError: DomainError? = null,
        val isAlreadySaved: Boolean = false
    ) : LinkImportUiState

    data class Error(val error: DomainError) : LinkImportUiState
}

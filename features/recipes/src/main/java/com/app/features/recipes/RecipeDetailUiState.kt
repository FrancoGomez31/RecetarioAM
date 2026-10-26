package com.app.features.recipes

import com.app.core.domain.Attempt
import com.app.core.domain.DomainError
import com.app.core.domain.Recipe

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    /** Intentos de la receta, del primero al último. */
    val attempts: List<Attempt> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    /** La pantalla lo observa: cuando pasa a true, vuelve atrás. */
    val deleted: Boolean = false,
    val error: DomainError? = null
)
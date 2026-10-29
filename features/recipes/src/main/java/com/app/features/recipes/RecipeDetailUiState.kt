package com.app.features.recipes

import com.app.core.domain.Attempt
import com.app.core.domain.DomainError
import com.app.core.domain.Recipe
import com.app.core.domain.UserLocation

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    /** Intentos de la receta, del primero al último. */
    val attempts: List<Attempt> = emptyList(),
    /** Dónde está el usuario, para calcular la distancia al origen. Null si no hay permiso. */
    val userLocation: UserLocation? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    /** La pantalla lo observa: cuando pasa a true, vuelve atrás. */
    val deleted: Boolean = false,
    val isSavingCopy: Boolean = false,
    val copySaved: Boolean = false,
    /** True si el usuario ya tiene una copia de esta receta en su recetario. */
    val alreadySaved: Boolean = false,
    val error: DomainError? = null
)

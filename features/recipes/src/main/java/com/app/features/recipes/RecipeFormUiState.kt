package com.app.features.recipes

import com.app.core.domain.DomainError
import com.app.core.domain.RecipeField
import com.app.core.domain.Visibility

/** Fila editable de ingrediente. El id es solo de UI: mantiene estable cada fila al agregar o borrar. */
data class IngredientInput(
    val id: Long,
    val quantity: String = "",
    val name: String = ""
)

data class StepInput(
    val id: Long,
    val text: String = ""
)

data class RecipeFormUiState(
    /** true = editando una receta existente; false = receta nueva. */
    val isEditing: Boolean = false,
    val originalName: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    /** La pantalla lo observa: cuando pasa a true, vuelve atrás. */
    val saved: Boolean = false,

    val name: String = "",
    val description: String = "",
    // Se guardan como texto para poder dejar el campo vacío mientras se escribe.
    val servings: String = "4",
    val prepTime: String = "",
    // Provisorio: ciudad y país a mano hasta tener el selector de origen en el mapa.
    val city: String = "",
    val country: String = "",
    val visibility: Visibility = Visibility.PRIVATE,
    val ingredients: List<IngredientInput> = emptyList(),
    val steps: List<StepInput> = emptyList(),

    /** Campos que el dominio rechazó en el último intento de guardar. */
    val invalidFields: Set<RecipeField> = emptySet(),
    val error: DomainError? = null
) {
    val canSave: Boolean
        get() = !isSaving && !isLoading
}
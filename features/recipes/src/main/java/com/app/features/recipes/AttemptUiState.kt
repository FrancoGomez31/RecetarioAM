package com.app.features.recipes

import com.app.core.domain.Attempt
import com.app.core.domain.DomainError
import com.app.core.domain.Recipe

data class AttemptUiState(
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    /** Intentos anteriores de esta receta (para comparar). No incluye el que se está registrando. */
    val previousAttempts: List<Attempt> = emptyList(),
    /** El intento en curso. Es null hasta que se guarda la primera foto o nota. */
    val currentAttempt: Attempt? = null,
    val attemptNumber: Int = 1,
    /** Paso al que se asocia la foto (0 = Paso 1). */
    val selectedStep: Int = 0,
    /** Foto elegida que todavía no se guardó ("en revisión"). */
    val pendingPhotoUri: String? = null,
    val pendingTakenAtMillis: Long? = null,
    val note: String = "",
    /** La nota como está guardada: sirve para saber si el usuario la cambió. */
    val savedNote: String = "",
    val isSaving: Boolean = false,
    val error: DomainError? = null
) {
    val noteChanged: Boolean
        get() = note.trim() != savedNote

    /** Hay algo para guardar: una foto en revisión, o una nota nueva o modificada. */
    val canSave: Boolean
        get() = !isSaving && !isLoading && recipe != null &&
                (pendingPhotoUri != null || noteChanged)
}
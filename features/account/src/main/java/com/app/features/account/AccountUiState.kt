package com.app.features.account

import com.app.core.domain.DomainError

data class AccountUiState(
    val email: String = "",
    /** Lo que hay escrito en el campo ahora. */
    val name: String = "",
    /** El nombre que ya está guardado en Firebase. */
    val savedName: String = "",
    val isSaving: Boolean = false,
    val justSaved: Boolean = false,
    val error: DomainError? = null
) {
    /** Solo se puede guardar si hay un cambio real y no hay un guardado en curso. */
    val canSave: Boolean
        get() = !isSaving && name.trim() != savedName
}
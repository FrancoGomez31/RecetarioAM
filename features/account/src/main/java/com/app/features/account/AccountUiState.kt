package com.app.features.account

import com.app.core.domain.DomainError
import com.app.core.domain.LanguagePreference
import com.app.core.domain.ThemePreference

data class AccountUiState(
    val email: String = "",
    val name: String = "",
    val savedName: String = "",
    val themeMode: ThemePreference = ThemePreference.SYSTEM,
    val language: LanguagePreference = LanguagePreference.SPANISH,
    val isSaving: Boolean = false,
    val justSaved: Boolean = false,
    val error: DomainError? = null
) {
    val canSave: Boolean
        get() = !isSaving && name.trim() != savedName
}

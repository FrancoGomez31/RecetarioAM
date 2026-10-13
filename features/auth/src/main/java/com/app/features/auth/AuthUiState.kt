package com.app.features.auth

import com.app.core.domain.DomainError

enum class AuthMode {
    LOGIN,
    REGISTER
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val isAuthenticated: Boolean = false
)
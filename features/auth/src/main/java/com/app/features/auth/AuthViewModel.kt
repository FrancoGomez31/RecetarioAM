package com.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.AuthRepository
import com.app.core.domain.DomainError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = authRepository.getCurrentUser() != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onModeChange(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.isLoading) return

        if (!state.email.contains("@")) {
            _uiState.update { it.copy(error = DomainError.InvalidEmail) }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = DomainError.WeakPassword) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = when (state.mode) {
                AuthMode.LOGIN -> authRepository.signIn(state.email, state.password)
                AuthMode.REGISTER -> authRepository.signUp(state.email, state.password)
            }
            when (result) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isAuthenticated = true)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}
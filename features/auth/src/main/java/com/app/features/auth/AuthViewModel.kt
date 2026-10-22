package com.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.usecase.GetCurrentUserUseCase
import com.app.core.domain.usecase.ObserveSessionUseCase
import com.app.core.domain.usecase.SignInUseCase
import com.app.core.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @HiltViewModel + @Inject: Hilt arma este ViewModel y le entrega los casos de uso.
 * El ViewModel ya no valida nada: las reglas de negocio viven en el dominio.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
    observeSessionUseCase: ObserveSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = getCurrentUserUseCase() != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Este ViewModel vive mientras la Activity esté viva. Al cerrar sesión hay que dejar el
        // formulario como nuevo: sin contraseña escrita ni "ya autenticado" de la sesión anterior.
        viewModelScope.launch {
            observeSessionUseCase().collect { loggedIn ->
                if (!loggedIn) _uiState.value = AuthUiState()
            }
        }
    }

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

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = when (state.mode) {
                AuthMode.LOGIN -> signInUseCase(state.email, state.password)
                AuthMode.REGISTER -> signUpUseCase(state.email, state.password)
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
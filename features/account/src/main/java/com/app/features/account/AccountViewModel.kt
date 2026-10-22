package com.app.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.usecase.GetCurrentUserUseCase
import com.app.core.domain.usecase.SignOutUseCase
import com.app.core.domain.usecase.UpdateDisplayNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateDisplayNameUseCase: UpdateDisplayNameUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState(getCurrentUserUseCase()?.email, getCurrentUserUseCase()?.displayName))
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, justSaved = false, error = null) }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, justSaved = false, error = null) }

        viewModelScope.launch {
            when (val result = updateDisplayNameUseCase(state.name)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        name = result.data.displayName,
                        savedName = result.data.displayName,
                        justSaved = true
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.error)
                }
            }
        }
    }

    /**
     * No hace falta navegar a ningún lado: al cerrar la sesión, Firebase avisa y
     * SessionViewModel cambia solo a la pantalla de acceso.
     */
    fun onSignOut() {
        viewModelScope.launch { signOutUseCase() }
    }
}

private fun initialState(email: String?, displayName: String?): AccountUiState {
    val name = displayName.orEmpty()
    return AccountUiState(email = email.orEmpty(), name = name, savedName = name)
}
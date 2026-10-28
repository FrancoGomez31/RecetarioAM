package com.app.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.LanguagePreference
import com.app.core.domain.ThemePreference
import com.app.core.domain.UserPreferencesRepository
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
    private val signOutUseCase: SignOutUseCase,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        initialState(
            email = getCurrentUserUseCase()?.email,
            displayName = getCurrentUserUseCase()?.displayName,
            themeMode = preferencesRepository.themeMode.value,
            language = preferencesRepository.language.value
        )
    )
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.themeMode.collect { theme ->
                _uiState.update { it.copy(themeMode = theme) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.language.collect { lang ->
                _uiState.update { it.copy(language = lang) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, justSaved = false, error = null) }
    }

    fun onThemeChange(mode: ThemePreference) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun onLanguageChange(language: LanguagePreference) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(language)
        }
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

    fun onSignOut() {
        viewModelScope.launch { signOutUseCase() }
    }
}

private fun initialState(
    email: String?,
    displayName: String?,
    themeMode: ThemePreference,
    language: LanguagePreference
): AccountUiState {
    val name = displayName.orEmpty()
    return AccountUiState(
        email = email.orEmpty(),
        name = name,
        savedName = name,
        themeMode = themeMode,
        language = language
    )
}

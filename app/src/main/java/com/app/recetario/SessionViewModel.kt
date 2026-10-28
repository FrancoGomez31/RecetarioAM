package com.app.recetario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.LanguagePreference
import com.app.core.domain.NetworkMonitor
import com.app.core.domain.ThemePreference
import com.app.core.domain.UserPreferencesRepository
import com.app.core.domain.usecase.GetCurrentUserUseCase
import com.app.core.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SessionViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    observeSessionUseCase: ObserveSessionUseCase,
    preferencesRepository: UserPreferencesRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = observeSessionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = getCurrentUserUseCase() != null
        )

    val themeMode: StateFlow<ThemePreference> = preferencesRepository.themeMode

    val language: StateFlow<LanguagePreference> = preferencesRepository.language

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
}
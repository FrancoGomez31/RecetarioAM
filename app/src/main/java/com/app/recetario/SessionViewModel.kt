package com.app.recetario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.usecase.GetCurrentUserUseCase
import com.app.core.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Dueño de la pregunta "¿hay una sesión iniciada?". Decide si se muestra el acceso o la app.
 *
 * La respuesta viene de Firebase: iniciar o cerrar sesión desde cualquier pantalla
 * cambia este valor solo, sin que nadie tenga que avisarle.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    observeSessionUseCase: ObserveSessionUseCase
) : ViewModel() {

    // El valor inicial evita un parpadeo del acceso al abrir la app con la sesión ya iniciada.
    val isLoggedIn: StateFlow<Boolean> = observeSessionUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = getCurrentUserUseCase() != null
        )
}
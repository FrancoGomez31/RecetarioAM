package com.app.recetario

import androidx.lifecycle.ViewModel
import com.app.core.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Dueño de la pregunta "¿hay una sesión iniciada?". Decide si se muestra el acceso o la app.
 *
 * Hoy la sesión vive en memoria. Cuando conectemos Firebase, este es el único lugar que
 * cambia: pasa a observar el estado de autenticación real y la sesión sobrevive entre ejecuciones.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(getCurrentUserUseCase() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun onAuthenticated() {
        _isLoggedIn.value = true
    }
}
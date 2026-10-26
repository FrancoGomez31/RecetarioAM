package com.app.features.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.NewAttemptPhoto
import com.app.core.domain.usecase.GetAttemptsUseCase
import com.app.core.domain.usecase.GetRecipeUseCase
import com.app.core.domain.usecase.SaveAttemptProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AttemptViewModel @Inject constructor(
    private val getRecipeUseCase: GetRecipeUseCase,
    private val getAttemptsUseCase: GetAttemptsUseCase,
    private val saveAttemptProgressUseCase: SaveAttemptProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttemptUiState())
    val uiState: StateFlow<AttemptUiState> = _uiState.asStateFlow()

    /** La pantalla llama a start() cada vez que se compone; solo la primera vez hace algo. */
    private var started = false

    fun start(recipeId: String) {
        if (started) return
        started = true

        viewModelScope.launch {
            val recipeResult = getRecipeUseCase(recipeId)
            val attempts = when (val result = getAttemptsUseCase(recipeId)) {
                is AppResult.Success -> result.data
                // Sin los intentos anteriores igual se puede registrar uno; solo no hay con qué comparar.
                is AppResult.Error -> emptyList()
            }

            when (recipeResult) {
                is AppResult.Success -> {
                    val recipe = recipeResult.data
                    // El número sale del mayor entre el contador de la receta y los intentos guardados.
                    val number = maxOf(recipe.attempts, attempts.maxOfOrNull { it.number } ?: 0) + 1
                    // update y no asignación: si la foto ya llegó (por ejemplo, tras recrearse la
                    // pantalla mientras estaba la cámara), no la pisamos.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recipe = recipe,
                            previousAttempts = attempts,
                            attemptNumber = number
                        )
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = recipeResult.error)
                }
            }
        }
    }

    fun onStepSelected(index: Int) {
        _uiState.update { it.copy(selectedStep = index) }
    }

    /** La cámara o la galería entregaron una foto. Queda "en revisión" hasta guardarla. */
    fun onPhotoSelected(uri: String) {
        _uiState.update {
            it.copy(
                pendingPhotoUri = uri,
                pendingTakenAtMillis = System.currentTimeMillis(),
                error = null
            )
        }
    }

    /** No se pudo abrir la cámara (sin permiso, o el teléfono no tiene app de cámara). */
    fun onPhotoUnavailable(message: String) {
        _uiState.update { it.copy(error = DomainError.Unknown(message)) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun onSave() {
        val state = _uiState.value
        val recipe = state.recipe ?: return
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val photo = state.pendingPhotoUri?.let { NewAttemptPhoto(stepIndex = state.selectedStep, sourceUri = it) }
            val result = saveAttemptProgressUseCase(
                recipeId = recipe.id,
                current = state.currentAttempt,
                attemptNumber = state.attemptNumber,
                photo = photo,
                note = state.note
            )
            when (result) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        currentAttempt = result.data,
                        pendingPhotoUri = null,
                        pendingTakenAtMillis = null,
                        note = result.data.note,
                        savedNote = result.data.note
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isSaving = false, error = result.error)
                }
            }
        }
    }
}
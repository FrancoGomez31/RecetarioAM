package com.app.features.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.Visibility
import com.app.core.domain.usecase.DeleteRecipeUseCase
import com.app.core.domain.usecase.GetAttemptsUseCase
import com.app.core.domain.usecase.GetRecipeUseCase
import com.app.core.domain.usecase.GetUserLocationUseCase
import com.app.core.domain.usecase.SaveRecipeUseCase
import com.app.core.domain.usecase.SetRecipeReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val getRecipeUseCase: GetRecipeUseCase,
    private val getAttemptsUseCase: GetAttemptsUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val setRecipeReminderUseCase: SetRecipeReminderUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    /**
     * Se llama cada vez que la pantalla aparece (primera vez y al volver de editar o de
     * registrar un intento). El spinner solo se muestra si todavía no hay receta: al recargar
     * con los datos ya a la vista, se actualizan sin parpadeo.
     */
    fun load(recipeId: String) {
        _uiState.update { it.copy(isLoading = it.recipe == null, error = null, copySaved = false) }
        viewModelScope.launch {
            when (val result = getRecipeUseCase(recipeId)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(recipe = result.data, isLoading = false)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }

            // Los intentos son un extra: si fallan, el detalle se muestra igual sin esa lista.
            val attemptsResult = getAttemptsUseCase(recipeId)
            if (attemptsResult is AppResult.Success) {
                _uiState.update { it.copy(attempts = attemptsResult.data) }
            }

            // La ubicación también: sin ella (sin permiso, GPS apagado) no se muestra la distancia.
            // Va al final para que la receta aparezca sin esperar al GPS.
            val location = getUserLocationUseCase()
            _uiState.update { it.copy(userLocation = location) }
        }
    }

    fun onSaveCopy() {
        val recipe = _uiState.value.recipe ?: return
        if (_uiState.value.isSavingCopy) return

        _uiState.update { it.copy(isSavingCopy = true, error = null) }
        viewModelScope.launch {
            // Crea una copia personal de la receta para el usuario
            val copyToSave = recipe.copy(
                id = "",
                visibility = Visibility.PRIVATE
            )
            when (val result = saveRecipeUseCase(copyToSave)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(isSavingCopy = false, copySaved = true)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isSavingCopy = false, error = result.error)
                    }
                }
            }
        }
    }

    fun onDelete() {
        val state = _uiState.value
        val recipe = state.recipe ?: return
        if (state.isDeleting) return

        _uiState.update { it.copy(isDeleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteRecipeUseCase(recipe.id)) {
                is AppResult.Success -> _uiState.update { it.copy(isDeleting = false, deleted = true) }
                is AppResult.Error -> _uiState.update { it.copy(isDeleting = false, error = result.error) }
            }
        }
    }

    fun onToggleReminder(triggerAtMillis: Long?) {
        val state = _uiState.value
        val recipe = state.recipe ?: return
        viewModelScope.launch {
            when (val result = setRecipeReminderUseCase(recipe, triggerAtMillis)) {
                is AppResult.Success -> _uiState.update { it.copy(recipe = result.data) }
                is AppResult.Error -> _uiState.update { it.copy(error = result.error) }
            }
        }
    }
}

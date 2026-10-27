package com.app.features.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.DomainError
import com.app.core.domain.usecase.ExtractRecipeFromLinkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LinkImportViewModel @Inject constructor(
    private val extractRecipeFromLinkUseCase: ExtractRecipeFromLinkUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LinkImportUiState>(LinkImportUiState.Idle)
    val uiState: StateFlow<LinkImportUiState> = _uiState.asStateFlow()

    private var nextId = 0L
    private fun nextRowId() = nextId++

    fun onExtract(url: String) {
        if (url.isBlank()) return
        _uiState.value = LinkImportUiState.Extracting

        viewModelScope.launch {
            when (val result = extractRecipeFromLinkUseCase(url)) {
                is com.app.core.domain.AppResult.Success -> {
                    val r = result.data
                    _uiState.value = LinkImportUiState.Review(
                        name = r.name,
                        servings = r.servings.toString(),
                        prepTime = r.prepTimeMinutes.toString(),
                        ingredients = r.ingredients.map { IngredientInput(nextRowId(), it.quantity, it.name) }
                            .ifEmpty { listOf(IngredientInput(nextRowId())) },
                        steps = r.steps.map { StepInput(nextRowId(), it) }
                            .ifEmpty { listOf(StepInput(nextRowId())) }
                    )
                }
                is com.app.core.domain.AppResult.Error -> {
                    _uiState.value = LinkImportUiState.Error(result.error)
                }
            }
        }
    }

    fun onRetry() {
        _uiState.value = LinkImportUiState.Idle
    }

    /**
     * Convierte el estado Review en un RecipeFormUiState para pre-llenar el formulario
     * y que el usuario confirme antes de guardar.
     */
    fun buildFormState(): RecipeFormUiState? {
        val review = _uiState.value as? LinkImportUiState.Review ?: return null
        return RecipeFormUiState(
            name = review.name,
            servings = review.servings,
            prepTime = review.prepTime,
            ingredients = review.ingredients,
            steps = review.steps
        )
    }
}

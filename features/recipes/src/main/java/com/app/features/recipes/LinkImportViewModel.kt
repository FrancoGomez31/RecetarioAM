package com.app.features.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility
import com.app.core.domain.usecase.ExtractRecipeFromLinkUseCase
import com.app.core.domain.usecase.GetRecipesUseCase
import com.app.core.domain.usecase.SaveRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LinkImportViewModel @Inject constructor(
    private val extractRecipeFromLinkUseCase: ExtractRecipeFromLinkUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val getRecipesUseCase: GetRecipesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LinkImportUiState>(LinkImportUiState.Idle)
    val uiState: StateFlow<LinkImportUiState> = _uiState.asStateFlow()

    private var nextId = 0L
    private fun nextRowId() = nextId++

    fun onExtract(url: String) {
        if (url.isBlank()) return
        _uiState.value = LinkImportUiState.Extracting

        viewModelScope.launch {
            val existingRecipes = when (val res = getRecipesUseCase()) {
                is AppResult.Success -> res.data
                else -> emptyList()
            }

            when (val result = extractRecipeFromLinkUseCase(url)) {
                is AppResult.Success -> {
                    val r = result.data
                    val alreadySaved = existingRecipes.any {
                        it.name.trim().equals(r.name.trim(), ignoreCase = true)
                    }

                    _uiState.value = LinkImportUiState.Review(
                        recipeDraft = r,
                        name = r.name,
                        description = r.description,
                        servings = r.servings.toString(),
                        prepTime = r.prepTimeMinutes.toString(),
                        city = r.city,
                        country = r.country,
                        restaurant = r.restaurant,
                        photoUrl = r.photoUrl,
                        ingredients = r.ingredients.map { IngredientInput(nextRowId(), it.quantity, it.name) }
                            .ifEmpty { listOf(IngredientInput(nextRowId())) },
                        steps = r.steps.map { StepInput(nextRowId(), it) }
                            .ifEmpty { listOf(StepInput(nextRowId())) },
                        isAlreadySaved = alreadySaved
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = LinkImportUiState.Error(result.error)
                }
            }
        }
    }

    fun onSaveDirectly() {
        val current = _uiState.value as? LinkImportUiState.Review ?: return
        if (current.isSaving) return

        _uiState.value = current.copy(isSaving = true, saveError = null)

        viewModelScope.launch {
            val recipeToSave = current.recipeDraft?.copy(
                name = current.name,
                description = current.description,
                servings = current.servings.toIntOrNull() ?: 4,
                prepTimeMinutes = current.prepTime.toIntOrNull() ?: 30,
                city = current.city,
                country = current.country.ifBlank { "Internacional" },
                restaurant = current.restaurant,
                photoUrl = current.photoUrl,
                ingredients = current.ingredients.map { Ingredient(it.quantity, it.name) },
                steps = current.steps.map { it.text }
            ) ?: Recipe(
                id = "",
                name = current.name,
                description = current.description,
                photoUrl = current.photoUrl,
                city = current.city,
                country = current.country.ifBlank { "Internacional" },
                restaurant = current.restaurant,
                prepTimeMinutes = current.prepTime.toIntOrNull() ?: 30,
                attempts = 0,
                visibility = Visibility.PRIVATE,
                servings = current.servings.toIntOrNull() ?: 4,
                ingredients = current.ingredients.map { Ingredient(it.quantity, it.name) },
                steps = current.steps.map { it.text }
            )

            when (val res = saveRecipeUseCase(recipeToSave)) {
                is AppResult.Success -> {
                    _uiState.value = current.copy(isSaving = false, saved = true)
                }
                is AppResult.Error -> {
                    _uiState.value = current.copy(isSaving = false, saveError = res.error)
                }
            }
        }
    }

    fun onRetry() {
        _uiState.value = LinkImportUiState.Idle
    }

    /**
     * Convierte el estado Review en un RecipeFormUiState para pre-llenar el formulario
     * cuando el usuario decide ir a editar.
     */
    fun buildFormState(): RecipeFormUiState? {
        val review = _uiState.value as? LinkImportUiState.Review ?: return null
        return RecipeFormUiState(
            name = review.name,
            description = review.description,
            servings = review.servings,
            prepTime = review.prepTime,
            city = review.city,
            country = review.country,
            restaurant = review.restaurant,
            coverUri = review.photoUrl,
            ingredients = review.ingredients,
            steps = review.steps
        )
    }
}

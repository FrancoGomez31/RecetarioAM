package com.app.features.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Ingredient
import com.app.core.domain.Place
import com.app.core.domain.PlaceType
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeField
import com.app.core.domain.Visibility
import com.app.core.domain.usecase.GetRecipeUseCase
import com.app.core.domain.usecase.SaveRecipeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecipeFormViewModel @Inject constructor(
    private val getRecipeUseCase: GetRecipeUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase
) : ViewModel() {

    // Contador de ids de fila. Tiene que declararse antes que _uiState, que ya lo usa.
    private var nextRowId = 0L
    private fun newRowId(): Long = nextRowId++

    private val _uiState = MutableStateFlow(
        RecipeFormUiState(
            ingredients = listOf(IngredientInput(newRowId())),
            steps = listOf(StepInput(newRowId()))
        )
    )
    val uiState: StateFlow<RecipeFormUiState> = _uiState.asStateFlow()

    /** Receta que se está editando: de ahí salen los datos que el formulario no muestra (foto, intentos). */
    private var original: Recipe? = null

    /** La pantalla llama a start() cada vez que se compone; solo la primera vez hace algo. */
    private var started = false

    fun start(recipeId: String?) {
        if (started) return
        started = true
        if (recipeId == null) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getRecipeUseCase(recipeId)) {
                is AppResult.Success -> {
                    original = result.data
                    _uiState.value = result.data.toFormState()
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    // --- Campos simples ---

    fun onNameChange(value: String) = updateField(RecipeField.NAME) { it.copy(name = value) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onServingsChange(value: String) = updateField(RecipeField.SERVINGS) {
        it.copy(servings = value.filter(Char::isDigit).take(3))
    }

    fun onPrepTimeChange(value: String) = updateField(RecipeField.TIME) {
        it.copy(prepTime = value.filter(Char::isDigit).take(4))
    }

    fun onVisibilityChange(value: Visibility) = _uiState.update { it.copy(visibility = value) }

    // --- Origen ---

    /**
     * Llega desde el selector con mapa: el lugar elegido y la posición final del pin
     * (que puede ser distinta a la del lugar si el usuario la ajustó).
     * Se reemplaza el origen completo, así no quedan restos de una elección anterior.
     */
    fun onOriginSelected(place: Place, latitude: Double, longitude: Double) =
        updateField(RecipeField.ORIGIN) {
            it.copy(
                city = place.city,
                country = place.country,
                restaurant = if (place.type == PlaceType.RESTAURANT) place.name else "",
                latitude = latitude,
                longitude = longitude
            )
        }

    // --- Ingredientes ---

    fun onIngredientQuantityChange(id: Long, value: String) = updateField(RecipeField.INGREDIENTS) { state ->
        state.copy(ingredients = state.ingredients.map { if (it.id == id) it.copy(quantity = value) else it })
    }

    fun onIngredientNameChange(id: Long, value: String) = updateField(RecipeField.INGREDIENTS) { state ->
        state.copy(ingredients = state.ingredients.map { if (it.id == id) it.copy(name = value) else it })
    }

    fun onAddIngredient() = _uiState.update {
        it.copy(ingredients = it.ingredients + IngredientInput(newRowId()))
    }

    fun onRemoveIngredient(id: Long) = _uiState.update { state ->
        state.copy(ingredients = state.ingredients.filterNot { it.id == id })
    }

    // --- Pasos ---

    fun onStepChange(id: Long, value: String) = updateField(RecipeField.STEPS) { state ->
        state.copy(steps = state.steps.map { if (it.id == id) it.copy(text = value) else it })
    }

    fun onAddStep() = _uiState.update { it.copy(steps = it.steps + StepInput(newRowId())) }

    fun onRemoveStep(id: Long) = _uiState.update { state ->
        state.copy(steps = state.steps.filterNot { it.id == id })
    }

    // --- Guardado ---

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true, error = null, invalidFields = emptySet()) }

        viewModelScope.launch {
            when (val result = saveRecipeUseCase(state.toRecipe())) {
                is AppResult.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = result.error,
                        invalidFields = (result.error as? DomainError.InvalidRecipe)?.fields.orEmpty()
                    )
                }
            }
        }
    }

    // --- Helpers ---

    /** Aplica un cambio y, de paso, saca el error de ese campo: el usuario ya lo está corrigiendo. */
    private fun updateField(
        field: RecipeField,
        change: (RecipeFormUiState) -> RecipeFormUiState
    ) {
        _uiState.update { change(it).copy(invalidFields = it.invalidFields - field, error = null) }
    }

    /** Arma la receta a guardar: parte de la original (si se edita) y le pisa lo que muestra el formulario. */
    private fun RecipeFormUiState.toRecipe(): Recipe {
        val base = original ?: Recipe(
            id = "",
            name = "",
            photoUrl = null,
            city = "",
            country = "",
            prepTimeMinutes = 0,
            attempts = 0,
            visibility = Visibility.PRIVATE
        )
        return base.copy(
            name = name,
            description = description,
            servings = servings.toIntOrNull() ?: 0,
            prepTimeMinutes = prepTime.toIntOrNull() ?: 0,
            city = city,
            country = country,
            restaurant = restaurant,
            latitude = latitude,
            longitude = longitude,
            visibility = visibility,
            ingredients = ingredients.map { Ingredient(quantity = it.quantity, name = it.name) },
            steps = steps.map { it.text }
        )
    }

    private fun Recipe.toFormState(): RecipeFormUiState = RecipeFormUiState(
        isEditing = true,
        originalName = name,
        name = name,
        description = description,
        servings = servings.toString(),
        prepTime = prepTimeMinutes.toString(),
        city = city,
        country = country,
        restaurant = restaurant,
        latitude = latitude,
        longitude = longitude,
        visibility = visibility,
        ingredients = ingredients
            .map { IngredientInput(newRowId(), it.quantity, it.name) }
            .ifEmpty { listOf(IngredientInput(newRowId())) },
        steps = steps
            .map { StepInput(newRowId(), it) }
            .ifEmpty { listOf(StepInput(newRowId())) }
    )
}
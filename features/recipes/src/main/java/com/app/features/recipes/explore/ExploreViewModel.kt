package com.app.features.recipes.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.ExploreCatalog
import com.app.core.domain.LanguagePreference
import com.app.core.domain.UserPreferencesRepository
import com.app.core.domain.Visibility
import com.app.core.domain.usecase.GetRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadExploreRecipes()
        viewModelScope.launch {
            preferencesRepository.language.collect {
                loadExploreRecipes()
            }
        }
    }

    fun loadExploreRecipes() {
        val isEnglish = preferencesRepository.language.value == LanguagePreference.ENGLISH
        val recipesToUse = ExploreCatalog.getCatalog(isEnglish)

        _uiState.update { it.copy(isLoading = it.recipes.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = getRecipesUseCase()) {
                is AppResult.Success -> {
                    val publicFromRepo = result.data.filter { it.visibility == Visibility.PUBLIC }
                    val combined = (publicFromRepo + recipesToUse)
                        .distinctBy { it.id }
                    _uiState.update {
                        it.copy(recipes = combined, isLoading = false)
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(recipes = recipesToUse, isLoading = false, error = result.error)
                    }
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onFilterChange(filter: ExploreFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun onCountrySelect(country: String?) {
        _uiState.update {
            it.copy(selectedCountry = if (it.selectedCountry == country) null else country)
        }
    }
}

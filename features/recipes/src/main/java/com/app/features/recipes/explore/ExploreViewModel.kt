package com.app.features.recipes.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.ExploreCatalog
import com.app.core.domain.LanguagePreference
import com.app.core.domain.RecipeSearcher
import com.app.core.domain.UserPreferencesRepository
import com.app.core.domain.Visibility
import com.app.core.domain.usecase.GetRecipesUseCase
import com.app.core.domain.usecase.SearchRecipesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

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
        scheduleAiSearch(query, SEARCH_DELAY_MS)
    }

    /** Busca ya, sin esperar la pausa (tecla "buscar" del teclado). */
    fun onSearchNow() {
        scheduleAiSearch(_uiState.value.query, 0L)
    }

    /**
     * Busca con IA (primera página) después de una pausa al escribir. Cada letra nueva cancela
     * la búsqueda pendiente, así solo se llama a la IA cuando el usuario deja de escribir.
     */
    private fun scheduleAiSearch(query: String, delayMs: Long) {
        searchJob?.cancel()
        val text = query.trim()
        if (text.length < MIN_QUERY_LENGTH) {
            _uiState.update {
                it.copy(
                    aiRecipes = emptyList(), aiPage = 0, canLoadMore = false,
                    isSearching = false, isLoadingMore = false, searchError = null
                )
            }
            return
        }
        // Mientras llega la búsqueda nueva no se puede pedir "ver más" de la anterior
        _uiState.update { it.copy(canLoadMore = false, isLoadingMore = false) }
        searchJob = viewModelScope.launch {
            delay(delayMs)
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            when (val result = searchRecipesUseCase(text, 0)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        aiRecipes = result.data,
                        aiPage = 1,
                        canLoadMore = hasMorePages(pagesLoaded = 1, lastPageSize = result.data.size),
                        isSearching = false
                    )
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        aiRecipes = emptyList(), aiPage = 0, canLoadMore = false,
                        isSearching = false, searchError = result.error
                    )
                }
            }
        }
    }

    /** Carga la página siguiente de la IA y la agrega a las ya cargadas. */
    fun onLoadMore() {
        val state = _uiState.value
        if (state.isSearching || state.isLoadingMore || !state.canLoadMore) return
        val text = state.query.trim()
        val page = state.aiPage

        _uiState.update { it.copy(isLoadingMore = true, searchError = null) }
        searchJob = viewModelScope.launch {
            when (val result = searchRecipesUseCase(text, page)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        aiRecipes = (it.aiRecipes + result.data).distinctBy { r -> r.id },
                        aiPage = page + 1,
                        canLoadMore = hasMorePages(pagesLoaded = page + 1, lastPageSize = result.data.size),
                        isLoadingMore = false
                    )
                }
                // Si falla, los resultados ya cargados quedan y se puede reintentar
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoadingMore = false, searchError = result.error)
                }
            }
        }
    }

    /** Hay más si no se llegó al máximo de páginas y la última trajo recetas nuevas. */
    private fun hasMorePages(pagesLoaded: Int, lastPageSize: Int): Boolean =
        pagesLoaded < RecipeSearcher.MAX_PAGES && lastPageSize >= MIN_RESULTS_FOR_MORE

    fun onFilterChange(filter: ExploreFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun onCountrySelect(country: String?) {
        _uiState.update {
            it.copy(selectedCountry = if (it.selectedCountry == country) null else country)
        }
    }

    private companion object {
        const val SEARCH_DELAY_MS = 800L
        const val MIN_QUERY_LENGTH = 3

        /** Si una página trae menos que esto, se asume que ya no quedan más recetas. */
        const val MIN_RESULTS_FOR_MORE = 5
    }
}
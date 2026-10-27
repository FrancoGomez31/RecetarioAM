package com.app.features.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.Place
import com.app.core.domain.PlaceType
import com.app.core.domain.usecase.GetUserLocationUseCase
import com.app.core.domain.usecase.SearchPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlacePickerViewModel @Inject constructor(
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacePickerUiState())
    val uiState: StateFlow<PlacePickerUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var focusCounter = 0L

    /**
     * Se llama cada vez que el selector se abre. Casi siempre responde al instante (la caché
     * ya tiene la ubicación que averiguó la pestaña Mapa); sin permiso queda en null.
     */
    fun loadUserLocation() {
        viewModelScope.launch {
            val location = getUserLocationUseCase()
            _uiState.update { it.copy(userLocation = location) }
        }
    }

    /** Cambiar de nivel (país / ciudad / restaurante) descarta lo anterior: los resultados ya no corresponden. */
    fun onTypeChange(type: PlaceType) {
        if (type == _uiState.value.type) return
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                type = type,
                isSearching = false,
                results = emptyList(),
                hasSearched = false,
                showResults = false,
                error = null,
                selected = null,
                pinLatitude = null,
                pinLongitude = null,
                focus = null
            )
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    /** La búsqueda es manual (botón o Enter): el servicio limita a 1 consulta por segundo. */
    fun onSearch() {
        val state = _uiState.value
        if (!state.canSearch) return

        searchJob?.cancel()
        _uiState.update { it.copy(isSearching = true, showResults = true, error = null) }

        searchJob = viewModelScope.launch {
            when (val result = searchPlacesUseCase(state.query, state.type)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSearching = false, results = result.data, hasSearched = true)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isSearching = false,
                        results = emptyList(),
                        hasSearched = true,
                        error = result.error
                    )
                }
            }
        }
    }

    fun onPlaceSelected(place: Place) {
        focusCounter++
        _uiState.update {
            it.copy(
                selected = place,
                showResults = false,
                pinLatitude = place.latitude,
                pinLongitude = place.longitude,
                focus = MapFocus(
                    latitude = place.latitude,
                    longitude = place.longitude,
                    zoom = zoomFor(place.type),
                    token = focusCounter
                )
            )
        }
    }

    /** El mapa avisa cada vez que se mueve. Solo importa si ya hay un lugar elegido. */
    fun onPinMoved(latitude: Double, longitude: Double) {
        _uiState.update {
            if (it.selected == null) it else it.copy(pinLatitude = latitude, pinLongitude = longitude)
        }
    }

    fun onShowMap() {
        _uiState.update { it.copy(showResults = false) }
    }

    /**
     * Se llama al confirmar o cancelar, para que la próxima apertura del selector arranque limpia.
     * La ubicación del usuario se conserva: no es parte de lo que se estaba eligiendo.
     */
    fun reset() {
        searchJob?.cancel()
        _uiState.update { PlacePickerUiState(userLocation = it.userLocation) }
    }
}
package com.app.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.core.domain.AppResult
import com.app.core.domain.GeoLocation
import com.app.core.domain.RecipeOrigin
import com.app.core.domain.UserLocation
import com.app.core.domain.usecase.GetCurrentLocationUseCase
import com.app.core.domain.usecase.GetOriginsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getOriginsUseCase: GetOriginsUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var fitCounter = 0L

    /**
     * Se llama cada vez que la pestaña aparece. El spinner solo se muestra si todavía no hay
     * orígenes; y el mapa solo se vuelve a encuadrar si cambió qué hay para mostrar (una receta
     * nueva, por ejemplo), para no mover el mapa mientras el usuario lo explora.
     */
    fun load() {
        _uiState.update { it.copy(isLoading = it.origins.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = getOriginsUseCase()) {
                is AppResult.Success -> _uiState.update { state ->
                    val changed = result.data.signature() != state.origins.signature()
                    // Si el país filtrado ya no existe (se eliminó su única receta), se vuelve a "Todos".
                    val country = state.selectedCountry?.takeIf { c -> result.data.any { it.country == c } }
                    val next = state.copy(
                        origins = result.data,
                        selectedCountry = country,
                        isLoading = false
                    )
                    if (changed || state.fit == null) {
                        next.copy(fit = fitRequest(next.visibleOrigins, next.userLocation.takeIf { country == null }))
                    } else {
                        next
                    }
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    // --- Filtros y selección ---

    /** null = "Todos". */
    fun onCountrySelected(country: String?) {
        _uiState.update { state ->
            val next = state.copy(selectedCountry = country, selectedOriginKey = null)
            // El encuadre de un país no incluye la ubicación del usuario: sería un mapa enorme.
            next.copy(fit = fitRequest(next.visibleOrigins, if (country == null) next.userLocation else null))
        }
    }

    /** Desde la lista "Otras cocinas exploradas": el mapa vuela hasta ese origen. */
    fun onOriginSelected(key: String) = focusOrigin(key, zoom = 6.0, keepZoom = false)

    /** Desde un pin del mapa: se selecciona y se centra, sin cambiar el zoom que el usuario eligió. */
    fun onOriginTapped(key: String) = focusOrigin(key, zoom = null, keepZoom = true)

    private fun focusOrigin(key: String, zoom: Double?, keepZoom: Boolean) {
        _uiState.update { state ->
            val origin = state.origins.firstOrNull { it.key == key } ?: return@update state
            state.copy(
                selectedOriginKey = key,
                fit = FitRequest(
                    token = ++fitCounter,
                    points = listOf(GeoLocation(origin.latitude, origin.longitude)),
                    zoom = zoom,
                    keepZoom = keepZoom
                )
            )
        }
    }

    // --- Ubicación del usuario ---

    /** Centra el mapa en la ubicación del usuario (si ya la tenemos). */
    fun onLocate() {
        _uiState.update { state ->
            val user = state.userLocation ?: return@update state
            state.copy(
                fit = FitRequest(
                    token = ++fitCounter,
                    points = listOf(GeoLocation(user.latitude, user.longitude)),
                    zoom = 9.0
                )
            )
        }
    }

    /** Al aparecer la pestaña: si ya hay ubicación (o se está buscando), no se vuelve a pedir. */
    fun ensureLocation() {
        val status = _uiState.value.locationStatus
        if (status == LocationStatus.READY || status == LocationStatus.LOADING) return
        refreshLocation()
    }

    fun refreshLocation() {
        if (_uiState.value.locationStatus == LocationStatus.LOADING) return

        // Se conserva la ubicación anterior mientras se busca la nueva: el pin no desaparece.
        _uiState.update { it.copy(locationStatus = LocationStatus.LOADING, locationError = null) }
        viewModelScope.launch {
            when (val result = getCurrentLocationUseCase()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(userLocation = result.data, locationStatus = LocationStatus.READY)
                }
                is AppResult.Error -> _uiState.update {
                    it.copy(locationStatus = LocationStatus.FAILED, locationError = result.error)
                }
            }
        }
    }

    fun onPermissionNeeded() {
        _uiState.update {
            if (it.locationStatus == LocationStatus.READY) it
            else it.copy(locationStatus = LocationStatus.PERMISSION_NEEDED)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) refreshLocation() else onPermissionNeeded()
    }

    // --- Helpers ---

    private fun fitRequest(origins: List<RecipeOrigin>, user: UserLocation?): FitRequest? {
        val points = origins.map { GeoLocation(it.latitude, it.longitude) } +
                listOfNotNull(user?.let { GeoLocation(it.latitude, it.longitude) })
        if (points.isEmpty()) return null
        return FitRequest(token = ++fitCounter, points = points)
    }

    /** Huella de qué hay en el atlas: sirve para saber si hay que volver a encuadrar el mapa. */
    private fun List<RecipeOrigin>.signature(): List<String> =
        map { "${it.key}:${it.recipes.size}" }
}
package com.app.features.recipes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.core.domain.Place
import com.app.core.domain.PlaceType
import com.app.core.domain.distanceKm
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay

// Punto de referencia PROVISORIO para mostrar la distancia. Se reemplaza por la ubicación real del usuario (RF5).
private const val REFERENCE_NAME = "Buenos Aires"
private const val REFERENCE_LAT = -34.6037
private const val REFERENCE_LON = -58.3816
private const val DEFAULT_ZOOM = 3.0

private val ArgentineLocale: Locale = Locale.forLanguageTag("es-AR")

data class PlacePickerActions(
    val onBack: () -> Unit = {},
    val onTypeChange: (PlaceType) -> Unit = {},
    val onQueryChange: (String) -> Unit = {},
    val onSearch: () -> Unit = {},
    val onPlaceSelected: (Place) -> Unit = {},
    val onPinMoved: (Double, Double) -> Unit = { _, _ -> },
    val onShowMap: () -> Unit = {},
    val onConfirm: () -> Unit = {}
)

@Composable
fun PlacePickerRoute(
    viewModel: PlacePickerViewModel,
    onBack: () -> Unit,
    onConfirm: (place: Place, latitude: Double, longitude: Double) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // El botón "atrás" del sistema cierra el selector y vuelve al formulario.
    BackHandler(onBack = onBack)

    PlacePickerScreen(
        state = state,
        actions = PlacePickerActions(
            onBack = onBack,
            onTypeChange = viewModel::onTypeChange,
            onQueryChange = viewModel::onQueryChange,
            onSearch = viewModel::onSearch,
            onPlaceSelected = viewModel::onPlaceSelected,
            onPinMoved = viewModel::onPinMoved,
            onShowMap = viewModel::onShowMap,
            onConfirm = {
                val place = state.selected
                val latitude = state.pinLatitude
                val longitude = state.pinLongitude
                if (place != null && latitude != null && longitude != null) {
                    onConfirm(place, latitude, longitude)
                }
            }
        )
    )
}

@Composable
fun PlacePickerScreen(
    state: PlacePickerUiState,
    actions: PlacePickerActions
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PickerHeader(onBack = actions.onBack)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceType.entries.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { actions.onTypeChange(type) },
                        label = { Text(typeTabLabel(type)) }
                    )
                }
            }

            OutlinedTextField(
                value = state.query,
                onValueChange = actions.onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(searchPlaceholder(state.type)) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            actions.onSearch()
                        },
                        enabled = state.canSearch
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        actions.onSearch()
                    }
                ),
                shape = RoundedCornerShape(50)
            )

            if (state.showResults) {
                ResultsPanel(
                    state = state,
                    actions = actions,
                    modifier = Modifier.weight(1f)
                )
            } else {
                MapPanel(
                    state = state,
                    actions = actions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PickerHeader(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Column {
                Text(
                    text = "EL LUGAR DETRÁS DE LA RECETA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Elegir origen",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Text(
            text = "Elegí un país, una ciudad o el restaurante donde nació tu inspiración.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------- Resultados

@Composable
private fun ResultsPanel(
    state: PlacePickerUiState,
    actions: PlacePickerActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.selected != null) {
            TextButton(onClick = actions.onShowMap) {
                Text("Volver al mapa")
            }
        }

        when {
            state.isSearching -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator()
            }

            state.error != null -> Text(
                text = state.error.asMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            state.hasSearched && state.results.isEmpty() -> Text(
                text = "No encontramos lugares con ese nombre. Probá con otra búsqueda o cambiá de pestaña.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.results) { place ->
                    PlaceRow(place = place, onClick = { actions.onPlaceSelected(place) })
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(
    place: Place,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = placeTitle(place),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = placeDetail(place),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------- Mapa

@Composable
private fun MapPanel(
    state: PlacePickerUiState,
    actions: PlacePickerActions,
    modifier: Modifier = Modifier
) {
    val place = state.selected
    val pinLatitude = state.pinLatitude
    val pinLongitude = state.pinLongitude

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            OsmMap(
                initialCenter = if (pinLatitude != null && pinLongitude != null) {
                    GeoPoint(pinLatitude, pinLongitude)
                } else {
                    GeoPoint(REFERENCE_LAT, REFERENCE_LON)
                },
                initialZoom = if (place != null) zoomFor(place.type) else DEFAULT_ZOOM,
                focus = state.focus,
                onCenterChanged = actions.onPinMoved,
                modifier = Modifier.fillMaxSize()
            )

            // Pin fijo al centro: el que se mueve es el mapa. El offset sube el ícono
            // la mitad de su alto para que la punta quede justo en el centro.
            if (place != null) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Pin del origen",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .offset(y = (-22).dp)
                )
            }

            // La licencia de OpenStreetMap exige mostrar esta atribución sobre el mapa.
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "© colaboradores de OpenStreetMap",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (place == null || pinLatitude == null || pinLongitude == null) {
            Text(
                text = "Buscá un lugar para ubicarlo en el mapa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            SelectedPlaceInfo(
                place = place,
                latitude = pinLatitude,
                longitude = pinLongitude
            )
        }

        Button(
            onClick = actions.onConfirm,
            enabled = place != null && pinLatitude != null && pinLongitude != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = if (place != null) "Usar ${place.name} como origen" else "Elegí un lugar",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SelectedPlaceInfo(
    place: Place,
    latitude: Double,
    longitude: Double
) {
    val distance = distanceKm(REFERENCE_LAT, REFERENCE_LON, latitude, longitude)

    // Scroll propio y alto acotado: así, en pantallas chicas, el mapa no desaparece.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = placeTitle(place),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = placeDetail(place),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatCoordinate(latitude, "N", "S")} · ${formatCoordinate(longitude, "E", "W")}",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "Desde $REFERENCE_NAME ${formatDistance(distance)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Distancia geográfica en línea recta, no distancia de viaje.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Tu ubicación no define el origen",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$REFERENCE_NAME es tu punto de referencia actual. Podés elegir una cocina de cualquier lugar del mundo.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = "Mové el mapa para ajustar el pin. También podés mantener presionado un punto.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Mapa de OpenStreetMap (osmdroid). osmdroid es una vista clásica de Android, no de Compose,
 * así que se usa dentro de AndroidView y hay que avisarle a mano el ciclo de vida.
 */
@Composable
private fun OsmMap(
    initialCenter: GeoPoint,
    initialZoom: Double,
    focus: MapFocus?,
    onCenterChanged: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // El mapa vive más que una recomposición: este State siempre apunta al callback más nuevo.
    val currentOnCenterChanged by rememberUpdatedState(onCenterChanged)
    // Última orden de "ir a" ya ejecutada: evita repetirla al rotar la pantalla.
    var handledFocusToken by rememberSaveable { mutableStateOf(-1L) }

    val mapView = remember {
        // osmdroid exige identificarse con un User-Agent, y por defecto cachea las
        // teselas en almacenamiento externo: lo movemos a la caché interna de la app.
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            minZoomLevel = 2.0
            maxZoomLevel = 19.0
            controller.setZoom(initialZoom)
            controller.setCenter(initialCenter)

            // Mantener presionado un punto centra el mapa ahí (y con él, el pin).
            overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false

                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        if (p == null) return false
                        controller.animateTo(p)
                        return true
                    }
                })
            )

            // Se agrega al final: así no se dispara por la configuración inicial.
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    event?.source?.mapCenter?.let { currentOnCenterChanged(it.latitude, it.longitude) }
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    event?.source?.mapCenter?.let { currentOnCenterChanged(it.latitude, it.longitude) }
                    return false
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            // Libera hilos y cachés del mapa
            mapView.onDetach()
        }
    }

    LaunchedEffect(focus?.token) {
        if (focus != null && focus.token != handledFocusToken) {
            handledFocusToken = focus.token
            mapView.controller.animateTo(GeoPoint(focus.latitude, focus.longitude), focus.zoom, 800L)
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

// ------------------------------------------------------------------- Textos

private fun typeTabLabel(type: PlaceType): String = when (type) {
    PlaceType.COUNTRY -> "País"
    PlaceType.CITY -> "Ciudad"
    PlaceType.RESTAURANT -> "Restaurante"
}

private fun searchPlaceholder(type: PlaceType): String = when (type) {
    PlaceType.COUNTRY -> "Buscar un país"
    PlaceType.CITY -> "Buscar una ciudad"
    PlaceType.RESTAURANT -> "Buscar un restaurante"
}

private fun placeTitle(place: Place): String = when (place.type) {
    PlaceType.CITY -> "${place.name}, ${place.country}"
    PlaceType.COUNTRY, PlaceType.RESTAURANT -> place.name
}

/** "Ciudad · Campania, Italia", "Restaurante · Nápoles, Campania, Italia" o solo "País". */
private fun placeDetail(place: Place): String {
    val area = when (place.type) {
        PlaceType.COUNTRY -> emptyList()
        PlaceType.CITY -> listOf(place.region, place.country)
        PlaceType.RESTAURANT -> listOf(place.city, place.region, place.country)
    }.filter { it.isNotBlank() }.joinToString(", ")

    val label = when (place.type) {
        PlaceType.COUNTRY -> "País"
        PlaceType.CITY -> "Ciudad"
        PlaceType.RESTAURANT -> "Restaurante"
    }
    return if (area.isEmpty()) label else "$label · $area"
}

/** 40,8518° N: cuatro decimales, coma decimal y hemisferio en lugar del signo. */
private fun formatCoordinate(value: Double, positive: String, negative: String): String {
    val hemisphere = if (value >= 0) positive else negative
    return String.format(ArgentineLocale, "%.4f° %s", abs(value), hemisphere)
}

/** Redondea para que no parezca más preciso de lo que es: "≈ 11.200 km". */
private fun formatDistance(km: Double): String {
    if (km < 10) return "< 10 km"
    val rounded = if (km >= 1000) (km / 100).roundToInt() * 100 else (km / 10).roundToInt() * 10
    return "≈ ${NumberFormat.getIntegerInstance(ArgentineLocale).format(rounded)} km"
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun PlacePickerResultsPreview() {
    RecetarioTheme {
        PlacePickerScreen(
            state = PlacePickerUiState(
                type = PlaceType.CITY,
                query = "Nápoles",
                showResults = true,
                hasSearched = true,
                results = listOf(
                    Place("Nápoles", "Nápoles", "Campania", "Italia", 40.8518, 14.2681, PlaceType.CITY),
                    Place("Naples", "Naples", "Florida", "Estados Unidos", 26.1420, -81.7948, PlaceType.CITY)
                )
            ),
            actions = PlacePickerActions()
        )
    }
}
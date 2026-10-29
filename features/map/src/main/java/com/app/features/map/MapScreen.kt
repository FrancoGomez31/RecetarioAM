package com.app.features.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.app.core.domain.RecipeOrigin
import com.app.core.domain.UserLocation
import com.app.core.domain.distanceKm
import com.app.core.ui.LeafGreen
import com.app.core.ui.R
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage
import com.app.core.ui.formatDistance
import kotlinx.coroutines.launch

data class MapActions(
    val onRequestLocation: () -> Unit = {},
    val onCountrySelected: (String?) -> Unit = {},
    val onOriginSelected: (String) -> Unit = {},
    val onOriginTapped: (String) -> Unit = {},
    val onLocate: () -> Unit = {},
    val onRecipeClick: (String) -> Unit = {},
    val onRetry: () -> Unit = {}
)

@Composable
fun MapRoute(
    viewModel: MapViewModel,
    onRecipeClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Hoisted: así la posición del scroll se conserva al volver desde el detalle de una receta.
    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Con la ubicación precisa o la aproximada alcanza para ubicar una ciudad.
        viewModel.onPermissionResult(granted = results.values.any { it })
    }

    fun requestLocation() {
        if (hasLocationPermission(context)) {
            viewModel.refreshLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Cada vez que la pestaña aparece: recarga los orígenes y, si ya hay permiso, busca la ubicación.
    LaunchedEffect(Unit) {
        viewModel.load()
        if (hasLocationPermission(context)) viewModel.ensureLocation() else viewModel.onPermissionNeeded()
    }

    MapScreen(
        state = state,
        scrollState = scrollState,
        actions = MapActions(
            onRequestLocation = ::requestLocation,
            onCountrySelected = viewModel::onCountrySelected,
            onOriginSelected = viewModel::onOriginSelected,
            onOriginTapped = viewModel::onOriginTapped,
            // Si todavía no hay ubicación, el botón de centrar la pide en vez de no hacer nada.
            onLocate = { if (state.userLocation != null) viewModel.onLocate() else requestLocation() },
            onRecipeClick = onRecipeClick,
            onRetry = viewModel::load
        )
    )
}

private fun hasLocationPermission(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

@Composable
fun MapScreen(
    state: MapUiState,
    scrollState: ScrollState,
    actions: MapActions
) {
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AtlasHeader(state)
            LocationCard(state = state, onAction = actions.onRequestLocation)

            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                state.error != null && state.origins.isEmpty() -> Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.error.asMessage(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(onClick = actions.onRetry) { Text(stringResource(R.string.map_loc_retry)) }
                }

                state.origins.isEmpty() -> EmptyAtlas()

                else -> AtlasContent(
                    state = state,
                    actions = actions,
                    onOtherOriginClick = { key ->
                        actions.onOriginSelected(key)
                        // La tarjeta del origen elegido está arriba: se sube para que el cambio se vea.
                        scope.launch { scrollState.animateScrollTo(0) }
                    }
                )
            }

            InfoCard()
        }
    }
}

@Composable
private fun AtlasHeader(state: MapUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (state.origins.isNotEmpty()) {
            val countries = if (state.countryCount == 1) stringResource(R.string.map_country_count_one) else stringResource(R.string.map_country_count_many, state.countryCount)
            val origins = if (state.originCount == 1) stringResource(R.string.map_origin_count_one) else stringResource(R.string.map_origin_count_many, state.originCount)
            Text(
                text = "$countries / $origins",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(text = stringResource(R.string.map_title), style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun LocationCard(
    state: MapUiState,
    onAction: () -> Unit
) {
    val user = state.userLocation
    val loading = state.locationStatus == LocationStatus.LOADING ||
            state.locationStatus == LocationStatus.IDLE

    // Qué decir y qué ofrecer según el estado de la ubicación
    val title: String
    val subtitle: String
    val actionLabel: String?
    when {
        state.locationStatus == LocationStatus.PERMISSION_NEEDED && user == null -> {
            title = stringResource(R.string.map_loc_enable_title)
            subtitle = stringResource(R.string.map_loc_enable_desc)
            actionLabel = stringResource(R.string.map_loc_allow)
        }

        state.locationStatus == LocationStatus.FAILED && user == null -> {
            title = stringResource(R.string.map_loc_failed_title)
            subtitle = state.locationError?.asMessageText() ?: ""
            actionLabel = stringResource(R.string.map_loc_retry)
        }

        user != null -> {
            title = user.label.ifBlank { stringResource(R.string.map_legend_user) }
            subtitle = if (loading) stringResource(R.string.map_loc_updating) else stringResource(R.string.account_permission_location)
            actionLabel = if (loading) null else "OK"
        }

        else -> {
            title = stringResource(R.string.map_loc_searching)
            subtitle = ""
            actionLabel = null
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionLabel != null) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            } else if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun AtlasContent(
    state: MapUiState,
    actions: MapActions,
    onOtherOriginClick: (String) -> Unit
) {
    val user = state.userLocation
    val selected = state.selectedOrigin

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Chips por país: "Todos" y uno por cada país con su cantidad de recetas
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedCountry == null,
                onClick = { actions.onCountrySelected(null) },
                label = { Text(stringResource(R.string.map_all)) }
            )
            state.countries.forEach { chip ->
                FilterChip(
                    selected = state.selectedCountry == chip.country,
                    onClick = { actions.onCountrySelected(chip.country) },
                    label = { Text("${chip.country} · ${chip.recipeCount}") }
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AtlasMap(
                origins = state.visibleOrigins,
                selectedKey = selected?.key,
                user = user,
                fit = state.fit,
                onOriginTapped = actions.onOriginTapped,
                onLocate = actions.onLocate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Legend()
        }

        if (selected != null) {
            SelectedOriginCard(
                origin = selected,
                distanceKm = user?.let { distanceTo(it, selected) },
                onRecipeClick = actions.onRecipeClick
            )
        }

        if (state.otherOrigins.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = stringResource(R.string.map_other_origins), style = MaterialTheme.typography.headlineSmall)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        state.otherOrigins.forEachIndexed { index, origin ->
                            if (index > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            OtherOriginRow(
                                origin = origin,
                                distanceKm = user?.let { distanceTo(it, origin) },
                                onClick = { onOtherOriginClick(origin.key) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = MaterialTheme.colorScheme.primary, text = stringResource(R.string.map_legend_origin))
        LegendItem(color = LeafGreen, text = stringResource(R.string.map_legend_user))
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectedOriginCard(
    origin: RecipeOrigin,
    distanceKm: Double?,
    onRecipeClick: (String) -> Unit
) {
    // Las recetas vienen ordenadas: primero la que más intentos tiene
    val featured = origin.recipes.first()
    val others = origin.recipes.drop(1)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = origin.label, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = stringResource(R.string.map_collection_count, recipesText(origin.recipes.size)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                onClick = { onRecipeClick(featured.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = featured.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${featured.prepTimeMinutes} min · ${attemptsText(featured.attempts)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.map_view_recipe),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (others.isNotEmpty()) {
                Text(
                    text = "${stringResource(R.string.explore_card_servings).let { "También: " }}${joinNames(others.map { it.name })}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (distanceKm != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.map_from_user_loc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatDistance(distanceKm),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OtherOriginRow(
    origin: RecipeOrigin,
    distanceKm: Double?,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = origin.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = listOfNotNull(
                        recipesText(origin.recipes.size),
                        distanceKm?.let { formatDistance(it) }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyAtlas() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = stringResource(R.string.map_empty_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.map_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LeafGreen.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = LeafGreen
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.map_info_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.map_info_desc),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ------------------------------------------------------------------- Textos

private fun distanceTo(user: UserLocation, origin: RecipeOrigin): Double =
    distanceKm(user.latitude, user.longitude, origin.latitude, origin.longitude)

private fun recipesText(count: Int): String = if (count == 1) "1 receta" else "$count recetas"

@Composable
private fun attemptsText(attempts: Int): String = when (attempts) {
    0 -> stringResource(R.string.detail_attempts_zero)
    1 -> stringResource(R.string.detail_attempts_one)
    else -> stringResource(R.string.detail_attempts_many, attempts)
}

/** ["A"] → "A"; ["A", "B"] → "A y B"; ["A", "B", "C"] → "A, B y C". */
private fun joinNames(names: List<String>): String = when (names.size) {
    0 -> ""
    1 -> names.first()
    else -> names.dropLast(1).joinToString(", ") + " y " + names.last()
}

/** El texto de un error del dominio fuera de un @Composable (para armar el subtítulo de la tarjeta). */
private fun com.app.core.domain.DomainError.asMessageText(): String? =
    (this as? com.app.core.domain.DomainError.Unknown)?.message

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MapScreenPreview() {
    // Sin orígenes a propósito: el mapa (AndroidView) no se puede mostrar en el preview.
    RecetarioTheme {
        MapScreen(
            state = MapUiState(
                isLoading = false,
                userLocation = UserLocation(-34.6037, -58.3816, "Buenos Aires", "Argentina"),
                locationStatus = LocationStatus.READY
            ),
            scrollState = rememberScrollState(),
            actions = MapActions()
        )
    }
}
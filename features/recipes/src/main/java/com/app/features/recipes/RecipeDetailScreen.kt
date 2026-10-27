package com.app.features.recipes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.core.domain.Attempt
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility
import com.app.core.domain.distanceKm
import com.app.core.domain.originLabel
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage
import java.io.File
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

// Punto de referencia PROVISORIO para la distancia (igual que en el selector de origen).
// Se reemplaza por la ubicación real del usuario (RF5).
private const val REFERENCE_NAME = "Buenos Aires"
private const val REFERENCE_LAT = -34.6037
private const val REFERENCE_LON = -58.3816

private val ArgentineLocale: Locale = Locale.forLanguageTag("es-AR")
private val DayFormatter = DateTimeFormatter.ofPattern("d MMM", ArgentineLocale)

data class RecipeDetailActions(
    val onBack: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onDeleteClick: () -> Unit = {},
    val onRegisterAttempt: () -> Unit = {},
    val onToggleReminder: (Long?) -> Unit = {}
)

@Composable
fun RecipeDetailRoute(
    viewModel: RecipeDetailViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onRegisterAttempt: (String) -> Unit,
    onDeleted: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Que el diálogo esté abierto es puro estado de UI; rememberSaveable lo conserva al rotar.
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }

    RecipeDetailScreen(
        state = state,
        actions = RecipeDetailActions(
            onBack = onBack,
            onEdit = { state.recipe?.let { onEdit(it.id) } },
            onDeleteClick = { showDeleteDialog = true },
            onRegisterAttempt = { state.recipe?.let { onRegisterAttempt(it.id) } },
            onToggleReminder = viewModel::onToggleReminder
        )
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar receta?") },
            text = {
                Text("Vas a eliminar “${state.recipe?.name.orEmpty()}”. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onDelete()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RecipeDetailScreen(
    state: RecipeDetailUiState,
    actions: RecipeDetailActions
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val recipe = state.recipe
        when {
            recipe != null -> RecipeDetailContent(recipe = recipe, state = state, actions = actions)

            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.error?.asMessage() ?: "No pudimos cargar la receta.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = actions.onBack) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    state: RecipeDetailUiState,
    actions: RecipeDetailActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DetailTopBar(recipe = recipe, enabled = !state.isDeleting, actions = actions)

        // Error al eliminar (si falla, la receta sigue acá y se muestra el motivo)
        state.error?.let {
            Text(
                text = it.asMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Portada provisoria: se reemplaza cuando carguemos fotos de portada.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(
                    text = if (recipe.visibility == Visibility.PUBLIC) "Pública" else "Privada",
                    container = MaterialTheme.colorScheme.secondaryContainer
                )
                Pill(text = "Receta propia", container = MaterialTheme.colorScheme.primaryContainer)
            }
            Text(text = recipe.name, style = MaterialTheme.typography.headlineMedium)
            if (recipe.description.isNotBlank()) {
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(text = "${recipe.prepTimeMinutes} min", container = MaterialTheme.colorScheme.surface, bordered = true)
                Pill(text = servingsText(recipe.servings), container = MaterialTheme.colorScheme.surface, bordered = true)
            }
        }

        IngredientsBlock(recipe = recipe)
        StepsBlock(recipe = recipe)
        OriginBlock(recipe = recipe)
        ReminderBlock(recipe = recipe, onToggleReminder = actions.onToggleReminder)
        ProgressBlock(recipe = recipe, attempts = state.attempts, onRegisterAttempt = actions.onRegisterAttempt)
    }
}

@Composable
private fun DetailTopBar(
    recipe: Recipe,
    enabled: Boolean,
    actions: RecipeDetailActions
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = actions.onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }
        Text(
            text = listOf(recipe.country, recipe.city)
                .filter { it.isNotBlank() }
                .joinToString(" / ")
                .uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = actions.onEdit, enabled = enabled) {
            Icon(Icons.Outlined.Edit, contentDescription = "Editar receta")
        }
        IconButton(onClick = actions.onDeleteClick, enabled = enabled) {
            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar receta")
        }
    }
}

@Composable
private fun IngredientsBlock(recipe: Recipe) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Ingredientes", trailing = servingsText(recipe.servings))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                recipe.ingredients.forEachIndexed { index, ingredient ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ingredient.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ingredient.quantity,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsBlock(recipe: Recipe) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "Paso a paso",
            trailing = if (recipe.steps.size == 1) "1 paso" else "${recipe.steps.size} pasos"
        )

        recipe.steps.forEachIndexed { index, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun OriginBlock(recipe: Recipe) {
    val latitude = recipe.latitude
    val longitude = recipe.longitude

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "El origen de este sabor", style = MaterialTheme.typography.headlineSmall)

        // Las recetas sin coordenadas (por ejemplo, las de prueba) muestran solo el texto.
        if (latitude != null && longitude != null) {
            OriginMap(
                latitude = latitude,
                longitude = longitude,
                zoom = originZoom(recipe),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }

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
                if (latitude != null && longitude != null) {
                    val distance = distanceKm(REFERENCE_LAT, REFERENCE_LON, latitude, longitude)
                    Text(
                        text = "${recipe.originLabel}  ${formatDistance(distance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Origen a nivel de ${originLevelLabel(recipe)} desde $REFERENCE_NAME",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Distancia geográfica en línea recta; no indica una ruta ni un desplazamiento real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = recipe.originLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Origen a nivel de ${originLevelLabel(recipe)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderBlock(
    recipe: Recipe,
    onToggleReminder: (Long?) -> Unit
) {
    val isReminderSet = recipe.reminderEpochMillis != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isReminderSet) Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = if (isReminderSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        text = "Recordatorio para cocinar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isReminderSet) {
                            "Aviso programado para cocinar esta receta"
                        } else {
                            "Sin programar (avisarme en 10 segundos para probar)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isReminderSet,
                onCheckedChange = { checked ->
                    if (checked) {
                        // Para prueba rápida y funcional: dispara a los 10 segundos
                        val triggerTime = System.currentTimeMillis() + 10_000L
                        onToggleReminder(triggerTime)
                    } else {
                        onToggleReminder(null)
                    }
                }
            )
        }
    }
}

@Composable
private fun ProgressBlock(
    recipe: Recipe,
    attempts: List<Attempt>,
    onRegisterAttempt: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Mi progreso", style = MaterialTheme.typography.headlineSmall)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = when (recipe.attempts) {
                        0 -> "Todavía no cocinaste esta receta."
                        1 -> "Cocinaste esta receta 1 vez."
                        else -> "Cocinaste esta receta ${recipe.attempts} veces."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                attempts.forEach { attempt -> AttemptRow(attempt) }

                Button(
                    onClick = onRegisterAttempt,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Registrar intento")
                }
            }
        }
    }
}

@Composable
private fun AttemptRow(attempt: Attempt) {
    val cover = attempt.photos.firstOrNull()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (cover != null) {
                AsyncImage(
                    model = cover.uri,
                    contentDescription = "Foto del intento ${attempt.number}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Intento ${attempt.number} · ${formatDay(attempt.createdAtMillis)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = when (attempt.photos.size) {
                    0 -> "Sin fotos"
                    1 -> "1 foto"
                    else -> "${attempt.photos.size} fotos"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (attempt.note.isNotBlank()) {
                Text(
                    text = attempt.note,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = trailing,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Pill(
    text: String,
    container: androidx.compose.ui.graphics.Color,
    bordered: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = container,
        border = if (bordered) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Mapa de solo lectura con el pin en el origen. Una capa transparente encima absorbe los
 * toques: así el mapa queda fijo y los gestos de arrastre siguen moviendo el scroll de la pantalla.
 */
@Composable
private fun OriginMap(
    latitude: Double,
    longitude: Double,
    zoom: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember(latitude, longitude, zoom) {
        // Misma configuración que el selector: User-Agent propio y caché en almacenamiento interno.
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitude, longitude))
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
            mapView.onDetach()
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        // Absorbe los toques para que el mapa no se mueva ni compita con el scroll.
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) { detectTapGestures { } }
        )

        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Origen de la receta",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(44.dp)
                .offset(y = (-22).dp)
        )

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
}

// ------------------------------------------------------------------- Textos

private fun servingsText(servings: Int): String =
    if (servings == 1) "1 porción" else "$servings porciones"

private fun originLevelLabel(recipe: Recipe): String = when {
    recipe.restaurant.isNotBlank() -> "restaurante"
    recipe.city.isNotBlank() -> "ciudad"
    else -> "país"
}

/** Cuánto se acerca el mapa según el nivel del origen. */
private fun originZoom(recipe: Recipe): Double = when {
    recipe.restaurant.isNotBlank() -> 15.0
    recipe.city.isNotBlank() -> 11.0
    else -> 5.0
}

/** Redondea para que no parezca más preciso de lo que es: "≈ 11.200 km". */
private fun formatDistance(km: Double): String {
    if (km < 10) return "< 10 km"
    val rounded = if (km >= 1000) (km / 100).roundToInt() * 100 else (km / 10).roundToInt() * 10
    return "≈ ${NumberFormat.getIntegerInstance(ArgentineLocale).format(rounded)} km"
}

/** "28 sep": día y mes abreviado, sin el punto que agrega el formato de la región. */
private fun formatDay(millis: Long): String =
    DayFormatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())).replace(".", "")

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun RecipeDetailScreenPreview() {
    RecetarioTheme {
        RecipeDetailScreen(
            state = RecipeDetailUiState(
                isLoading = false,
                // Sin coordenadas a propósito: el mapa (AndroidView) no se puede mostrar en el preview.
                recipe = Recipe(
                    id = "1",
                    name = "Ñoquis al pomodoro",
                    photoUrl = null,
                    city = "Nápoles",
                    country = "Italia",
                    prepTimeMinutes = 35,
                    attempts = 2,
                    visibility = Visibility.PUBLIC,
                    description = "Un clásico napolitano para cocinar despacio y compartir en casa.",
                    servings = 4,
                    ingredients = listOf(
                        Ingredient("1 kg", "Papas"),
                        Ingredient("250 g", "Harina 0000"),
                        Ingredient("1", "Huevo")
                    ),
                    steps = listOf(
                        "Herví las papas con piel 20 min. Pelá y pisá mientras estén tibias.",
                        "Sumá huevo y harina. Uní sin amasar de más, cortá y marcá con tenedor."
                    )
                ),
                attempts = listOf(
                    Attempt("a1", "1", 1, 1790000000000, "Masa más firme"),
                    Attempt("a2", "1", 2, 1790600000000, "Menos harina, mejor textura")
                )
            ),
            actions = RecipeDetailActions()
        )
    }
}
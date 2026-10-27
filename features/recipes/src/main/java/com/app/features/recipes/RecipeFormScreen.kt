package com.app.features.recipes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.core.domain.RecipeField
import com.app.core.domain.Visibility
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage

/** Todo lo que la pantalla puede pedir. Agruparlo evita una firma de 15 parámetros y facilita los previews. */
data class RecipeFormActions(
    val onBack: () -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onDescriptionChange: (String) -> Unit = {},
    val onServingsChange: (String) -> Unit = {},
    val onPrepTimeChange: (String) -> Unit = {},
    val onTakeCover: () -> Unit = {},
    val onPickCoverFromGallery: () -> Unit = {},
    val onRemoveCover: () -> Unit = {},
    val onOriginClick: () -> Unit = {},
    val onVisibilityChange: (Visibility) -> Unit = {},
    val onIngredientQuantityChange: (Long, String) -> Unit = { _, _ -> },
    val onIngredientNameChange: (Long, String) -> Unit = { _, _ -> },
    val onAddIngredient: () -> Unit = {},
    val onRemoveIngredient: (Long) -> Unit = {},
    val onStepChange: (Long, String) -> Unit = { _, _ -> },
    val onAddStep: () -> Unit = {},
    val onRemoveStep: (Long) -> Unit = {},
    val onSave: () -> Unit = {}
)

@Composable
fun RecipeFormRoute(
    viewModel: RecipeFormViewModel,
    placePickerViewModel: PlacePickerViewModel,
    recipeId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // El selector de origen es un paso del formulario, no un destino aparte: se muestra EN LUGAR
    // del formulario, y así su resultado vuelve directo al ViewModel sin pasar datos entre pantallas.
    var showOriginPicker by rememberSaveable { mutableStateOf(false) }

    // El scroll se levanta acá: mientras el selector está abierto el formulario sale de la
    // composición, y sin esto volvería siempre arriba de todo.
    val scrollState = rememberScrollState()

    // Cámara y galería de la portada. Se registran acá (y no dentro del formulario) para que
    // sigan activos mientras el selector de origen tapa la pantalla.
    val photoPicker = rememberPhotoPicker(
        onPhotoSelected = viewModel::onCoverSelected,
        onUnavailable = viewModel::onCoverUnavailable
    )

    // El ViewModel ignora llamadas repetidas, así que una rotación no recarga ni pisa lo escrito.
    LaunchedEffect(recipeId) { viewModel.start(recipeId) }
    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    if (showOriginPicker) {
        PlacePickerRoute(
            viewModel = placePickerViewModel,
            onBack = {
                placePickerViewModel.reset()
                showOriginPicker = false
            },
            onConfirm = { place, latitude, longitude ->
                viewModel.onOriginSelected(place, latitude, longitude)
                placePickerViewModel.reset()
                showOriginPicker = false
            }
        )
    } else {
        RecipeFormScreen(
            state = state,
            scrollState = scrollState,
            actions = RecipeFormActions(
                onBack = onBack,
                onNameChange = viewModel::onNameChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onServingsChange = viewModel::onServingsChange,
                onPrepTimeChange = viewModel::onPrepTimeChange,
                onTakeCover = photoPicker.takePhoto,
                onPickCoverFromGallery = photoPicker.pickFromGallery,
                onRemoveCover = viewModel::onCoverRemoved,
                onOriginClick = { showOriginPicker = true },
                onVisibilityChange = viewModel::onVisibilityChange,
                onIngredientQuantityChange = viewModel::onIngredientQuantityChange,
                onIngredientNameChange = viewModel::onIngredientNameChange,
                onAddIngredient = viewModel::onAddIngredient,
                onRemoveIngredient = viewModel::onRemoveIngredient,
                onStepChange = viewModel::onStepChange,
                onAddStep = viewModel::onAddStep,
                onRemoveStep = viewModel::onRemoveStep,
                onSave = viewModel::onSave
            )
        )
    }
}

@Composable
fun RecipeFormScreen(
    state: RecipeFormUiState,
    scrollState: ScrollState,
    actions: RecipeFormActions
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormHeader(state = state, onBack = actions.onBack)

                CoverSection(state = state, actions = actions)

                FormField(label = "Nombre de la receta *") {
                    val invalid = RecipeField.NAME in state.invalidFields
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = actions.onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = invalid,
                        supportingText = requiredMessage(invalid, "Ingresá el nombre de la receta"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                FormField(label = "Descripción") {
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = actions.onDescriptionChange,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        placeholder = { Text("Contá de qué se trata esta receta") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormField(label = "Porciones *", modifier = Modifier.weight(1f)) {
                        val invalid = RecipeField.SERVINGS in state.invalidFields
                        OutlinedTextField(
                            value = state.servings,
                            onValueChange = actions.onServingsChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = invalid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            suffix = { Text("personas") },
                            supportingText = requiredMessage(invalid, "Mínimo 1"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    FormField(label = "Tiempo total *", modifier = Modifier.weight(1f)) {
                        val invalid = RecipeField.TIME in state.invalidFields
                        OutlinedTextField(
                            value = state.prepTime,
                            onValueChange = actions.onPrepTimeChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = invalid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            suffix = { Text("min") },
                            supportingText = requiredMessage(invalid, "Indicá el tiempo"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                IngredientsSection(state = state, actions = actions)
                StepsSection(state = state, actions = actions)
                OriginSection(state = state, actions = actions)
                VisibilitySection(state = state, actions = actions)

                state.error?.let { error ->
                    Text(
                        text = error.asMessage(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = actions.onSave,
                    enabled = state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.isEditing) "Guardar cambios" else "Guardar receta")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormHeader(
    state: RecipeFormUiState,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Column {
                Text(
                    text = if (state.isEditing) "CARGA MANUAL / MI RECETA" else "CARGA MANUAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (state.isEditing) "Editar receta" else "Nueva receta",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        Text(
            text = if (state.isEditing) {
                "Tu versión de ${state.originalName}. Los cambios no se publican hasta guardar."
            } else {
                "Tu receta empieza siendo privada."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Portada opcional: la foto de la receta (o un lugar vacío), con cámara, galería y la opción de quitarla. */
@Composable
private fun CoverSection(
    state: RecipeFormUiState,
    actions: RecipeFormActions
) {
    FormField(label = "Portada (opcional)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (state.coverUri != null) {
                    AsyncImage(
                        model = state.coverUri,
                        contentDescription = "Portada de la receta",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = actions.onTakeCover,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(if (state.coverUri == null) "Tomar foto" else "Volver a tomar")
                }
                OutlinedButton(
                    onClick = actions.onPickCoverFromGallery,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Galería")
                }
            }

            if (state.coverUri != null) {
                TextButton(onClick = actions.onRemoveCover) {
                    Text("Quitar portada")
                }
            }

            if (state.coverMessage != null) {
                Text(
                    text = state.coverMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "Elegí una foto de la galería o tomá una con la cámara.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IngredientsSection(
    state: RecipeFormUiState,
    actions: RecipeFormActions
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Ingredientes",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = actions.onAddIngredient) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar")
            }
        }

        if (RecipeField.INGREDIENTS in state.invalidFields) {
            Text(
                text = "Agregá al menos un ingrediente con nombre.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        state.ingredients.forEach { ingredient ->
            // key: cada fila conserva su texto aunque se borre otra de más arriba.
            key(ingredient.id) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ingredient.quantity,
                        onValueChange = { actions.onIngredientQuantityChange(ingredient.id, it) },
                        modifier = Modifier.weight(0.4f),
                        singleLine = true,
                        placeholder = { Text("1 kg") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = ingredient.name,
                        onValueChange = { actions.onIngredientNameChange(ingredient.id, it) },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true,
                        placeholder = { Text("Ingrediente") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(onClick = { actions.onRemoveIngredient(ingredient.id) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Quitar ingrediente")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepsSection(
    state: RecipeFormUiState,
    actions: RecipeFormActions
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Preparación",
            style = MaterialTheme.typography.headlineSmall
        )

        if (RecipeField.STEPS in state.invalidFields) {
            Text(
                text = "Agregá al menos un paso.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        state.steps.forEachIndexed { index, step ->
            key(step.id) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paso ${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { actions.onRemoveStep(step.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Quitar paso ${index + 1}")
                        }
                    }
                    OutlinedTextField(
                        value = step.text,
                        onValueChange = { actions.onStepChange(step.id, it) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        placeholder = { Text("Explicá qué hacer en este paso") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = actions.onAddStep,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar paso")
        }
    }
}

/** Tarjeta que abre el selector con mapa. Muestra el origen elegido, o invita a elegirlo. */
@Composable
private fun OriginSection(
    state: RecipeFormUiState,
    actions: RecipeFormActions
) {
    val invalid = RecipeField.ORIGIN in state.invalidFields

    FormField(label = "Origen *") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                onClick = actions.onOriginClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (invalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (state.hasOrigin) {
                            Text(
                                text = "${state.originLevel} seleccionado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = state.originLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Elegir o cambiar en el mapa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Elegir el origen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Un país, una ciudad o un restaurante",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (invalid) {
                Text(
                    text = "Elegí el origen de la receta.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun VisibilitySection(
    state: RecipeFormUiState,
    actions: RecipeFormActions
) {
    FormField(label = "¿Quién puede verla?") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.visibility == Visibility.PRIVATE,
                    onClick = { actions.onVisibilityChange(Visibility.PRIVATE) },
                    label = { Text("Privada") }
                )
                FilterChip(
                    selected = state.visibility == Visibility.PUBLIC,
                    onClick = { actions.onVisibilityChange(Visibility.PUBLIC) },
                    label = { Text("Pública") }
                )
            }
            Text(
                text = if (state.visibility == Visibility.PUBLIC) {
                    "Pública: la comunidad puede verla y guardar una copia, pero no editar tu original."
                } else {
                    "Privada: solo vos podés verla."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

/** Texto de ayuda bajo un campo: solo existe (y ocupa lugar) cuando el campo tiene error. */
private fun requiredMessage(show: Boolean, message: String): (@Composable () -> Unit)? =
    if (show) ({ Text(message) }) else null

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun RecipeFormScreenPreview() {
    RecetarioTheme {
        RecipeFormScreen(
            state = RecipeFormUiState(
                isEditing = true,
                originalName = "Ñoquis al pomodoro",
                name = "Ñoquis al pomodoro",
                description = "Un clásico napolitano para cocinar despacio y compartir en casa.",
                servings = "4",
                prepTime = "35",
                city = "Nápoles",
                country = "Italia",
                ingredients = listOf(
                    IngredientInput(1, "1 kg", "Papas"),
                    IngredientInput(2, "250 g", "Harina 0000")
                ),
                steps = listOf(
                    StepInput(3, "Herví las papas con piel 20 min. Pelá y pisá mientras estén tibias."),
                    StepInput(4, "Sumá huevo y harina. Uní sin amasar de más.")
                )
            ),
            scrollState = rememberScrollState(),
            actions = RecipeFormActions()
        )
    }
}
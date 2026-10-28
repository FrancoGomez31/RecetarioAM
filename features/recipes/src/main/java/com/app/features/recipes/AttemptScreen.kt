package com.app.features.recipes

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.core.domain.Attempt
import com.app.core.domain.AttemptPhoto
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility
import com.app.core.ui.R
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val ArgentineLocale: Locale = Locale.forLanguageTag("es-AR")
private val DayFormatter = DateTimeFormatter.ofPattern("d MMM", ArgentineLocale)
private val FullFormatter = DateTimeFormatter.ofPattern("d MMM yyyy · H:mm", ArgentineLocale)

data class AttemptActions(
    val onBack: () -> Unit = {},
    val onStepSelected: (Int) -> Unit = {},
    val onTakePhoto: () -> Unit = {},
    val onPickFromGallery: () -> Unit = {},
    val onNoteChange: (String) -> Unit = {},
    val onSave: () -> Unit = {}
)

@Composable
fun AttemptRoute(
    viewModel: AttemptViewModel,
    recipeId: String,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(recipeId) { viewModel.start(recipeId) }

    // Dirección temporal donde la app de cámara deja la foto. Se guarda con rememberSaveable:
    // si el sistema recrea esta pantalla mientras la cámara está abierta, el resultado igual llega.
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = cameraUri
        if (saved && uri != null) viewModel.onPhotoSelected(uri)
        cameraUri = null
    }

    // El selector de fotos del sistema no necesita ningún permiso.
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoSelected(uri.toString())
    }

    fun launchCamera() {
        val folder = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(folder, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri.toString()
        try {
            takePicture.launch(uri)
        } catch (e: ActivityNotFoundException) {
            cameraUri = null
            viewModel.onPhotoUnavailable("No encontramos una app de cámara en este teléfono. Podés elegir una foto de la galería.")
        }
    }

    // Como declaramos el permiso CAMERA en el manifest, Android exige pedirlo antes de abrir la cámara.
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            viewModel.onPhotoUnavailable("Sin permiso de cámara no podemos sacar la foto. Podés elegir una de la galería.")
        }
    }

    AttemptScreen(
        state = state,
        actions = AttemptActions(
            onBack = onBack,
            onStepSelected = viewModel::onStepSelected,
            onTakePhoto = {
                val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                if (granted) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
            },
            onPickFromGallery = {
                pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onNoteChange = viewModel::onNoteChange,
            onSave = viewModel::onSave
        )
    )
}

@Composable
fun AttemptScreen(
    state: AttemptUiState,
    actions: AttemptActions
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val recipe = state.recipe
        when {
            recipe != null -> AttemptContent(recipe = recipe, state = state, actions = actions)

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
                    text = state.error?.asMessage() ?: stringResource(R.string.detail_load_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = actions.onBack) {
                    Text(stringResource(R.string.common_back))
                }
            }
        }
    }
}

@Composable
private fun AttemptContent(
    recipe: Recipe,
    state: AttemptUiState,
    actions: AttemptActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AttemptHeader(recipe = recipe, state = state, onBack = actions.onBack)

        // Las recetas de prueba pueden no tener pasos; las creadas con el formulario siempre tienen al menos uno.
        if (recipe.steps.isNotEmpty()) {
            StepSelector(recipe = recipe, selected = state.selectedStep, onSelected = actions.onStepSelected)
        }

        PhotoReviewBlock(state = state, actions = actions)

        if (recipe.steps.isNotEmpty()) {
            ComparisonBlock(state = state)
        }

        state.currentAttempt?.takeIf { it.photos.isNotEmpty() }?.let { attempt ->
            SavedPhotosBlock(attempt = attempt, onAddPhoto = actions.onTakePhoto)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.attempt_note_title), style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = state.note,
                onValueChange = actions.onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text(stringResource(R.string.attempt_note_placeholder)) },
                shape = RoundedCornerShape(12.dp)
            )
        }

        Text(
            text = stringResource(R.string.attempt_privacy_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        state.error?.let {
            Text(
                text = it.asMessage(),
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
                Text(
                    text = when {
                        state.pendingPhotoUri != null -> stringResource(R.string.attempt_btn_save_photo, state.attemptNumber)
                        state.canSave -> stringResource(R.string.attempt_btn_save_note, state.attemptNumber)
                        else -> stringResource(R.string.attempt_btn_choose_photo)
                    }
                )
            }
        }
    }
}

@Composable
private fun AttemptHeader(
    recipe: Recipe,
    state: AttemptUiState,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
            }
            Column {
                Text(
                    text = recipe.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = stringResource(R.string.attempt_title), style = MaterialTheme.typography.headlineMedium)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(
                text = stringResource(R.string.attempt_today, state.attemptNumber),
                container = MaterialTheme.colorScheme.primaryContainer
            )
            StatusPill(
                text = if (state.currentAttempt == null) stringResource(R.string.attempt_not_saved) else stringResource(R.string.attempt_saved),
                container = MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}

@Composable
private fun StepSelector(
    recipe: Recipe,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(R.string.attempt_step_assoc), style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recipe.steps.indices.forEach { index ->
                FilterChip(
                    selected = selected == index,
                    onClick = { onSelected(index) },
                    label = { Text(stringResource(R.string.attempt_step_label, index + 1)) }
                )
            }
        }

        Text(
            text = "${stringResource(R.string.attempt_step_label, selected + 1)} · ${recipe.steps.getOrElse(selected) { "" }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PhotoReviewBlock(
    state: AttemptUiState,
    actions: AttemptActions
) {
    val pending = state.pendingPhotoUri

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (pending != null) stringResource(R.string.attempt_photo_review) else stringResource(R.string.attempt_photo_step),
            style = MaterialTheme.typography.headlineSmall
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (pending != null) {
                AsyncImage(
                    model = pending,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.attempt_no_photo_chosen),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pending != null) {
                OutlinedButton(
                    onClick = actions.onTakePhoto,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) { Text(stringResource(R.string.attempt_retake_photo)) }
            } else {
                Button(
                    onClick = actions.onTakePhoto,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) { Text(stringResource(R.string.attempt_take_photo)) }
            }
            OutlinedButton(
                onClick = actions.onPickFromGallery,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50)
            ) { Text(stringResource(R.string.attempt_gallery)) }
        }
    }
}

/** El mismo paso en los dos intentos anteriores más recientes, para ver qué cambió. */
@Composable
private fun ComparisonBlock(state: AttemptUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(R.string.attempt_comparison_title), style = MaterialTheme.typography.headlineSmall)

        if (state.previousAttempts.isEmpty()) {
            Text(
                text = stringResource(R.string.attempt_first_attempt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = stringResource(R.string.attempt_comparison_hint, state.selectedStep + 1),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.previousAttempts.takeLast(2).forEach { attempt ->
                    ComparisonCard(
                        attempt = attempt,
                        stepIndex = state.selectedStep,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard(
    attempt: Attempt,
    stepIndex: Int,
    modifier: Modifier = Modifier
) {
    val photo = attempt.photos.firstOrNull { it.stepIndex == stepIndex }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (photo != null) {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = stringResource(R.string.attempt_no_photo_step),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.detail_attempt_number, attempt.number, formatDay(attempt.createdAtMillis)),
            style = MaterialTheme.typography.labelLarge
        )
        if (attempt.note.isNotBlank()) {
            Text(
                text = attempt.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SavedPhotosBlock(
    attempt: Attempt,
    onAddPhoto: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.attempt_saved_photos_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (attempt.photos.size == 1) stringResource(R.string.detail_one_photo) else stringResource(R.string.detail_many_photos, attempt.photos.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            attempt.photos.forEach { photo -> SavedPhotoThumb(photo) }

            // "Agregar otra foto": abre la cámara para seguir sumando fotos a este mismo intento.
            Surface(
                onClick = onAddPhoto,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.size(width = 88.dp, height = 88.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(R.string.attempt_add_another_photo),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedPhotoThumb(photo: AttemptPhoto) {
    Column(
        modifier = Modifier.size(width = 88.dp, height = 112.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = "Foto del paso ${photo.stepIndex + 1}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(14.dp))
        )
        Text(
            text = "Paso ${photo.stepIndex + 1} · Guardada",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    container: androidx.compose.ui.graphics.Color
) {
    Surface(shape = RoundedCornerShape(50), color = container) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ------------------------------------------------------------------- Textos

/** "28 sep": día y mes abreviado, sin el punto que agrega el formato de la región. */
private fun formatDay(millis: Long): String =
    DayFormatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())).replace(".", "")

/** "7 oct 2026 · 9:41" */
private fun formatFull(millis: Long): String =
    FullFormatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())).replace(".", "")

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun AttemptScreenPreview() {
    RecetarioTheme {
        AttemptScreen(
            state = AttemptUiState(
                isLoading = false,
                recipe = Recipe(
                    id = "1",
                    name = "Ñoquis al pomodoro",
                    photoUrl = null,
                    city = "Nápoles",
                    country = "Italia",
                    prepTimeMinutes = 35,
                    attempts = 2,
                    visibility = Visibility.PUBLIC,
                    steps = listOf(
                        "Herví las papas con piel 20 min.",
                        "Sumá huevo y harina. Uní sin amasar de más, cortá y marcá con tenedor."
                    )
                ),
                previousAttempts = listOf(
                    Attempt("a1", "1", 1, 1790000000000, "Masa más firme"),
                    Attempt("a2", "1", 2, 1790600000000, "Menos harina, mejor textura")
                ),
                attemptNumber = 3,
                selectedStep = 1
            ),
            actions = AttemptActions()
        )
    }
}
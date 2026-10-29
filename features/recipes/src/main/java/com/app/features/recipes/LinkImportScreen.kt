package com.app.features.recipes

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.core.domain.DomainError
import com.app.core.ui.R
import com.app.core.ui.asMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkImportRoute(
    viewModel: LinkImportViewModel,
    onBack: () -> Unit,
    onEdit: (RecipeFormUiState) -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        val review = state as? LinkImportUiState.Review
        if (review?.saved == true) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.link_import_top_bar)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
        ) {
            when (val s = state) {
                is LinkImportUiState.Idle -> LinkInputStep(
                    onExtract = viewModel::onExtract
                )
                is LinkImportUiState.Extracting -> ExtractingStep()
                is LinkImportUiState.Review -> ReviewStep(
                    review = s,
                    onSaveDirectly = viewModel::onSaveDirectly,
                    onEdit = {
                        viewModel.buildFormState()?.let { formState -> onEdit(formState) }
                    },
                    onRetry = viewModel::onRetry
                )
                is LinkImportUiState.Error -> ErrorStep(
                    error = s.error,
                    onRetry = viewModel::onRetry
                )
            }
        }
    }
}

// ─── Paso 1: Ingresar el link ────────────────────────────────────────────────

@Composable
private fun LinkInputStep(onExtract: (String) -> Unit) {
    var url by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.link_import_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.link_import_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.link_import_url_label)) },
            leadingIcon = { Icon(Icons.Outlined.Link, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = { if (url.isNotBlank()) onExtract(url) }
            ),
            singleLine = true
        )
        Button(
            onClick = { onExtract(url) },
            enabled = url.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.link_import_btn_extract))
        }
    }
}

// ─── Paso 2: Extrayendo ──────────────────────────────────────────────────────

@Composable
private fun ExtractingStep() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(stringResource(R.string.link_import_extracting), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ─── Paso 3: Revisar extracción ───────────────────────────────────────────────

@Composable
private fun ReviewStep(
    review: LinkImportUiState.Review,
    onSaveDirectly: () -> Unit,
    onEdit: () -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = stringResource(R.string.link_import_extracted_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (review.isAlreadySaved) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.link_import_already_saved_banner),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (!review.photoUrl.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = review.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        item {
            SectionLabel(stringResource(R.string.link_import_name))
            Text(review.name.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }

        val originText = listOf(review.restaurant, review.city, review.country)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        if (originText.isNotBlank()) {
            item {
                SectionLabel(stringResource(R.string.link_import_origin))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(originText, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    SectionLabel(stringResource(R.string.link_import_servings))
                    Text(review.servings.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge)
                }
                Column {
                    SectionLabel(stringResource(R.string.link_import_prep_time))
                    Text(review.prepTime.ifBlank { "—" }, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (review.description.isNotBlank()) {
            item {
                SectionLabel(stringResource(R.string.form_desc_label))
                Text(review.description, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item { SectionLabel(stringResource(R.string.link_import_ingredients)) }

        itemsIndexed(review.ingredients) { _, ing ->
            Text(
                text = "• ${ing.quantity} ${ing.name}".trim().ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item { SectionLabel(stringResource(R.string.link_import_steps)) }

        itemsIndexed(review.steps) { i, step ->
            Text(
                text = "${i + 1}. ${step.text}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (review.saveError != null) {
            item {
                Text(
                    text = review.saveError.asMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }

        item {
            Text(
                text = stringResource(R.string.link_import_edit_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveDirectly,
                    enabled = !review.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (review.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.link_import_saving))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.link_import_btn_save))
                    }
                }

                OutlinedButton(
                    onClick = onEdit,
                    enabled = !review.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.link_import_btn_edit))
                }

                TextButton(
                    onClick = onRetry,
                    enabled = !review.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.link_import_btn_try_another))
                }
            }
        }
    }
}

// ─── Error ────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorStep(error: DomainError, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(stringResource(R.string.link_import_error_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(error.asMessage(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) {
                Text(stringResource(R.string.link_import_btn_retry))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

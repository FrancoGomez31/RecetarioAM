package com.app.features.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.app.core.domain.Recipe
import com.app.core.domain.Visibility
import com.app.core.domain.originLabel
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage

@Composable
fun RecipesRoute(
    viewModel: RecipesViewModel,
    onCreateManual: () -> Unit,
    onCreateFromLink: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Si el sheet está abierto es puro estado de UI: no pertenece al ViewModel.
    // rememberSaveable hace que siga abierto si el usuario rota el teléfono.
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }

    RecipesScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onAddRecipe = { showCreateSheet = true },
        onRecipeClick = onRecipeClick,
        onNotificationsClick = onNotificationsClick,
        onAccountClick = onAccountClick
    )

    if (showCreateSheet) {
        CreateRecipeSheet(
            onDismiss = { showCreateSheet = false },
            onManualClick = {
                showCreateSheet = false
                onCreateManual()
            },
            onLinkClick = {
                showCreateSheet = false
                onCreateFromLink()
            }
        )
    }
}

@Composable
fun RecipesScreen(
    state: RecipesUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (RecipeFilter) -> Unit,
    onAddRecipe: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 24.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Header(
                        onNotificationsClick = onNotificationsClick,
                        onAccountClick = onAccountClick
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Sabores que ya son tuyos.",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = summaryText(state),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscá por receta u origen") },
                        singleLine = true,
                        shape = RoundedCornerShape(50)
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.filter == RecipeFilter.ALL,
                            onClick = { onFilterChange(RecipeFilter.ALL) },
                            label = { Text("Todas · ${state.totalCount}") }
                        )
                        FilterChip(
                            selected = state.filter == RecipeFilter.PRIVATE,
                            onClick = { onFilterChange(RecipeFilter.PRIVATE) },
                            label = { Text("Privadas · ${state.privateCount}") }
                        )
                        FilterChip(
                            selected = state.filter == RecipeFilter.PUBLIC,
                            onClick = { onFilterChange(RecipeFilter.PUBLIC) },
                            label = { Text("Públicas · ${state.publicCount}") }
                        )
                    }
                }

                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.error != null) {
                    item {
                        Text(
                            text = state.error.asMessage(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (state.visibleRecipes.isEmpty()) {
                    item {
                        Text(
                            text = "No encontramos recetas con ese filtro.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(state.visibleRecipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddRecipe,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(text = "+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun Header(
    onNotificationsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TU CUADERNO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Mis recetas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        }
        TextButton(onClick = onNotificationsClick) {
            Text("🔔")
        }
        OutlinedButton(onClick = onAccountClick) {
            Text("Mi cuenta")
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Miniatura: la portada de la receta si la tiene; si no, el recuadro de color de siempre.
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (recipe.photoUrl != null) {
                    AsyncImage(
                        model = recipe.photoUrl,
                        contentDescription = "Portada de ${recipe.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = recipe.originLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${recipe.prepTimeMinutes} min · ${attemptsText(recipe.attempts)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (recipe.visibility == Visibility.PUBLIC) {
                    Text(
                        text = "Pública",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "Privada",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun attemptsText(attempts: Int): String = when (attempts) {
    0 -> "Sin cocinar"
    1 -> "1 intento"
    else -> "$attempts intentos"
}

private fun summaryText(state: RecipesUiState): String {
    val recipes = if (state.totalCount == 1) "1 receta" else "${state.totalCount} recetas"
    val countries = if (state.countryCount == 1) "1 país" else "${state.countryCount} países"
    return "$recipes · $countries · muchas ganas de cocinar"
}

@Preview(showBackground = true)
@Composable
private fun RecipesScreenPreview() {
    RecetarioTheme {
        RecipesScreen(
            state = RecipesUiState(
                recipes = listOf(
                    Recipe("1", "Ñoquis al pomodoro", null, "Nápoles", "Italia", 35, 2, Visibility.PUBLIC),
                    Recipe("2", "Ramen shoyu", null, "Kioto", "Japón", 50, 1, Visibility.PRIVATE),
                    Recipe("3", "Empanadas criollas", null, "Córdoba", "Argentina", 70, 3, Visibility.PUBLIC),
                    Recipe("4", "Pizza margherita", null, "Nápoles", "Italia", 90, 0, Visibility.PRIVATE)
                ),
                isLoading = false
            ),
            onQueryChange = {},
            onFilterChange = {},
            onAddRecipe = {},
            onRecipeClick = {},
            onNotificationsClick = {},
            onAccountClick = {}
        )
    }
}
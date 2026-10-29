package com.app.recetario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.app.features.account.AccountRoute
import com.app.features.account.AccountViewModel
import com.app.features.map.MapRoute
import com.app.features.map.MapViewModel
import com.app.features.recipes.AttemptRoute
import com.app.features.recipes.AttemptViewModel
import com.app.features.recipes.LinkImportRoute
import com.app.features.recipes.LinkImportViewModel
import com.app.features.recipes.PlacePickerViewModel
import com.app.features.recipes.RecipeDetailRoute
import com.app.features.recipes.RecipeDetailViewModel
import com.app.features.recipes.RecipeFormRoute
import com.app.features.recipes.RecipeFormViewModel
import com.app.features.recipes.RecipesRoute
import com.app.features.recipes.RecipesViewModel
import com.app.features.recipes.achievements.AchievementsRoute
import com.app.features.recipes.achievements.AchievementsViewModel
import com.app.features.recipes.explore.ExploreRoute
import com.app.features.recipes.explore.ExploreViewModel
import com.app.recetario.ui.ComingSoonScreen

/**
 * La navegación vive en :app. Las features no se conocen entre sí:
 * solo exponen callbacks (onRecipeClick, onAccountClick…) y acá decidimos qué hace cada uno.
 */
@Composable
fun AppNavDisplay(
    navigationState: NavigationState,
    modifier: Modifier = Modifier
) {
    val entryProvider = entryProvider<NavKey> {

        entry<RecipesDestination> {
            val viewModel: RecipesViewModel = hiltViewModel()
            LaunchedEffect(Unit) { viewModel.loadRecipes() }
            RecipesRoute(
                viewModel = viewModel,
                onCreateManual = { navigationState.navigate(RecipeFormDestination()) },
                onCreateFromLink = { navigationState.navigate(LinkImportDestination) },
                onRecipeClick = { id -> navigationState.navigate(RecipeDetailDestination(id)) },
                onNotificationsClick = { /* las notificaciones se configuran desde el detalle */ },
                onAccountClick = { navigationState.navigate(AccountDestination) }
            )
        }

        entry<LinkImportDestination> {
            val viewModel: LinkImportViewModel = hiltViewModel()
            LinkImportRoute(
                viewModel = viewModel,
                onBack = { navigationState.goBackFrom(LinkImportDestination) },
                onConfirm = { formState ->
                    navigationState.navigate(
                        RecipeFormDestination(
                            prefillName = formState.name,
                            prefillServings = formState.servings,
                            prefillPrepTime = formState.prepTime
                        )
                    )
                }
            )
        }

        entry<MapDestination> {
            val viewModel: MapViewModel = hiltViewModel()
            MapRoute(
                viewModel = viewModel,
                onRecipeClick = { id -> navigationState.navigate(RecipeDetailDestination(id)) }
            )
        }

        entry<ExploreDestination> {
            val viewModel: ExploreViewModel = hiltViewModel()
            LaunchedEffect(Unit) { viewModel.loadExploreRecipes() }
            ExploreRoute(
                viewModel = viewModel,
                onRecipeClick = { id -> navigationState.navigate(RecipeDetailDestination(id)) }
            )
        }

        entry<AchievementsDestination> {
            val viewModel: AchievementsViewModel = hiltViewModel()
            LaunchedEffect(Unit) { viewModel.load() }
            AchievementsRoute(viewModel = viewModel)
        }

        entry<AccountDestination> {
            val viewModel: AccountViewModel = hiltViewModel()
            AccountRoute(
                viewModel = viewModel,
                onBack = { navigationState.goBackFrom(AccountDestination) }
            )
        }

        entry<RecipeDetailDestination> { key ->
            val viewModel: RecipeDetailViewModel = hiltViewModel()
            LaunchedEffect(key.recipeId) { viewModel.load(key.recipeId) }
            RecipeDetailRoute(
                viewModel = viewModel,
                onBack = { navigationState.goBackFrom(key) },
                onEdit = { id -> navigationState.navigate(RecipeFormDestination(id)) },
                onRegisterAttempt = { id -> navigationState.navigate(AttemptDestination(id)) },
                onDeleted = { navigationState.goBackFrom(key) }
            )
        }

        entry<RecipeFormDestination> { key ->
            val viewModel: RecipeFormViewModel = hiltViewModel()
            val placePickerViewModel: PlacePickerViewModel = hiltViewModel()

            LaunchedEffect(key) {
                if (key.recipeId != null) {
                    viewModel.start(key.recipeId)
                } else if (key.prefillName.isNotBlank()) {
                    viewModel.prefill(
                        name = key.prefillName,
                        servings = key.prefillServings,
                        prepTime = key.prefillPrepTime
                    )
                } else {
                    viewModel.start(null)
                }
            }

            RecipeFormRoute(
                viewModel = viewModel,
                placePickerViewModel = placePickerViewModel,
                recipeId = key.recipeId,
                onBack = { navigationState.goBackFrom(key) },
                onSaved = { navigationState.goBackFrom(key) }
            )
        }

        entry<AttemptDestination> { key ->
            val viewModel: AttemptViewModel = hiltViewModel()
            AttemptRoute(
                viewModel = viewModel,
                recipeId = key.recipeId,
                onBack = { navigationState.goBackFrom(key) }
            )
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = navigationState::goBack,
        modifier = modifier
    )
}

/**
 * Convierte cada back stack en entradas "decoradas" y junta las de las pestañas visibles.
 */
@Composable
private fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): List<NavEntry<NavKey>> {
    val entriesByTab = backStacks.mapValues { (_, backStack) ->
        rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider
        )
    }
    return visibleTabs.flatMap { tab -> entriesByTab.getValue(tab) }
}

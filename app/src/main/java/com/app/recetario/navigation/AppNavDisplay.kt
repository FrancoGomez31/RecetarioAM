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
import com.app.features.recipes.PlacePickerViewModel
import com.app.features.recipes.RecipeFormRoute
import com.app.features.recipes.RecipeFormViewModel
import com.app.features.recipes.RecipesRoute
import com.app.features.recipes.RecipesViewModel
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
    // entryProvider: "para esta key, mostrá esta pantalla".
    val entryProvider = entryProvider<NavKey> {

        entry<RecipesDestination> {
            // hiltViewModel() pide el ViewModel al grafo: Hilt resuelve toda la cadena
            // (casos de uso -> repositorio) sin que acá nombremos nada de eso.
            // Se crea UNA vez por entrada del back stack y sobrevive a rotaciones.
            val viewModel: RecipesViewModel = hiltViewModel()

            // Cada vez que Home vuelve a mostrarse (por ejemplo al guardar una receta y volver
            // atrás) recarga la lista. Provisorio: con Firestore el repositorio avisará los
            // cambios solo y este efecto ya no hará falta.
            LaunchedEffect(Unit) { viewModel.loadRecipes() }

            RecipesRoute(
                viewModel = viewModel,
                onCreateManual = { navigationState.navigate(RecipeFormDestination()) },
                onCreateFromLink = { /* pendiente: carga por link con IA */ },
                // TEMPORAL: abre el formulario en modo edición para probarlo.
                // Se reemplaza por la pantalla de detalle (Paso 4).
                onRecipeClick = { id -> navigationState.navigate(RecipeFormDestination(id)) },
                onNotificationsClick = { /* pendiente: notificaciones */ },
                // navigate() agrega la pantalla encima del back stack de la pestaña actual
                onAccountClick = { navigationState.navigate(AccountDestination) }
            )
        }

        entry<MapDestination> { ComingSoonScreen(title = "Mapa") }
        entry<ExploreDestination> { ComingSoonScreen(title = "Explorar") }
        entry<AchievementsDestination> { ComingSoonScreen(title = "Logros") }

        entry<AccountDestination> {
            val viewModel: AccountViewModel = hiltViewModel()
            AccountRoute(
                viewModel = viewModel,
                // goBackFrom: si el usuario toca la flecha dos veces rápido, el segundo toque no hace nada
                onBack = { navigationState.goBackFrom(AccountDestination) }
            )
        }

        // `key` es la RecipeFormDestination con la que se navegó: de ahí sale el id (o null).
        entry<RecipeFormDestination> { key ->
            val viewModel: RecipeFormViewModel = hiltViewModel()
            // El selector de origen es parte del formulario: comparte su entrada del back stack.
            val placePickerViewModel: PlacePickerViewModel = hiltViewModel()
            RecipeFormRoute(
                viewModel = viewModel,
                placePickerViewModel = placePickerViewModel,
                recipeId = key.recipeId,
                onBack = { navigationState.goBackFrom(key) },
                onSaved = { navigationState.goBackFrom(key) }
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
 *
 * Decoramos TODOS los back stacks, aunque no se vean: así, al cambiar de pestaña,
 * las pantallas de la otra conservan su ViewModel y su estado (scroll, filtros, formularios…).
 */
@Composable
private fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): List<NavEntry<NavKey>> {
    val entriesByTab = backStacks.mapValues { (_, backStack) ->
        rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(
                // Guarda el estado de rememberSaveable de cada pantalla
                rememberSaveableStateHolderNavEntryDecorator(),
                // Le da a cada pantalla su propio ViewModelStore
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider
        )
    }
    return visibleTabs.flatMap { tab -> entriesByTab.getValue(tab) }
}
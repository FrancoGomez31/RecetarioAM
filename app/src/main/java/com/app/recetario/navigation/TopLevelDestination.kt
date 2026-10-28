package com.app.recetario.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.app.recetario.R

/**
 * Las pestañas de la barra inferior. Cada una tiene su propio back stack, que arranca en [startKey].
 * Agregar una pestaña = agregar una entrada acá y su pantalla en AppNavDisplay.
 */
enum class TopLevelDestination(
    val startKey: NavKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val label: Int
) {
    RECIPES(
        startKey = RecipesDestination,
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        label = R.string.tab_recipes
    ),
    MAP(
        startKey = MapDestination,
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
        label = R.string.tab_map
    ),
    EXPLORE(
        startKey = ExploreDestination,
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        label = R.string.tab_explore
    ),
}
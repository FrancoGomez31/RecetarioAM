package com.app.recetario.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Todo el estado de navegación: qué pestaña está activa y el back stack de cada una.
 *
 * Con Navigation 3 no hay NavController: navegar es modificar una lista.
 * Como los back stacks son estado observable, Compose recompone la UI cuando cambian.
 */
class NavigationState(
    private val startTab: TopLevelDestination,
    currentTabState: MutableState<TopLevelDestination>,
    val backStacks: Map<TopLevelDestination, NavBackStack<NavKey>>
) {
    var currentTab: TopLevelDestination by currentTabState
        private set

    private val currentStack: NavBackStack<NavKey>
        get() = backStacks.getValue(currentTab)

    /** La pantalla que se ve ahora. */
    val currentKey: NavKey
        get() = currentStack.last()

    /** true si estamos en la pantalla principal de una pestaña (ahí se muestra la barra inferior). */
    val isOnTabRoot: Boolean
        get() = currentStack.size == 1

    /**
     * Back stacks que se muestran, uno detrás del otro. La pestaña inicial siempre queda "abajo":
     * desde Mapa, "atrás" vuelve a Mis recetas antes de salir de la app.
     */
    val visibleTabs: List<TopLevelDestination>
        get() = listOf(startTab, currentTab).distinct()

    fun navigate(key: NavKey) {
        currentStack.add(key)
    }

    fun goBack() {
        when {
            currentStack.size > 1 -> currentStack.removeAt(currentStack.lastIndex)
            currentTab != startTab -> currentTab = startTab
        }
    }

    /**
     * "Atrás" pedido por una pantalla concreta: solo actúa si esa pantalla sigue arriba.
     * Si el usuario toca la flecha dos veces rápido, el segundo toque no hace nada.
     */
    fun goBackFrom(key: NavKey) {
        if (currentKey == key) goBack()
    }

    fun selectTab(tab: TopLevelDestination) {
        if (tab == currentTab) {
            // Tocar la pestaña actual vuelve a su pantalla principal
            val stack = backStacks.getValue(tab)
            while (stack.size > 1) stack.removeAt(stack.lastIndex)
        } else {
            currentTab = tab
        }
    }
}

@Composable
fun rememberNavigationState(
    startTab: TopLevelDestination = TopLevelDestination.RECIPES
): NavigationState {
    // rememberSaveable / rememberNavBackStack: la pestaña actual y cada back stack sobreviven a la rotación.
    val currentTab = rememberSaveable { mutableStateOf(startTab) }
    val backStacks = TopLevelDestination.entries.associateWith { tab -> rememberNavBackStack(tab.startKey) }
    return remember(backStacks) { NavigationState(startTab, currentTab, backStacks) }
}
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

class NavigationState(
    private val startTab: TopLevelDestination,
    currentTabState: MutableState<TopLevelDestination>,
    val backStacks: Map<TopLevelDestination, NavBackStack<NavKey>>
) {
    var currentTab: TopLevelDestination by currentTabState
        private set

    private val currentStack: NavBackStack<NavKey>
        get() = backStacks.getValue(currentTab)

    val currentKey: NavKey
        get() = currentStack.last()

    val isOnTabRoot: Boolean
        get() = currentStack.size == 1

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

    fun goBackFrom(key: NavKey) {
        if (currentKey == key) goBack()
    }

    fun selectTab(tab: TopLevelDestination) {
        if (tab == currentTab) {
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
    val currentTab = rememberSaveable { mutableStateOf(startTab) }
    val backStacks = TopLevelDestination.entries.associateWith { tab -> rememberNavBackStack(tab.startKey) }
    return remember(backStacks) { NavigationState(startTab, currentTab, backStacks) }
}

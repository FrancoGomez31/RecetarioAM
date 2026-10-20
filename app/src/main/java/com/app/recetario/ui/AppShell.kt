package com.app.recetario.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.app.recetario.navigation.AppNavDisplay
import com.app.recetario.navigation.NavigationState
import com.app.recetario.navigation.TopLevelDestination
import com.app.recetario.navigation.rememberNavigationState

/** Estructura general de la app (ya con sesión iniciada): barra inferior + NavDisplay. */
@Composable
fun AppShell(
    navigationState: NavigationState = rememberNavigationState()
) {
    Scaffold(
        bottomBar = {
            // Leer navigationState acá es leer estado: cuando cambia el back stack, esto se recompone solo.
            // La barra solo se muestra en la pantalla principal de cada pestaña, no en los detalles.
            AnimatedVisibility(
                visible = navigationState.isOnTabRoot,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                AppBottomBar(
                    currentTab = navigationState.currentTab,
                    onTabClick = navigationState::selectTab
                )
            }
        }
    ) { innerPadding ->
        AppNavDisplay(
            navigationState = navigationState,
            // consumeWindowInsets: el Scaffold ya aplicó los márgenes del sistema,
            // así las pantallas no vuelven a sumarlos.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}

@Composable
private fun AppBottomBar(
    currentTab: TopLevelDestination,
    onTabClick: (TopLevelDestination) -> Unit
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabClick(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(tab.label)) }
            )
        }
    }
}
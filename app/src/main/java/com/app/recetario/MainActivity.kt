package com.app.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.app.core.data.FakeAuthRepository
import com.app.core.data.FakeRecipeRepository
import com.app.core.domain.AuthRepository
import com.app.core.domain.RecipeRepository
import com.app.core.ui.RecetarioTheme
import com.app.features.auth.AuthRoute
import com.app.features.auth.AuthViewModel
import com.app.features.auth.AuthViewModelFactory
import com.app.features.recipes.RecipesRoute
import com.app.features.recipes.RecipesViewModel
import com.app.features.recipes.RecipesViewModelFactory

private enum class MainTab(val label: String, val icon: String) {
    RECIPES("Mis recetas", "📖"),
    MAP("Mapa", "🗺️"),
    EXPLORE("Explorar", "🧭"),
    ACHIEVEMENTS("Logros", "🏆")
}

class MainActivity : ComponentActivity() {

    // Temporal: repositorios falsos en memoria. Se reemplazan por Firebase más adelante.
    private val authRepository: AuthRepository = FakeAuthRepository()
    private val recipeRepository: RecipeRepository = FakeRecipeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(authRepository)
        )[AuthViewModel::class.java]

        val recipesViewModel = ViewModelProvider(
            this,
            RecipesViewModelFactory(recipeRepository)
        )[RecipesViewModel::class.java]

        setContent {
            RecetarioTheme {
                var loggedIn by rememberSaveable { mutableStateOf(false) }
                var selectedTab by rememberSaveable { mutableStateOf(MainTab.RECIPES) }

                if (!loggedIn) {
                    AuthRoute(
                        viewModel = authViewModel,
                        onAuthenticated = { loggedIn = true }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                MainTab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = selectedTab == tab,
                                        onClick = { selectedTab = tab },
                                        icon = { Text(tab.icon) },
                                        label = { Text(tab.label) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            when (selectedTab) {
                                MainTab.RECIPES -> RecipesRoute(
                                    viewModel = recipesViewModel,
                                    onAddRecipe = { /* pendiente: alta de receta */ },
                                    onRecipeClick = { /* pendiente: detalle */ },
                                    onNotificationsClick = { /* pendiente: notificaciones */ },
                                    onAccountClick = { /* pendiente: Mi cuenta */ }
                                )
                                MainTab.MAP -> Text("Mapa (pendiente)")
                                MainTab.EXPLORE -> Text("Explorar (pendiente)")
                                MainTab.ACHIEVEMENTS -> Text("Logros (pendiente)")
                            }
                        }
                    }
                }
            }
        }
    }
}
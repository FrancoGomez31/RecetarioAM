package com.app.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.core.ui.RecetarioTheme
import com.app.features.auth.AuthRoute
import com.app.recetario.ui.AppShell
import dagger.hilt.android.AndroidEntryPoint

/**
 * @AndroidEntryPoint: habilita que hiltViewModel() encuentre el grafo de dependencias.
 * La Activity solo decide entre dos mundos: sin sesión (acceso) y con sesión (la app).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecetarioTheme {
                val sessionViewModel: SessionViewModel = hiltViewModel()
                val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()

                if (isLoggedIn) {
                    AppShell()
                } else {
                    AuthRoute(
                        viewModel = hiltViewModel(),
                        // La sesión se observa sola desde Firebase: no hay nada que avisar.
                        onAuthenticated = { }
                    )
                }
            }
        }
    }
}
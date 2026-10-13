package com.app.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.app.core.data.FakeAuthRepository
import com.app.core.domain.AuthRepository
import com.app.core.ui.RecetarioTheme
import com.app.features.auth.AuthRoute
import com.app.features.auth.AuthViewModel
import com.app.features.auth.AuthViewModelFactory

class MainActivity : ComponentActivity() {

    // Temporal: un único repositorio falso para toda la app
    private val authRepository: AuthRepository = FakeAuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(authRepository)
        )[AuthViewModel::class.java]

        setContent {
            RecetarioTheme {
                var loggedIn by remember { mutableStateOf(false) }

                if (loggedIn) {
                    // Temporal: acá va "Mis recetas" cuando la hagamos
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sesión iniciada")
                    }
                } else {
                    AuthRoute(
                        viewModel = authViewModel,
                        onAuthenticated = { loggedIn = true }
                    )
                }
            }
        }
    }
}
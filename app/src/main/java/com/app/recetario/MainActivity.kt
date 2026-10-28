package com.app.recetario

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.core.domain.LanguagePreference
import com.app.core.domain.ThemePreference
import com.app.core.ui.RecetarioTheme
import com.app.features.auth.AuthRoute
import com.app.recetario.ui.AppShell
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val sessionViewModel: SessionViewModel = hiltViewModel()
            val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val themeMode by sessionViewModel.themeMode.collectAsStateWithLifecycle()
            val language by sessionViewModel.language.collectAsStateWithLifecycle()
            val isOnline by sessionViewModel.isOnline.collectAsStateWithLifecycle()

            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemePreference.SYSTEM -> systemDark
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            val locale = remember(language) {
                Locale.forLanguageTag(language.code)
            }
            LaunchedEffect(locale) {
                Locale.setDefault(locale)
            }

            val configuration = LocalConfiguration.current
            val updatedConfiguration = remember(configuration, locale) {
                Configuration(configuration).apply {
                    setLocale(locale)
                }
            }

            val context = LocalContext.current
            LaunchedEffect(updatedConfiguration) {
                context.resources.updateConfiguration(updatedConfiguration, context.resources.displayMetrics)
            }

            CompositionLocalProvider(
                LocalConfiguration provides updatedConfiguration
            ) {
                RecetarioTheme(darkTheme = isDark) {
                    if (isLoggedIn) {
                        AppShell(isOnline = isOnline)
                    } else {
                        AuthRoute(
                            viewModel = hiltViewModel(),
                            onAuthenticated = { }
                        )
                    }
                }
            }
        }
    }
}
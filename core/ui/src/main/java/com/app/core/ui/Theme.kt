package com.app.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val code: String) {
    SPANISH("es"),
    ENGLISH("en")
}

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = White,
    primaryContainer = TerracottaLight,
    onPrimaryContainer = Terracotta,
    secondary = LeafGreen,
    onSecondary = White,
    background = Cream,
    onBackground = InkDark,
    surface = White,
    onSurface = InkDark,
    surfaceVariant = CreamDark,
    onSurfaceVariant = InkMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkTerracotta,
    onPrimary = White,
    primaryContainer = DarkTerracottaContainer,
    onPrimaryContainer = DarkOnTerracottaContainer,
    secondary = DarkLeafGreen,
    onSecondary = White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun RecetarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

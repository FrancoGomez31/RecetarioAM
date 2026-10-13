package com.app.core.ui
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RecetarioColorScheme = lightColorScheme(
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

@Composable
fun RecetarioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RecetarioColorScheme,
        content = content
    )
}
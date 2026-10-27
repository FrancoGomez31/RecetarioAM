package com.app.core.domain

import kotlinx.coroutines.flow.StateFlow

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

enum class LanguagePreference(val code: String) {
    SPANISH("es"),
    ENGLISH("en")
}

interface UserPreferencesRepository {
    val themeMode: StateFlow<ThemePreference>
    val language: StateFlow<LanguagePreference>
    suspend fun setThemeMode(mode: ThemePreference)
    suspend fun setLanguage(language: LanguagePreference)
}

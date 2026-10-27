package com.app.core.data

import android.content.Context
import com.app.core.domain.LanguagePreference
import com.app.core.domain.ThemePreference
import com.app.core.domain.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SharedPreferencesUserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readStoredTheme())
    override val themeMode: StateFlow<ThemePreference> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(readStoredLanguage())
    override val language: StateFlow<LanguagePreference> = _language.asStateFlow()

    override suspend fun setThemeMode(mode: ThemePreference) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    override suspend fun setLanguage(language: LanguagePreference) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _language.value = language
    }

    private fun readStoredTheme(): ThemePreference {
        val stored = prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name)
        return try {
            ThemePreference.valueOf(stored ?: ThemePreference.SYSTEM.name)
        } catch (_: Exception) {
            ThemePreference.SYSTEM
        }
    }

    private fun readStoredLanguage(): LanguagePreference {
        val stored = prefs.getString(KEY_LANGUAGE, LanguagePreference.SPANISH.name)
        return try {
            LanguagePreference.valueOf(stored ?: LanguagePreference.SPANISH.name)
        } catch (_: Exception) {
            LanguagePreference.SPANISH
        }
    }

    companion object {
        private const val KEY_THEME = "pref_theme_mode"
        private const val KEY_LANGUAGE = "pref_language"
    }
}

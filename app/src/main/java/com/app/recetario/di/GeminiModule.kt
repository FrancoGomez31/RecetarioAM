package com.app.recetario.di

import com.app.recetario.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provee la API key de Gemini desde BuildConfig (que la lee de local.properties, nunca del repo).
 * Se declara en :app para que tenga acceso a BuildConfig sin crear dependencias circulares.
 */
@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    @Named("gemini_api_key")
    fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY
}

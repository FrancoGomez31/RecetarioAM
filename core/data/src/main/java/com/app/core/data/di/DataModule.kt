package com.app.core.data.di

import com.app.core.data.ConnectivityNetworkMonitor
import com.app.core.data.FirebaseAuthRepository
import com.app.core.data.FusedLocationProvider
import com.app.core.data.GeminiLinkExtractor
import com.app.core.data.InMemoryUserLocationCache
import com.app.core.data.LocalPhotoStorage
import com.app.core.data.NominatimPlaceRepository
import com.app.core.data.SharedPreferencesUserPreferencesRepository
import com.app.core.data.firestore.FirestoreAttemptRepository
import com.app.core.data.firestore.FirestoreRecipeRepository
import com.app.core.data.reminder.AndroidReminderScheduler
import com.app.core.domain.AttemptRepository
import com.app.core.domain.AuthRepository
import com.app.core.domain.LocationProvider
import com.app.core.domain.NetworkMonitor
import com.app.core.domain.PhotoStorage
import com.app.core.domain.PlaceRepository
import com.app.core.domain.RecipeLinkExtractor
import com.app.core.domain.RecipeRepository
import com.app.core.domain.ReminderScheduler
import com.app.core.domain.UserLocationCache
import com.app.core.domain.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindRecipeRepository(impl: FirestoreRecipeRepository): RecipeRepository

    @Binds @Singleton
    abstract fun bindAttemptRepository(impl: FirestoreAttemptRepository): AttemptRepository

    @Binds @Singleton
    abstract fun bindPhotoStorage(impl: LocalPhotoStorage): PhotoStorage

    @Binds @Singleton
    abstract fun bindPlaceRepository(impl: NominatimPlaceRepository): PlaceRepository

    @Binds @Singleton
    abstract fun bindLocationProvider(impl: FusedLocationProvider): LocationProvider

    @Binds @Singleton
    abstract fun bindReminderScheduler(impl: AndroidReminderScheduler): ReminderScheduler

    @Binds @Singleton
    abstract fun bindRecipeLinkExtractor(impl: GeminiLinkExtractor): RecipeLinkExtractor

    @Binds @Singleton
    abstract fun bindUserLocationCache(impl: InMemoryUserLocationCache): UserLocationCache

    @Binds @Singleton
    abstract fun bindUserPreferencesRepository(impl: SharedPreferencesUserPreferencesRepository): UserPreferencesRepository

    @Binds @Singleton
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}

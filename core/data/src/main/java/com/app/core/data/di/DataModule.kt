package com.app.core.data.di

import com.app.core.data.FakeAttemptRepository
import com.app.core.data.FakeRecipeRepository
import com.app.core.data.FirebaseAuthRepository
import com.app.core.data.FusedLocationProvider
import com.app.core.data.LocalPhotoStorage
import com.app.core.data.NominatimPlaceRepository
import com.app.core.data.reminder.AndroidReminderScheduler
import com.app.core.domain.AttemptRepository
import com.app.core.domain.AuthRepository
import com.app.core.domain.LocationProvider
import com.app.core.domain.PhotoStorage
import com.app.core.domain.PlaceRepository
import com.app.core.domain.RecipeRepository
import com.app.core.domain.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Esta capa declara qué implementación usa cada contrato del dominio.
 * Autenticación ya es real (Firebase); recetas e intentos siguen siendo falsos hasta conectar
 * Firestore; las fotos se guardan en el teléfono hasta decidir la nube; la búsqueda de
 * lugares usa Nominatim (OpenStreetMap) y la ubicación sale de Google Play Services.
 *
 * @Singleton: una sola instancia para toda la app. Los repositorios falsos guardan
 * datos en memoria, y sin esto cada pantalla tendría su propia copia.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: FakeRecipeRepository): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindAttemptRepository(impl: FakeAttemptRepository): AttemptRepository

    @Binds
    @Singleton
    abstract fun bindPhotoStorage(impl: LocalPhotoStorage): PhotoStorage

    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: NominatimPlaceRepository): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindLocationProvider(impl: FusedLocationProvider): LocationProvider

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: AndroidReminderScheduler): ReminderScheduler
}

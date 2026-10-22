package com.app.core.data.di

import com.app.core.data.FakeRecipeRepository
import com.app.core.data.FirebaseAuthRepository
import com.app.core.data.NominatimPlaceRepository
import com.app.core.domain.AuthRepository
import com.app.core.domain.PlaceRepository
import com.app.core.domain.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Esta capa declara qué implementación usa cada contrato del dominio.
 * Autenticación ya es real (Firebase); las recetas siguen siendo falsas hasta conectar Firestore;
 * la búsqueda de lugares usa Nominatim (OpenStreetMap).
 *
 * @Singleton: una sola instancia para toda la app. El repositorio falso de recetas guarda
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
    abstract fun bindPlaceRepository(impl: NominatimPlaceRepository): PlaceRepository
}
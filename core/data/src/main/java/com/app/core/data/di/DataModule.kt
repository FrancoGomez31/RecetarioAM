package com.app.core.data.di

import com.app.core.data.FakeAuthRepository
import com.app.core.data.FakeRecipeRepository
import com.app.core.domain.AuthRepository
import com.app.core.domain.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Esta capa declara qué implementación usa cada contrato del dominio.
 * Cuando conectemos Firebase, cambiar FakeAuthRepository por FirebaseAuthRepository
 * es tocar UNA línea acá: ningún ViewModel ni pantalla se entera.
 *
 * @Singleton: una sola instancia para toda la app. Los repositorios falsos guardan
 * datos en memoria, y sin esto cada pantalla tendría su propia copia.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: FakeRecipeRepository): RecipeRepository
}
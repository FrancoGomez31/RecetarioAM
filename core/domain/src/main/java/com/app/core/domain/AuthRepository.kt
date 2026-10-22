package com.app.core.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): User?

    /** Emite el usuario cada vez que alguien inicia o cierra sesión (y apenas te suscribís). */
    fun observeCurrentUser(): Flow<User?>

    suspend fun signIn(email: String, password: String): AppResult<User>
    suspend fun signUp(email: String, password: String): AppResult<User>
    suspend fun updateDisplayName(name: String): AppResult<User>
    suspend fun signOut()
}
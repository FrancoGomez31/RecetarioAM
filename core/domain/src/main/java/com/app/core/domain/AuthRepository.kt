package com.app.core.domain

interface AuthRepository {
    fun getCurrentUser(): User?
    suspend fun signIn(email: String, password: String): AppResult<User>
    suspend fun signUp(email: String, password: String): AppResult<User>
    suspend fun signOut()
}
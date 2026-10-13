package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.AuthRepository
import com.app.core.domain.User

// Implementación temporal en memoria. Se reemplaza por Firebase más adelante.
class FakeAuthRepository : AuthRepository {

    private val accounts = mutableMapOf<String, String>() // email -> contraseña
    private var currentUser: User? = null

    override fun getCurrentUser(): User? = currentUser

    override suspend fun signIn(email: String, password: String): AppResult<User> {
        val savedPassword = accounts[email]
            ?: return AppResult.Error("No existe una cuenta con ese correo")
        if (savedPassword != password) {
            return AppResult.Error("Contraseña incorrecta")
        }
        val user = User(id = email, email = email)
        currentUser = user
        return AppResult.Success(user)
    }

    override suspend fun signUp(email: String, password: String): AppResult<User> {
        if (accounts.containsKey(email)) {
            return AppResult.Error("Ya existe una cuenta con ese correo")
        }
        accounts[email] = password
        val user = User(id = email, email = email)
        currentUser = user
        return AppResult.Success(user)
    }

    override suspend fun signOut() {
        currentUser = null
    }
}
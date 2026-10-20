package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.AuthRepository
import com.app.core.domain.DomainError
import com.app.core.domain.User
import com.app.core.firebase.AuthSourceException
import com.app.core.firebase.FirebaseAuthDataSource
import com.app.core.firebase.FirebaseAuthUser
import javax.inject.Inject

/**
 * Implementación real de [AuthRepository]. Traduce lo que devuelve el data source
 * (usuarios y excepciones de Firebase) al vocabulario del dominio: [User] y [DomainError].
 */
class FirebaseAuthRepository @Inject constructor(
    private val dataSource: FirebaseAuthDataSource
) : AuthRepository {

    override fun getCurrentUser(): User? = dataSource.currentUser()?.toDomain()

    override suspend fun signIn(email: String, password: String): AppResult<User> =
        authCall { dataSource.signIn(email, password) }

    override suspend fun signUp(email: String, password: String): AppResult<User> =
        authCall { dataSource.signUp(email, password) }

    override suspend fun signOut() {
        dataSource.signOut()
    }
}

private suspend fun authCall(block: suspend () -> FirebaseAuthUser): AppResult<User> = try {
    AppResult.Success(block().toDomain())
} catch (e: AuthSourceException) {
    AppResult.Error(e.toDomainError())
}

private fun FirebaseAuthUser.toDomain() = User(id = uid, email = email)

private fun AuthSourceException.toDomainError(): DomainError = when (this) {
    is AuthSourceException.InvalidEmail -> DomainError.InvalidEmail
    is AuthSourceException.InvalidCredentials -> DomainError.InvalidCredentials
    is AuthSourceException.EmailAlreadyInUse -> DomainError.EmailAlreadyInUse
    is AuthSourceException.WeakPassword -> DomainError.WeakPassword
    is AuthSourceException.Network -> DomainError.Network
    // Sin mensaje: la UI muestra el texto genérico en español, no el de Firebase
    is AuthSourceException.Unknown -> DomainError.Unknown()
}
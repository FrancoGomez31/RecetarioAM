package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.AuthRepository
import com.app.core.domain.User
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<User> {
        val cleanEmail = email.trim()
        validateCredentials(cleanEmail, password)?.let { return AppResult.Error(it) }
        return authRepository.signIn(cleanEmail, password)
    }
}
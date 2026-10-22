package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.AuthRepository
import com.app.core.domain.User
import javax.inject.Inject

class UpdateDisplayNameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String): AppResult<User> =
        authRepository.updateDisplayName(name.trim())
}
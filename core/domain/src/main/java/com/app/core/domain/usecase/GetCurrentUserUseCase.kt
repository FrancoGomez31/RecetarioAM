package com.app.core.domain.usecase

import com.app.core.domain.AuthRepository
import com.app.core.domain.User
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): User? = authRepository.getCurrentUser()
}
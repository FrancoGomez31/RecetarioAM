package com.app.core.domain.usecase

import com.app.core.domain.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** "¿Hay una sesión iniciada?", como un flujo que cambia solo. */
class ObserveSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> =
        authRepository.observeCurrentUser().map { user -> user != null }
}
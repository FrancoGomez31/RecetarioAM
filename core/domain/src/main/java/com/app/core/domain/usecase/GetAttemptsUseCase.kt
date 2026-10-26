package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Attempt
import com.app.core.domain.AttemptRepository
import javax.inject.Inject

class GetAttemptsUseCase @Inject constructor(
    private val attemptRepository: AttemptRepository
) {
    suspend operator fun invoke(recipeId: String): AppResult<List<Attempt>> =
        attemptRepository.getAttempts(recipeId)
}
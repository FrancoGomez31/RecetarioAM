package com.app.features.recipes.achievements

import com.app.core.domain.Achievement
import com.app.core.domain.DomainError

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true,
    val error: DomainError? = null
) {
    val unlockedCount: Int get() = achievements.count { it.isUnlocked }
}
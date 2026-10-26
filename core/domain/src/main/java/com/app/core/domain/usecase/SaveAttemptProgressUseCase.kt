package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.Attempt
import com.app.core.domain.AttemptPhoto
import com.app.core.domain.AttemptRepository
import com.app.core.domain.DomainError
import com.app.core.domain.NewAttemptPhoto
import com.app.core.domain.PhotoStorage
import com.app.core.domain.RecipeRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Guarda el avance de un intento: una foto nueva (opcional) y la nota.
 *
 * El intento se crea al guardar por primera vez ([current] == null); a partir de ahí cada
 * llamada lo va completando. Al crearlo se suma uno al contador de intentos de la receta.
 */
class SaveAttemptProgressUseCase @Inject constructor(
    private val attemptRepository: AttemptRepository,
    private val photoStorage: PhotoStorage,
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(
        recipeId: String,
        current: Attempt?,
        attemptNumber: Int,
        photo: NewAttemptPhoto?,
        note: String
    ): AppResult<Attempt> {
        val now = System.currentTimeMillis()

        // 1. La foto primero: si no se puede guardar, no tocamos nada más.
        val savedPhoto = if (photo != null) {
            when (val stored = photoStorage.save(photo.sourceUri)) {
                is AppResult.Success -> AttemptPhoto(
                    id = UUID.randomUUID().toString(),
                    stepIndex = photo.stepIndex,
                    uri = stored.data,
                    takenAtMillis = now
                )
                is AppResult.Error -> return AppResult.Error(stored.error)
            }
        } else {
            null
        }

        // 2. Se arma el intento: el existente, o uno nuevo.
        val base = current ?: Attempt(
            id = "",
            recipeId = recipeId,
            number = attemptNumber,
            createdAtMillis = now
        )
        val updated = base.copy(
            note = note.trim(),
            photos = base.photos + listOfNotNull(savedPhoto)
        )

        // Un intento sin foto ni nota no dice nada: no se guarda.
        if (updated.photos.isEmpty() && updated.note.isEmpty()) {
            return AppResult.Error(DomainError.Unknown("Agregá una foto o una nota para guardar el intento."))
        }

        val saved = when (val result = attemptRepository.saveAttempt(updated)) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return AppResult.Error(result.error)
        }

        // 3. Intento nuevo: la receta suma uno al contador.
        // Con Firestore esto pasará a ser una sola escritura conjunta, para que no quede a medias.
        if (current == null) {
            when (val recipeResult = recipeRepository.getRecipe(recipeId)) {
                is AppResult.Success -> {
                    val recipe = recipeResult.data
                    val withAttempt = recipe.copy(attempts = maxOf(recipe.attempts, saved.number))
                    val update = recipeRepository.saveRecipe(withAttempt)
                    if (update is AppResult.Error) return AppResult.Error(update.error)
                }
                is AppResult.Error -> return AppResult.Error(recipeResult.error)
            }
        }

        return AppResult.Success(saved)
    }
}
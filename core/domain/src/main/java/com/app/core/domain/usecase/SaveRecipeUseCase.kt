package com.app.core.domain.usecase

import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.PhotoStorage
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import javax.inject.Inject

class SaveRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val photoStorage: PhotoStorage
) {
    /**
     * @param newCoverUri portada recién elegida (URI temporal de la cámara o la galería). Si viene,
     * se guarda de forma permanente y su referencia pasa a ser la portada de la receta. Si es null,
     * la receta conserva el photoUrl que ya trae (o ninguno, si el usuario la quitó).
     */
    suspend operator fun invoke(recipe: Recipe, newCoverUri: String? = null): AppResult<Recipe> {
        val clean = recipe.cleaned()
        val invalidFields = validateRecipe(clean)
        if (invalidFields.isNotEmpty()) {
            return AppResult.Error(DomainError.InvalidRecipe(invalidFields))
        }

        // La foto se guarda DESPUÉS de validar: una receta inválida no deja archivos sueltos.
        val withCover = if (newCoverUri != null) {
            when (val stored = photoStorage.save(newCoverUri)) {
                is AppResult.Success -> clean.copy(photoUrl = stored.data)
                is AppResult.Error -> return AppResult.Error(stored.error)
            }
        } else {
            clean
        }

        return recipeRepository.saveRecipe(withCover)
    }
}
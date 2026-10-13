package com.app.features.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.core.domain.RecipeRepository

class RecipesViewModelFactory(
    private val recipeRepository: RecipeRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecipesViewModel(recipeRepository) as T
    }
}
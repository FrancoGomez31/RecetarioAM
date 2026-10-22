package com.app.core.domain

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: DomainError) : AppResult<Nothing>
}

/**
 * Errores que entiende el negocio. No tienen texto: la UI decide cómo mostrarlos,
 * y la capa de datos decide cómo traducir un error de Firebase o de red a uno de estos.
 */
sealed interface DomainError {
    data object InvalidEmail : DomainError
    data object WeakPassword : DomainError
    data object InvalidCredentials : DomainError
    data object EmailAlreadyInUse : DomainError
    data object Network : DomainError
    data object NotFound : DomainError
    data object Unauthenticated : DomainError

    /** La receta no cumple las reglas; [fields] dice cuáles son los campos que faltan o están mal. */
    data class InvalidRecipe(val fields: Set<RecipeField>) : DomainError

    data class Unknown(val message: String? = null) : DomainError
}
package com.app.core.domain

interface AttemptRepository {
    /** Intentos de una receta, ordenados por número (el primero primero). */
    suspend fun getAttempts(recipeId: String): AppResult<List<Attempt>>

    /** Guarda un intento nuevo (id vacío) o actualiza uno existente. Devuelve el intento guardado, con su id. */
    suspend fun saveAttempt(attempt: Attempt): AppResult<Attempt>
}
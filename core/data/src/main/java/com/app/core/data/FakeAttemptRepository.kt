package com.app.core.data

import com.app.core.domain.AppResult
import com.app.core.domain.Attempt
import com.app.core.domain.AttemptRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

// Implementación temporal en memoria. Se reemplaza por Firestore más adelante.
class FakeAttemptRepository @Inject constructor() : AttemptRepository {

    // Los dos intentos de "Ñoquis al pomodoro" que muestra el diseño (sin fotos).
    private val attempts = mutableListOf(
        Attempt(
            id = "a1",
            recipeId = "1",
            number = 1,
            createdAtMillis = millisOf(2026, 9, 28),
            note = "Masa más firme"
        ),
        Attempt(
            id = "a2",
            recipeId = "1",
            number = 2,
            createdAtMillis = millisOf(2026, 10, 5),
            note = "Menos harina, mejor textura"
        )
    )

    override suspend fun getAttempts(recipeId: String): AppResult<List<Attempt>> {
        return AppResult.Success(
            attempts.filter { it.recipeId == recipeId }.sortedBy { it.number }
        )
    }

    override suspend fun saveAttempt(attempt: Attempt): AppResult<Attempt> {
        // id vacío = intento nuevo: acá se le asigna el id (Firestore hará lo mismo con el suyo).
        val toSave = if (attempt.id.isBlank()) attempt.copy(id = UUID.randomUUID().toString()) else attempt
        val index = attempts.indexOfFirst { it.id == toSave.id }
        if (index >= 0) attempts[index] = toSave else attempts.add(toSave)
        return AppResult.Success(toSave)
    }

    private fun millisOf(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day)
            .atTime(20, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
}
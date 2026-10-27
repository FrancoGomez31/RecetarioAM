package com.app.core.data.firestore

import com.app.core.domain.AppResult
import com.app.core.domain.Attempt
import com.app.core.domain.AttemptPhoto
import com.app.core.domain.AttemptRepository
import com.app.core.domain.DomainError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirestoreAttemptRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AttemptRepository {

    private fun attemptsCollection(uid: String, recipeId: String) =
        firestore.collection("users")
            .document(uid)
            .collection("recipes")
            .document(recipeId)
            .collection("attempts")

    override suspend fun getAttempts(recipeId: String): AppResult<List<Attempt>> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext AppResult.Error(DomainError.Unauthenticated)
        try {
            val snapshot = attemptsCollection(uid, recipeId).get().await()
            val list = snapshot.documents.mapNotNull { it.toAttempt(recipeId) }.sortedBy { it.number }
            AppResult.Success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Error(DomainError.Network)
        }
    }

    override suspend fun saveAttempt(attempt: Attempt): AppResult<Attempt> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext AppResult.Error(DomainError.Unauthenticated)
        try {
            val collection = attemptsCollection(uid, attempt.recipeId)
            val docRef = if (attempt.id.isBlank()) collection.document() else collection.document(attempt.id)
            val toSave = attempt.copy(id = docRef.id)
            docRef.set(toSave.toMap()).await()
            AppResult.Success(toSave)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppResult.Error(DomainError.Network)
        }
    }
}

private fun Attempt.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "recipeId" to recipeId,
    "number" to number,
    "createdAtMillis" to createdAtMillis,
    "note" to note,
    "photos" to photos.map { photo ->
        mapOf(
            "id" to photo.id,
            "stepIndex" to photo.stepIndex,
            "uri" to photo.uri,
            "takenAtMillis" to photo.takenAtMillis
        )
    }
)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toAttempt(fallbackRecipeId: String): Attempt? {
    if (!exists()) return null
    val id = id
    val recipeId = getString("recipeId") ?: fallbackRecipeId
    val number = getLong("number")?.toInt() ?: 1
    val createdAtMillis = getLong("createdAtMillis") ?: 0L
    val note = getString("note").orEmpty()
    val photosRaw = get("photos") as? List<Map<String, Any?>> ?: emptyList()
    val photos = photosRaw.map {
        AttemptPhoto(
            id = it["id"] as? String ?: "",
            stepIndex = (it["stepIndex"] as? Long)?.toInt() ?: 0,
            uri = it["uri"] as? String ?: "",
            takenAtMillis = (it["takenAtMillis"] as? Long) ?: 0L
        )
    }
    return Attempt(
        id = id,
        recipeId = recipeId,
        number = number,
        createdAtMillis = createdAtMillis,
        note = note,
        photos = photos
    )
}

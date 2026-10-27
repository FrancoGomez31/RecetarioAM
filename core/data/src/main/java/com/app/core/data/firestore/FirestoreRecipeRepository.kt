package com.app.core.data.firestore

import android.util.Log
import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.Ingredient
import com.app.core.domain.Recipe
import com.app.core.domain.RecipeRepository
import com.app.core.domain.Visibility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirestoreRecipeRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : RecipeRepository {

    private fun userRecipesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("recipes")

    override suspend fun getRecipes(): AppResult<List<Recipe>> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext AppResult.Error(DomainError.Unauthenticated)
        try {
            val snapshot = userRecipesCollection(uid).get().await()
            val list = snapshot.documents.mapNotNull { it.toRecipe() }
            AppResult.Success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreRecipeRepo", "Error al obtener recetas de Firestore", e)
            AppResult.Error(mapFirestoreError(e))
        }
    }

    override suspend fun getRecipe(id: String): AppResult<Recipe> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext AppResult.Error(DomainError.Unauthenticated)
        try {
            val doc = userRecipesCollection(uid).document(id).get().await()
            val recipe = doc.toRecipe() ?: return@withContext AppResult.Error(DomainError.NotFound)
            AppResult.Success(recipe)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreRecipeRepo", "Error al obtener receta $id", e)
            AppResult.Error(mapFirestoreError(e))
        }
    }

    override suspend fun saveRecipe(recipe: Recipe): AppResult<Recipe> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext AppResult.Error(DomainError.Unauthenticated)
        try {
            val collection = userRecipesCollection(uid)
            val docRef = if (recipe.id.isBlank()) collection.document() else collection.document(recipe.id)
            val toSave = recipe.copy(id = docRef.id)
            docRef.set(toSave.toMap()).await()
            AppResult.Success(toSave)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreRecipeRepo", "Error al guardar receta", e)
            AppResult.Error(mapFirestoreError(e))
        }
    }

    override suspend fun deleteRecipe(id: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext AppResult.Error(DomainError.Unauthenticated)
        try {
            userRecipesCollection(uid).document(id).delete().await()
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreRecipeRepo", "Error al eliminar receta $id", e)
            AppResult.Error(mapFirestoreError(e))
        }
    }
}

private fun mapFirestoreError(e: Exception): DomainError {
    return if (e is FirebaseFirestoreException) {
        when (e.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE -> DomainError.Network
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> DomainError.Unknown("Permiso denegado en Firestore: revisá las Security Rules en Firebase Console")
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> DomainError.Unauthenticated
            FirebaseFirestoreException.Code.NOT_FOUND -> DomainError.NotFound
            else -> DomainError.Unknown("Error Firestore (${e.code}): ${e.message}")
        }
    } else {
        DomainError.Unknown(e.localizedMessage ?: "Error inesperado de base de datos")
    }
}

private fun Recipe.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "photoUrl" to photoUrl,
    "city" to city,
    "country" to country,
    "prepTimeMinutes" to prepTimeMinutes,
    "attempts" to attempts,
    "visibility" to visibility.name,
    "description" to description,
    "servings" to servings,
    "ingredients" to ingredients.map { mapOf("quantity" to it.quantity, "name" to it.name) },
    "steps" to steps,
    "latitude" to latitude,
    "longitude" to longitude,
    "restaurant" to restaurant,
    "reminderEpochMillis" to reminderEpochMillis
)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toRecipe(): Recipe? {
    if (!exists()) return null
    val id = id
    val name = getString("name").orEmpty()
    val photoUrl = getString("photoUrl")
    val city = getString("city").orEmpty()
    val country = getString("country").orEmpty()
    val prepTimeMinutes = getLong("prepTimeMinutes")?.toInt() ?: 0
    val attempts = getLong("attempts")?.toInt() ?: 0
    val visibilityStr = getString("visibility")
    val visibility = try {
        Visibility.valueOf(visibilityStr ?: Visibility.PRIVATE.name)
    } catch (_: Exception) {
        Visibility.PRIVATE
    }
    val description = getString("description").orEmpty()
    val servings = getLong("servings")?.toInt() ?: 4

    val ingredientsRaw = get("ingredients") as? List<Map<String, Any?>> ?: emptyList()
    val ingredients = ingredientsRaw.map {
        Ingredient(
            quantity = it["quantity"] as? String ?: "",
            name = it["name"] as? String ?: ""
        )
    }

    val steps = (get("steps") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
    val latitude = getDouble("latitude")
    val longitude = getDouble("longitude")
    val restaurant = getString("restaurant").orEmpty()
    val reminderEpochMillis = getLong("reminderEpochMillis")

    return Recipe(
        id = id,
        name = name,
        photoUrl = photoUrl,
        city = city,
        country = country,
        prepTimeMinutes = prepTimeMinutes,
        attempts = attempts,
        visibility = visibility,
        description = description,
        servings = servings,
        ingredients = ingredients,
        steps = steps,
        latitude = latitude,
        longitude = longitude,
        restaurant = restaurant,
        reminderEpochMillis = reminderEpochMillis
    )
}

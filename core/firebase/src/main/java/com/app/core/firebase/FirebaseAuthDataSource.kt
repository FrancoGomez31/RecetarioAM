package com.app.core.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

/** Lo único que el resto de la app necesita saber de un usuario de Firebase. */
data class FirebaseAuthUser(
    val uid: String,
    val email: String,
    val displayName: String
)

/**
 * Habla con Firebase Authentication. Devuelve datos simples y, si algo falla,
 * lanza [AuthSourceException] (nunca excepciones de Firebase).
 */
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {

    /** Firebase guarda la sesión en el dispositivo: tras reabrir la app, esto devuelve al usuario. */
    fun currentUser(): FirebaseAuthUser? = auth.currentUser?.toAuthUser()

    /**
     * Emite el usuario actual apenas te suscribís y cada vez que alguien inicia o cierra sesión.
     * Es un callbackFlow: convierte el listener de Firebase en un Flow, y lo quita al dejar de observar.
     */
    fun observeUser(): Flow<FirebaseAuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAuthUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): FirebaseAuthUser = runAuth {
        auth.signInWithEmailAndPassword(email, password).await().user.toAuthUserOrThrow()
    }

    suspend fun signUp(email: String, password: String): FirebaseAuthUser = runAuth {
        auth.createUserWithEmailAndPassword(email, password).await().user.toAuthUserOrThrow()
    }

    suspend fun updateDisplayName(name: String): FirebaseAuthUser = runAuth {
        val user = auth.currentUser
            ?: throw AuthSourceException.Unknown(IllegalStateException("No hay una sesión iniciada"))
        val changes = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(changes).await()
        // Firebase confirmó el cambio: devolvemos el nombre que acabamos de guardar
        user.toAuthUser().copy(displayName = name)
    }

    fun signOut() {
        auth.signOut()
    }
}

private suspend fun <T> runAuth(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e // cancelar una corrutina no es un error de autenticación
} catch (e: AuthSourceException) {
    throw e
} catch (e: Exception) {
    throw e.toAuthSourceException()
}

private fun Throwable.toAuthSourceException(): AuthSourceException = when (this) {
    // El orden importa: WeakPassword es una subclase de InvalidCredentials
    is FirebaseAuthWeakPasswordException -> AuthSourceException.WeakPassword(this)
    is FirebaseAuthInvalidCredentialsException ->
        if (errorCode == "ERROR_INVALID_EMAIL") {
            AuthSourceException.InvalidEmail(this)
        } else {
            AuthSourceException.InvalidCredentials(this)
        }
    // Cuenta inexistente o deshabilitada: no le contamos al usuario cuál de las dos
    is FirebaseAuthInvalidUserException -> AuthSourceException.InvalidCredentials(this)
    is FirebaseAuthUserCollisionException -> AuthSourceException.EmailAlreadyInUse(this)
    is FirebaseNetworkException -> AuthSourceException.Network(this)
    else -> AuthSourceException.Unknown(this)
}

private fun FirebaseUser.toAuthUser() = FirebaseAuthUser(
    uid = uid,
    email = email.orEmpty(),
    displayName = displayName.orEmpty()
)

private fun FirebaseUser?.toAuthUserOrThrow(): FirebaseAuthUser =
    this?.toAuthUser()
        ?: throw AuthSourceException.Unknown(IllegalStateException("Firebase no devolvió el usuario"))

/** Convierte el callback de Firebase (Task) en una función suspend. */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { value -> continuation.resume(value) }
    addOnFailureListener { error -> continuation.resumeWithException(error) }
    addOnCanceledListener { continuation.cancel() }
}
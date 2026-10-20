package com.app.core.firebase

/**
 * Errores de autenticación en el vocabulario de ESTE módulo.
 * Así quien use el data source no necesita conocer las excepciones de Firebase:
 * :core:data las traduce a errores de dominio.
 */
sealed class AuthSourceException(cause: Throwable? = null) : Exception(cause) {
    class InvalidEmail(cause: Throwable? = null) : AuthSourceException(cause)
    class InvalidCredentials(cause: Throwable? = null) : AuthSourceException(cause)
    class EmailAlreadyInUse(cause: Throwable? = null) : AuthSourceException(cause)
    class WeakPassword(cause: Throwable? = null) : AuthSourceException(cause)
    class Network(cause: Throwable? = null) : AuthSourceException(cause)
    class Unknown(cause: Throwable? = null) : AuthSourceException(cause)
}
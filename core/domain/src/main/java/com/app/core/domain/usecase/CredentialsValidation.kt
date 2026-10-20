package com.app.core.domain.usecase

import com.app.core.domain.DomainError

private const val MIN_PASSWORD_LENGTH = 6

/**
 * Reglas de negocio de las credenciales: viven en el dominio, no en el ViewModel,
 * para que se testeen sin Android y las compartan el login y el registro.
 * Devuelve el error, o null si son válidas.
 */
internal fun validateCredentials(email: String, password: String): DomainError? = when {
    !email.contains("@") -> DomainError.InvalidEmail
    password.length < MIN_PASSWORD_LENGTH -> DomainError.WeakPassword
    else -> null
}
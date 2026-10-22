package com.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.app.core.domain.DomainError

/**
 * El dominio dice QUÉ pasó; la UI decide CÓMO contarlo. Usar string resources
 * permite traducir la app sin tocar ViewModels ni dominio.
 */
@Composable
fun DomainError.asMessage(): String = when (this) {
    DomainError.InvalidEmail -> stringResource(R.string.core_ui_error_invalid_email)
    DomainError.WeakPassword -> stringResource(R.string.core_ui_error_weak_password)
    DomainError.InvalidCredentials -> stringResource(R.string.core_ui_error_invalid_credentials)
    DomainError.EmailAlreadyInUse -> stringResource(R.string.core_ui_error_email_in_use)
    DomainError.Network -> stringResource(R.string.core_ui_error_network)
    DomainError.NotFound -> stringResource(R.string.core_ui_error_not_found)
    DomainError.Unauthenticated -> stringResource(R.string.core_ui_error_unauthenticated)
    is DomainError.InvalidRecipe -> stringResource(R.string.core_ui_error_invalid_recipe)
    is DomainError.Unknown -> message ?: stringResource(R.string.core_ui_error_unknown)
}
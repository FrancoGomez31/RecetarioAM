package com.app.features.account

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.core.domain.LanguagePreference
import com.app.core.domain.ThemePreference
import com.app.core.ui.R
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage

@Composable
fun AccountRoute(
    viewModel: AccountViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var locationGranted by remember { mutableStateOf(context.isLocationGranted()) }
    var cameraGranted by remember { mutableStateOf(context.isGranted(Manifest.permission.CAMERA)) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        locationGranted = context.isLocationGranted()
        cameraGranted = context.isGranted(Manifest.permission.CAMERA)
    }

    AccountScreen(
        state = state,
        locationGranted = locationGranted,
        cameraGranted = cameraGranted,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onThemeChange = viewModel::onThemeChange,
        onLanguageChange = viewModel::onLanguageChange,
        onSave = viewModel::onSave,
        onOpenSettings = { context.openAppSettings() },
        onSignOut = viewModel::onSignOut
    )
}

@Composable
fun AccountScreen(
    state: AccountUiState,
    locationGranted: Boolean,
    cameraGranted: Boolean,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onLanguageChange: (LanguagePreference) -> Unit,
    onSave: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(savedName = state.savedName, onBack = onBack)

            Text(
                text = stringResource(R.string.account_your_data),
                style = MaterialTheme.typography.headlineSmall
            )

            LabeledField(label = stringResource(R.string.account_name)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.account_name_placeholder)) },
                    trailingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            LabeledField(label = stringResource(R.string.account_email)) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                    supportingText = { Text(stringResource(R.string.account_email_read_only)) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            state.error?.let { error ->
                Text(
                    text = error.asMessage(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (state.justSaved) {
                Text(
                    text = stringResource(R.string.account_changes_saved),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.account_save_changes))
                }
            }

            // Sección de Ajustes de Apariencia e Idioma
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = stringResource(R.string.settings_appearance_title),
                style = MaterialTheme.typography.headlineSmall
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_theme_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.themeMode == ThemePreference.SYSTEM,
                        onClick = { onThemeChange(ThemePreference.SYSTEM) },
                        label = { Text(stringResource(R.string.settings_theme_system)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.themeMode == ThemePreference.LIGHT,
                        onClick = { onThemeChange(ThemePreference.LIGHT) },
                        label = { Text(stringResource(R.string.settings_theme_light)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.themeMode == ThemePreference.DARK,
                        onClick = { onThemeChange(ThemePreference.DARK) },
                        label = { Text(stringResource(R.string.settings_theme_dark)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_language_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.language == LanguagePreference.SPANISH,
                        onClick = { onLanguageChange(LanguagePreference.SPANISH) },
                        label = { Text(stringResource(R.string.settings_language_es)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.language == LanguagePreference.ENGLISH,
                        onClick = { onLanguageChange(LanguagePreference.ENGLISH) },
                        label = { Text(stringResource(R.string.settings_language_en)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Text(
                text = stringResource(R.string.account_permissions_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.account_permissions_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PermissionCard(
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.account_permission_location),
                granted = locationGranted,
                description = stringResource(R.string.account_permission_location_desc),
                onOpenSettings = onOpenSettings
            )
            PermissionCard(
                icon = Icons.Outlined.PhotoCamera,
                title = stringResource(R.string.account_permission_camera),
                granted = cameraGranted,
                description = stringResource(R.string.account_permission_camera_desc),
                onOpenSettings = onOpenSettings
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.account_sign_out))
            }
            Text(
                text = stringResource(R.string.account_sign_out_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Header(
    savedName: String,
    onBack: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
        }
        Column {
            Text(
                text = if (savedName.isBlank()) "TU CUADERNO" else "EL CUADERNO DE ${savedName.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.account_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    granted: Boolean,
    description: String,
    onOpenSettings: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (granted) "Permitida" else "No permitida",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (granted) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSettings)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ajustes del dispositivo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Context.isLocationGranted(): Boolean =
    isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AccountScreenPreview() {
    RecetarioTheme {
        AccountScreen(
            state = AccountUiState(
                email = "lucia.mendez@correo.com",
                name = "Lucía Méndez",
                savedName = "Lucía Méndez"
            ),
            locationGranted = true,
            cameraGranted = false,
            onBack = {},
            onNameChange = {},
            onThemeChange = {},
            onLanguageChange = {},
            onSave = {},
            onOpenSettings = {},
            onSignOut = {}
        )
    }
}

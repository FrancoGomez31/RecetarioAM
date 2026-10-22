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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.core.ui.RecetarioTheme
import com.app.core.ui.asMessage

@Composable
fun AccountRoute(
    viewModel: AccountViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Los permisos son del sistema Android, no de nuestra lógica de negocio: se leen acá, en la UI.
    var locationGranted by remember { mutableStateOf(context.isLocationGranted()) }
    var cameraGranted by remember { mutableStateOf(context.isGranted(Manifest.permission.CAMERA)) }

    // El usuario los cambia en Ajustes y vuelve a la app: al reanudar, los leemos de nuevo.
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
                text = "Tus datos",
                style = MaterialTheme.typography.headlineSmall
            )

            LabeledField(label = "Nombre") {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Tu nombre") },
                    trailingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            LabeledField(label = "Correo electrónico") {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    singleLine = true,
                    supportingText = { Text("No se puede cambiar desde la app") },
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
                    text = "Cambios guardados",
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
                    Text("Guardar cambios")
                }
            }

            Text(
                text = "Permisos del dispositivo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Se administran desde los ajustes de Android.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PermissionCard(
                icon = Icons.Outlined.LocationOn,
                title = "Ubicación",
                granted = locationGranted,
                description = "Para ubicarte en el mapa de orígenes.",
                onOpenSettings = onOpenSettings
            )
            PermissionCard(
                icon = Icons.Outlined.PhotoCamera,
                title = "Cámara",
                granted = cameraGranted,
                description = "Para sacar fotos de tus recetas e intentos.",
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
                Text("Cerrar sesión")
            }
            Text(
                text = "Volvés a la pantalla de acceso.",
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }
        Column {
            Text(
                text = if (savedName.isBlank()) "TU CUADERNO" else "EL CUADERNO DE ${savedName.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Mi cuenta",
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

/** Abre la pantalla de Android donde el usuario administra los permisos de esta app. */
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
            onSave = {},
            onOpenSettings = {},
            onSignOut = {}
        )
    }
}
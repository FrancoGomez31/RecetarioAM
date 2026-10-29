package com.app.features.recipes.achievements

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.core.domain.Achievement
import com.app.core.domain.AchievementId
import com.app.core.ui.R
import com.app.core.ui.asMessage

@Composable
fun AchievementsRoute(viewModel: AchievementsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AchievementsScreen(state = state)
}

@Composable
fun AchievementsScreen(state: AchievementsUiState) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.ach_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = stringResource(R.string.ach_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                state.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null && state.achievements.isEmpty() -> item {
                    Text(
                        text = state.error.asMessage(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    item { SummaryCard(unlocked = state.unlockedCount, total = state.achievements.size) }
                    item { NotificationPermissionBanner() }
                    items(state.achievements, key = { it.id }) { achievement ->
                        AchievementCard(achievement)
                    }
                }
            }
        }
    }
}

/** Tarjeta de arriba: cuántos logros lleva el usuario. */
@Composable
private fun SummaryCard(unlocked: Int, total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.ach_summary_count, unlocked, total),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.ach_summary_label),
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else unlocked.toFloat() / total },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onPrimary,
                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Desde Android 13 hay que pedir permiso para mostrar notificaciones.
 * Si ya está concedido (o el teléfono es más viejo) no se muestra nada.
 */
@Composable
private fun NotificationPermissionBanner() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        granted = isGranted
    }
    if (granted) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = null)
            Text(
                text = stringResource(R.string.ach_notifications_text),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                Text(stringResource(R.string.ach_notifications_button))
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val unlocked = achievement.isUnlocked
    val text = achievement.id.text()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = achievement.id.icon(),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(text.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(text.description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
            if (unlocked) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.ach_unlocked),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "${achievement.shownCurrent}/${achievement.target}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private class AchievementText(@StringRes val title: Int, @StringRes val description: Int)

private fun AchievementId.text(): AchievementText = when (this) {
    AchievementId.FIRST_RECIPE -> AchievementText(R.string.ach_first_recipe_title, R.string.ach_first_recipe_desc)
    AchievementId.FULL_NOTEBOOK -> AchievementText(R.string.ach_full_notebook_title, R.string.ach_full_notebook_desc)
    AchievementId.WORLD_KITCHENS -> AchievementText(R.string.ach_world_kitchens_title, R.string.ach_world_kitchens_desc)
    AchievementId.FIRST_ATTEMPT -> AchievementText(R.string.ach_first_attempt_title, R.string.ach_first_attempt_desc)
    AchievementId.LEARNING -> AchievementText(R.string.ach_learning_title, R.string.ach_learning_desc)
}

private fun AchievementId.icon(): ImageVector = when (this) {
    AchievementId.FIRST_RECIPE -> Icons.AutoMirrored.Outlined.MenuBook
    AchievementId.FULL_NOTEBOOK -> Icons.Outlined.Book
    AchievementId.WORLD_KITCHENS -> Icons.Outlined.Public
    AchievementId.FIRST_ATTEMPT -> Icons.Outlined.Restaurant
    AchievementId.LEARNING -> Icons.Outlined.PhotoCamera
}
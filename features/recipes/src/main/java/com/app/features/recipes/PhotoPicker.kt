package com.app.features.recipes

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** Las dos maneras de conseguir una foto: sacarla con la cámara o elegirla de la galería. */
class PhotoPicker(
    val takePhoto: () -> Unit,
    val pickFromGallery: () -> Unit
)

/**
 * Encapsula todo lo de Android para conseguir una foto: el permiso de cámara, la app de cámara
 * y el selector de fotos del sistema (que no necesita permisos).
 *
 * @param onPhotoSelected recibe la URI temporal de la foto elegida.
 * @param onUnavailable recibe el motivo (en castellano) cuando no se pudo abrir la cámara.
 */
@Composable
fun rememberPhotoPicker(
    onPhotoSelected: (String) -> Unit,
    onUnavailable: (String) -> Unit
): PhotoPicker {
    val context = LocalContext.current
    // Los launchers viven más que una recomposición: estos State apuntan siempre a los callbacks más nuevos.
    val currentOnPhotoSelected by rememberUpdatedState(onPhotoSelected)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)

    // Dirección temporal donde la app de cámara deja la foto. Se guarda con rememberSaveable:
    // si el sistema recrea la pantalla mientras la cámara está abierta, el resultado igual llega.
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = cameraUri
        if (saved && uri != null) currentOnPhotoSelected(uri)
        cameraUri = null
    }

    // El selector de fotos del sistema no necesita ningún permiso.
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) currentOnPhotoSelected(uri.toString())
    }

    fun launchCamera() {
        val folder = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(folder, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri.toString()
        try {
            takePicture.launch(uri)
        } catch (e: ActivityNotFoundException) {
            cameraUri = null
            currentOnUnavailable("No encontramos una app de cámara en este teléfono. Podés elegir una foto de la galería.")
        }
    }

    // Como el manifest declara el permiso CAMERA, Android exige pedirlo antes de abrir la cámara.
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            launchCamera()
        } else {
            currentOnUnavailable("Sin permiso de cámara no podemos sacar la foto. Podés elegir una de la galería.")
        }
    }

    return PhotoPicker(
        takePhoto = {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            if (granted) launchCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
        },
        pickFromGallery = {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    )
}
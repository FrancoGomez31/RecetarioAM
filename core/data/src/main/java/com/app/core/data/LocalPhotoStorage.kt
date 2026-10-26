package com.app.core.data

import android.content.Context
import android.net.Uri
import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.PhotoStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Almacenamiento temporal: copia la foto a la carpeta privada de la app.
 *
 * La copia es necesaria: la URI que entrega la cámara o la galería es temporal y puede
 * dejar de funcionar. Se reemplaza por una implementación que suba la imagen a la nube
 * (Firebase Storage, Cloudinary…) y devuelva su URL.
 */
class LocalPhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : PhotoStorage {

    override suspend fun save(sourceUri: String): AppResult<String> = withContext(Dispatchers.IO) {
        try {
            val directory = File(context.filesDir, "attempt_photos").apply { mkdirs() }
            val target = File(directory, "${UUID.randomUUID()}.jpg")

            val input = context.contentResolver.openInputStream(Uri.parse(sourceUri))
                ?: return@withContext AppResult.Error(DomainError.Unknown("No se pudo leer la foto."))

            // La copia pasa por el disco: no se carga la imagen entera en memoria.
            input.use { source ->
                target.outputStream().use { output -> source.copyTo(output) }
            }
            AppResult.Success(Uri.fromFile(target).toString())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            AppResult.Error(DomainError.Unknown("No se pudo guardar la foto en el teléfono."))
        } catch (e: SecurityException) {
            AppResult.Error(DomainError.Unknown("No tenemos permiso para leer esa foto."))
        }
    }
}
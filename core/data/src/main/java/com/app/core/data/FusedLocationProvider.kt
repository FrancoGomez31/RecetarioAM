package com.app.core.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.app.core.domain.AppResult
import com.app.core.domain.DomainError
import com.app.core.domain.GeoLocation
import com.app.core.domain.LocationProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Ubicación del dispositivo con el proveedor de Google Play Services, que combina GPS, WiFi
 * y antenas para dar una posición buena gastando poca batería. No necesita API key.
 */
class FusedLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationProvider {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    override suspend fun getCurrentLocation(): AppResult<GeoLocation> {
        if (!hasLocationPermission()) {
            return AppResult.Error(
                DomainError.Unknown("Necesitamos el permiso de ubicación para saber desde dónde cocinás.")
            )
        }

        return try {
            // Primero una lectura fresca; si no hay, la última conocida del teléfono.
            val location = requestCurrentLocation() ?: requestLastLocation()
            if (location == null) {
                AppResult.Error(
                    DomainError.Unknown("No pudimos obtener tu ubicación. Revisá que el GPS esté activado.")
                )
            } else {
                AppResult.Success(GeoLocation(location.latitude, location.longitude))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            AppResult.Error(DomainError.Unknown("No tenemos permiso para leer tu ubicación."))
        } catch (e: Exception) {
            AppResult.Error(DomainError.Unknown("No pudimos obtener tu ubicación."))
        }
    }

    // Con cualquiera de los dos permisos (precisa o aproximada) alcanza para ubicar una ciudad.
    private fun hasLocationPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    // La API de Google devuelve una "Task" con callbacks; acá se convierte en una función suspend.
    // Si la corrutina se cancela, se cancela también el pedido al GPS.
    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val tokenSource = CancellationTokenSource()
        continuation.invokeOnCancellation { tokenSource.cancel() }
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestLastLocation(): Location? = suspendCancellableCoroutine { continuation ->
        client.lastLocation
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}
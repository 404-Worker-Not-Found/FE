package com.workernotfound.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class DeviceCoordinates(val latitude: Double, val longitude: Double)
enum class LocationFailure { PERMISSION, DISABLED, UNAVAILABLE, TIMEOUT }
class DeviceLocationException(val reason: LocationFailure) : Exception()

/** Reads a current GPS fix only; coordinates are never persisted or logged. */
class DeviceLocationProvider @Inject constructor(@ApplicationContext private val context: Context) {
    @SuppressLint("MissingPermission") // Exact permission is checked immediately before registration.
    suspend fun current(): DeviceCoordinates = withContext(Dispatchers.Main.immediate) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            throw DeviceLocationException(LocationFailure.PERMISSION)
        val manager = context.getSystemService(LocationManager::class.java)
            ?: throw DeviceLocationException(LocationFailure.UNAVAILABLE)
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) throw DeviceLocationException(LocationFailure.DISABLED)
        try {
            withTimeoutOrNull(20_000) {
                val location = suspendCancellableCoroutine<Location> { continuation ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val signal = CancellationSignal()
                        continuation.invokeOnCancellation { signal.cancel() }
                        manager.getCurrentLocation(LocationManager.GPS_PROVIDER, signal, ContextCompat.getMainExecutor(context)) { fix ->
                            if (continuation.isActive) {
                                if (fix == null) continuation.resumeWithException(DeviceLocationException(LocationFailure.UNAVAILABLE))
                                else continuation.resume(fix)
                            }
                        }
                    } else {
                        val listener = object : LocationListener {
                            override fun onLocationChanged(fix: Location) {
                                manager.removeUpdates(this)
                                if (continuation.isActive) continuation.resume(fix)
                            }
                            override fun onProviderDisabled(provider: String) {
                                manager.removeUpdates(this)
                                if (continuation.isActive) continuation.resumeWithException(DeviceLocationException(LocationFailure.DISABLED))
                            }
                            override fun onProviderEnabled(provider: String) = Unit
                            @Deprecated("Legacy Android callback")
                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                        }
                        continuation.invokeOnCancellation { manager.removeUpdates(listener) }
                        @Suppress("DEPRECATION")
                        manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
                    }
                }
                DeviceCoordinates(location.latitude, location.longitude)
            } ?: throw DeviceLocationException(LocationFailure.TIMEOUT)
        } catch (_: SecurityException) {
            throw DeviceLocationException(LocationFailure.PERMISSION)
        }
    }
}

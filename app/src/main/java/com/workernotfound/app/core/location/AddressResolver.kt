package com.workernotfound.app.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ResolvedAddress(val address: String, val latitude: Double, val longitude: Double)
class AddressResolver @Inject constructor(@ApplicationContext private val context: Context) {
    fun available() = Geocoder.isPresent()
    suspend fun find(query: String): List<ResolvedAddress> = lookup(query, null)
    suspend fun reverse(latitude: Double, longitude: Double): List<ResolvedAddress> = lookup(null, latitude to longitude)
    private suspend fun lookup(query: String?, point: Pair<Double, Double>?): List<ResolvedAddress> {
        if (!available()) throw IOException("Address lookup unavailable")
        val geocoder = Geocoder(context, Locale.KOREA)
        val result = withTimeout(15_000) {
            if (Build.VERSION.SDK_INT >= 33) suspendCancellableCoroutine<List<Address>> { continuation ->
                val listener = object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) { if (continuation.isActive) continuation.resume(addresses) }
                    override fun onError(errorMessage: String?) { if (continuation.isActive) continuation.resumeWithException(IOException("Address lookup failed")) }
                }
                if (point == null) geocoder.getFromLocationName(requireNotNull(query), 3, listener)
                else geocoder.getFromLocation(point.first, point.second, 3, listener)
            } else runInterruptible(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                (if (point == null) geocoder.getFromLocationName(requireNotNull(query), 3)
                else geocoder.getFromLocation(point.first, point.second, 3)).orEmpty()
            }
        }
        return result.filter { it.hasLatitude() && it.hasLongitude() }.map {
            ResolvedAddress(it.getAddressLine(0).orEmpty(), it.latitude, it.longitude)
        }.filter { it.address.isNotBlank() }.distinctBy { Triple(it.address, it.latitude, it.longitude) }
    }
}

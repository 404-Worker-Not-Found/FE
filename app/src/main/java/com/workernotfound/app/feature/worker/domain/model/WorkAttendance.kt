package com.workernotfound.app.feature.worker.domain.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Exact rules returned by work-service; no client-defined radius/window defaults. */
data class WorkAttendance(
    val status: String,
    val latitude: Double?,
    val longitude: Double?,
    val radiusMeters: Double,
    val earliestCheckInMillis: Long,
    val latestCheckInMillis: Long,
    val checkedIn: Boolean,
    val timeZone: String,
) {
    fun allowsTime(nowMillis: Long): Boolean = nowMillis in earliestCheckInMillis..latestCheckInMillis
    fun distanceFrom(latitude: Double, longitude: Double): Double? {
        val targetLat = this.latitude ?: return null
        val targetLng = this.longitude ?: return null
        val lat1 = Math.toRadians(targetLat)
        val lat2 = Math.toRadians(latitude)
        val deltaLat = lat2 - lat1
        val deltaLng = Math.toRadians(longitude - targetLng)
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) + cos(lat1) * cos(lat2) * sin(deltaLng / 2) * sin(deltaLng / 2)
        return 2 * 6_371_000 * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}

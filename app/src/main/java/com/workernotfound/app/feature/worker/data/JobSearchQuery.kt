package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.TimeSlot

internal fun JobFilter.hasCoordinates() = latitude != null && longitude != null &&
    latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0

internal fun JobFilter.searchQuery(page: Int): Map<String, String> = buildMap {
    put("page", page.toString()); put("size", "100")
    require((latitude == null && longitude == null) || hasCoordinates())
    if (hasCoordinates()) {
        put("workerLat", latitude.toString()); put("workerLng", longitude.toString())
        if (maxDistanceKm < JobFilter.MAX_DISTANCE_KM) put("maxDistanceKm", maxDistanceKm.toString())
    } else require(maxDistanceKm == JobFilter.MAX_DISTANCE_KM)
    if (minWage > 0) put("minWage", minWage.coerceAtLeast(10320).toString())
    if (urgentOnly) put("type", "URGENT")
    val hours = when (timeSlot) {
        TimeSlot.MORNING -> "00:00" to "11:59:59"
        TimeSlot.AFTERNOON -> "12:00" to "16:59:59"
        TimeSlot.EVENING -> "17:00" to "23:59:59"
        TimeSlot.ANY -> null
    }
    hours?.let { put("startTimeFrom", it.first); put("startTimeTo", it.second) }
}

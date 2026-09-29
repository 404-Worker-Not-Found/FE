package com.workernotfound.app.feature.owner.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Display formatting shared by the owner screens. */
internal object OwnerFormat {
    private val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    private val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
    private val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun won(amount: Int): String = "%,d원".format(amount)

    /** 230 → "3시간 50분", 45 → "45분", 120 → "2시간". */
    fun duration(minutes: Long): String {
        val hours = minutes / 60
        val rest = minutes % 60
        return when {
            hours == 0L -> "${rest}분"
            rest == 0L -> "${hours}시간"
            else -> "${hours}시간 ${rest}분"
        }
    }

    /** Stopwatch style "HH:MM:SS" (negative values clamp to 0). */
    fun elapsed(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0L) / 1000
        return "%02d:%02d:%02d".format(totalSeconds / 3600, totalSeconds % 3600 / 60, totalSeconds % 60)
    }

    /** "MM:SS" countdown (negative values clamp to 0). */
    fun countdown(millis: Long): String {
        val totalSeconds = (millis.coerceAtLeast(0L) + 999) / 1000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    fun clock(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zone).format(CLOCK)

    fun date(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zone).format(DATE)

    fun dateTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zone).format(DATE_TIME)
}

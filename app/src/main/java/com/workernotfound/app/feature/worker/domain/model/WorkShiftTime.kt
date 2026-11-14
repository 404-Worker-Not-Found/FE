package com.workernotfound.app.feature.worker.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The job API has no next-day flag; work records do. Equal end/start times span midnight. */
fun workShiftEndsAt(
    date: String,
    startTime: String,
    endTime: String,
    zone: ZoneId = ZoneId.of("Asia/Seoul"),
    endTimeNextDay: Boolean = !LocalTime.parse(endTime).isAfter(LocalTime.parse(startTime)),
): Instant {
    val endDate = LocalDate.parse(date).plusDays(if (endTimeNextDay) 1 else 0)
    return endDate.atTime(LocalTime.parse(endTime)).atZone(zone).toInstant()
}

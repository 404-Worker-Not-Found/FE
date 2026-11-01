package com.workernotfound.app.feature.worker.data.remote

import android.content.Context
import com.workernotfound.app.R
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

class WorkerJobMapper @Inject constructor(@ApplicationContext private val context: Context) {
    fun card(dto: JobCardResponseDto) = WorkerJobPosting(
        id = dto.id.toString(), storeName = dto.storeName, category = dto.categoryName,
        workTime = "${dto.startTime.take(5)} ~ ${dto.endTime.take(5)}",
        hourlyWage = dto.baseHourlyWage + dto.extraWage,
        distanceText = dto.distanceKm?.let { if (it < 1) "${(it * 1000).toInt()}m" else String.format(Locale.KOREA, "%.1fkm", it) }
            ?: context.getString(R.string.backend_distance_unknown),
        isUrgent = dto.isUrgent,
        deadlineText = if (dto.remainingMinutes > 0) context.getString(R.string.backend_deadline_minutes, dto.remainingMinutes)
            else context.getString(R.string.backend_deadline_closed),
    )

    fun detail(dto: JobDetailResponseDto) = WorkerJobDetail(
        id = dto.id.toString(), storeName = dto.storeName, category = dto.categoryName, address = dto.address,
        distanceKm = null, baseWage = dto.baseHourlyWage,
        urgentBonus = 0, timeBonus = 0, extraWage = dto.extraWage,
        workDate = dto.workDate, workTimeRange = "${dto.startTime.take(5)} ~ ${dto.endTime.take(5)}",
        totalWorkHours = context.getString(R.string.backend_duration, dto.workHours, dto.workMinutes),
        startsInSeconds = startsInSeconds(dto.workDate, dto.startTime), description = dto.description,
        withinRadius = null, radiusText = context.getString(R.string.backend_radius_unknown),
        applicantCount = dto.applicantCount, matchScore = null, isUrgent = false,
    )
}

/** Backend uses Korea local date/time, independent of the device's timezone. */
internal fun startsInSeconds(date: String, time: String, clock: Clock = Clock.systemUTC()): Long {
    val start = LocalDate.parse(date).atTime(LocalTime.parse(time)).atZone(ZoneId.of("Asia/Seoul")).toInstant()
    return Duration.between(clock.instant(), start).seconds.coerceAtLeast(0)
}

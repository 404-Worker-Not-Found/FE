package com.workernotfound.app.feature.worker.data.remote

import android.content.Context
import com.workernotfound.app.R
import com.workernotfound.app.core.ui.KoreanDateTime
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
        deadlineText = deadlineText(context, dto.remainingMinutes),
        distanceKm = dto.distanceKm?.takeIf { it.isFinite() && it >= 0 },
        remainingMinutes = dto.remainingMinutes,
        status = dto.status,
        workDate = dto.workDate,
        startTime = dto.startTime.take(5),
        endTime = dto.endTime.take(5),
    )

    fun detail(dto: JobDetailResponseDto) = WorkerJobDetail(
        id = dto.id.toString(), storeName = dto.storeName, category = dto.categoryName, address = dto.address,
        distanceKm = null, baseWage = dto.baseHourlyWage, extraWage = dto.extraWage,
        workDate = KoreanDateTime.workDate(dto.workDate), workTimeRange = "${dto.startTime.take(5)} ~ ${dto.endTime.take(5)}",
        totalWorkHours = context.getString(R.string.backend_duration, dto.workHours, dto.workMinutes),
        startsInSeconds = startsInSeconds(dto.workDate, dto.startTime), description = dto.description,
        applicantCount = dto.applicantCount, matchScore = null, isUrgent = false,
    )
}

/** 마감까지 남은 시간: `마감 39시간 34분 전`, 정각이면 `마감 2시간 전`, 1시간 미만이면 `마감 45분 전`. */
private fun deadlineText(context: Context, remainingMinutes: Long): String {
    val (hours, minutes) = deadlineParts(remainingMinutes) ?: return context.getString(R.string.backend_deadline_closed)
    return when {
        hours == 0L -> context.getString(R.string.backend_deadline_minutes, minutes)
        minutes == 0L -> context.getString(R.string.worker_deadline_hours, hours)
        else -> context.getString(R.string.worker_deadline_hours_minutes, hours, minutes)
    }
}

/** (hours, minutes) left before the application deadline, or null once it has passed. */
internal fun deadlineParts(remainingMinutes: Long): Pair<Long, Long>? =
    if (remainingMinutes <= 0) null else remainingMinutes / 60 to remainingMinutes % 60

/** Backend uses Korea local date/time, independent of the device's timezone. */
internal fun startsInSeconds(date: String, time: String, clock: Clock = Clock.systemUTC()): Long {
    val start = LocalDate.parse(date).atTime(LocalTime.parse(time)).atZone(ZoneId.of("Asia/Seoul")).toInstant()
    return Duration.between(clock.instant(), start).seconds.coerceAtLeast(0)
}

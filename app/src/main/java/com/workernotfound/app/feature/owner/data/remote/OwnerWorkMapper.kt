package com.workernotfound.app.feature.owner.data.remote

import android.content.Context
import com.workernotfound.app.R
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import com.workernotfound.app.feature.worker.data.remote.AttendancePolicyResponseDto
import com.workernotfound.app.feature.worker.data.remote.JobDetailResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkResponseDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class OwnerWorkMapper @Inject constructor(@ApplicationContext private val context: Context) {
    fun map(work: WorkResponseDto, job: JobDetailResponseDto, policy: AttendancePolicyResponseDto): OwnerWork {
        val (start, end) = scheduledInterval(work.workDate, work.startTime, work.endTime, work.endTimeNextDay, policy.timeZone)
        return OwnerWork(
            id = work.workId.toString(), postingId = work.jobPostId.toString(), workSummary = job.description,
            workerId = work.workerMemberId.toString(), workerName = context.getString(R.string.backend_member_reference, work.workerMemberId),
            scheduledStartMillis = start, scheduledEndMillis = end,
            hourlyWage = job.baseHourlyWage, bonusPerHour = job.extraWage,
            isAttendanceVerified = work.checkedInAt != null, workerDistanceMeters = null,
            status = when (work.status) {
                "SCHEDULED", "CHECKED_IN", "IN_PROGRESS" -> WorkProgressStatus.ACTIVE
                "COMPLETED" -> WorkProgressStatus.COMPLETED
                "CANCELED" -> WorkProgressStatus.CANCELED
                "FAILED" -> WorkProgressStatus.FAILED
                "NO_SHOW" -> WorkProgressStatus.NO_SHOW
                else -> throw ApiException("UNKNOWN_WORK_STATUS")
            },
            backendStatus = work.status,
            targetLatitude = work.latitude, targetLongitude = work.longitude,
            actualStartedMillis = work.startedAt?.let { LocalDateTime.parse(it).atZone(ZoneId.of(policy.timeZone)).toInstant().toEpochMilli() },
            canStart = work.status == "CHECKED_IN",
            completionAllowed = policy.completionRole == "OWNER", allowEarlyCompletion = policy.allowEarlyCompletion,
        )
    }
}

internal fun scheduledInterval(date: String, startTime: String, endTime: String, nextDay: Boolean, timeZone: String): Pair<Long, Long> {
    val day = LocalDate.parse(date)
    val zone = ZoneId.of(timeZone)
    val start = day.atTime(LocalTime.parse(startTime)).atZone(zone).toInstant().toEpochMilli()
    val end = day.plusDays(if (nextDay) 1 else 0).atTime(LocalTime.parse(endTime)).atZone(zone).toInstant().toEpochMilli()
    return start to end
}

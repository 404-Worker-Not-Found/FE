package com.workernotfound.app.feature.worker.domain

import com.workernotfound.app.feature.worker.domain.model.ApplyAvailability
import com.workernotfound.app.feature.worker.domain.model.ApplyStatus
import com.workernotfound.app.feature.worker.domain.model.AvailableTime
import com.workernotfound.app.feature.worker.domain.model.JobEligibility
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.model.WorkerPreference
import com.workernotfound.app.feature.worker.domain.model.availability
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JobEligibilityTest {
    private val preference = WorkerPreference(
        name = "김알바", desiredHourlyWage = 11000, activityRadiusKm = 3,
        baseLatitude = 37.5, baseLongitude = 127.0,
        preferredBusinessTypes = listOf("카페/음료"),
        // 2026-06-17 is a Wednesday.
        availableTimes = listOf(AvailableTime(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))),
    )

    private fun job(
        distanceKm: Double? = 1.2, remaining: Long = 30, status: String = "OPEN", wage: Int = 12000,
        category: String = "카페/음료", start: String = "10:00", end: String = "14:00",
        applied: ApplyStatus = ApplyStatus.AVAILABLE,
    ) = WorkerJobPosting(
        id = "1", storeName = "가게", category = category, workTime = "$start ~ $end", hourlyWage = wage,
        distanceText = "", isUrgent = false, applyStatus = applied, distanceKm = distanceKm,
        remainingMinutes = remaining, status = status, workDate = "2026-06-17", startTime = start, endTime = end,
    )

    @Test fun applicableNeedsOpenDeadlineRadiusAndNotApplied() {
        assertTrue(JobEligibility.isApplicable(job(), preference))
        assertFalse(JobEligibility.isApplicable(job(status = "MATCHING"), preference))
        assertFalse(JobEligibility.isApplicable(job(remaining = 0), preference))
        assertFalse(JobEligibility.isApplicable(job(distanceKm = 3.5), preference))
        assertFalse(JobEligibility.isApplicable(job(applied = ApplyStatus.APPLIED), preference))
        // Unknown distance cannot be confirmed inside the radius.
        assertFalse(JobEligibility.isApplicable(job(distanceKm = null), preference))
    }

    @Test fun radiusBoundaryIsInclusive() {
        assertEquals(true, JobEligibility.withinRadius(job(distanceKm = 3.0), preference))
        assertNull(JobEligibility.withinRadius(job(), preference.copy(activityRadiusKm = null)))
    }

    @Test fun detailAvailabilityFromCard() {
        assertEquals(ApplyAvailability.UNKNOWN, JobEligibility.availability(null, preference))
        assertEquals(ApplyAvailability.CLOSED, JobEligibility.availability(job(remaining = 0), preference))
        assertEquals(ApplyAvailability.OUT_OF_RADIUS, JobEligibility.availability(job(distanceKm = 4.0), preference))
        assertEquals(ApplyAvailability.AVAILABLE, JobEligibility.availability(job(), preference))
        assertEquals(ApplyAvailability.UNKNOWN, JobEligibility.availability(job(distanceKm = null), preference))
    }
}

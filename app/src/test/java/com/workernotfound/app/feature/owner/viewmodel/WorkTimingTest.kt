package com.workernotfound.app.feature.owner.viewmodel

import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkTimingTest {

    private val start = 1_000_000_000L
    private val minute = 60_000L

    private fun work(verified: Boolean, status: WorkProgressStatus = WorkProgressStatus.ACTIVE) = OwnerWork(
        id = "w", postingId = "p", workSummary = "s", workerId = "a", workerName = "n",
        scheduledStartMillis = start, scheduledEndMillis = start + 240 * minute,
        hourlyWage = 12000, bonusPerHour = 0, isAttendanceVerified = verified,
        workerDistanceMeters = 0, status = status,
    )

    @Test
    fun `elapsed time is zero before start`() {
        val timing = WorkTiming(work(verified = false), start - minute)
        assertFalse(timing.hasStarted)
        assertEquals(0L, timing.elapsedMillis)
    }

    @Test
    fun `complete requires started shift and verified attendance`() {
        assertTrue(WorkTiming(work(verified = true), start + minute).canCompleteWork)
        assertFalse(WorkTiming(work(verified = false), start + minute).canCompleteWork)
        assertFalse(WorkTiming(work(verified = true), start - minute).canCompleteWork)
    }

    @Test
    fun `no-show is not available once the shift is no longer active`() {
        val late = start + 45 * minute
        assertTrue(WorkTiming(work(verified = false), late).canConfirmNoShow)
        assertFalse(WorkTiming(work(verified = false, status = WorkProgressStatus.NO_SHOW), late).canConfirmNoShow)
    }
}

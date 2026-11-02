package com.workernotfound.app.feature.owner.data.remote

import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import com.workernotfound.app.feature.owner.viewmodel.WorkTiming
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class OwnerWorkContractTest {
    @Test fun overnightShiftUsesExplicitNextDayFlagAndServerTimezone() {
        val (start, end) = scheduledInterval("2026-11-01", "22:00:00", "02:00:00", true, "Asia/Seoul")
        assertEquals(Instant.parse("2026-11-01T13:00:00Z").toEpochMilli(), start)
        assertEquals(Instant.parse("2026-11-01T17:00:00Z").toEpochMilli(), end)
        assertEquals(4 * 60 * 60 * 1000L, end - start)
    }

    @Test fun backendWorkDoesNotEnableManualNoShowAndUsesActualStart() {
        val work = work().copy(actualStartedMillis = 2000, scheduledStartMillis = 1000)
        val timing = WorkTiming(work, nowMillis = 3000)
        assertEquals(1000L, timing.elapsedMillis)
        assertFalse(WorkTiming(work.copy(isAttendanceVerified = false), nowMillis = 10_000_000).canConfirmNoShow)
    }

    @Test fun completionUnlocksAtServerEndTimeWithoutReloadingSnapshot() {
        val work = work().copy(actualStartedMillis = 1000, scheduledEndMillis = 5000, allowEarlyCompletion = false)
        assertFalse(WorkTiming(work, nowMillis = 4999).canCompleteWork)
        assertTrue(WorkTiming(work, nowMillis = 5000).canCompleteWork)
        assertFalse(WorkTiming(work.copy(completionAllowed = false), nowMillis = 9000).canCompleteWork)
    }

    private fun work() = OwnerWork("1", "2", "work", "3", "member #3", 1000, 5000, 10320, 500,
        isAttendanceVerified = true, workerDistanceMeters = null, status = WorkProgressStatus.ACTIVE,
        backendStatus = "IN_PROGRESS")
}

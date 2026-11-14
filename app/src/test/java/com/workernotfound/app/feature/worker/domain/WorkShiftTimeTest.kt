package com.workernotfound.app.feature.worker.domain

import com.workernotfound.app.feature.worker.domain.model.workShiftEndsAt
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkShiftTimeTest {
    @Test fun daytimeShiftEndsOnWorkDate() {
        assertEquals(Instant.parse("2026-11-19T04:00:00Z"),
            workShiftEndsAt("2026-11-19", "09:00", "13:00"))
    }

    @Test fun overnightShiftEndsOnNextDay() {
        assertEquals(Instant.parse("2026-11-19T18:00:00Z"),
            workShiftEndsAt("2026-11-19", "22:00", "03:00"))
    }

    @Test fun workRecordNextDayFlagTakesPrecedence() {
        assertEquals(Instant.parse("2026-11-20T04:00:00Z"),
            workShiftEndsAt("2026-11-19", "09:00", "13:00", endTimeNextDay = true))
    }
}

package com.workernotfound.app.feature.worker.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeadlinePartsTest {
    @Test fun splitsRemainingMinutesIntoDaysHoursAndMinutes() {
        assertEquals(Triple(4L, 2L, 47L), deadlineParts(98 * 60 + 47L))
        assertEquals(Triple(1L, 15L, 34L), deadlineParts(2374))
        assertEquals(Triple(0L, 2L, 0L), deadlineParts(120))
        assertEquals(Triple(0L, 0L, 45L), deadlineParts(45))
        assertEquals(Triple(1L, 0L, 0L), deadlineParts(24 * 60))
    }

    @Test fun passedDeadlineHasNoParts() {
        assertNull(deadlineParts(0))
        assertNull(deadlineParts(-5))
    }
}

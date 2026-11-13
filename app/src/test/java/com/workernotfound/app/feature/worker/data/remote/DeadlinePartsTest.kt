package com.workernotfound.app.feature.worker.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeadlinePartsTest {
    @Test fun splitsRemainingMinutesIntoHoursAndMinutes() {
        assertEquals(39L to 34L, deadlineParts(2374))
        assertEquals(2L to 0L, deadlineParts(120))
        assertEquals(0L to 45L, deadlineParts(45))
        assertEquals(1L to 0L, deadlineParts(60))
    }

    @Test fun passedDeadlineHasNoParts() {
        assertNull(deadlineParts(0))
        assertNull(deadlineParts(-5))
    }
}

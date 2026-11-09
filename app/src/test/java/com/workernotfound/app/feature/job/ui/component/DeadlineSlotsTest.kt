package com.workernotfound.app.feature.job.ui.component

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DeadlineSlotsTest {
    @Test fun slotsStartAfterNowAndEndBeforeWorkStart() {
        val slots = deadlineSlots(LocalDateTime.of(2026, 11, 9, 21, 40), LocalDateTime.of(2026, 11, 10, 0, 0))
        assertEquals(listOf(LocalDateTime.of(2026, 11, 9, 22, 0), LocalDateTime.of(2026, 11, 9, 22, 30),
            LocalDateTime.of(2026, 11, 9, 23, 0), LocalDateTime.of(2026, 11, 9, 23, 30)), slots)
    }
    @Test fun noSlotsWithoutWorkStart() = assertEquals(emptyList<LocalDateTime>(), deadlineSlots(LocalDateTime.of(2026, 11, 9, 9, 0), null))
}

package com.workernotfound.app.feature.owner.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoShowPolicyTest {

    private val start = 1_000_000_000L
    private val minute = 60_000L

    @Test
    fun `disabled before the shift starts`() {
        assertFalse(NoShowPolicy.canConfirmNoShow(start, start - minute, isAttendanceVerified = false))
    }

    @Test
    fun `disabled at exactly 30 minutes after start`() {
        assertFalse(NoShowPolicy.canConfirmNoShow(start, start + 30 * minute, isAttendanceVerified = false))
    }

    @Test
    fun `enabled once more than 30 minutes have passed`() {
        assertTrue(NoShowPolicy.canConfirmNoShow(start, start + 30 * minute + 1, isAttendanceVerified = false))
    }

    @Test
    fun `disabled when attendance is verified`() {
        assertFalse(NoShowPolicy.canConfirmNoShow(start, start + 60 * minute, isAttendanceVerified = true))
    }

    @Test
    fun `remaining time counts down to zero`() {
        assertEquals(10 * minute, NoShowPolicy.millisUntilEnabled(start, start + 20 * minute))
        assertEquals(0L, NoShowPolicy.millisUntilEnabled(start, start + 45 * minute))
    }
}

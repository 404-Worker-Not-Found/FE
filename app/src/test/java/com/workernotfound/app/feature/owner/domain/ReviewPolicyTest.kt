package com.workernotfound.app.feature.owner.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPolicyTest {

    private val end = 1_000_000_000_000L
    private val hour = 3_600_000L
    private val day = 24 * hour

    @Test
    fun `disabled before the shift ends`() {
        assertFalse(ReviewPolicy.canWriteReview(end, end - hour))
    }

    @Test
    fun `enabled right after the shift ends`() {
        assertTrue(ReviewPolicy.canWriteReview(end, end))
        assertTrue(ReviewPolicy.canWriteReview(end, end + 3 * day))
    }

    @Test
    fun `enabled up to exactly seven days`() {
        assertTrue(ReviewPolicy.canWriteReview(end, end + 7 * day))
    }

    @Test
    fun `disabled after seven days`() {
        assertFalse(ReviewPolicy.canWriteReview(end, end + 7 * day + 1))
    }

    @Test
    fun `remaining days round up and stop at zero`() {
        assertEquals(7L, ReviewPolicy.remainingDays(end, end))
        assertEquals(5L, ReviewPolicy.remainingDays(end, end + 2 * day + hour))
        assertEquals(0L, ReviewPolicy.remainingDays(end, end + 8 * day))
    }
}

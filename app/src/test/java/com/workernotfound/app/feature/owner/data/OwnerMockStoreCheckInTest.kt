package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.RematchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerMockStoreCheckInTest {

    private val store = OwnerMockStore()
    private val now = System.currentTimeMillis()

    @Test
    fun `matched worker checks in automatically once the ETA has passed`() {
        // 이하은 (a2) has a 12 minute ETA.
        val workId = store.confirmMatch("1", "a2", now)

        assertFalse(store.work(workId, now + minutes(11)).isAttendanceVerified)
        val arrived = store.work(workId, now + minutes(12))
        assertTrue(arrived.isAttendanceVerified)
        assertEquals(0, arrived.workerDistanceMeters)
    }

    @Test
    fun `rematched worker checks in after their ETA from the rematch time`() {
        store.confirmNoShow("w6")
        val newWork = (store.rematch("w6", now) as RematchResult.Success).newWork

        // 정하늘 (a10) has a 9 minute ETA.
        assertFalse(store.activeWorks(now + minutes(8)).first { it.id == newWork.id }.isAttendanceVerified)
        assertTrue(store.activeWorks(now + minutes(9)).first { it.id == newWork.id }.isAttendanceVerified)
    }

    @Test
    fun `seeded no-show demo shifts never check in`() {
        val later = now + minutes(24 * 60)
        assertFalse(store.work("w6", later).isAttendanceVerified)
        assertFalse(store.work("w7", later).isAttendanceVerified)
    }

    private fun minutes(value: Long) = value * 60_000L
}

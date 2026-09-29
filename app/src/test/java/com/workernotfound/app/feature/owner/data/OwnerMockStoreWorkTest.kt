package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerMockStoreWorkTest {

    private val store = OwnerMockStore()
    private val now = System.currentTimeMillis()

    @Test
    fun `seeded shifts are listed as active`() {
        val ids = store.activeWorks(now).map { it.id }
        assertTrue(ids.containsAll(listOf("w5", "w6", "w7")))
    }

    @Test
    fun `completing a shift calculates the settlement and awaits approval`() {
        val settlement = store.completeWork("w5", now)

        assertEquals(WorkProgressStatus.AWAITING_SETTLEMENT, store.work("w5", now).status)
        assertEquals(settlement, store.settlement("w5"))
        assertTrue(settlement.totalPay > 0)
    }

    @Test
    fun `approving the settlement records a completed past posting`() {
        val settlement = store.completeWork("w5", now)
        store.confirmSettlement("w5", now)

        assertFalse(store.activeWorks(now).any { it.id == "w5" })
        assertEquals(PostingStatus.DONE, store.postings(now).first { it.id == "5" }.status)
        val past = store.pastPosting("5")
        assertEquals(PastPostingStatus.COMPLETED, past.status)
        assertEquals(settlement.totalPay, past.settlement?.totalPay)
        assertEquals(now, past.settlement?.paidAtMillis)
    }
}

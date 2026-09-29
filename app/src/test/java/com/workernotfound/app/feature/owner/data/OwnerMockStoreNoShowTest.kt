package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.domain.model.RematchResult
import com.workernotfound.app.feature.owner.domain.model.WorkProgressStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerMockStoreNoShowTest {

    private val store = OwnerMockStore()
    private val now = System.currentTimeMillis()

    @Test
    fun `rematch picks the remaining applicant and restarts the shift`() {
        store.confirmNoShow("w6")
        val result = store.rematch("w6", now)

        assertTrue(result is RematchResult.Success)
        val success = result as RematchResult.Success
        assertEquals("정하늘", success.applicant.name)
        assertTrue(success.applicant.isMatched)
        assertEquals(now, success.newWork.scheduledStartMillis)
        assertEquals(WorkProgressStatus.NO_SHOW, store.work("w6", now).status)
        assertTrue(store.activeWorks(now).any { it.id == success.newWork.id })
    }

    @Test
    fun `rematch fails when nobody else applied`() {
        store.confirmNoShow("w7")
        assertEquals(RematchResult.Failure, store.rematch("w7", now))
    }

    @Test
    fun `reopening after a failed rematch puts the posting back to recruiting`() {
        store.confirmNoShow("w7")
        store.rematch("w7", now)
        store.reopenPosting("w7")

        val board = store.applicantBoard("7", now)
        assertEquals(PostingStatus.RECRUITING, board.posting.status)
        assertNull(board.matchedApplicantId)
    }

    @Test
    fun `closing after a failed rematch records a no-show past posting`() {
        store.confirmNoShow("w7")
        store.rematch("w7", now)
        store.closePosting("w7")

        assertEquals(PostingStatus.CLOSED, store.postings(now).first { it.id == "7" }.status)
        assertEquals(PastPostingStatus.NO_SHOW, store.pastPosting("7").status)
    }

    @Test(expected = IllegalStateException::class)
    fun `rematch requires a confirmed no-show`() {
        store.rematch("w6", now)
    }
}

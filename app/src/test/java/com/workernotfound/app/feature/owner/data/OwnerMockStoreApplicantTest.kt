package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerMockStoreApplicantTest {

    private val store = OwnerMockStore()
    private val now = System.currentTimeMillis()

    @Test
    fun `applicant count on home matches the applicant board`() {
        val homeCount = store.postings(now).first { it.id == "1" }.applicantCount
        val board = store.applicantBoard("1", now)
        assertEquals(homeCount, board.applicants.size)
    }

    @Test
    fun `applicants are sorted by match score descending`() {
        val scores = store.applicantBoard("1", now).applicants.map { it.matchScore }
        assertEquals(scores.sortedDescending(), scores)
    }

    @Test
    fun `category experience reflects the posting category`() {
        val applicants = store.applicantBoard("1", now).applicants.associateBy { it.id }
        assertTrue(applicants.getValue("a1").hasCategoryExperience)
        assertFalse(applicants.getValue("a2").hasCategoryExperience)
    }

    @Test
    fun `confirming a match closes the posting and opens a shift`() {
        val workId = store.confirmMatch("1", "a2")

        val board = store.applicantBoard("1", now)
        assertEquals("a2", board.matchedApplicantId)
        assertEquals(PostingStatus.CLOSED, board.posting.status)
        assertTrue(store.activeWorks().any { it.id == workId && it.workerName == "이하은" })
        assertFalse(store.applicantDetail("1", "a1").canConfirmMatch)
    }

    @Test(expected = IllegalStateException::class)
    fun `a posting cannot be matched twice`() {
        store.confirmMatch("1", "a2")
        store.confirmMatch("1", "a1")
    }

    @Test
    fun `recent work history is limited to five entries`() {
        val detail = store.applicantDetail("3", "a4")
        assertTrue(detail.recentWorks.size <= 5)
    }
}

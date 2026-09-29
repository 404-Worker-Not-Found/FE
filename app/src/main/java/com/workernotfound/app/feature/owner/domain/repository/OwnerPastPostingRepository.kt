package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.PastPosting

/**
 * Owner past postings (UI spec 2-6). Mock-backed for the demo
 * (decision: Demo Scope and Mock-First Strategy).
 */
interface OwnerPastPostingRepository {
    suspend fun getPastPostings(): List<PastPosting>

    suspend fun getPastPosting(postingId: String): PastPosting
}

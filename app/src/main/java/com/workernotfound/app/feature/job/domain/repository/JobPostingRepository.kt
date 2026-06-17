package com.workernotfound.app.feature.job.domain.repository

import com.workernotfound.app.feature.job.domain.model.JobPostingDraft

/**
 * Creates owner job postings (UI spec 2-2). Mock-backed for the demo; replace
 * with a network implementation when the backend contract is confirmed
 * (decision: Demo Scope and Mock-First Strategy).
 */
interface JobPostingRepository {
    /** Returns the created posting id on success. */
    suspend fun createPosting(draft: JobPostingDraft): Result<String>
}

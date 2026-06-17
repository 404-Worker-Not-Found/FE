package com.workernotfound.app.feature.job.data

import com.workernotfound.app.feature.job.domain.model.JobPostingDraft
import com.workernotfound.app.feature.job.domain.repository.JobPostingRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Mock implementation: simulates a network create with a short delay. */
class JobPostingRepositoryImpl @Inject constructor() : JobPostingRepository {

    override suspend fun createPosting(draft: JobPostingDraft): Result<String> {
        delay(600)
        // Demo: always succeeds. The error path stays wired for the failure dialog.
        return Result.success("posting_${draft.hashCode()}")
    }
}

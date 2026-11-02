package com.workernotfound.app.feature.job.data

import android.content.Context
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.job.domain.model.JobPostingDraft
import com.workernotfound.app.feature.job.domain.repository.JobPostingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay

/** Mock implementation: simulates a network create with a short delay. */
class JobPostingRepositoryImpl @Inject constructor(private val backend: BackendAccess, @ApplicationContext private val context: Context) : JobPostingRepository {

    override suspend fun createPosting(draft: JobPostingDraft): Result<String> {
        if (!BuildConfig.USE_DEMO_DATA) return Result.failure(IllegalStateException(context.getString(R.string.backend_posting_pending)))
        delay(600)
        // Demo: always succeeds. The error path stays wired for the failure dialog.
        return Result.success("posting_${draft.hashCode()}")
    }
}

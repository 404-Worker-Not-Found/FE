package com.workernotfound.app.feature.history.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.history.domain.*
import com.workernotfound.app.feature.owner.data.SettlementRemoteDataSource
import com.workernotfound.app.feature.owner.domain.model.ServerSettlement
import com.workernotfound.app.feature.worker.data.remote.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class WorkHistoryRepositoryImpl @Inject constructor(private val backend: BackendAccess,
    private val works: WorkerWorkApiService, private val jobs: WorkerJobApiService,
    private val settlements: SettlementRemoteDataSource) : WorkHistoryRepository {
    override suspend fun getAll(): List<WorkHistoryEntry> {
        backend.requireSession()
        val records = mutableListOf<WorkResponseDto>()
        var page = 0
        do {
            val result = works.list(page).requireData()
            if (result.totalPages < 0 || (result.content.isEmpty() && page + 1 < result.totalPages))
                throw ApiException("INVALID_WORK_PAGE")
            records += result.content
            page++
        } while (page < result.totalPages)
        val payouts = settlements.getAll().groupBy { it.workId }
        val jobCache = mutableMapOf<Long, JobDetailResponseDto>()
        return records.distinctBy { it.workId }.filter { isHistoricalWork(it.status) }.map { work ->
            val job = jobCache[work.jobPostId] ?: jobs.detail(work.jobPostId).requireData().also { jobCache[work.jobPostId] = it }
            if (job.id != work.jobPostId) throw ApiException("WORK_JOB_MISMATCH")
            val payout = matchingSettlement(work.workId, work.jobPostId, payouts[work.workId].orEmpty())
            WorkHistoryEntry(work.workId, work.jobPostId, job.storeName, work.workDate,
                work.startTime, work.endTime, work.endTimeNextDay, work.status, work.completedAt, payout)
        }.sortedWith(compareByDescending<WorkHistoryEntry> { it.date }.thenByDescending { it.workId })
    }
}
internal fun isHistoricalWork(status: String): Boolean = when (status) {
    "COMPLETED", "CANCELED", "FAILED", "NO_SHOW" -> true
    "SCHEDULED", "CHECKED_IN", "IN_PROGRESS" -> false
    else -> throw ApiException("UNKNOWN_WORK_STATUS")
}
internal fun matchingSettlement(workId: Long, jobId: Long, records: List<ServerSettlement>): ServerSettlement? {
    if (records.size > 1 || records.any { it.workId != workId || it.jobPostId != jobId })
        throw ApiException("AMBIGUOUS_WORK_SETTLEMENT")
    return records.singleOrNull()
}
@Module
@InstallIn(SingletonComponent::class)
object WorkHistoryModule {
    @Provides fun repository(implementation: WorkHistoryRepositoryImpl): WorkHistoryRepository = implementation
}

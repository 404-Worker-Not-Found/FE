package com.workernotfound.app.feature.owner.data

import android.content.Context
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.data.remote.OwnerJobApiService
import com.workernotfound.app.feature.owner.data.remote.OwnerJobCardResponseDto
import com.workernotfound.app.feature.owner.data.remote.scheduledInterval
import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.domain.repository.OwnerPastPostingRepository
import com.workernotfound.app.feature.owner.domain.repository.OwnerSettlementRepository
import com.workernotfound.app.feature.worker.data.remote.JobDetailResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerWorkApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay

class OwnerPastPostingRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val store: OwnerMockStore,
    private val works: WorkerWorkApiService,
    private val jobs: WorkerJobApiService,
    private val ownerJobs: OwnerJobApiService,
    private val settlements: OwnerSettlementRepository,
    @ApplicationContext private val context: Context,
) : OwnerPastPostingRepository {
    override suspend fun getPastPostings(): List<PastPosting> {
        if (BuildConfig.USE_DEMO_DATA) { delay(MOCK_DELAY_MILLIS); return store.pastPostings() }
        backend.requireSession()
        val allWorks = loadWorks()
        val postings = loadPostings()
        val amounts = settlements.getAll().associateBy { it.workId }
        val zone = works.policy().requireData().timeZone
        val details = mutableMapOf<Long, JobDetailResponseDto>()
        suspend fun detail(id: Long): JobDetailResponseDto = details[id]
            ?: jobs.detail(id).requireData().also { details[id] = it }
        val result = mutableListOf<PastPosting>()
        for (work in allWorks.filter { it.status in TERMINAL_STATUSES }) {
            val job = detail(work.jobPostId)
            val (start, end) = scheduledInterval(work.workDate, work.startTime, work.endTime, work.endTimeNextDay, zone)
            result += PastPosting(
                postingId = work.jobPostId.toString(), workSummary = postings.firstOrNull { it.id == work.jobPostId }?.title ?: job.description,
                workerName = context.getString(R.string.backend_member_reference, work.workerMemberId),
                workStartMillis = start, workEndMillis = end, hourlyWage = job.baseHourlyWage + job.extraWage,
                status = terminalPostingStatus(work.status), settlement = null,
                workId = work.workId.toString(), serverSettlement = amounts[work.workId],
            )
        }
        for (post in postings.filter { it.status == "CLOSED" && allWorks.none { work -> work.jobPostId == it.id } }) {
            val job = detail(post.id)
            val (start, end) = scheduledInterval(post.workDate, post.startTime, post.endTime, post.isEndTimeNextDay, zone)
            result += PastPosting(post.id.toString(), post.title, context.getString(R.string.past_no_worker),
                start, end, job.baseHourlyWage + job.extraWage, PastPostingStatus.CLOSED, null)
        }
        return result.sortedByDescending { it.workStartMillis }
    }

    override suspend fun getPastPosting(postingId: String): PastPosting {
        if (BuildConfig.USE_DEMO_DATA) { delay(MOCK_DELAY_MILLIS); return store.pastPosting(postingId) }
        return getPastPostings().firstOrNull { it.entryId == postingId }
            ?: throw NoSuchElementException()
    }

    private suspend fun loadWorks(): List<WorkResponseDto> {
        val result = mutableListOf<WorkResponseDto>()
        var page = 0
        do {
            val response = works.list(page).requireData()
            result += response.content
            page++
        } while (page < response.totalPages)
        return result
    }

    private suspend fun loadPostings(): List<OwnerJobCardResponseDto> {
        val result = mutableListOf<OwnerJobCardResponseDto>()
        var page = 0
        do {
            val response = ownerJobs.list(page).requireData()
            result += response.jobs
            page++
        } while (page < response.totalPages)
        return result
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
        val TERMINAL_STATUSES = setOf("COMPLETED", "NO_SHOW", "CANCELED", "FAILED")
    }
}

internal fun terminalPostingStatus(workStatus: String): PastPostingStatus = when (workStatus) {
    "COMPLETED" -> PastPostingStatus.COMPLETED
    "NO_SHOW" -> PastPostingStatus.NO_SHOW
    "CANCELED" -> PastPostingStatus.CANCELED
    "FAILED" -> PastPostingStatus.FAILED
    else -> throw IllegalArgumentException("Not a terminal work status")
}

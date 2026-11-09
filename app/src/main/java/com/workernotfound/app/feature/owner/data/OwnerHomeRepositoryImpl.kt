package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.data.remote.OwnerJobApiService
import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting
import com.workernotfound.app.feature.owner.domain.model.PostingStatus
import com.workernotfound.app.feature.owner.domain.repository.OwnerHomeRepository
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import javax.inject.Inject

class OwnerHomeRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val api: OwnerJobApiService,
    private val jobs: WorkerJobApiService,
) : OwnerHomeRepository {

    override suspend fun getMyPostings(): List<OwnerJobPosting> {
        backend.requireSession()
        val all = mutableListOf<OwnerJobPosting>()
        var page = 0
        do {
            val result = api.list(page).requireData()
            for (post in result.jobs) {
                val job = jobs.detail(post.id).requireData()
                all += OwnerJobPosting(
                    id = post.id.toString(), workSummary = post.title, workDate = post.workDate,
                    workTime = "${post.startTime.take(5)} ~ ${post.endTime.take(5)}",
                    hourlyWage = job.baseHourlyWage + job.extraWage,
                    status = when {
                        post.isFundingBlocked -> PostingStatus.FUNDING_BLOCKED
                        post.status == "PAYMENT_PENDING" -> PostingStatus.PAYMENT_PENDING
                        post.status == "OPEN" -> PostingStatus.RECRUITING
                        post.status == "MATCHING" -> PostingStatus.MATCHING
                        post.status == "CLOSED" -> PostingStatus.CLOSED
                        else -> throw ApiException("UNKNOWN_JOB_STATUS")
                    },
                    applicantCount = post.applicantCount, isUrgent = false,
                )
            }
            page++
        } while (page < result.totalPages)
        return all
    }
}

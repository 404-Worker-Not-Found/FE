package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.data.remote.OwnerWorkMapper
import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.RematchResult
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import com.workernotfound.app.feature.worker.data.remote.WorkResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerWorkApiService
import javax.inject.Inject

class OwnerWorkRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val api: WorkerWorkApiService,
    private val jobs: WorkerJobApiService,
    private val mapper: OwnerWorkMapper,
) : OwnerWorkRepository {

    override suspend fun startWork(workId: String): OwnerWork {
        backend.requireSession()
        val work = api.start(workId.toLong()).requireData()
        return map(work)
    }

    override suspend fun finishWork(workId: String): OwnerWork {
        backend.requireSession()
        val work = api.complete(workId.toLong()).requireData()
        return map(work)
    }

    private suspend fun map(work: WorkResponseDto): OwnerWork =
        mapper.map(work, jobs.detail(work.jobPostId).requireData(), api.policy().requireData())

    override suspend fun getActiveWorks(): List<OwnerWork> {
        backend.requireSession()
        val policy = api.policy().requireData()
        val result = mutableListOf<OwnerWork>()
        var page = 0
        do {
            val response = api.list(page).requireData()
            for (work in response.content.filter { it.status in setOf("SCHEDULED", "CHECKED_IN", "IN_PROGRESS") }) {
                result += mapper.map(work, jobs.detail(work.jobPostId).requireData(), policy)
            }
            page++
        } while (page < response.totalPages)
        return result
    }

    override suspend fun getWork(workId: String): OwnerWork {
        backend.requireSession()
        return map(api.detail(workId.toLong()).requireData())
    }

    override suspend fun confirmSettlement(workId: String) = backend.unsupported()

    override suspend fun confirmNoShow(workId: String) = backend.unsupported()

    override suspend fun requestRematch(workId: String): RematchResult = backend.unsupported()

    override suspend fun reopenPosting(workId: String) = backend.unsupported()

    override suspend fun closePosting(workId: String) = backend.unsupported()
}

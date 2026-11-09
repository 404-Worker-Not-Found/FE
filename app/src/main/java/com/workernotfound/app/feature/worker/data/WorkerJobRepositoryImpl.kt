package com.workernotfound.app.feature.worker.data

import android.content.Context
import com.workernotfound.app.R
import com.workernotfound.app.feature.worker.data.remote.JobCardResponseDto
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import com.workernotfound.app.feature.worker.data.remote.WorkerJobMapper
import com.workernotfound.app.feature.worker.domain.model.JobFilter
import com.workernotfound.app.feature.worker.domain.model.WorkerJobDetail
import com.workernotfound.app.feature.worker.domain.model.WorkerJobPosting
import com.workernotfound.app.feature.worker.domain.repository.JobSearchResult
import com.workernotfound.app.feature.worker.domain.repository.WorkerJobRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WorkerJobRepositoryImpl @Inject constructor(
    private val api: WorkerJobApiService,
    private val mapper: WorkerJobMapper,
    @ApplicationContext private val context: Context,
) : WorkerJobRepository {

    override suspend fun searchJobs(filter: JobFilter): JobSearchResult {
        // No location is fabricated. Distance filtering needs a real location first.
        check(filter.maxDistanceKm == JobFilter.MAX_DISTANCE_KM || filter.hasCoordinates()) { context.getString(R.string.backend_location_required) }
        val all = mutableListOf<JobCardResponseDto>()
        var page = 0
        do {
            val query = filter.searchQuery(page)
            val response = api.search(query).requireData()
            all += response.jobs
            page++
        } while (page < response.totalPages)
        val matched = all.distinctBy { it.id }
            .filter { filter.categories.isEmpty() || it.categoryName in filter.categories }
            .map(mapper::card)
        matched.forEach { cards[it.id] = it }
        return JobSearchResult(matched)
    }

    override suspend fun getJobDetail(id: String): WorkerJobDetail =
        mapper.detail(api.detail(id.toLong()).requireData())

    override fun cachedCard(id: String): WorkerJobPosting? = cards[id]

    private val cards = java.util.concurrent.ConcurrentHashMap<String, WorkerJobPosting>()
}

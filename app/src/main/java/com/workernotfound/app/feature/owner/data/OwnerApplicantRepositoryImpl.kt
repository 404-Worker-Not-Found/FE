package com.workernotfound.app.feature.owner.data

import android.content.Context
import com.workernotfound.app.BuildConfig
import com.workernotfound.app.R
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.data.remote.OwnerApplicantApiService
import com.workernotfound.app.feature.owner.data.remote.OwnerApplicantResponseDto
import com.workernotfound.app.feature.owner.domain.model.Applicant
import com.workernotfound.app.feature.owner.domain.model.ApplicantBoard
import com.workernotfound.app.feature.owner.domain.model.ApplicantDetail
import com.workernotfound.app.feature.owner.domain.repository.OwnerApplicantRepository
import com.workernotfound.app.feature.owner.domain.repository.OwnerHomeRepository
import com.workernotfound.app.feature.worker.data.remote.WorkerJobApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Mock applicant repository backed by [OwnerMockStore]. Chat-room creation and
 * the worker notification on 매칭 확정 are backend responsibilities and are not
 * simulated here.
 */
class OwnerApplicantRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val api: OwnerApplicantApiService,
    private val home: OwnerHomeRepository,
    private val jobs: WorkerJobApiService,
    @ApplicationContext private val context: Context,
    private val store: OwnerMockStore,
) : OwnerApplicantRepository {

    override suspend fun proposeMatch(postingId: String, applicantId: String) {
        backend.requireSession()
        val result = api.propose(postingId.toLong(), applicantId.toLong()).requireData()
        if (result.status != "PENDING") throw ApiException("UNEXPECTED_MATCHING_STATUS")
    }

    override suspend fun getApplicantBoard(postingId: String): ApplicantBoard {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val posting = home.getMyPostings().first { it.id == postingId }
            val job = jobs.detail(postingId.toLong()).requireData()
            val applicants = mutableListOf<Applicant>()
            var page = 0
            var batchId: Long? = null
            do {
                val result = api.list(postingId.toLong(), page, batchId).requireData()
                batchId = result.scoreBatchId
                applicants += result.applicants.map(::map)
                page++
            } while (page < result.totalPages)
            return ApplicantBoard(posting, job.categoryName, applicants, matchedApplicantId = null)
        }
        delay(MOCK_DELAY_MILLIS)
        return store.applicantBoard(postingId, System.currentTimeMillis())
    }

    override suspend fun getApplicantDetail(postingId: String, applicantId: String): ApplicantDetail {
        if (!BuildConfig.USE_DEMO_DATA) {
            backend.requireSession()
            val dto = api.detail(postingId.toLong(), applicantId.toLong()).requireData()
            val job = jobs.detail(postingId.toLong()).requireData()
            return ApplicantDetail(map(dto), job.categoryName, recentWorks = null, noShowCount = null,
                lastNoShowDate = null, canConfirmMatch = dto.status == "APPLIED")
        }
        delay(MOCK_DELAY_MILLIS)
        return store.applicantDetail(postingId, applicantId)
    }

    override suspend fun confirmMatch(postingId: String, applicantId: String): String {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        return store.confirmMatch(postingId, applicantId, System.currentTimeMillis())
    }

    private fun map(dto: OwnerApplicantResponseDto) = Applicant(
        id = dto.applicationId.toString(), name = dto.workerName ?: context.getString(R.string.backend_member_reference, dto.workerMemberId),
        age = null, profileImageUrl = null, rating = null, etaMinutes = dto.expectedArrivalMinutes,
        matchScore = dto.totalScore?.let { kotlin.math.round(it).toInt() }, noShowRisk = null,
        hasCategoryExperience = null, trustScore = null, isMatched = dto.status == "SELECTED", noShowProbability = dto.noShowProbability,
    )

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
    }
}

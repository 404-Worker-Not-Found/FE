package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.ApplicantBoard
import com.workernotfound.app.feature.owner.domain.model.ApplicantDetail
import com.workernotfound.app.feature.owner.domain.repository.OwnerApplicantRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock applicant repository backed by [OwnerMockStore]. Chat-room creation and
 * the worker notification on 매칭 확정 are backend responsibilities and are not
 * simulated here.
 */
class OwnerApplicantRepositoryImpl @Inject constructor(
    private val store: OwnerMockStore,
) : OwnerApplicantRepository {

    override suspend fun getApplicantBoard(postingId: String): ApplicantBoard {
        delay(MOCK_DELAY_MILLIS)
        return store.applicantBoard(postingId, System.currentTimeMillis())
    }

    override suspend fun getApplicantDetail(postingId: String, applicantId: String): ApplicantDetail {
        delay(MOCK_DELAY_MILLIS)
        return store.applicantDetail(postingId, applicantId)
    }

    override suspend fun confirmMatch(postingId: String, applicantId: String): String {
        delay(MOCK_DELAY_MILLIS)
        return store.confirmMatch(postingId, applicantId, System.currentTimeMillis())
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
    }
}

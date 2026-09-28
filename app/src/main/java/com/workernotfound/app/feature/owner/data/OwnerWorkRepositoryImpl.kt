package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.RematchResult
import com.workernotfound.app.feature.owner.domain.model.Settlement
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock work repository backed by [OwnerMockStore]. Automatic re-matching (a push /
 * server job in production) is simulated with a timer (decision: Demo Scope and
 * Mock-First Strategy — real-time and push are simulated in-app).
 */
class OwnerWorkRepositoryImpl @Inject constructor(
    private val store: OwnerMockStore,
) : OwnerWorkRepository {

    override suspend fun getActiveWorks(): List<OwnerWork> {
        delay(MOCK_DELAY_MILLIS)
        return store.activeWorks()
    }

    override suspend fun getWork(workId: String): OwnerWork {
        delay(MOCK_DELAY_MILLIS)
        return store.work(workId)
    }

    override suspend fun completeWork(workId: String): Settlement {
        delay(MOCK_DELAY_MILLIS)
        return store.completeWork(workId, System.currentTimeMillis())
    }

    override suspend fun getSettlement(workId: String): Settlement {
        delay(MOCK_DELAY_MILLIS)
        return store.settlement(workId)
    }

    override suspend fun confirmSettlement(workId: String) {
        delay(MOCK_DELAY_MILLIS)
        store.confirmSettlement(workId, System.currentTimeMillis())
    }

    override suspend fun confirmNoShow(workId: String) {
        delay(MOCK_DELAY_MILLIS)
        store.confirmNoShow(workId)
    }

    override suspend fun requestRematch(workId: String): RematchResult {
        delay(REMATCH_DELAY_MILLIS)
        return store.rematch(workId, System.currentTimeMillis())
    }

    override suspend fun reopenPosting(workId: String) {
        delay(MOCK_DELAY_MILLIS)
        store.reopenPosting(workId)
    }

    override suspend fun closePosting(workId: String) {
        delay(MOCK_DELAY_MILLIS)
        store.closePosting(workId)
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
        const val REMATCH_DELAY_MILLIS = 3_000L
    }
}

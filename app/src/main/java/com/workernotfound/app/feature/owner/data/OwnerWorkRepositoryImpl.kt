package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.RematchResult
import com.workernotfound.app.feature.owner.domain.model.Settlement
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Mock work repository backed by [OwnerMockStore]. Automatic re-matching (a push /
 * server job in production) and GPS 출근 인증 are simulated with timers (decision: Demo Scope and
 * Mock-First Strategy — real-time and push are simulated in-app).
 */
class OwnerWorkRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val store: OwnerMockStore,
) : OwnerWorkRepository {

    override suspend fun getActiveWorks(): List<OwnerWork> {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        return store.activeWorks(System.currentTimeMillis())
    }

    override suspend fun getWork(workId: String): OwnerWork {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        return store.work(workId, System.currentTimeMillis())
    }

    override suspend fun completeWork(workId: String): Settlement {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        return store.completeWork(workId, System.currentTimeMillis())
    }

    override suspend fun getSettlement(workId: String): Settlement {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        return store.settlement(workId)
    }

    override suspend fun confirmSettlement(workId: String) {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        store.confirmSettlement(workId, System.currentTimeMillis())
    }

    override suspend fun confirmNoShow(workId: String) {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        store.confirmNoShow(workId)
    }

    override suspend fun requestRematch(workId: String): RematchResult {
        backend.requireDemo()
        delay(REMATCH_DELAY_MILLIS)
        return store.rematch(workId, System.currentTimeMillis())
    }

    override suspend fun reopenPosting(workId: String) {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        store.reopenPosting(workId)
    }

    override suspend fun closePosting(workId: String) {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        store.closePosting(workId)
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
        const val REMATCH_DELAY_MILLIS = 3_000L
    }
}

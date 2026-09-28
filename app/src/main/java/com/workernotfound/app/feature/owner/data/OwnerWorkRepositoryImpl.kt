package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.Settlement
import com.workernotfound.app.feature.owner.domain.repository.OwnerWorkRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Mock work repository backed by [OwnerMockStore]. */
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

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
    }
}

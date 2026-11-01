package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.repository.OwnerPastPostingRepository
import javax.inject.Inject
import kotlinx.coroutines.delay

/** Mock past-posting repository backed by [OwnerMockStore]. */
class OwnerPastPostingRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val store: OwnerMockStore,
) : OwnerPastPostingRepository {

    override suspend fun getPastPostings(): List<PastPosting> {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        return store.pastPostings()
    }

    override suspend fun getPastPosting(postingId: String): PastPosting {
        backend.requireDemo()
        delay(MOCK_DELAY_MILLIS)
        return store.pastPosting(postingId)
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
    }
}

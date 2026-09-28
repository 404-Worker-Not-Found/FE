package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.repository.OwnerPastPostingRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/** Mock past-posting repository backed by [OwnerMockStore]. */
class OwnerPastPostingRepositoryImpl @Inject constructor(
    private val store: OwnerMockStore,
) : OwnerPastPostingRepository {

    override suspend fun getPastPostings(): List<PastPosting> {
        delay(MOCK_DELAY_MILLIS)
        return store.pastPostings()
    }

    override suspend fun getPastPosting(postingId: String): PastPosting {
        delay(MOCK_DELAY_MILLIS)
        return store.pastPosting(postingId)
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 300L
    }
}

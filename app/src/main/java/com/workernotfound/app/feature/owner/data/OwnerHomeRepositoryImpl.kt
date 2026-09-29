package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.feature.owner.domain.model.OwnerJobPosting
import com.workernotfound.app.feature.owner.domain.repository.OwnerHomeRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Mock-backed owner home repository for the demo. Reads the shared in-memory
 * [OwnerMockStore] (with a small delay to exercise the loading state) so that
 * matches and no-shows made on other owner screens are reflected here. Replace
 * with a network data source once the backend contract exists; the interface
 * and UI stay unchanged.
 */
class OwnerHomeRepositoryImpl @Inject constructor(
    private val store: OwnerMockStore,
) : OwnerHomeRepository {

    override suspend fun getMyPostings(): List<OwnerJobPosting> {
        delay(300)
        return store.postings(System.currentTimeMillis())
    }
}

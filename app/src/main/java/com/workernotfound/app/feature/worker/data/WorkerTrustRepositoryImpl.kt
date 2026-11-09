package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.worker.domain.model.TrustSummary
import com.workernotfound.app.feature.worker.domain.repository.WorkerTrustRepository
import javax.inject.Inject

/** 신뢰도 (UI spec 4-2). The trust summary has no confirmed public backend contract yet. */
class WorkerTrustRepositoryImpl @Inject constructor(private val backend: BackendAccess) : WorkerTrustRepository {
    override suspend fun getTrustSummary(): TrustSummary = backend.unsupported()
}

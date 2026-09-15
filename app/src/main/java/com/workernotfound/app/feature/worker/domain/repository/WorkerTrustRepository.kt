package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.TrustSummary

/** 신뢰도 데이터 (UI spec 4-2). Mock-first. */
interface WorkerTrustRepository {
    suspend fun getTrustSummary(): TrustSummary
}

package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.RematchResult
import com.workernotfound.app.feature.owner.domain.model.Settlement

/**
 * Owner shift monitoring, settlement and no-show handling (UI spec 2-4, 2-5).
 * Mock-backed for the demo (decision: Demo Scope and Mock-First Strategy).
 */
interface OwnerWorkRepository {
    /** Shifts that are running or waiting for settlement approval. */
    suspend fun getActiveWorks(): List<OwnerWork>

    suspend fun getWork(workId: String): OwnerWork

    /** 근무 완료: ends the shift and returns the calculated settlement. */
    suspend fun completeWork(workId: String): Settlement

    suspend fun getSettlement(workId: String): Settlement

    /** 정산 확인: approves the payment. */
    suspend fun confirmSettlement(workId: String)

    /** 노쇼 확정. */
    suspend fun confirmNoShow(workId: String)

    /** 자동 재매칭 after a no-show (simulated with a timer for the demo). */
    suspend fun requestRematch(workId: String): RematchResult

    /** 재매칭 실패 → 공고 재오픈. */
    suspend fun reopenPosting(workId: String)

    /** 재매칭 실패 → 마감. */
    suspend fun closePosting(workId: String)
}

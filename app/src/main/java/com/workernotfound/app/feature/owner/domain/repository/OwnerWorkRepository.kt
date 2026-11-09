package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.OwnerWork
import com.workernotfound.app.feature.owner.domain.model.RematchResult

/**
 * Owner shift monitoring and no-show handling (UI spec 2-4, 2-5).
 * No-show, re-matching and failed re-matching outcomes have no confirmed backend contract yet;
 * implementations report them as unsupported.
 */
interface OwnerWorkRepository {
    suspend fun startWork(workId: String): OwnerWork

    suspend fun finishWork(workId: String): OwnerWork

    /** Shifts that are scheduled or running. */
    suspend fun getActiveWorks(): List<OwnerWork>

    suspend fun getWork(workId: String): OwnerWork

    /** 정산 확인: approves the payment. */
    suspend fun confirmSettlement(workId: String)

    /** 노쇼 확정. */
    suspend fun confirmNoShow(workId: String)

    /** 자동 재매칭 after a no-show. */
    suspend fun requestRematch(workId: String): RematchResult

    /** 재매칭 실패 → 공고 재오픈. */
    suspend fun reopenPosting(workId: String)

    /** 재매칭 실패 → 마감. */
    suspend fun closePosting(workId: String)
}

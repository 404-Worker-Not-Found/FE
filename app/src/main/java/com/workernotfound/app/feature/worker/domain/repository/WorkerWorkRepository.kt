package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.WorkSchedule

/** 근무 관리 데이터 (UI spec 3-6). Mock-first. */
interface WorkerWorkRepository {
    suspend fun getWorks(): List<WorkSchedule>
}

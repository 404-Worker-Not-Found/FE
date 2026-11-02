package com.workernotfound.app.feature.worker.domain.repository

import com.workernotfound.app.feature.worker.domain.model.WorkSchedule

/** 근무 관리 데이터 (UI spec 3-6). Mock-first. */
interface WorkerWorkRepository {
    suspend fun getWorks(): List<WorkSchedule>
    suspend fun checkIn(workId: String, latitude: Double, longitude: Double): CheckInReceipt
}


data class CheckInReceipt(val workId: String, val status: String, val checkedInAt: String)

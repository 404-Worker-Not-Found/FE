package com.workernotfound.app.feature.worker.domain.model

/** A worker's scheduled/ongoing work shift (UI spec 3-6 근무 관리). */
data class WorkSchedule(
    val id: String,
    val storeName: String,
    val category: String,
    val workDate: String,
    val timeRange: String,
    val hourlyWage: Int,
    val status: WorkStatus,
)

/** 3-6: 예정 근무(SCHEDULED) / 진행 중 근무(IN_PROGRESS). */
enum class WorkStatus { SCHEDULED, IN_PROGRESS }

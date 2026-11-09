package com.workernotfound.app.feature.history.domain

import com.workernotfound.app.feature.owner.domain.model.ServerSettlement

data class WorkHistoryEntry(val workId: Long, val jobId: Long, val storeName: String,
    val date: String, val startTime: String, val endTime: String, val nextDay: Boolean,
    val status: String, val completedAt: String?, val settlement: ServerSettlement?)
interface WorkHistoryRepository { suspend fun getAll(): List<WorkHistoryEntry> }

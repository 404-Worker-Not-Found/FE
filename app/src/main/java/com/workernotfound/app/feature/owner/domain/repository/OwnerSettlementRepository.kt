package com.workernotfound.app.feature.owner.domain.repository

import com.workernotfound.app.feature.owner.domain.model.ServerSettlement

interface OwnerSettlementRepository {
    suspend fun getAll(): List<ServerSettlement>
    suspend fun getForWork(workId: String): ServerSettlement?
}

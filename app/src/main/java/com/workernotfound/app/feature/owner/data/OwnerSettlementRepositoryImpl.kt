package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.owner.data.remote.OwnerSettlementApiService
import com.workernotfound.app.feature.owner.domain.model.ServerSettlement
import com.workernotfound.app.feature.owner.domain.repository.OwnerSettlementRepository
import javax.inject.Inject

class OwnerSettlementRepositoryImpl @Inject constructor(
    private val backend: BackendAccess,
    private val source: SettlementRemoteDataSource,
) : OwnerSettlementRepository {
    override suspend fun getAll(): List<ServerSettlement> {
        backend.requireSession()
        return source.getAll()
    }

    override suspend fun getForWork(workId: String): ServerSettlement? {
        val matches = getAll().filter { it.workId == workId.toLong() }
        if (matches.size > 1) throw ApiException("AMBIGUOUS_WORK_SETTLEMENT")
        return matches.singleOrNull()
    }
}

/** Backend returns a descending-ID cursor list rather than a Spring page envelope. */
class SettlementRemoteDataSource @Inject constructor(private val api: OwnerSettlementApiService) {
    suspend fun getAll(): List<ServerSettlement> {
        val result = mutableListOf<ServerSettlement>()
        var beforeId: Long? = null
        do {
            val batch = api.list(beforeId, PAGE_SIZE).requireData()
            result += batch.map { it.toDomain() }
            if (batch.size < PAGE_SIZE) break
            val next = batch.last().id
            if (next <= 0 || (beforeId != null && next >= beforeId)) throw ApiException("INVALID_SETTLEMENT_CURSOR")
            beforeId = next
        } while (true)
        return result.distinctBy { it.id }
    }

    private companion object { const val PAGE_SIZE = 100 }
}

package com.workernotfound.app.feature.owner.data

import com.workernotfound.app.core.network.ApiResponse
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.owner.data.remote.OwnerSettlementApiService
import com.workernotfound.app.feature.owner.data.remote.SettlementResponseDto
import com.workernotfound.app.feature.owner.domain.model.PastPosting
import com.workernotfound.app.feature.owner.domain.model.PastPostingStatus
import com.workernotfound.app.feature.owner.ui.component.formatSettlementAmount
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OwnerSettlementContractTest {
    @Test fun followsDescendingIdCursorAndPreservesExactServerAmounts() = runBlocking {
        val cursors = mutableListOf<Long?>()
        val api = object : OwnerSettlementApiService {
            override suspend fun list(beforeId: Long?, size: Int): ApiResponse<List<SettlementResponseDto>> {
                cursors += beforeId
                assertEquals(100, size)
                val ids = if (beforeId == null) 200L downTo 101L else 100L downTo 81L
                return ApiResponse(true, null, ids.map(::record))
            }
            override suspend fun detail(id: Long) = ApiResponse(true, null, record(id))
        }
        val result = SettlementRemoteDataSource(api).getAll()
        assertEquals(listOf(null, 101L), cursors)
        assertEquals(120, result.size)
        assertEquals(BigDecimal("3000000000.25"), result.first().amount)
        assertEquals("RESERVED", result.first().status)
        assertNull(result.first().paidAt)
    }

    @Test fun repeatedCursorFailsInsteadOfLoopingForever() = runBlocking {
        val api = object : OwnerSettlementApiService {
            override suspend fun list(beforeId: Long?, size: Int) =
                ApiResponse(true, null, (200L downTo 101L).map(::record))
            override suspend fun detail(id: Long) = ApiResponse(true, null, record(id))
        }
        val error = runCatching { SettlementRemoteDataSource(api).getAll() }.exceptionOrNull()
        assertTrue(error is ApiException)
    }

    @Test fun currencyFormattingDoesNotOverflowOrRoundAwayFractions() {
        val text = formatSettlementAmount(BigDecimal("3000000000.25"), "KRW")
        assertTrue(text.contains("3,000,000,000.25"))
    }

    @Test fun multipleWorkersForOneJobHaveDifferentNavigationIdentities() {
        val first = PastPosting("7", "work", "worker", 1, 2, 10320, PastPostingStatus.COMPLETED, null, workId = "11")
        val second = first.copy(workId = "12")
        assertNotEquals(first.entryId, second.entryId)
        assertEquals("7", first.postingId)
        assertEquals(PastPostingStatus.CANCELED, terminalPostingStatus("CANCELED"))
        assertEquals(PastPostingStatus.NO_SHOW, terminalPostingStatus("NO_SHOW"))
        assertEquals(PastPostingStatus.FAILED, terminalPostingStatus("FAILED"))
    }

    private fun record(id: Long) = SettlementResponseDto(id, id + 1000, 7,
        BigDecimal("3000000000.25"), "KRW", "RESERVED", null, false, null, null, null)
}

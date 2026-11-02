package com.workernotfound.app.feature.history.data

import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.feature.owner.domain.model.ServerSettlement
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class WorkHistoryContractTest {
    @Test fun `history includes terminal work without treating scheduled work as finished`() {
        listOf("COMPLETED", "CANCELED", "FAILED", "NO_SHOW").forEach { assertTrue(isHistoricalWork(it)) }
        listOf("SCHEDULED", "CHECKED_IN", "IN_PROGRESS").forEach { assertFalse(isHistoricalWork(it)) }
        assertThrows(ApiException::class.java) { isHistoricalWork("OTHER") }
    }
    @Test fun `payout must belong to this exact work and job without ambiguity`() {
        val payout = ServerSettlement(1, 20, 30, BigDecimal("12345.67"), "KRW", "ON_HOLD",
            "FUNDING_BLOCKED", true, null, null, null)
        assertNull(matchingSettlement(20, 30, emptyList()))
        assertEquals(payout, matchingSettlement(20, 30, listOf(payout)))
        assertThrows(ApiException::class.java) { matchingSettlement(21, 30, listOf(payout)) }
        assertThrows(ApiException::class.java) { matchingSettlement(20, 31, listOf(payout)) }
        assertThrows(ApiException::class.java) { matchingSettlement(20, 30, listOf(payout, payout.copy(id = 2))) }
    }
}

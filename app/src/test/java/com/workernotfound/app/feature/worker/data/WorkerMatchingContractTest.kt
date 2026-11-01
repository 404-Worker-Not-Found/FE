package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.ApplicationStatus
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.*
import org.junit.Test

class WorkerMatchingContractTest {
    @Test fun pendingProposalIsNotConfirmedWorkAndSelectedIsConfirmed() {
        assertEquals(ApplicationStatus.APPLYING, applicationStatus("APPLIED", null))
        assertEquals(ApplicationStatus.MATCHING, applicationStatus("APPLIED", "PENDING"))
        assertEquals(ApplicationStatus.MATCHED, applicationStatus("SELECTED", "CONFIRMED"))
        assertEquals(ApplicationStatus.CANCELED, applicationStatus("CANCELED", "CANCELED"))
        assertEquals(ApplicationStatus.NOT_SELECTED, applicationStatus("REJECTED", "EXPIRED"))
    }

    @Test fun proposalUsesServerExpiryRatherThanFixedFiveMinutes() {
        val clock = Clock.fixed(Instant.parse("2026-11-01T08:00:00Z"), ZoneOffset.UTC)
        assertEquals(90L, expiresInSeconds("2026-11-01T17:01:30", clock))
        assertEquals(0L, expiresInSeconds("2026-11-01T16:59:59", clock))
    }
}

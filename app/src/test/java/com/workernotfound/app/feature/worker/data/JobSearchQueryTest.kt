package com.workernotfound.app.feature.worker.data

import com.workernotfound.app.feature.worker.domain.model.JobFilter
import org.junit.Assert.*
import org.junit.Test

class JobSearchQueryTest {
    @Test fun `only real coordinate pairs enable backend radius query`() {
        val unrestricted = JobFilter().searchQuery(0)
        assertFalse(unrestricted.containsKey("workerLat"))
        assertFalse(unrestricted.containsKey("maxDistanceKm"))
        val radius = JobFilter(maxDistanceKm = 3, latitude = 37.5, longitude = 127.0).searchQuery(2)
        assertEquals("37.5", radius["workerLat"])
        assertEquals("127.0", radius["workerLng"])
        assertEquals("3", radius["maxDistanceKm"])
        assertEquals("2", radius["page"])
    }
    @Test fun `missing partial and invalid coordinates cannot fabricate distance`() {
        listOf(JobFilter(maxDistanceKm = 3), JobFilter(latitude = 37.0),
            JobFilter(latitude = Double.NaN, longitude = 127.0),
            JobFilter(latitude = 91.0, longitude = 127.0)).forEach {
            assertThrows(IllegalArgumentException::class.java) { it.searchQuery(0) }
        }
    }
}

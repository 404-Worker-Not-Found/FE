package com.workernotfound.app.core.maps

import org.junit.Assert.*
import org.junit.Test

class MapPointTest {
    @Test fun onlyFiniteCoordinatesInsideEarthBoundsCanBecomePins() {
        assertTrue(MapPoint("job",37.0,127.0,"store").valid)
        assertTrue(MapPoint("edge",-90.0,180.0,"edge").valid)
        assertFalse(MapPoint("unknown",Double.NaN,127.0,"unknown").valid)
        assertFalse(MapPoint("invalid",91.0,127.0,"invalid").valid)
        assertFalse(MapPoint("invalid",37.0,181.0,"invalid").valid)
        assertFalse(MapPoint("invalid",Double.POSITIVE_INFINITY,127.0,"invalid").valid)
    }
}

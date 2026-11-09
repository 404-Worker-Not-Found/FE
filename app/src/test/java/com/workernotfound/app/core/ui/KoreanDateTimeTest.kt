package com.workernotfound.app.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class KoreanDateTimeTest {
    @Test fun utcTimestampUsesKoreanDateAndClock() {
        val value = "2026-11-02T22:05:00Z"
        assertEquals("2026년 11월 3일 화요일", KoreanDateTime.day(value))
        assertEquals("오전 7:05", KoreanDateTime.time(value))
    }

    @Test fun offsetFreeTimestampIsAlreadyKoreanLocalTime() {
        assertEquals("2026년 11월 3일 오후 7:05", KoreanDateTime.dateTime("2026-11-03T19:05:00"))
    }

    @Test fun offsetTimestampConvertsAcrossKoreanDayBoundary() {
        assertEquals("2026년 11월 3일 화요일", KoreanDateTime.day("2026-11-02T15:05:00Z"))
    }
}

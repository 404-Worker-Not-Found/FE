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

    @Test fun workDateAddsKoreanWeekday() {
        assertEquals("2026.06.17 (수)", KoreanDateTime.workDate("2026-06-17"))
        assertEquals("2026.11.03 (화)", KoreanDateTime.workDate("2026-11-03T19:05:00"))
        assertEquals("not-a-date", KoreanDateTime.workDate("not-a-date"))
    }

    @Test fun relativeUsesKoreanTodayAndYesterday() {
        // 2026-11-09 10:00 KST
        val clock = java.time.Clock.fixed(java.time.Instant.parse("2026-11-09T01:00:00Z"), java.time.ZoneOffset.UTC)
        assertEquals("오늘 14:32", KoreanDateTime.relative("2026-11-09T14:32:00", clock))
        assertEquals("어제 14:05", KoreanDateTime.relative("2026-11-08T14:05:00", clock))
        assertEquals("11.5 (목) 09:00", KoreanDateTime.relative("2026-11-05T09:00:00", clock))
        // 23:30 UTC on the 8th is already the 9th in Korea.
        assertEquals("오늘 08:30", KoreanDateTime.relative("2026-11-08T23:30:00Z", clock))
    }
}

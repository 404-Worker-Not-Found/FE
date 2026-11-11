package com.workernotfound.app.core.ui

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats server timestamps for Korean users. Offset-free values are server-local KST. */
object KoreanDateTime {
    private val seoul = ZoneId.of("Asia/Seoul")
    private val locale = Locale.KOREAN
    private val dayFormat = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", locale)
    private val timeFormat = DateTimeFormatter.ofPattern("a h:mm", locale)
    private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:mm", locale)

    fun date(raw: String): LocalDate? = parse(raw)?.toLocalDate()

    fun day(raw: String): String = parse(raw)?.format(dayFormat) ?: raw

    fun time(raw: String): String = parse(raw)?.format(timeFormat) ?: raw

    fun dateTime(raw: String): String = parse(raw)?.format(dateTimeFormat) ?: raw

    /** Work date shared by every worker screen: `2026.06.17 (수)`. Accepts `yyyy-MM-dd` or a timestamp. */
    fun workDate(raw: String): String {
        val date = runCatching { LocalDate.parse(raw) }.getOrNull() ?: date(raw) ?: return raw
        return date.format(workDateFormat)
    }

    /** Recent moment relative to Korean today: `오늘 14:32`, `어제 14:05`, otherwise `6.15 (월) 14:05`. */
    fun relative(raw: String, clock: Clock = Clock.systemUTC()): String {
        val value = parse(raw) ?: return raw
        val today = LocalDate.now(clock.withZone(seoul))
        val time = value.format(clockFormat)
        return when (value.toLocalDate()) {
            today -> "오늘 $time"
            today.minusDays(1) -> "어제 $time"
            else -> "${value.format(shortDayFormat)} $time"
        }
    }

    private val workDateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", locale)
    private val shortDayFormat = DateTimeFormatter.ofPattern("M.d (E)", locale)
    private val clockFormat = DateTimeFormatter.ofPattern("HH:mm", locale)

    private fun parse(raw: String): ZonedDateTime? {
        if (raw.isBlank()) return null
        return runCatching { OffsetDateTime.parse(raw).atZoneSameInstant(seoul) }.getOrNull()
            ?: runCatching { ZonedDateTime.parse(raw).withZoneSameInstant(seoul) }.getOrNull()
            ?: runCatching { Instant.parse(raw).atZone(seoul) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(raw).atZone(seoul) }.getOrNull()
    }
}

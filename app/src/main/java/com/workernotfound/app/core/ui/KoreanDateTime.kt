package com.workernotfound.app.core.ui

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

    private fun parse(raw: String): ZonedDateTime? {
        if (raw.isBlank()) return null
        return runCatching { OffsetDateTime.parse(raw).atZoneSameInstant(seoul) }.getOrNull()
            ?: runCatching { ZonedDateTime.parse(raw).withZoneSameInstant(seoul) }.getOrNull()
            ?: runCatching { Instant.parse(raw).atZone(seoul) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(raw).atZone(seoul) }.getOrNull()
    }
}

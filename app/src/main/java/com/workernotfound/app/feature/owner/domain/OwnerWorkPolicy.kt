package com.workernotfound.app.feature.owner.domain

import com.workernotfound.app.feature.owner.domain.model.Settlement

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L

/**
 * UI spec 2-4: 노쇼 확정 버튼은 근무 시작 후 30분 초과 시 활성화.
 * A worker whose GPS attendance is already verified cannot be marked as a no-show.
 */
object NoShowPolicy {
    const val GRACE_MINUTES = 30L
    private const val GRACE_MILLIS = GRACE_MINUTES * MILLIS_PER_MINUTE

    fun canConfirmNoShow(startMillis: Long, nowMillis: Long, isAttendanceVerified: Boolean): Boolean =
        !isAttendanceVerified && nowMillis - startMillis > GRACE_MILLIS

    /** Time left until the no-show button unlocks (0 once it is unlocked). */
    fun millisUntilEnabled(startMillis: Long, nowMillis: Long): Long =
        (startMillis + GRACE_MILLIS - nowMillis).coerceAtLeast(0L)
}

/** UI spec 2-6: 리뷰 작성 버튼은 근무 종료 후 7일 이내에만 활성화. */
object ReviewPolicy {
    const val REVIEW_WINDOW_DAYS = 7L
    private const val MILLIS_PER_DAY = 24 * MINUTES_PER_HOUR * MILLIS_PER_MINUTE
    private const val REVIEW_WINDOW_MILLIS = REVIEW_WINDOW_DAYS * MILLIS_PER_DAY

    fun canWriteReview(workEndMillis: Long, nowMillis: Long): Boolean {
        val elapsed = nowMillis - workEndMillis
        return elapsed in 0..REVIEW_WINDOW_MILLIS
    }

    /** Whole days left in the review window, rounded up (0 once it has closed). */
    fun remainingDays(workEndMillis: Long, nowMillis: Long): Long {
        val left = workEndMillis + REVIEW_WINDOW_MILLIS - nowMillis
        if (left <= 0) return 0L
        return (left + MILLIS_PER_DAY - 1) / MILLIS_PER_DAY
    }
}

/**
 * UI spec 2-4 정산: 총 근무시간 × (시급 + 가산액). Worked time runs from the scheduled
 * start to the completion time, capped at the scheduled end. Amounts are floored to won.
 */
object SettlementCalculator {

    fun workedMinutes(startMillis: Long, endMillis: Long, completedAtMillis: Long): Long {
        val until = minOf(completedAtMillis, endMillis)
        return ((until - startMillis) / MILLIS_PER_MINUTE).coerceAtLeast(0L)
    }

    fun calculate(workedMinutes: Long, hourlyWage: Int, bonusPerHour: Int): Settlement {
        val minutes = workedMinutes.coerceAtLeast(0L)
        return Settlement(
            workedMinutes = minutes,
            hourlyWage = hourlyWage,
            basePay = (hourlyWage * minutes / MINUTES_PER_HOUR).toInt(),
            bonusAmount = (bonusPerHour * minutes / MINUTES_PER_HOUR).toInt(),
        )
    }
}

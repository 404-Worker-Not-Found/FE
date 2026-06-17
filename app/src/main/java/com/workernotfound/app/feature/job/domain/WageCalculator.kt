package com.workernotfound.app.feature.job.domain

/** Wage breakdown shown on the posting form (UI spec 2-2: 자동 시급 가산). */
data class WageBreakdown(
    val base: Int,
    val urgencyBonus: Int,
    val nightBonus: Int,
) {
    val total: Int get() = base + urgencyBonus + nightBonus
    val totalBonus: Int get() = urgencyBonus + nightBonus
}

/**
 * Computes the automatic surcharge from urgency and time-of-day (UI spec 2-2:
 * 긴급도·시간대 기반 가산액 자동 계산). Deterministic, demo pricing rules.
 */
object WageCalculator {

    fun calculate(baseWage: Int, startTime: String?): WageBreakdown {
        if (baseWage <= 0) return WageBreakdown(0, 0, 0)
        // 긴급 공고 가산 10%
        val urgencyBonus = roundTo100(baseWage * 0.10)
        // 야간(22:00~06:00 시작) 가산 15%
        val nightBonus = if (isNightStart(startTime)) roundTo100(baseWage * 0.15) else 0
        return WageBreakdown(base = baseWage, urgencyBonus = urgencyBonus, nightBonus = nightBonus)
    }

    private fun isNightStart(startTime: String?): Boolean {
        val hour = startTime?.substringBefore(":")?.toIntOrNull() ?: return false
        return hour >= 22 || hour < 6
    }

    /** Round to the nearest 100 won for clean display. */
    private fun roundTo100(value: Double): Int = (Math.round(value / 100.0) * 100).toInt()
}

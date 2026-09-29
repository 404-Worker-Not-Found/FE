package com.workernotfound.app.feature.owner.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SettlementCalculatorTest {

    private val start = 1_000_000_000L
    private val minute = 60_000L

    @Test
    fun `worked minutes run from start to completion`() {
        assertEquals(230L, SettlementCalculator.workedMinutes(start, start + 240 * minute, start + 230 * minute))
    }

    @Test
    fun `worked minutes are capped at the scheduled end`() {
        assertEquals(240L, SettlementCalculator.workedMinutes(start, start + 240 * minute, start + 300 * minute))
    }

    @Test
    fun `worked minutes never go negative`() {
        assertEquals(0L, SettlementCalculator.workedMinutes(start, start + 240 * minute, start - minute))
    }

    @Test
    fun `settlement adds base pay and bonus`() {
        val settlement = SettlementCalculator.calculate(workedMinutes = 230, hourlyWage = 12000, bonusPerHour = 1200)
        assertEquals(46000, settlement.basePay)
        assertEquals(4600, settlement.bonusAmount)
        assertEquals(50600, settlement.totalPay)
    }

    @Test
    fun `partial hours are floored to whole won`() {
        val settlement = SettlementCalculator.calculate(workedMinutes = 7, hourlyWage = 10320, bonusPerHour = 0)
        assertEquals(1204, settlement.basePay)
        assertEquals(1204, settlement.totalPay)
    }
}

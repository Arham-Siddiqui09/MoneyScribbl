package com.moneyscribbl

import com.moneyscribbl.data.FinanceTransaction
import com.moneyscribbl.data.TransactionSource
import com.moneyscribbl.data.TransactionType
import com.moneyscribbl.viewmodel.buildFinanceInsights
import com.moneyscribbl.viewmodel.calculateSavedAmount
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FinanceAnalyticsTest {

    @Test
    fun `calculateSavedAmount returns income minus expenses`() {
        val transactions = listOf(
            transaction("income-1", 5000.0, TransactionType.INCOME, "Salary", 2026, Calendar.APRIL, 1),
            transaction("expense-1", 1200.0, TransactionType.EXPENSE, "Food", 2026, Calendar.APRIL, 2),
            transaction("expense-2", 800.0, TransactionType.EXPENSE, "Transport", 2026, Calendar.APRIL, 3)
        )

        assertEquals(3000.0, calculateSavedAmount(transactions), 0.0)
    }

    @Test
    fun `buildFinanceInsights aggregates weekly and monthly expenses`() {
        val now = calendarOf(2026, Calendar.APRIL, 3).timeInMillis
        val transactions = listOf(
            transaction("t1", 500.0, TransactionType.EXPENSE, "Food", 2026, Calendar.MARCH, 30),
            transaction("t2", 300.0, TransactionType.EXPENSE, "Food", 2026, Calendar.APRIL, 1),
            transaction("t3", 450.0, TransactionType.EXPENSE, "Shopping", 2026, Calendar.APRIL, 2),
            transaction("t4-week-old", 725.0, TransactionType.EXPENSE, "Travel", 2026, Calendar.MARCH, 23),
            transaction("t4", 1000.0, TransactionType.INCOME, "Salary", 2026, Calendar.APRIL, 2)
        )

        val result = buildFinanceInsights(transactions, now)

        assertEquals(7, result.weeklyExpensePoints.size)
        assertEquals(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), result.weeklyExpenseLabels)
        assertEquals(500.0, result.weeklyExpensePoints[0].amount, 0.0)
        assertEquals(300.0, result.weeklyExpensePoints[2].amount, 0.0)
        assertEquals(450.0, result.weeklyExpensePoints[3].amount, 0.0)
        assertEquals(4, result.currentWeekdayIndex)
        assertEquals(6, result.monthlyTrendPoints.size)
        assertEquals("Food", result.highestSpendingCategory)
        assertEquals(800.0, result.highestSpendingAmount, 0.0)
        assertEquals(TransactionType.EXPENSE, result.frequentTransactionType)
    }

    @Test
    fun `buildFinanceInsights handles empty transactions`() {
        val result = buildFinanceInsights(emptyList(), calendarOf(2026, Calendar.APRIL, 3).timeInMillis)

        assertEquals(7, result.weeklyExpensePoints.size)
        assertEquals(7, result.weeklyExpenseLabels.size)
        assertEquals(6, result.monthlyTrendPoints.size)
        assertNull(result.highestSpendingCategory)
        assertEquals(0.0, result.highestSpendingAmount, 0.0)
    }

    @Test
    fun `buildFinanceInsights filters category breakdown by period`() {
        // Today is Saturday, July 11, 2026
        val now = calendarOf(2026, Calendar.JULY, 11).timeInMillis
        val transactions = listOf(
            // Last year (should not be in any)
            transaction("t1", 100.0, TransactionType.EXPENSE, "Food", 2025, Calendar.DECEMBER, 15),
            // Earlier this year (should be in YEARLY)
            transaction("t2", 200.0, TransactionType.EXPENSE, "Food", 2026, Calendar.MARCH, 15),
            // Earlier this month, but not this week (should be in YEARLY, MONTH)
            transaction("t3", 300.0, TransactionType.EXPENSE, "Food", 2026, Calendar.JULY, 2),
            // This week, Wednesday (should be in YEARLY, MONTH, WEEK)
            transaction("t4", 400.0, TransactionType.EXPENSE, "Food", 2026, Calendar.JULY, 8)
        )

        val weekResult = buildFinanceInsights(transactions, now, com.moneyscribbl.viewmodel.TimePeriod.WEEK)
        val monthResult = buildFinanceInsights(transactions, now, com.moneyscribbl.viewmodel.TimePeriod.MONTH)
        val yearResult = buildFinanceInsights(transactions, now, com.moneyscribbl.viewmodel.TimePeriod.YEARLY)

        assertEquals(400.0, weekResult.categoryBreakdown.find { it.category == "Food" }?.amount ?: 0.0, 0.0)
        assertEquals(700.0, monthResult.categoryBreakdown.find { it.category == "Food" }?.amount ?: 0.0, 0.0)
        assertEquals(900.0, yearResult.categoryBreakdown.find { it.category == "Food" }?.amount ?: 0.0, 0.0)
    }

    private fun transaction(
        id: String,
        amount: Double,
        type: TransactionType,
        category: String,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): FinanceTransaction {
        return FinanceTransaction(
            id = id,
            amount = amount,
            type = type,
            category = category,
            dateMillis = calendarOf(year, month, dayOfMonth).timeInMillis,
            note = null,
            merchantName = null,
            payeeVpa = null,
            source = TransactionSource.MANUAL,
            upiAppPackage = null,
            upiAppLabel = null
        )
    }

    private fun calendarOf(year: Int, month: Int, dayOfMonth: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}


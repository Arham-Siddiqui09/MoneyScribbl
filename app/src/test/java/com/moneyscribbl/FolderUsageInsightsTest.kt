package com.moneyscribbl

import com.moneyscribbl.data.Folder
import com.moneyscribbl.data.FinanceTransaction
import com.moneyscribbl.data.TransactionSource
import com.moneyscribbl.data.TransactionType
import com.moneyscribbl.viewmodel.buildFolderUsageInsights
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderUsageInsightsTest {

    @Test
    fun `buildFolderUsageInsights calculates percentage from eligible expenses`() {
        val folders = listOf(
            Folder(
                name = "Shopping",
                limitAmount = 5000.0,
                limitStartDateMillis = calendarOf(2026, Calendar.APRIL, 1, 9).timeInMillis,
                limitEndDateMillis = calendarOf(2026, Calendar.APRIL, 30, 12).timeInMillis
            )
        )
        val transactions = listOf(
            transaction("t1", 2000.0, "Shopping", 2026, Calendar.APRIL, 2, 12),
            transaction("t2", 1000.0, "Shopping", 2026, Calendar.APRIL, 12, 15),
            transaction("t3", 900.0, "Shopping", 2026, Calendar.MARCH, 31, 18),
            transaction("t4", 700.0, "Bills", 2026, Calendar.APRIL, 10, 10),
            income("t5", 4000.0, "Shopping", 2026, Calendar.APRIL, 8, 11)
        )

        val result = buildFolderUsageInsights(folders, transactions).single()

        assertEquals("Shopping", result.folderName)
        assertEquals(3000.0, result.usedAmount, 0.0)
        assertEquals(60, result.usagePercent)
        assertFalse(result.isWarning)
        assertFalse(result.isExceeded)
    }

    @Test
    fun `buildFolderUsageInsights warns at eighty percent and excludes out of range expenses`() {
        val folders = listOf(
            Folder(
                name = "Shopping",
                limitAmount = 5000.0,
                limitStartDateMillis = calendarOf(2026, Calendar.APRIL, 1, 0).timeInMillis,
                limitEndDateMillis = calendarOf(2026, Calendar.APRIL, 30, 12).timeInMillis
            )
        )
        val transactions = listOf(
            transaction("t1", 4000.0, "Shopping", 2026, Calendar.APRIL, 30, 21),
            transaction("t2", 900.0, "Shopping", 2026, Calendar.MAY, 1, 1)
        )

        val result = buildFolderUsageInsights(folders, transactions).single()

        assertEquals(80, result.usagePercent)
        assertTrue(result.isWarning)
        assertFalse(result.isExceeded)
    }

    @Test
    fun `buildFolderUsageInsights marks exceeded and sorts by percent used descending`() {
        val folders = listOf(
            Folder(
                name = "Shopping",
                limitAmount = 5000.0,
                limitStartDateMillis = calendarOf(2026, Calendar.APRIL, 1, 0).timeInMillis,
                limitEndDateMillis = calendarOf(2026, Calendar.APRIL, 30, 12).timeInMillis
            ),
            Folder(
                name = "Bills",
                limitAmount = 2000.0,
                limitStartDateMillis = calendarOf(2026, Calendar.APRIL, 1, 0).timeInMillis,
                limitEndDateMillis = calendarOf(2026, Calendar.APRIL, 30, 12).timeInMillis
            )
        )
        val transactions = listOf(
            transaction("t1", 6000.0, "Shopping", 2026, Calendar.APRIL, 10, 10),
            transaction("t2", 1000.0, "Bills", 2026, Calendar.APRIL, 8, 10)
        )

        val result = buildFolderUsageInsights(folders, transactions)

        assertEquals(listOf("Shopping", "Bills"), result.map { it.folderName })
        assertEquals(120, result.first().usagePercent)
        assertTrue(result.first().isExceeded)
    }

    private fun transaction(
        id: String,
        amount: Double,
        category: String,
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hour: Int
    ): FinanceTransaction {
        return FinanceTransaction(
            id = id,
            amount = amount,
            type = TransactionType.EXPENSE,
            category = category,
            dateMillis = calendarOf(year, month, dayOfMonth, hour).timeInMillis,
            note = null,
            merchantName = null,
            payeeVpa = null,
            source = TransactionSource.MANUAL,
            upiAppPackage = null,
            upiAppLabel = null
        )
    }

    private fun income(
        id: String,
        amount: Double,
        category: String,
        year: Int,
        month: Int,
        dayOfMonth: Int,
        hour: Int
    ): FinanceTransaction {
        return transaction(id, amount, category, year, month, dayOfMonth, hour).copy(type = TransactionType.INCOME)
    }

    private fun calendarOf(year: Int, month: Int, dayOfMonth: Int, hour: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}


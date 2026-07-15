package com.moneyscribbl

import com.moneyscribbl.data.FALLBACK_FOLDER
import com.moneyscribbl.data.Folder
import com.moneyscribbl.data.FinanceTransaction
import com.moneyscribbl.data.TransactionSource
import com.moneyscribbl.data.TransactionType
import com.moneyscribbl.data.deserializeFolders
import com.moneyscribbl.data.reassignFolder
import com.moneyscribbl.data.serializeFolders
import com.moneyscribbl.data.sanitizeFolders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderManagementTest {

    @Test
    fun `sanitizeFolders trims de-duplicates and keeps fallback`() {
        val result = listOf(
            Folder(" Shopping "),
            Folder("shopping", limitAmount = 1000.0),
            Folder("Health"),
            Folder("other")
        ).sanitizeFolders()

        assertEquals(listOf("Shopping", "Health", FALLBACK_FOLDER), result.map(Folder::name))
    }

    @Test
    fun `sanitizeFolders appends fallback when missing`() {
        val result = listOf(Folder("Salary"), Folder("Food")).sanitizeFolders()

        assertEquals(listOf("Salary", "Food", FALLBACK_FOLDER), result.map(Folder::name))
    }

    @Test
    fun `reassignFolder moves matching transactions to fallback`() {
        val transactions = listOf(
            transaction(id = "1", category = "Shopping"),
            transaction(id = "2", category = "Health")
        )

        val result = reassignFolder(transactions, from = "shopping", to = FALLBACK_FOLDER)

        assertEquals(FALLBACK_FOLDER, result.first().category)
        assertEquals("Health", result.last().category)
        assertTrue(result.zip(transactions).all { (updated, original) -> updated.id == original.id })
    }

    @Test
    fun `deserializeFolders migrates legacy string folders`() {
        val result = deserializeFolders("""["Shopping","Bills"]""").sanitizeFolders()

        assertEquals(listOf("Shopping", "Bills", FALLBACK_FOLDER), result.map(Folder::name))
        assertNull(result.first().limitAmount)
        assertNull(result.first().limitStartDateMillis)
        assertNull(result.first().limitEndDateMillis)
    }

    @Test
    fun `serializeFolders round trips folder limit fields`() {
        val folders = listOf(
            Folder(
                name = "Shopping",
                limitAmount = 5000.0,
                limitStartDateMillis = 100L,
                limitEndDateMillis = 200L
            )
        )

        val result = deserializeFolders(serializeFolders(folders))

        assertEquals(folders, result)
    }

    private fun transaction(id: String, category: String): FinanceTransaction {
        return FinanceTransaction(
            id = id,
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = category,
            dateMillis = 1L,
            note = null,
            merchantName = null,
            payeeVpa = null,
            source = TransactionSource.MANUAL,
            upiAppPackage = null,
            upiAppLabel = null
        )
    }
}


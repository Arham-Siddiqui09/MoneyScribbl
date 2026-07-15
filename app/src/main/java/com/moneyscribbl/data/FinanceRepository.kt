package com.moneyscribbl.data

import android.util.Log
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG = "FinanceRepository"

class FinanceRepository(
    private val dao: FinanceDao
) {

    companion object {
        @Volatile
        private var INSTANCE: FinanceRepository? = null

        fun getInstance(dao: FinanceDao): FinanceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FinanceRepository(dao).also { INSTANCE = it }
            }
        }
    }

    // ── Transactions ──────────────────────────────────────────────────────────────────────────

    fun getTransactions(): Flow<List<FinanceTransaction>> =
        dao.observeAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getSavingsGoal(): Flow<SavingsGoal?> =
        dao.observeGoal().map { it?.toDomain() }

    fun getSavingsLedger(): Flow<List<SavingsLedgerEntry>> =
        dao.observeLedger().map { entities -> entities.map { it.toDomain() } }

    fun getFolders(): Flow<List<Folder>> =
        dao.observeFolders().map { entities ->
            val folders = entities.map { it.toDomain() }.sanitizeFolders()
            if (entities.isEmpty()) {
                // Seed defaults on first launch
                val defaults = defaultFinanceCategories.sanitizeFolders()
                defaults.forEach { dao.upsertFolder(it.toEntity()) }
                defaults
            } else {
                folders
            }
        }

    suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        dateMillis: Long,
        note: String?,
        merchantName: String? = null,
        payeeVpa: String? = null,
        source: TransactionSource = TransactionSource.MANUAL,
        upiAppPackage: String? = null,
        upiAppLabel: String? = null,
        isPending: Boolean = false
    ): FinanceTransaction {
        val transaction = FinanceTransaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            category = category,
            dateMillis = dateMillis,
            note = note?.trim()?.takeIf(String::isNotBlank),
            merchantName = merchantName?.trim()?.takeIf(String::isNotBlank),
            payeeVpa = payeeVpa?.trim()?.takeIf(String::isNotBlank),
            source = source,
            upiAppPackage = upiAppPackage,
            upiAppLabel = upiAppLabel,
            isPending = isPending
        )
        dao.upsertTransaction(transaction.toEntity())
        return transaction
    }

    suspend fun updateTransaction(transaction: FinanceTransaction) {
        dao.upsertTransaction(transaction.toEntity())
    }

    suspend fun deleteTransaction(id: String) {
        dao.deleteTransaction(id)
    }

    suspend fun getTransaction(id: String): FinanceTransaction? =
        dao.getTransactionById(id)?.toDomain()

    suspend fun saveSavingsGoal(goal: SavingsGoal) {
        dao.upsertGoal(goal.toEntity())
    }

    suspend fun clearSavingsGoal() {
        dao.deleteGoal()
    }

    suspend fun clearAllData() {
        dao.deleteAllTransactions()
        dao.deleteAllFolders()
        dao.deleteGoal()
        dao.deleteAllLedgerEntries()
    }

    private suspend fun appendLedgerEntry(entry: SavingsLedgerEntry) {
        dao.insertLedgerEntry(entry.toEntity())
    }

    suspend fun deleteSavingsLedgerEntry(id: String) {
        // id format is "${folderId}_${closedAt}" — split on last underscore
        val lastUnderscore = id.lastIndexOf('_')
        if (lastUnderscore == -1) return
        val folderId = id.substring(0, lastUnderscore)
        val closedAt = id.substring(lastUnderscore + 1).toLongOrNull() ?: return
        dao.deleteLedgerEntry(folderId, closedAt)
    }

    /**
     * Client-side sweep: finds folders whose [Folder.limitEndDateMillis] is in the past
     * and whose cycle is still logically "open" (they have a limit set).
     *
     * For each such folder:
     * - Computes [spent] within the cycle window from [transactions].
     * - If [spent] <= [limit] (not overspent), writes an immutable ledger entry and
     *   adds [saved] to the Savings Goal's [SavingsGoal.savedAmount].
     * - Clears the folder's limit regardless of overspend status (resets to no-limit).
     *
     * Safe to call multiple times — folders whose limits have already been cleared
     * for the current month will be ignored.
     */
    suspend fun sweepClosedFolders(
        transactions: List<FinanceTransaction>,
        nowMillis: Long
    ) {
        val folders = dao.getAllFolders().map { it.toDomain() }
        val goal = dao.getGoal()?.toDomain()

        var vaultContribution = 0.0
        val newEntries = mutableListOf<SavingsLedgerEntry>()
        val foldersToReset = mutableListOf<String>()

        for (folder in folders) {
            val limit = folder.limitAmount ?: continue
            val startMillis = folder.limitStartDateMillis ?: continue
            val endMillis = folder.limitEndDateMillis ?: continue

            // Only sweep cycles that have actually ended
            if (endMillis >= nowMillis) continue

            val spent = transactions
                .filter { it.type == TransactionType.EXPENSE }
                .filter { it.category.equals(folder.name, ignoreCase = true) }
                .filter { it.dateMillis in startMillis..endMillis }
                .sumOf { it.amount }

            val saved = maxOf(limit - spent, 0.0)

            // Write ledger entry only when something was actually saved
            if (spent <= limit && saved > 0.0) {
                newEntries += SavingsLedgerEntry(
                    folderId = folder.name,
                    folderName = folder.name,
                    limit = limit,
                    spent = spent,
                    saved = saved,
                    cycleEndDate = endMillis,
                    closedAt = nowMillis
                )
                vaultContribution += saved
            }
            foldersToReset += folder.name
        }

        if (newEntries.isEmpty() && foldersToReset.isEmpty()) return

        // Write all new ledger entries
        if (newEntries.isNotEmpty()) {
            dao.insertLedgerEntries(newEntries.map { it.toEntity() })
        }

        // Bump the savings goal's vault contribution
        if (vaultContribution > 0.0) {
            val updatedGoal = (goal ?: SavingsGoal(targetAmount = 0.0))
                .copy(savedAmount = (goal?.savedAmount ?: 0.0) + vaultContribution)
            dao.upsertGoal(updatedGoal.toEntity())
        }

        // Clear limits on all swept folders
        for (name in foldersToReset) {
            clearFolderLimit(name)
        }
    }

    suspend fun createFolder(name: String, emoji: String? = null): List<String> {
        val normalized = name.trim()
        if (normalized.isBlank()) return dao.getAllFolders().map { it.name }

        val existing = dao.getAllFolders().map { it.toDomain() }
        val updated = (existing + Folder(normalized, emoji = emoji)).sanitizeFolders()
        updated.forEach { dao.upsertFolder(it.toEntity()) }
        return updated.map(Folder::name)
    }

    /** Creates [name] folder only if it does not already exist. */
    suspend fun ensureFolderExists(name: String) {
        val folders = dao.getAllFolders().map { it.toDomain() }
        if (folders.none { it.name.equals(name, ignoreCase = true) }) {
            createFolder(name)
        }
    }

    suspend fun deleteFolder(name: String): List<String> {
        val folders = dao.getAllFolders().map { it.toDomain() }
        if (name.equals(FALLBACK_FOLDER, ignoreCase = true)) {
            return folders.map(Folder::name)
        }
        // Reassign transactions to fallback category
        val txns = dao.observeAllTransactions().first().map { it.toDomain() }
        val reassigned = reassignFolder(txns, from = name, to = FALLBACK_FOLDER)
        for (txn in reassigned) {
            dao.upsertTransaction(txn.toEntity())
        }

        dao.deleteFolder(name)
        return dao.getAllFolders().map { it.name }
    }

    suspend fun renameFolder(oldName: String, newName: String, newEmoji: String?): List<String> {
        val normalizedNew = newName.trim()
        val folders = dao.getAllFolders().map { it.toDomain() }

        if (oldName.equals(FALLBACK_FOLDER, ignoreCase = true) || normalizedNew.isBlank()) {
            return folders.map(Folder::name)
        }
        if (!oldName.equals(normalizedNew, ignoreCase = true) &&
            folders.any { it.name.equals(normalizedNew, ignoreCase = true) }
        ) {
            return folders.map(Folder::name)
        }

        val oldEntity = folders.firstOrNull { it.name.equals(oldName, ignoreCase = true) }
            ?: return folders.map(Folder::name)
        // Delete old, upsert new
        dao.deleteFolder(oldName)
        dao.upsertFolder(oldEntity.copy(name = normalizedNew, emoji = newEmoji).toEntity())

        // Reassign transactions
        val txns = dao.observeAllTransactions().first().map { it.toDomain() }
        val reassigned = reassignFolder(txns, from = oldName, to = normalizedNew)
        for (txn in reassigned.filter { it.category.equals(normalizedNew, ignoreCase = true) }) {
            dao.upsertTransaction(txn.toEntity())
        }

        return dao.getAllFolders().map { it.name }
    }

    suspend fun updateFolderLimit(
        name: String,
        limitAmount: Double,
        limitStartDateMillis: Long,
        limitEndDateMillis: Long
    ) {
        val folder = dao.getAllFolders().map { it.toDomain() }
            .firstOrNull { it.name.equals(name, ignoreCase = true) } ?: return
        dao.upsertFolder(
            folder.copy(
                limitAmount = limitAmount,
                limitStartDateMillis = limitStartDateMillis,
                limitEndDateMillis = limitEndDateMillis
            ).toEntity()
        )
    }

    suspend fun clearFolderLimit(name: String) {
        val folder = dao.getAllFolders().map { it.toDomain() }
            .firstOrNull { it.name.equals(name, ignoreCase = true) } ?: return
        dao.upsertFolder(
            folder.copy(
                limitAmount = null,
                limitStartDateMillis = null,
                limitEndDateMillis = null
            ).toEntity()
        )
    }
}



fun List<Folder>.sanitizeFolders(): List<Folder> {
    val map = linkedMapOf<String, Folder>()
    for (folder in this) {
        val key = folder.name.trim().lowercase()
        if (key.isNotBlank()) map[key] = folder
    }
    // Ensure fallback exists
    val fallbackKey = FALLBACK_FOLDER.lowercase()
    if (!map.containsKey(fallbackKey)) {
        map[fallbackKey] = Folder(FALLBACK_FOLDER, emoji = "📦")
    }
    return map.values.toList()
}

fun reassignFolder(
    txns: List<FinanceTransaction>,
    from: String,
    to: String
): List<FinanceTransaction> {
    return txns.map {
        if (it.category.equals(from, ignoreCase = true)) {
            it.copy(category = to)
        } else {
            it
        }
    }
}

fun deserializeFolders(json: String): List<Folder> {
    val array = org.json.JSONArray(json)
    return buildList {
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            add(
                Folder(
                    name = item.getString("name"),
                    limitAmount = item.optDouble("limitAmount").takeIf { !item.isNull("limitAmount") },
                    limitStartDateMillis = item.optLong("limitStartDateMillis").takeIf { !item.isNull("limitStartDateMillis") },
                    limitEndDateMillis = item.optLong("limitEndDateMillis").takeIf { !item.isNull("limitEndDateMillis") },
                    emoji = item.optString("emoji", null).takeIf { !it.isNullOrBlank() }
                )
            )
        }
    }
}

fun serializeFolders(folders: List<Folder>): String {
    val array = org.json.JSONArray()
    folders.forEach { folder ->
        val obj = org.json.JSONObject()
        obj.put("name", folder.name)
        folder.limitAmount?.let { obj.put("limitAmount", it) }
        folder.limitStartDateMillis?.let { obj.put("limitStartDateMillis", it) }
        folder.limitEndDateMillis?.let { obj.put("limitEndDateMillis", it) }
        folder.emoji?.let { obj.put("emoji", it) }
        array.put(obj)
    }
    return array.toString()
}

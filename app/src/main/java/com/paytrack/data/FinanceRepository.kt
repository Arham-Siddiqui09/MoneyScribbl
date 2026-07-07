package com.paytrack.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private const val TRANSACTIONS_KEY = "transactions_json"
private const val SAVINGS_GOAL_KEY = "savings_goal_json"
private const val FOLDERS_KEY = "folders_json"
private const val SAVINGS_LEDGER_KEY = "savings_ledger_json"
private const val TAG = "FinanceRepository"

class FinanceRepository(
    private val context: Context
) {

    companion object {
        @Volatile
        private var INSTANCE: FinanceRepository? = null

        fun getInstance(context: Context): FinanceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FinanceRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val transactionsKey = stringPreferencesKey(TRANSACTIONS_KEY)
    private val savingsGoalKey = stringPreferencesKey(SAVINGS_GOAL_KEY)
    private val foldersKey = stringPreferencesKey(FOLDERS_KEY)
    private val savingsLedgerKey = stringPreferencesKey(SAVINGS_LEDGER_KEY)

    fun getTransactions(): Flow<List<FinanceTransaction>> {
        return context.payTrackPreferencesDataStore.data
            .catchPreferences()
            .map { preferences ->
                preferences[transactionsKey]
                    ?.takeIf(String::isNotBlank)
                    ?.let(::safeJsonToTransactions)
                    .orEmpty()
                    .sortedByDescending(FinanceTransaction::dateMillis)
            }
    }

    fun getSavingsGoal(): Flow<SavingsGoal?> {
        return context.payTrackPreferencesDataStore.data
            .catchPreferences()
            .map { preferences ->
                preferences[savingsGoalKey]
                    ?.takeIf(String::isNotBlank)
                    ?.let(::safeJsonToSavingsGoal)
            }
    }

    fun getSavingsLedger(): Flow<List<SavingsLedgerEntry>> {
        return context.payTrackPreferencesDataStore.data
            .catchPreferences()
            .map { preferences ->
                preferences[savingsLedgerKey]
                    ?.takeIf(String::isNotBlank)
                    ?.let(::safeJsonToLedger)
                    .orEmpty()
            }
    }

    fun getFolders(): Flow<List<Folder>> {
        return context.payTrackPreferencesDataStore.data
            .catchPreferences()
            .map { preferences ->
                val storedFolders = preferences[foldersKey]
                    ?.takeIf(String::isNotBlank)
                    ?.let(::safeJsonToFolders)
                    ?.sanitizeFolders()
                    ?.takeIf { it.isNotEmpty() }

                val folders = storedFolders ?: defaultFinanceCategories.map(::Folder).sanitizeFolders()
                if (storedFolders == null) {
                    saveFolders(folders)
                }
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
        upiAppLabel: String? = null
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
            upiAppLabel = upiAppLabel
        )
        saveTransactions(getTransactions().first() + transaction)
        return transaction
    }

    suspend fun updateTransaction(transaction: FinanceTransaction) {
        val updatedTransactions = getTransactions().first().map { current ->
            if (current.id == transaction.id) transaction else current
        }
        saveTransactions(updatedTransactions)
    }

    suspend fun deleteTransaction(id: String) {
        saveTransactions(getTransactions().first().filterNot { it.id == id })
    }

    suspend fun getTransaction(id: String): FinanceTransaction? {
        return getTransactions().first().firstOrNull { it.id == id }
    }

    suspend fun saveSavingsGoal(goal: SavingsGoal) {
        context.payTrackPreferencesDataStore.edit { preferences ->
            preferences[savingsGoalKey] = savingsGoalToJson(goal)
        }
    }

    /**
     * Appends a single immutable ledger entry. This is the only allowed write operation
     * on the ledger — entries are never mutated or deleted.
     */
    private suspend fun appendLedgerEntry(entry: SavingsLedgerEntry) {
        val existing = getSavingsLedger().first()
        saveLedger(existing + entry)
    }

    private suspend fun saveLedger(entries: List<SavingsLedgerEntry>) {
        context.payTrackPreferencesDataStore.edit { preferences ->
            preferences[savingsLedgerKey] = ledgerToJson(entries)
        }
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
     * will not have a limit to sweep.
     */
    suspend fun sweepClosedFolders(
        transactions: List<FinanceTransaction>,
        nowMillis: Long
    ) {
        val folders = getFolders().first()
        val goal = getSavingsGoal().first()

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
            val existing = getSavingsLedger().first()
            saveLedger(existing + newEntries)
        }

        // Bump the savings goal's vault contribution
        if (vaultContribution > 0.0) {
            val updatedGoal = (goal ?: SavingsGoal(targetAmount = 0.0))
                .copy(savedAmount = (goal?.savedAmount ?: 0.0) + vaultContribution)
            saveSavingsGoal(updatedGoal)
        }

        // Clear limits on all swept folders
        for (name in foldersToReset) {
            clearFolderLimit(name)
        }
    }

    suspend fun addFolder(name: String): List<String> {
        val normalized = name.trim()
        val folders = getFolders().first()
        val updated = (folders + Folder(normalized)).sanitizeFolders()
        saveFolders(updated)
        return updated.map(Folder::name)
    }

    /** Creates [name] folder only if it does not already exist. */
    suspend fun ensureFolderExists(name: String) {
        val folders = getFolders().first()
        if (folders.none { it.name.equals(name, ignoreCase = true) }) {
            addFolder(name)
        }
    }

    suspend fun deleteFolder(name: String): List<String> {
        val folders = getFolders().first()
        if (name.equals(FALLBACK_FOLDER, ignoreCase = true)) {
            return folders.map(Folder::name)
        }

        val updatedFolders = folders
            .filterNot { it.name.equals(name, ignoreCase = true) }
            .sanitizeFolders()
        val updatedTransactions = reassignFolder(getTransactions().first(), from = name, to = FALLBACK_FOLDER)

        saveTransactions(updatedTransactions)
        saveFolders(updatedFolders)
        return updatedFolders.map(Folder::name)
    }

    suspend fun updateFolderLimit(name: String, limitAmount: Double, limitStartDateMillis: Long, limitEndDateMillis: Long) {
        val updatedFolders = getFolders().first().map { folder ->
            if (folder.name.equals(name, ignoreCase = true)) {
                folder.copy(
                    limitAmount = limitAmount,
                    limitStartDateMillis = limitStartDateMillis,
                    limitEndDateMillis = limitEndDateMillis
                )
            } else {
                folder
            }
        }.sanitizeFolders()
        saveFolders(updatedFolders)
    }

    suspend fun clearFolderLimit(name: String) {
        val updatedFolders = getFolders().first().map { folder ->
            if (folder.name.equals(name, ignoreCase = true)) {
                folder.copy(
                    limitAmount = null,
                    limitStartDateMillis = null,
                    limitEndDateMillis = null
                )
            } else {
                folder
            }
        }.sanitizeFolders()
        saveFolders(updatedFolders)
    }

    private suspend fun saveTransactions(transactions: List<FinanceTransaction>) {
        context.payTrackPreferencesDataStore.edit { preferences ->
            preferences[transactionsKey] = transactionsToJson(transactions)
        }
    }

    private suspend fun saveFolders(folders: List<Folder>) {
        context.payTrackPreferencesDataStore.edit { preferences ->
            preferences[foldersKey] = serializeFolders(folders.sanitizeFolders())
        }
    }

    private fun transactionsToJson(transactions: List<FinanceTransaction>): String {
        val jsonArray = JSONArray()
        transactions.forEach { transaction ->
            jsonArray.put(
                JSONObject()
                    .put("id", transaction.id)
                    .put("amount", transaction.amount)
                    .put("type", transaction.type.name)
                    .put("category", transaction.category)
                    .put("dateMillis", transaction.dateMillis)
                    .put("note", transaction.note)
                    .put("merchantName", transaction.merchantName)
                    .put("payeeVpa", transaction.payeeVpa)
                    .put("source", transaction.source.name)
                    .put("upiAppPackage", transaction.upiAppPackage)
                    .put("upiAppLabel", transaction.upiAppLabel)
            )
        }
        return jsonArray.toString()
    }

    private fun jsonToTransactions(json: String): List<FinanceTransaction> {
        val jsonArray = JSONArray(json)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(index)
                val transactionType = item.optString("type")
                    .toEnumOrNull<TransactionType>()
                    ?: continue
                add(
                    FinanceTransaction(
                        id = item.getString("id"),
                        amount = item.getDouble("amount"),
                        type = transactionType,
                        category = item.getString("category"),
                        dateMillis = item.getLong("dateMillis"),
                        note = item.optString("note").takeIf(String::isNotBlank),
                        merchantName = item.optString("merchantName").takeIf(String::isNotBlank),
                        payeeVpa = item.optString("payeeVpa").takeIf(String::isNotBlank),
                        source = item.optString("source")
                            .takeIf(String::isNotBlank)
                            ?.toEnumOrNull<TransactionSource>()
                            ?: TransactionSource.MANUAL,
                        upiAppPackage = item.optString("upiAppPackage").takeIf(String::isNotBlank),
                        upiAppLabel = item.optString("upiAppLabel").takeIf(String::isNotBlank)
                    )
                )
            }
        }
    }

    private fun savingsGoalToJson(goal: SavingsGoal): String {
        return JSONObject()
            .put("targetAmount", goal.targetAmount)
            .put("startDateMillis", goal.startDateMillis)
            .put("targetDateMillis", goal.targetDateMillis)
            .put("savedAmount", goal.savedAmount)
            .toString()
    }

    private fun jsonToSavingsGoal(json: String): SavingsGoal {
        val item = JSONObject(json)
        return SavingsGoal(
            targetAmount = item.optDouble("targetAmount"),
            startDateMillis = item.optLong("startDateMillis").takeIf { it > 0L },
            targetDateMillis = item.optLong("targetDateMillis").takeIf { it > 0L },
            savedAmount = item.optDouble("savedAmount", 0.0).takeIf { it > 0.0 } ?: 0.0
        )
    }

    private fun ledgerToJson(entries: List<SavingsLedgerEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("folderId", entry.folderId)
                    .put("folderName", entry.folderName)
                    .put("limit", entry.limit)
                    .put("spent", entry.spent)
                    .put("saved", entry.saved)
                    .put("cycleEndDate", entry.cycleEndDate)
                    .put("closedAt", entry.closedAt)
            )
        }
        return array.toString()
    }

    private fun jsonToLedger(json: String): List<SavingsLedgerEntry> {
        val array = JSONArray(json)
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    SavingsLedgerEntry(
                        folderId = item.optString("folderId"),
                        folderName = item.optString("folderName"),
                        limit = item.optDouble("limit"),
                        spent = item.optDouble("spent"),
                        saved = item.optDouble("saved"),
                        cycleEndDate = item.optLong("cycleEndDate"),
                        closedAt = item.optLong("closedAt")
                    )
                )
            }
        }
    }

    private fun safeJsonToLedger(json: String): List<SavingsLedgerEntry> {
        return try {
            jsonToLedger(json)
        } catch (exception: JSONException) {
            Log.w(TAG, "Ignoring malformed stored savings ledger JSON", exception)
            emptyList()
        }
    }

    private fun safeJsonToTransactions(json: String): List<FinanceTransaction> {
        return try {
            jsonToTransactions(json)
        } catch (exception: JSONException) {
            Log.w(TAG, "Ignoring malformed stored transactions JSON", exception)
            emptyList()
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Ignoring stored transactions with unsupported enum values", exception)
            emptyList()
        }
    }

    private fun safeJsonToSavingsGoal(json: String): SavingsGoal? {
        return try {
            jsonToSavingsGoal(json)
        } catch (exception: JSONException) {
            Log.w(TAG, "Ignoring malformed stored savings goal JSON", exception)
            null
        }
    }

    private fun safeJsonToFolders(json: String): List<Folder> {
        return try {
            deserializeFolders(json)
        } catch (exception: JSONException) {
            Log.w(TAG, "Ignoring malformed stored folders JSON", exception)
            emptyList()
        }
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
    return enumValues<T>().firstOrNull { it.name == this }
}

internal fun List<Folder>.sanitizeFolders(): List<Folder> {
    val sanitized = buildList {
        this@sanitizeFolders.forEach { folder ->
            folder.name.trim()
                .takeIf(String::isNotBlank)
                ?.let { add(folder.copy(name = it)) }
        }
    }.distinctBy { it.name.lowercase() }

    return if (sanitized.any { it.name.equals(FALLBACK_FOLDER, ignoreCase = true) }) {
        sanitized.map { folder ->
            if (folder.name.equals(FALLBACK_FOLDER, ignoreCase = true)) {
                folder.copy(name = FALLBACK_FOLDER)
            } else {
                folder
            }
        }
    } else {
        sanitized + Folder(name = FALLBACK_FOLDER)
    }
}

internal fun serializeFolders(folders: List<Folder>): String {
    val jsonArray = JSONArray()
    folders.forEach { folder ->
        jsonArray.put(
            JSONObject()
                .put("name", folder.name)
                .put("limitAmount", folder.limitAmount)
                .put("limitStartDateMillis", folder.limitStartDateMillis)
                .put("limitEndDateMillis", folder.limitEndDateMillis)
        )
    }
    return jsonArray.toString()
}

internal fun deserializeFolders(json: String): List<Folder> {
    val jsonArray = JSONArray(json)
    return buildList {
        for (index in 0 until jsonArray.length()) {
            when (val item = jsonArray.opt(index)) {
                is JSONObject -> {
                    val name = item.optString("name").trim().takeIf(String::isNotBlank) ?: continue
                    add(
                        Folder(
                            name = name,
                            limitAmount = item.optDouble("limitAmount").takeIf { !item.isNull("limitAmount") && it > 0.0 },
                            limitStartDateMillis = item.optLong("limitStartDateMillis").takeIf {
                                !item.isNull("limitStartDateMillis") && it > 0L
                            },
                            limitEndDateMillis = item.optLong("limitEndDateMillis").takeIf {
                                !item.isNull("limitEndDateMillis") && it > 0L
                            }
                        )
                    )
                }
                is String -> {
                    item.trim()
                        .takeIf(String::isNotBlank)
                        ?.let { add(Folder(name = it)) }
                }
            }
        }
    }
}

internal fun reassignFolder(
    transactions: List<FinanceTransaction>,
    from: String,
    to: String
): List<FinanceTransaction> {
    return transactions.map { transaction ->
        if (transaction.category.equals(from, ignoreCase = true)) {
            transaction.copy(category = to)
        } else {
            transaction
        }
    }
}

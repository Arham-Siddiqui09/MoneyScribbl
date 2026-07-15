package com.moneyscribbl.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

private const val TAG = "DataStoreMigration"
private val MIGRATION_DONE_KEY = booleanPreferencesKey("finance_room_migration_done")

/**
 * One-shot migration: reads all financial data from the legacy DataStore JSON blobs,
 * inserts them into Room, then clears the DataStore keys so they are never migrated again.
 *
 * Safe to call multiple times — exits immediately if the migration flag is already set.
 */
suspend fun migrateDataStoreToRoom(context: Context, dao: FinanceDao) {
    val prefs = context.moneyscribblPreferencesDataStore.data.first()

    // Already migrated — nothing to do
    if (prefs[MIGRATION_DONE_KEY] == true) return

    Log.i(TAG, "Starting DataStore → Room migration")

    try {
        val transactionsKey = stringPreferencesKey("transactions_json")
        val foldersKey = stringPreferencesKey("folders_json")
        val goalKey = stringPreferencesKey("savings_goal_json")
        val ledgerKey = stringPreferencesKey("savings_ledger_json")

        // Migrate transactions
        prefs[transactionsKey]?.takeIf(String::isNotBlank)?.let { json ->
            try {
                val transactions = legacyJsonToTransactions(json)
                dao.upsertTransactions(transactions.map { txn -> txn.toEntity() })
                Log.i(TAG, "Migrated ${transactions.size} transactions")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to migrate transactions — skipping", e)
            }
        }

        // Migrate folders
        prefs[foldersKey]?.takeIf(String::isNotBlank)?.let { json ->
            try {
                val folders = legacyDeserializeFolders(json).sanitizeFolders()
                dao.upsertFolders(folders.map { it.toEntity() })
                Log.i(TAG, "Migrated ${folders.size} folders")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to migrate folders — skipping", e)
            }
        }

        // Migrate savings goal
        prefs[goalKey]?.takeIf(String::isNotBlank)?.let { json ->
            try {
                val goal = legacyJsonToSavingsGoal(json)
                dao.upsertGoal(goal.toEntity())
                Log.i(TAG, "Migrated savings goal")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to migrate savings goal — skipping", e)
            }
        }

        // Migrate ledger
        prefs[ledgerKey]?.takeIf(String::isNotBlank)?.let { json ->
            try {
                val ledger = legacyJsonToLedger(json)
                dao.insertLedgerEntries(ledger.map { it.toEntity() })
                Log.i(TAG, "Migrated ${ledger.size} ledger entries")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to migrate ledger — skipping", e)
            }
        }

        // Mark migration complete and clear the old DataStore keys
        context.moneyscribblPreferencesDataStore.edit { p ->
            p[MIGRATION_DONE_KEY] = true
            p.remove(transactionsKey)
            p.remove(foldersKey)
            p.remove(goalKey)
            p.remove(ledgerKey)
        }

        Log.i(TAG, "DataStore → Room migration completed successfully")

    } catch (e: Exception) {
        Log.e(TAG, "DataStore → Room migration failed", e)
        // Do NOT mark as done — will retry on next launch
    }
}

// ── Legacy parsers (copied from FinanceRepository for migration use) ──────────────────────────

private fun legacyJsonToTransactions(json: String): List<FinanceTransaction> {
    return try {
        // Reuse the existing deserialize helpers via the companion-accessible functions
        val array = org.json.JSONArray(json)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val type = item.optString("type")
                    .let { s -> TransactionType.entries.firstOrNull { it.name == s } } ?: continue
                add(
                    FinanceTransaction(
                        id = item.getString("id"),
                        amount = item.getDouble("amount"),
                        type = type,
                        category = item.getString("category"),
                        dateMillis = item.getLong("dateMillis"),
                        note = item.optString("note").takeIf(String::isNotBlank),
                        merchantName = item.optString("merchantName").takeIf(String::isNotBlank),
                        payeeVpa = item.optString("payeeVpa").takeIf(String::isNotBlank),
                        source = item.optString("source")
                            .let { s -> TransactionSource.entries.firstOrNull { it.name == s } }
                            ?: TransactionSource.MANUAL,
                        upiAppPackage = item.optString("upiAppPackage").takeIf(String::isNotBlank),
                        upiAppLabel = item.optString("upiAppLabel").takeIf(String::isNotBlank),
                        isPending = item.optBoolean("isPending", false)
                    )
                )
            }
        }
    } catch (e: Exception) { emptyList() }
}

private fun legacyDeserializeFolders(json: String): List<Folder> {
    return try {
        deserializeFolders(json)
    } catch (e: Exception) { emptyList() }
}

private fun legacyJsonToSavingsGoal(json: String): SavingsGoal {
    val item = org.json.JSONObject(json)
    return SavingsGoal(
        targetAmount = item.optDouble("targetAmount"),
        startDateMillis = item.optLong("startDateMillis").takeIf { it > 0L },
        targetDateMillis = item.optLong("targetDateMillis").takeIf { it > 0L },
        savedAmount = item.optDouble("savedAmount", 0.0).takeIf { it > 0.0 } ?: 0.0
    )
}

private fun legacyJsonToLedger(json: String): List<SavingsLedgerEntry> {
    return try {
        val array = org.json.JSONArray(json)
        buildList {
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
    } catch (e: Exception) { emptyList() }
}

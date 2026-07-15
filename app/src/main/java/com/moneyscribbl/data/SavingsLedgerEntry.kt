package com.moneyscribbl.data

/**
 * Represents a single ledger entry in the vault's transaction history.
 * These entries are append-only and must never be mutated or deleted — they form the audit trail
 * for all money moving into and out of the vault.
 */
data class SavingsLedgerEntry(
    /** The folder name, used as a stable identifier across the app. */
    val folderId: String,
    val folderName: String,
    val limit: Double,
    val spent: Double,
    /** The amount saved: max(limit - spent, 0). Always > 0 by contract (entries with saved == 0 are not written). */
    val saved: Double,
    /** The limitEndDateMillis from the folder at the time the cycle closed. */
    val cycleEndDate: Long,
    /** System.currentTimeMillis() at the exact moment the sweep ran. */
    val closedAt: Long
)


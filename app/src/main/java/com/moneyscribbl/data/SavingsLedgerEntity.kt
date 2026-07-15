package com.moneyscribbl.data

import androidx.room.Entity

/** Composite primary key: folderId + closedAt uniquely identifies a ledger entry. */
@Entity(
    tableName = "savings_ledger",
    primaryKeys = ["folderId", "closedAt"]
)
data class SavingsLedgerEntity(
    val folderId: String,
    val folderName: String,
    val limit: Double,
    val spent: Double,
    val saved: Double,
    val cycleEndDate: Long,
    val closedAt: Long
)

fun SavingsLedgerEntity.toDomain(): SavingsLedgerEntry = SavingsLedgerEntry(
    folderId = folderId, folderName = folderName, limit = limit,
    spent = spent, saved = saved, cycleEndDate = cycleEndDate, closedAt = closedAt
)

fun SavingsLedgerEntry.toEntity(): SavingsLedgerEntity = SavingsLedgerEntity(
    folderId = folderId, folderName = folderName, limit = limit,
    spent = spent, saved = saved, cycleEndDate = cycleEndDate, closedAt = closedAt
)

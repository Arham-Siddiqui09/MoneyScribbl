package com.moneyscribbl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // ── Transactions ──────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun observeAllTransactions(): Flow<List<FinanceTransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): FinanceTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(entity: FinanceTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransactions(entities: List<FinanceTransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // ── Folders ───────────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM folders")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders")
    suspend fun getAllFolders(): List<FolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolder(entity: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolders(entities: List<FolderEntity>)

    @Query("DELETE FROM folders WHERE name = :name")
    suspend fun deleteFolder(name: String)

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()

    // ── Savings Goal ──────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM savings_goal WHERE id = 1 LIMIT 1")
    fun observeGoal(): Flow<SavingsGoalEntity?>

    @Query("SELECT * FROM savings_goal WHERE id = 1 LIMIT 1")
    suspend fun getGoal(): SavingsGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(entity: SavingsGoalEntity)

    @Query("DELETE FROM savings_goal")
    suspend fun deleteGoal()

    // ── Savings Ledger ────────────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM savings_ledger ORDER BY closedAt DESC")
    fun observeLedger(): Flow<List<SavingsLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLedgerEntry(entity: SavingsLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLedgerEntries(entities: List<SavingsLedgerEntity>)

    @Query("DELETE FROM savings_ledger WHERE folderId = :folderId AND closedAt = :closedAt")
    suspend fun deleteLedgerEntry(folderId: String, closedAt: Long)

    @Query("DELETE FROM savings_ledger")
    suspend fun deleteAllLedgerEntries()
}

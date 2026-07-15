package com.moneyscribbl.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class FinanceTransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val dateMillis: Long,
    val note: String?,
    val merchantName: String?,
    val payeeVpa: String?,
    val source: TransactionSource,
    val upiAppPackage: String?,
    val upiAppLabel: String?,
    val isPending: Boolean
)

fun FinanceTransactionEntity.toDomain(): FinanceTransaction = FinanceTransaction(
    id = id, amount = amount, type = type, category = category,
    dateMillis = dateMillis, note = note, merchantName = merchantName,
    payeeVpa = payeeVpa, source = source, upiAppPackage = upiAppPackage,
    upiAppLabel = upiAppLabel, isPending = isPending
)

fun FinanceTransaction.toEntity(): FinanceTransactionEntity = FinanceTransactionEntity(
    id = id, amount = amount, type = type, category = category,
    dateMillis = dateMillis, note = note, merchantName = merchantName,
    payeeVpa = payeeVpa, source = source, upiAppPackage = upiAppPackage,
    upiAppLabel = upiAppLabel, isPending = isPending
)

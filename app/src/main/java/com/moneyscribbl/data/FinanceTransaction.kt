package com.moneyscribbl.data

data class FinanceTransaction(
    val id: String,
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
    val isPending: Boolean = false
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionSource {
    MANUAL,
    QR_UPI,
    SMS_BANK
}


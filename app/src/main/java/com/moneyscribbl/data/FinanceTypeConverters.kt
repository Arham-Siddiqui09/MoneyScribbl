package com.moneyscribbl.data

import androidx.room.TypeConverter

class FinanceTypeConverters {

    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType =
        enumValueOf(value)

    @TypeConverter
    fun transactionSourceToString(value: TransactionSource): String = value.name

    @TypeConverter
    fun stringToTransactionSource(value: String): TransactionSource =
        enumValueOf(value)
}

package com.moneyscribbl.data

data class Folder(
    val name: String,
    val limitAmount: Double? = null,
    val limitStartDateMillis: Long? = null,
    val limitEndDateMillis: Long? = null,
    val emoji: String? = null
)


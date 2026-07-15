package com.moneyscribbl.data

data class SavingsGoal(
    val targetAmount: Double,
    val startDateMillis: Long? = null,
    val targetDateMillis: Long? = null,
    /** Cumulative amount added from closed Savings Vault folder cycles. Append-only. */
    val savedAmount: Double = 0.0
)


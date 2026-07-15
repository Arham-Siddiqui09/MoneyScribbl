package com.moneyscribbl.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Singleton table — always uses id = 1. */
@Entity(tableName = "savings_goal")
data class SavingsGoalEntity(
    @PrimaryKey val id: Int = 1,
    val targetAmount: Double,
    val startDateMillis: Long?,
    val targetDateMillis: Long?,
    val savedAmount: Double
)

fun SavingsGoalEntity.toDomain(): SavingsGoal = SavingsGoal(
    targetAmount = targetAmount,
    startDateMillis = startDateMillis,
    targetDateMillis = targetDateMillis,
    savedAmount = savedAmount
)

fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    targetAmount = targetAmount,
    startDateMillis = startDateMillis,
    targetDateMillis = targetDateMillis,
    savedAmount = savedAmount
)

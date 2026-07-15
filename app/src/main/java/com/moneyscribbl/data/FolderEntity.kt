package com.moneyscribbl.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val name: String,
    val limitAmount: Double?,
    val limitStartDateMillis: Long?,
    val limitEndDateMillis: Long?,
    val emoji: String?
)

fun FolderEntity.toDomain(): Folder = Folder(
    name = name, limitAmount = limitAmount,
    limitStartDateMillis = limitStartDateMillis,
    limitEndDateMillis = limitEndDateMillis, emoji = emoji
)

fun Folder.toEntity(): FolderEntity = FolderEntity(
    name = name, limitAmount = limitAmount,
    limitStartDateMillis = limitStartDateMillis,
    limitEndDateMillis = limitEndDateMillis, emoji = emoji
)

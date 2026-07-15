package com.moneyscribbl.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val phone: String,
    val profileImagePath: String?
)


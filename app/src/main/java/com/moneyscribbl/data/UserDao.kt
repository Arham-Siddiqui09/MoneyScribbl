package com.moneyscribbl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    fun observeUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY id ASC LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}


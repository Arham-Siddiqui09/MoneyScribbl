package com.moneyscribbl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        FinanceTransactionEntity::class,
        FolderEntity::class,
        SavingsGoalEntity::class,
        SavingsLedgerEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(FinanceTypeConverters::class)
abstract class moneyscribblDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: moneyscribblDatabase? = null

        fun getInstance(context: Context): moneyscribblDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    moneyscribblDatabase::class.java,
                    "moneyscribbl.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

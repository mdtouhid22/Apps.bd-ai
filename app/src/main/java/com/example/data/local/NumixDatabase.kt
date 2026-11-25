package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ConversionHistoryEntity::class], version = 1, exportSchema = false)
abstract class NumixDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: NumixDatabase? = null

        fun getInstance(context: Context): NumixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NumixDatabase::class.java,
                    "numix_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.aistudio.hiromant.kxsrwa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ReadingEntity::class,
        UserProfileEntity::class,
        BillingStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PalmistDatabase : RoomDatabase() {
    abstract fun palmistDao(): PalmistDao

    companion object {
        @Volatile
        private var INSTANCE: PalmistDatabase? = null

        fun getDatabase(context: Context): PalmistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PalmistDatabase::class.java,
                    "palmist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

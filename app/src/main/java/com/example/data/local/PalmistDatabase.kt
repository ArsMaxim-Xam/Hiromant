package com.example.data.local

import androidx.room.Database
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
}

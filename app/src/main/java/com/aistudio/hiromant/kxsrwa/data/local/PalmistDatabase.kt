package com.aistudio.hiromant.kxsrwa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ReadingEntity::class,
        UserProfileEntity::class,
        BillingStateEntity::class,
        PaymentHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PalmistDatabase : RoomDatabase() {
    abstract fun palmistDao(): PalmistDao
}

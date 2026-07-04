package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.PalmistDatabase
import com.example.data.repository.PalmistRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PalmistApplication : Application() {

    lateinit var database: PalmistDatabase
        private set

    lateinit var repository: PalmistRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        database = Room.databaseBuilder(
            applicationContext,
            PalmistDatabase::class.java,
            "palmist_database"
        )
        .fallbackToDestructiveMigration()
        .build()

        repository = PalmistRepository(applicationContext, database.palmistDao())

        // Initialize default billing state if empty
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch {
            repository.initializeBillingStateIfEmpty()
        }
    }
}

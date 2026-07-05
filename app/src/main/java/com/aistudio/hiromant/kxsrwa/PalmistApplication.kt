package com.aistudio.hiromant.kxsrwa

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.aistudio.hiromant.kxsrwa.data.local.PalmistDatabase
import com.aistudio.hiromant.kxsrwa.data.repository.PalmistRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

class PalmistApplication : Application() {

    lateinit var database: PalmistDatabase
        private set

    lateinit var repository: PalmistRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Setup robust global uncaught exception handler
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()
                
                // Use commit() to write to shared preferences synchronously before the app terminates
                val sharedPrefs = getSharedPreferences("palmist_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("last_crash_log", stackTrace).commit()
                
                // Write to crash.log file in external directory for offline access
                val logDir = getExternalFilesDir(null)
                if (logDir != null && (logDir.exists() || logDir.mkdirs())) {
                    val logFile = File(logDir, "crash.log")
                    val writer = FileWriter(logFile, true)
                    writer.write("\n--- Uncaught Crash at ${System.currentTimeMillis()} ---\n")
                    writer.write(stackTrace)
                    writer.write("\n--- End of crash ---\n\n")
                    writer.flush()
                    writer.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Let the standard OS crash dialog take over
            oldHandler?.uncaughtException(thread, throwable)
        }

        try {
            database = Room.databaseBuilder(
                applicationContext,
                PalmistDatabase::class.java,
                "palmist_database"
            )
            .fallbackToDestructiveMigration()
            .build()

            repository = PalmistRepository(applicationContext, database.palmistDao())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize default billing state if empty, with safety try-catch
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                if (::repository.isInitialized) {
                    repository.initializeBillingStateIfEmpty()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}

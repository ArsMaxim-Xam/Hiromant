package com.aistudio.hiromant.kxsrwa

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.aistudio.hiromant.kxsrwa.data.local.PalmistDatabase
import com.aistudio.hiromant.kxsrwa.data.repository.PalmistRepository
import com.aistudio.hiromant.kxsrwa.utils.AppLogger
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
        
        // Initialize AppLogger first
        AppLogger.init(this)
        AppLogger.i("PalmistApplication", "Application onCreate triggered. Version code/name from gradle compiled.")
        
        // Setup robust global uncaught exception handler
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()
                
                // Write to AppLogger
                AppLogger.e("UncaughtCrash", "Crash in thread ${thread.name}: ${throwable.message}", throwable)
                
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

        // Миграция со 2 на 3 версию базы данных для добавления таблицы истории платежей (сохраняет старые данные)
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Создаем таблицу истории платежей, если она еще не создана
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payment_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `amountRub` INTEGER NOT NULL,
                        `paymentSystem` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `readingType` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        try {
            AppLogger.i("PalmistApplication", "Building PalmistDatabase instance...")
            database = Room.databaseBuilder(
                applicationContext,
                PalmistDatabase::class.java,
                "palmist_database"
            )
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
            AppLogger.i("PalmistApplication", "PalmistDatabase successfully built.")

            AppLogger.i("PalmistApplication", "Building PalmistRepository instance...")
            repository = PalmistRepository(applicationContext, database.palmistDao())
            AppLogger.i("PalmistApplication", "PalmistRepository successfully built.")
        } catch (e: Exception) {
            AppLogger.e("PalmistApplication", "Failed to build DB or Repository", e)
            e.printStackTrace()
        }

        // Initialize default billing state if empty, with safety try-catch
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                if (::repository.isInitialized) {
                    AppLogger.i("PalmistApplication", "Initializing billing state if empty...")
                    repository.initializeBillingStateIfEmpty()
                    AppLogger.i("PalmistApplication", "Billing state checked/initialized.")
                }
            } catch (e: Throwable) {
                AppLogger.e("PalmistApplication", "Billing state initialization failed", e)
                e.printStackTrace()
            }
        }
    }
}

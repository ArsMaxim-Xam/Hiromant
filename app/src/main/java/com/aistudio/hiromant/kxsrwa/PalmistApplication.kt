package com.aistudio.hiromant.kxsrwa

import android.app.Application
import android.util.Log
import com.aistudio.hiromant.kxsrwa.data.local.PalmistDatabase
import com.aistudio.hiromant.kxsrwa.data.repository.PalmistRepository
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

class PalmistApplication : Application() {

    // Инициализация репозитория (ленивая, чтобы не нагружать старт)
    val repository: PalmistRepository by lazy {
        PalmistRepository(
            context = applicationContext,
            dao = PalmistDatabase.getDatabase(applicationContext).palmistDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        try {
            // Инициализация репозитория (вызов lazy сработает автоматически при первом обращении)
            // Можем также вызвать repository, чтобы инициализировать сейчас, но не обязательно
            // repository // если хотим инициализировать сразу
        } catch (e: Exception) {
            logException(e)
        }
    }

    private fun logException(e: Exception) {
        try {
            val logDir = filesDir
            if (logDir.exists() || logDir.mkdirs()) {
                val logFile = File(logDir, "crash.log")
                val writer = FileWriter(logFile, true)
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                e.printStackTrace(pw)
                writer.write("\n--- Crash at ${System.currentTimeMillis()} ---\n")
                writer.write(sw.toString())
                writer.write("\n--- End of crash ---\n\n")
                writer.flush()
                writer.close()
            }
        } catch (ex: Exception) {
            Log.e("PalmistApplication", "Не удалось записать лог", ex)
        }
    }
}

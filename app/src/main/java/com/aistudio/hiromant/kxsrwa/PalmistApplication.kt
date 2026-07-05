package com.aistudio.hiromant.kxsrwa

import android.app.Application
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

class PalmistApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Перехватываем все исключения на уровне Application
        try {
            // Здесь инициализация репозитория и других компонентов
            // (если они есть)
        } catch (e: Exception) {
            logException(e)
        }
    }

    private fun logException(e: Exception) {
        try {
            val logDir = filesDir // внутреннее хранилище приложения
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
            // Если и запись упала — просто логируем в Logcat
            Log.e("PalmistApplication", "Не удалось записать лог", ex)
        }
    }
}

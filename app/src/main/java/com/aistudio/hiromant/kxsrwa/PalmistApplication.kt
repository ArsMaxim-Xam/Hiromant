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

    // Репозиторий создаётся лениво, чтобы не тормозить старт
    val repository: PalmistRepository by lazy {
        val database = PalmistDatabase.getDatabase(applicationContext)
        PalmistRepository(
            context = applicationContext,
            dao = database.palmistDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        try {
            // Принудительно инициализируем репозиторий, чтобы поймать ошибку
            // Если здесь произойдёт исключение, оно будет записано в лог
            repository
        } catch (e: Exception) {
            logException(e)
        }
    }

    // Универсальный метод для записи ошибок в файл
    private fun logException(e: Exception) {
        try {
            val logDir = filesDir // внутреннее хранилище /data/data/.../files/
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
            // Если и запись упала — пишем в стандартный лог
            Log.e("PalmistApplication", "Не удалось записать лог", ex)
        }
    }
}

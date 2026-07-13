package com.aistudio.hiromant.kxsrwa.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: Long,
    val timeStr: String,
    val level: String, // "I", "D", "W", "E"
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
)

object AppLogger {
    private const val TAG = "AppLogger"
    private const val MAX_LOGS = 1000
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private var logFile: File? = null

    fun init(context: Context) {
        try {
            val cacheDir = context.cacheDir
            logFile = File(cacheDir, "operations_log.txt")
            if (logFile?.exists() == false) {
                logFile?.createNewFile()
            }
            log("I", TAG, "Logger initialized. File path: ${logFile?.absolutePath}")
            
            // Add existing crash logs if any
            val sharedPrefs = context.getSharedPreferences("palmist_prefs", Context.MODE_PRIVATE)
            val lastCrash = sharedPrefs.getString("last_crash_log", null)
            if (lastCrash != null) {
                log("E", "CrashReporter", "Detected last crash log:\n$lastCrash")
                // Clear after reading
                sharedPrefs.edit().remove("last_crash_log").apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize log file", e)
        }
    }

    @Synchronized
    fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = System.currentTimeMillis()
        val timeStr = timeFormat.format(Date(timestamp))
        val entry = LogEntry(timestamp, timeStr, level, tag, message, throwable)

        // Write to system logcat
        when (level) {
            "D" -> Log.d(tag, message, throwable)
            "W" -> Log.w(tag, message, throwable)
            "E" -> Log.e(tag, message, throwable)
            else -> Log.i(tag, message, throwable)
        }

        // Add to flow
        val currentList = _logs.value.toMutableList()
        currentList.add(entry)
        if (currentList.size > MAX_LOGS) {
            currentList.removeAt(0)
        }
        _logs.value = currentList

        // Write to operations_log.txt
        try {
            logFile?.let { file ->
                val writer = FileWriter(file, true)
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable?.printStackTrace(pw)
                val stackTrace = sw.toString()

                val line = if (stackTrace.isNotEmpty()) {
                    "[$timeStr] [$level/$tag] $message\n$stackTrace\n"
                } else {
                    "[$timeStr] [$level/$tag] $message\n"
                }
                writer.write(line)
                writer.flush()
                writer.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write to log file", e)
        }
    }

    fun d(tag: String, message: String) = log("D", tag, message)
    fun i(tag: String, message: String) = log("I", tag, message)
    fun w(tag: String, message: String) = log("W", tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = log("E", tag, message, throwable)

    fun getLogText(): String {
        return _logs.value.joinToString("\n") { entry ->
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            entry.throwable?.printStackTrace(pw)
            val stackTrace = sw.toString()
            val traceStr = if (stackTrace.isNotEmpty()) "\n$stackTrace" else ""
            "[${entry.timeStr}] [${entry.level}/${entry.tag}] ${entry.message}$traceStr"
        }
    }

    fun clear() {
        _logs.value = emptyList()
        try {
            logFile?.let { file ->
                if (file.exists()) {
                    file.delete()
                    file.createNewFile()
                }
            }
            log("I", TAG, "Log cleared.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear log file", e)
        }
    }
}

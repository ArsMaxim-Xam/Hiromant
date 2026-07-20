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

// Дата-класс, представляющий отдельную запись лога в приложении
data class LogEntry(
    val timestamp: Long, // Временная метка в миллисекундах
    val timeStr: String, // Отформатированная строка времени (yyyy-MM-dd HH:mm:ss.SSS)
    val level: String, // Уровень логирования: "I" (Info), "D" (Debug), "W" (Warning), "E" (Error)
    val tag: String, // Тег (источник) лога
    val message: String, // Текст сообщения лога
    val throwable: Throwable? = null // Объект исключения (если имеется ошибка)
)

// Синглтон-объект AppLogger для ведения системного и локального лога приложения
object AppLogger {
    private const val TAG = "AppLogger" // Постоянный тег для внутренних логов самого логгера
    private const val MAX_LOGS = 1000 // Максимальное количество записей в оперативной памяти
    // Форматировщик времени лога (год-месяц-день часы:минуты:секунды.миллисекунды)
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    // Поток списка записей лога для отображения в отладочном UI в реальном времени
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow() // Публичный неизменяемый поток

    // Файл для долговременного сохранения логов в кэш-директории
    private var logFile: File? = null

    // Инициализация логгера (вызывается из PalmistApplication)
    fun init(context: Context) {
        try {
            val cacheDir = context.cacheDir // Директория кэша приложения
            logFile = File(cacheDir, "operations_log.txt") // Файл operations_log.txt
            if (logFile?.exists() == false) {
                logFile?.createNewFile() // Создание нового файла, если его не существует
            }
            log("I", TAG, "Логгер успешно инициализирован. Путь к файлу: ${logFile?.absolutePath}")
            
            // Проверка наличия сохраненных данных о предыдущем падении приложения
            val sharedPrefs = context.getSharedPreferences("palmist_prefs", Context.MODE_PRIVATE)
            val lastCrash = sharedPrefs.getString("last_crash_log", null)
            if (lastCrash != null) {
                // Если краш-лог найден, выводим его как ошибку в логгер
                log("E", "CrashReporter", "Обнаружен отчет о предыдущем падении приложения:\n$lastCrash")
                // Очистка отчета в настройках после его прочтения и записи в логгер
                sharedPrefs.edit().remove("last_crash_log").apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось инициализировать лог-файл", e) // Системный вывод в Logcat
        }
    }

    // Синхронизированный метод добавления лога во избежание конфликтов из параллельных потоков
    @Synchronized
    fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = System.currentTimeMillis() // Текущее время в миллисекундах
        val timeStr = timeFormat.format(Date(timestamp)) // Преобразование в форматированную строку
        val entry = LogEntry(timestamp, timeStr, level, tag, message, throwable) // Создание объекта лога

        // Дублирование лога в стандартную утилиту Android Logcat
        when (level) {
            "D" -> Log.d(tag, message, throwable)
            "W" -> Log.w(tag, message, throwable)
            "E" -> Log.e(tag, message, throwable)
            else -> Log.i(tag, message, throwable)
        }

        // Добавление записи лога в реактивный список в памяти
        val currentList = _logs.value.toMutableList()
        currentList.add(entry)
        if (currentList.size > MAX_LOGS) {
            currentList.removeAt(0) // Удаление самой старой записи при переполнении лимита
        }
        _logs.value = currentList // Применение обновленного списка логов

        // Запись строки лога в постоянный файл operations_log.txt
        try {
            logFile?.let { file ->
                val writer = FileWriter(file, true) // Открытие писателя в режиме добавления в конец
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable?.printStackTrace(pw) // Сброс трассировки стека ошибки в буфер
                val stackTrace = sw.toString() // Преобразование трейса ошибки в строку

                // Форматирование финальной строки лога для текстового файла
                val line = if (stackTrace.isNotEmpty()) {
                    "[$timeStr] [$level/$tag] $message\n$stackTrace\n"
                } else {
                    "[$timeStr] [$level/$tag] $message\n"
                }
                writer.write(line) // Запись в файл
                writer.flush() // Выталкивание данных из буфера
                writer.close() // Закрытие писателя
            }
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось записать лог в файл", e) // Вывод ошибки записи в консоль Android
        }
    }

    // Вспомогательные функции для быстрой записи логов различных уровней
    fun d(tag: String, message: String) = log("D", tag, message) // Отладка
    fun i(tag: String, message: String) = log("I", tag, message) // Информирование
    fun w(tag: String, message: String) = log("W", tag, message) // Предупреждение
    fun e(tag: String, message: String, throwable: Throwable? = null) = log("E", tag, message, throwable) // Ошибка

    // Метод получения полного текста накопленных логов в виде единой отформатированной строки
    fun getLogText(): String {
        return _logs.value.joinToString("\n") { entry ->
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            entry.throwable?.printStackTrace(pw) // Получение стека ошибки для записи
            val stackTrace = sw.toString()
            val traceStr = if (stackTrace.isNotEmpty()) "\n$stackTrace" else ""
            "[${entry.timeStr}] [${entry.level}/${entry.tag}] ${entry.message}$traceStr" // Сборка строки лога
        }
    }

    // Полная очистка накопленных логов в памяти и пересоздание чистого файла лога на диске
    fun clear() {
        _logs.value = emptyList() // Очистка списка в памяти
        try {
            logFile?.let { file ->
                if (file.exists()) {
                    file.delete() // Удаление старого файла лога
                    file.createNewFile() // Создание пустого файла заново
                }
            }
            log("I", TAG, "Лог-файлы успешно очищены.")
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось очистить лог-файл", e) // Вывод ошибки в стандартный лог Android
        }
    }
}

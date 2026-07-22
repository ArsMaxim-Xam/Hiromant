package com.aistudio.hiromant.kxsrwa.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.hiromant.kxsrwa.PalmistApplication
import com.aistudio.hiromant.kxsrwa.data.local.BillingStateEntity
import com.aistudio.hiromant.kxsrwa.data.local.ReadingEntity
import com.aistudio.hiromant.kxsrwa.data.local.UserProfileEntity
import com.aistudio.hiromant.kxsrwa.ui.language.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PalmistViewModel(application: Application) : AndroidViewModel(application) {

    // Инициализация репозитория через PalmistApplication для доступа к данным и API
    private val repository = (application as PalmistApplication).repository

    // Состояние выбранного языка приложения (по умолчанию установлен русский)
    private val _selectedLanguage = MutableStateFlow(AppLanguage.RUS)
    // Публичный поток выбранного языка для наблюдения во Compose-компонентах
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage

    // Состояние масштаба шрифта интерфейса
    private val _fontScale = MutableStateFlow(1.0f)
    // Публичный поток масштаба шрифта для наблюдения
    val fontScale: StateFlow<Float> = _fontScale

    // Состояние активности озвучивания текста (TTS)
    private val _ttsEnabled = MutableStateFlow(true)
    // Публичный поток статуса активности озвучивания
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled

    // Выбранный пол голоса озвучки (женский/мужской)
    private val _ttsGender = MutableStateFlow("Female")
    // Публичный поток пола голоса озвучки
    val ttsGender: StateFlow<String> = _ttsGender

    // Индекс выбранного голоса в системе TTS
    private val _ttsVoiceIndex = MutableStateFlow(0)
    // Публичный поток индекса голоса в системе
    val ttsVoiceIndex: StateFlow<Int> = _ttsVoiceIndex

    // Скорость озвучивания текста синтезатором речи
    private val _ttsSpeechRate = MutableStateFlow(1.0f)
    // Публичный поток скорости воспроизведения озвучки
    val ttsSpeechRate: StateFlow<Float> = _ttsSpeechRate

    // Высота тона речи озвучки
    private val _ttsPitch = MutableStateFlow(1.0f)
    // Публичный поток высоты тона озвучки
    val ttsPitch: StateFlow<Float> = _ttsPitch

    // Профиль текущего пользователя из базы данных
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile.stateIn(
        scope = viewModelScope, // Область жизненного цикла ViewModel
        started = SharingStarted.WhileSubscribed(5000), // Сохранение при временной потере подписчиков
        initialValue = null // Изначальное значение до загрузки из БД
    )

    // Текущий статус биллинга (лимиты и баланс анализов)
    val billingState: StateFlow<BillingStateEntity?> = repository.billingState.stateIn(
        scope = viewModelScope, // Область жизненного цикла ViewModel
        started = SharingStarted.WhileSubscribed(5000), // Подписка с задержкой отписки в 5 секунд
        initialValue = null // Изначальное значение пустое
    )

    // Список всех сеансов анализа, сохраненных в истории
    val allReadings: StateFlow<List<ReadingEntity>> = repository.allReadings.stateIn(
        scope = viewModelScope, // Область сопрограмм ViewModel
        started = SharingStarted.WhileSubscribed(5000), // Оптимальная стратегия подписки
        initialValue = emptyList() // Начальное значение - пустой список
    )

    // Поток всех платежей пользователя из базы данных для отображения на экране "Кабинет"
    val allPayments: StateFlow<List<com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity>> = repository.allPayments.stateIn(
        scope = viewModelScope, // Сопрограммы привязаны к ViewModel
        started = SharingStarted.WhileSubscribed(5000), // Автоматическое отключение при фоне
        initialValue = emptyList() // По умолчанию пустой список
    )

    // Состояние процесса анализа (true, когда идет отправка/обработка)
    val isAnalyzing = MutableStateFlow(false)
    // Числовой прогресс выполнения анализа от 0 до 100 процентов
    val analysisProgress = MutableStateFlow(0)
    // Текстовый статус текущего шага анализа для пользователя
    val analysisStatus = MutableStateFlow("")

    // Текущий выбранный результат анализа ладони
    val currentReading = MutableStateFlow<ReadingEntity?>(null)
    // Текущий выбранный результат анализа совместимости партнеров
    val currentCompatibilityReading = MutableStateFlow<ReadingEntity?>(null)

    // Кэшированные изображения ладоней для экрана загрузки (левая ладонь)
    val bitmapLeftPalm = MutableStateFlow<Bitmap?>(null)
    // Кэшированные изображения ладоней для экрана загрузки (тыльная сторона левой руки)
    val bitmapLeftBack = MutableStateFlow<Bitmap?>(null)
    // Кэшированные изображения ладоней для экрана загрузки (правая ладонь)
    val bitmapRightPalm = MutableStateFlow<Bitmap?>(null)
    // Кэшированные изображения ладоней для экрана загрузки (тыльная сторона правой руки)
    val bitmapRightBack = MutableStateFlow<Bitmap?>(null)

    // Пути к сохраненным файлам изображений (левая ладонь)
    val leftPalmPath = MutableStateFlow<String?>(null)
    // Пути к сохраненным файлам изображений (тыльная сторона левой руки)
    val leftBackPath = MutableStateFlow<String?>(null)
    // Пути к сохраненным файлам изображений (правая ладонь)
    val rightPalmPath = MutableStateFlow<String?>(null)
    // Пути к сохраненным файлам изображений (тыльная сторона правой руки)
    val rightBackPath = MutableStateFlow<String?>(null)

    // Кэш снимка большого пальца
    val bitmapThumb = MutableStateFlow<Bitmap?>(null)
    // Кэш снимка ребра ладони
    val bitmapEdge = MutableStateFlow<Bitmap?>(null)
    // Путь к файлу снимка большого пальца
    val thumbPath = MutableStateFlow<String?>(null)
    // Путь к файлу снимка ребра ладони
    val edgePath = MutableStateFlow<String?>(null)

    // Ссылки на видеозаписи
    val videoUri = MutableStateFlow<android.net.Uri?>(null)
    val leftVideoUri = MutableStateFlow<android.net.Uri?>(null)
    val rightVideoUri = MutableStateFlow<android.net.Uri?>(null)
    // Выбранный тип анализа (по умолчанию brief_char)
    val currentAnalysisTypeState = MutableStateFlow("brief_char")
    // Флаг показа экрана интерпретации результатов
    val showInterpretationScreen = MutableStateFlow(false)

    // Активный раздел нижней панели навигации (по умолчанию upload)
    val activeTab = MutableStateFlow("upload")

    // Флаг возврата из программы на экран заставки
    val isReturnedToSplash = MutableStateFlow(false)

    // Предустановленная сумма платежа для быстрой поддержки проекта
    val paymentAmountToPreselect = MutableStateFlow("250")

    init {
        // Чтение ранее выбранного пользователем языка интерфейса при запуске приложения
        val code = repository.getSelectedLanguage()
        // Нахождение соответствующего перечисления языка по его коду
        val lang = AppLanguage.values().find { it.code == code } ?: AppLanguage.RUS
        _selectedLanguage.value = lang

        // Загрузка ранее настроенного масштаба шрифта
        _fontScale.value = repository.getFontScale()

        // Загрузка начальных параметров озвучивания (TTS) из SharedPreferences
        _ttsEnabled.value = repository.getTtsEnabled()
        _ttsGender.value = repository.getTtsGender()
        _ttsVoiceIndex.value = repository.getTtsVoiceIndex()
        _ttsSpeechRate.value = repository.getTtsSpeechRate()
        _ttsPitch.value = repository.getTtsPitch()
    }

    // Метод изменения масштаба шрифта приложения
    fun changeFontScale(scale: Float) {
        val clamped = scale.coerceIn(0.8f, 1.6f) // Обеспечение допустимых границ размера
        _fontScale.value = clamped // Сохранение локально
        repository.setFontScale(clamped) // Запись в SharedPreferences
    }

    // Метод переключения языка интерфейса приложения
    fun changeLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang // Применение в потоке данных
        repository.setSelectedLanguage(lang.code) // Запись в постоянные настройки
    }

    // Метод проверки выбора языка (для обхода приветственного экрана)
    fun isLanguageSelected(): Boolean {
        return repository.isLanguageSelected() // Чтение статуса выбора из репозитория
    }

    // Пометка о том, что язык успешно выбран пользователем
    fun markLanguageSelected() {
        repository.setLanguageSelected(true) // Сохранение флага в SharedPreferences
    }

    // --- Действия по настройке синтезатора речи TTS ---

    // Изменение статуса включения голосового сопровождения в приложении
    fun changeTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled // Применение локально во ViewModel
        repository.setTtsEnabled(enabled) // Запись значения в постоянные настройки
    }

    // Изменение пола голоса озвучки (женский/мужской)
    fun changeTtsGender(gender: String) {
        _ttsGender.value = gender // Обновление локального состояния во ViewModel
        repository.setTtsGender(gender) // Запись настройки пола в репозиторий для сохранения
    }

    // Изменение индекса голоса в системе TTS
    fun changeTtsVoiceIndex(index: Int) {
        _ttsVoiceIndex.value = index // Обновление локального значения индекса голоса
        repository.setTtsVoiceIndex(index) // Запись индекса голоса в постоянные настройки
    }

    // Изменение скорости озвучивания текста
    fun changeTtsSpeechRate(rate: Float) {
        _ttsSpeechRate.value = rate // Обновление скорости речи во ViewModel
        repository.setTtsSpeechRate(rate) // Запись скорости в постоянную конфигурацию
    }

    // Изменение высоты тона озвучивания
    fun changeTtsPitch(pitch: Float) {
        _ttsPitch.value = pitch // Обновление высоты тона речи во ViewModel
        repository.setTtsPitch(pitch) // Запись высоты тона в настройки репозитория
    }

    // --- Действия с профилем пользователя ---

    // Метод сохранения личных данных профиля пользователя в локальную базу данных
    fun saveProfile(
        name: String, // Имя пользователя
        gender: String, // Пол пользователя
        age: Int, // Возраст пользователя
        height: Int, // Рост пользователя
        dominantHand: String, // Активная (доминантная) рука
        email: String? = null, // Электронная почта (опционально)
        phone: String? = null, // Номер телефона (опционально)
        isRegistered: Boolean = false // Флаг регистрации пользователя
    ) {
        viewModelScope.launch { // Запуск в контексте сопрограмм жизненного цикла
            // Вызов метода репозитория для сохранения сущности профиля в локальную БД
            repository.saveUserProfile(name, gender, age, height, dominantHand, email, phone, isRegistered)
        }
    }

    // --- Действия биллинга (Имитация платежей для тестирования, полностью обновляет состояние БД!) ---

    // Имитация покупки премиум подписки (начисление 10 сеансов анализов в БД)
    fun simulateBuySubscription(amount: Int = 999, method: String = "Google Play (Встроенная)") {
        viewModelScope.launch { // Запуск асинхронной задачи в viewModelScope
            repository.addAnalyses(10) // Начисление 10 сеансов в базу данных
            // Создание записи об успешной оплате подписки в локальной истории транзакций
            val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = amount, // Сумма в рублях
                paymentSystem = method, // Метод оплаты
                status = "Успешно", // Статус транзакции
                readingType = "Премиум подписка (10 сеансов)" // Тип покупки
            )
            repository.insertPayment(payment) // Вставка записи платежа в базу данных
        }
    }

    // Имитация покупки одиночного подробного сеанса анализа ладони
    fun simulateBuySingleAnalysis(analysisType: String, amount: Int = 199, method: String = "ЮKassa (Банковская карта)") {
        viewModelScope.launch { // Запуск сопрограммы во ViewModel
            repository.unlockFeature(analysisType) // Разблокировка конкретной функции анализа в настройках
            repository.addAnalyses(1) // Добавление 1 доступного анализа в баланс
            // Создание сущности истории платежа
            val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = amount, // Стоимость одиночного анализа
                paymentSystem = method, // Система оплаты
                status = "Успешно", // Флаг успешной оплаты
                readingType = "Одиночный анализ: $analysisType" // Описание типа анализа
            )
            repository.insertPayment(payment) // Запись транзакции в базу данных
        }
    }

    // Имитация покупки разблокировки анализа совместимости по ладоням
    fun simulateBuyCompatibility() {
        viewModelScope.launch { // Асинхронный запуск в фоне
            repository.unlockFeature("compatibility") // Разблокировка функции совместимости в репозитории
        }
    }

    // Метод проверки разблокирована ли определенная платная функция анализа
    fun checkFeatureUnlocked(analysisType: String, onCheckComplete: (Boolean) -> Unit) {
        viewModelScope.launch { // Запуск сопрограммы
            val isUnlocked = repository.hasUnlocked(analysisType) // Получение статуса разблокировки функции из БД
            onCheckComplete(isUnlocked) // Возврат результата через лямбда-колбэк
        }
    }

    // --- Действия по работе с историей ---

    // Полная очистка локальной истории анализов
    fun clearHistory() {
        viewModelScope.launch { // Запуск сопрограммы очистки в фоне
            repository.clearHistory() // Вызов метода удаления всех записей истории из БД
        }
    }

    // Полный сброс всех данных приложения (очистка истории, сброс настроек и обнуление профиля)
    fun resetApplicationData() {
        viewModelScope.launch { // Запуск фоновой сопрограммы сброса настроек
            repository.clearHistory() // Удаление всех анализов из БД
            repository.setLanguageSelected(false) // Сброс отметки о пройденном выборе языка интерфейса
            try {
                // Запись пустого дефолтного профиля в базу данных для инициализации
                repository.saveUserProfile("", "", 25, 175, "Right", null, null, false)
            } catch (e: Exception) {
                e.printStackTrace() // Логирование ошибок в консоль разработчика
            }
        }
    }

    // Удаление конкретного сеанса анализа по его уникальному ID
    fun deleteReading(id: Long) {
        viewModelScope.launch { // Запуск удаления в фоновом потоке
            repository.deleteReading(id) // Удаление записи из БД через репозиторий
        }
    }

    // --- Методы для работы с историей оплат и реферальными начислениями (сохранение в БД) ---

    // Добавление новой записи о платеже в единую базу данных и начисление оплаченных анализов
    fun addPayment(amount: Int, paymentSystem: String, readingType: String, status: String = "Успешно") {
        viewModelScope.launch { // Запуск фоновой сопрограммы
            // Создаем сущность платежа для записи в базу данных
            val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = amount, // Сумма платежа в рублях
                paymentSystem = paymentSystem, // Способ или система платежа
                status = status, // Статус успешного прохождения оплаты
                readingType = readingType // Описание типа анализа
            )
            // Вставляем платеж в БД через репозиторий
            repository.insertPayment(payment)
            
            // Начисляем анализы в зависимости от суммы платежа
            val count = when {
                amount >= 999 -> 10 // Премиум-подписка (10 анализов)
                amount >= 499 -> 5  // Набор из 5 анализов
                amount >= 199 -> 3  // Набор из 3 анализов
                else -> 1           // 1 одиночный полный анализ
            }
            repository.addAnalyses(count) // Начисление анализов пользователю на баланс
        }
    }

    // Полная очистка истории платежей в базе данных
    fun clearPaymentHistory() {
        viewModelScope.launch { // Запуск сопрограммы асинхронной очистки
            repository.clearPaymentHistory() // Запрос удаления всех записей истории платежей в БД
        }
    }

    // Начисление вознаграждения (+3 полных анализа) за успешный шеринг и установку приложения
    fun rewardUserForSharing() {
        viewModelScope.launch { // Запуск сопрограммы начисления бонуса
            // Записываем информацию о бонусе в таблицу истории платежей БД для прозрачности
            val promoReward = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = 0, // Бонусное начисление (0 рублей)
                paymentSystem = "Бонус за установку (Поделиться)", // Название операции
                status = "Успешно начислено +3", // Информация о начислении в статусе
                readingType = "sharing_bonus" // Тип бонуса для идентификации
            )
            // Сохраняем промо-начисление в единую базу данных
            repository.insertPayment(promoReward)
            // Добавляем 3 анализа пользователю в профиль в БД
            repository.addAnalyses(3)
        }
    }

    // --- Запуск анализа ладони через нейросеть Gemini ---

    fun runPalmAnalysis(
        bitmaps: List<Bitmap>, // Снимки ладоней для распознавания линий ИИ
        videoUri: String?, // Дополнительное видео ладоней пользователя
        analysisType: String, // Тип анализа ("brief_char", "full_char", "brief_path", "full_path")
        leftPalmPath: String? = null, // Путь к файлу изображения левой ладони
        leftBackPath: String? = null, // Путь к тыльной стороне левой руки
        rightPalmPath: String? = null, // Путь к правой ладони в памяти
        rightBackPath: String? = null, // Путь к тыльной стороне правой руки
        onCompleted: () -> Unit // Функция обратного вызова при завершении
    ) {
        currentCompatibilityReading.value = null // Сброс текущей совместимости перед расчётом
        isAnalyzing.value = true // Перевод флага анализатора в активный режим
        analysisProgress.value = 0 // Сброс прогресса
        
        val isRussian = _selectedLanguage.value == AppLanguage.RUS // Определение языка системы
        analysisStatus.value = if (isRussian) "Запуск мистических сил..." else "Summoning cosmic currents..."

        viewModelScope.launch { // Старт сопрограммы
            try {
                // Шаг 1: Симуляция загрузки и обработки снимков (прогресс до 50%)
                for (p in 1..50) {
                    analysisProgress.value = p // Передача прогресса в UI
                    if (p <= 25) {
                        analysisStatus.value = if (isRussian) "Подготовка и сжатие снимков ладоней..." else "Preparing and compressing palm images..."
                        delay(40) // Искусственная пауза для мягкости отображения в UI
                    } else {
                        analysisStatus.value = if (isRussian) "Отправка данных на сервер..." else "Sending data to server..."
                        delay(40) // Пауза отправки сетевого пакета
                    }
                }
                
                analysisStatus.value = if (isRussian) "Данные отправлены. Ожидание ответа..." else "Data sent. Waiting for response..."
                
                // Вызов метода отправки изображений и параметров на Gemini API
                val reading = repository.analyzePalm(
                    bitmaps = bitmaps,
                    videoUri = videoUri,
                    analysisType = analysisType,
                    langCode = _selectedLanguage.value.code,
                    leftPalmPath = leftPalmPath,
                    leftBackPath = leftBackPath,
                    rightPalmPath = rightPalmPath,
                    rightBackPath = rightBackPath
                )
                
                currentReading.value = reading // Сохранение структуры результатов анализа
                
                // Снятие 1 сеанса из доступного количества анализов в локальной БД
                repository.decrementAnalyses()

                // Шаг 2: Симуляция получения и расшифровки линий (прогресс 51-100%)
                for (p in 51..100) {
                    analysisProgress.value = p // Передача процента в UI
                    if (p <= 75) {
                        analysisStatus.value = if (isRussian) "Получен ответ. Интерпретация линий..." else "Response received. Interpreting lines..."
                    } else {
                        analysisStatus.value = if (isRussian) "Сохранение результатов и построение карты..." else "Saving results and building map..."
                    }
                    delay(30) // Шаг анимации
                }

                delay(300) // Пауза перед скрытием диалога
                isAnalyzing.value = false // Сброс признака загрузки
                onCompleted() // Триггер перехода на экран результатов
            } catch (e: Exception) {
                e.printStackTrace() // Логирование ошибок
                isAnalyzing.value = false // Принудительный сброс режима загрузки при неудаче
            }
        }
    }

    // --- Запуск анализа совместимости партнеров по ладоням через Gemini ---

    fun runCompatibilityAnalysis(
        selfBitmap: Bitmap?, // Фотографии ладони первого партнёра
        partnerBitmap: Bitmap?, // Фотографии ладони второго партнёра
        selfName: String, // Имя первого партнёра
        partnerName: String, // Имя второго партнёра
        onCompleted: () -> Unit // Функция завершения работы
    ) {
        currentReading.value = null // Сброс одиночного анализа перед совместимостью
        isAnalyzing.value = true // Перевод в режим ИИ-расчёта
        analysisProgress.value = 0 // Сброс прогресс-бара
        
        val isRussian = _selectedLanguage.value == AppLanguage.RUS // Определение языка
        analysisStatus.value = if (isRussian) "Слияние аур..." else "Sensing energetic affinity..."

        viewModelScope.launch { // Старт сопрограммы во ViewModel
            try {
                // Фазы расчёта совместимости для визуального отображения
                val steps = if (isRussian) listOf(
                    "Сравнение линий сердца...",
                    "Оценка взаимных планетных вибраций...",
                    "Анализ совместимости холмов...",
                    "Составление прогноза союза..."
                ) else listOf(
                    "Comparing romantic heart lines...",
                    "Evaluating planet alignment parameters...",
                    "Analysing mount synergies...",
                    "Synthesising relationship future..."
                )

                val stepSize = 100 / steps.size // Вычисление диапазона шага прогресса
                for (i in 0 until steps.size) {
                    analysisStatus.value = steps[i] // Показ описания текущей фазы
                    val startProgress = i * stepSize // Начало отрезка
                    val endProgress = (i + 1) * stepSize // Конец отрезка
                    for (p in startProgress..endProgress) {
                        analysisProgress.value = p // Повышение процентов
                        delay(30) // Шаг задержки
                    }
                    delay(300) // Пауза между шагами для реалистичности
                }

                // Запрос к ИИ Gemini на расчёт перекрестной хиромантии партнеров
                val reading = repository.analyzeCompatibility(
                    selfBitmap = selfBitmap,
                    partnerBitmap = partnerBitmap,
                    selfName = selfName,
                    partnerName = partnerName,
                    langCode = _selectedLanguage.value.code
                )
                
                currentCompatibilityReading.value = reading // Сохранение сущности совместимости
                analysisProgress.value = 100 // Завершение шкалы
                delay(300) // Пауза
                isAnalyzing.value = false // Выключение режима загрузки
                onCompleted() // Колбэк успеха
            } catch (e: Exception) {
                e.printStackTrace() // Запись ошибки
                isAnalyzing.value = false // Отключение состояния прогресс-бара
            }
        }
    }

    // Метод платной разблокировки сохраненного анализа из истории (например, через СБП или банковскую карту)
    fun unlockPaidReading(readingId: Long, amount: Int = 150, method: String = "ЮKassa (СБП)", onUnlocked: () -> Unit) {
        viewModelScope.launch { // Запуск сопрограммы асинхронного обновления данных
            repository.unlockPaidReading(readingId) // Обновление флага разблокировки в базе данных
            val updated = repository.getReadingById(readingId) // Повторный запрос обновлённой записи из БД
            if (updated != null) {
                // Синхронизация активных данных во ViewModel для мгновенного отображения разблокированного текста
                if (updated.analysisType == "compatibility") {
                    currentCompatibilityReading.value = updated // Сохранение в поток совместимости
                } else {
                    currentReading.value = updated // Сохранение в поток обычного анализа
                }
                
                // Автоматически регистрируем платеж в локальной базе данных транзакций для отображения в Кабинете
                val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                    amountRub = amount, // Сумма платежа в рублях
                    paymentSystem = method, // Выбранная система оплаты
                    status = "Успешно", // Статус проведения платежа
                    readingType = "Разблокировка: " + (if (updated.analysisType == "compatibility") "Совместимость" else "Анализ ладони") // Описание покупки
                )
                repository.insertPayment(payment) // Запись транзакции платежа в локальную базу данных
            }
            onUnlocked() // Вызов лямбда-колбэка для оповещения UI
        }
    }

    // Метод проведения добровольного пожертвования (доната) пользователем на поддержку развития проекта
    fun addSupportPayment(amountRub: Int, paymentSystem: String) {
        viewModelScope.launch { // Запуск фонового процесса обработки транзакции поддержки
            try {
                // Извлечение текущих активных данных о пользователе
                val currentRead = currentReading.value ?: currentCompatibilityReading.value
                val profile = repository.getUserProfileSync()
                
                // Определение имени и возраста пользователя для логирования платежа
                val name = currentRead?.name ?: profile?.name ?: "Искатель"
                val age = currentRead?.age ?: profile?.age ?: 25
                val lp = currentRead?.leftPalmPath
                val lb = currentRead?.leftBackPath
                val rp = currentRead?.rightPalmPath
                val rb = currentRead?.rightBackPath
                
                val granted = amountRub / 100 // Начисление бонусных анализов: +1 анализ за каждые 100 рублей поддержки
                
                val currentBilling = repository.getBillingStateSync() // Получение текущего баланса
                val currentRemaining = currentBilling?.remainingAnalyses ?: 0 // Текущий остаток сеансов
                val newRemaining = currentRemaining + granted // Новый остаток после начисления донат-бонуса
                
                // Создание подробной записи истории транзакции поддержки в локальной базе данных
                val supportPayment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                    amountRub = amountRub, // Сумма поддержки в рублях
                    paymentSystem = paymentSystem, // Система проведения платежа
                    status = "Успешно", // Флаг успешной транзакции
                    readingType = "Поддержка проекта (+$granted анализов)", // Описание операции в истории
                    userName = name, // Имя пользователя
                    userAge = age, // Возраст пользователя
                    leftPalmPath = lp, // Ссылка на левую ладонь
                    leftBackPath = lb, // Ссылка на левую тыльную
                    rightPalmPath = rp, // Ссылка на правую ладонь
                    rightBackPath = rb, // Ссылка на правую тыльную
                    grantedAnalyses = granted, // Количество начисленных анализов
                    remainingAnalysesAfterPayment = newRemaining // Остаток баланса после проведения операции
                )
                
                repository.insertPayment(supportPayment) // Запись донат-транзакции в базу данных
                
                if (granted > 0) {
                    repository.addAnalyses(granted) // Фактическое начисление бонусов на баланс пользователя в БД
                }
            } catch (e: Exception) {
                e.printStackTrace() // Печать стека ошибок в логгер при возникновении сбоя
            }
        }
    }

    // Состояние процесса получения ответа на уточняющий вопрос пользователя (загрузка)
    val followUpLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    // Состояние, содержащее текстовый ответ от ИИ Gemini на уточняющий вопрос
    val followUpResponse = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    // Метод отправки ИИ Gemini уточняющего вопроса по текущему тексту результатов анализа
    fun sendFollowUpQuestion(analysisText: String, question: String, onComplete: (String) -> Unit = {}) {
        viewModelScope.launch { // Запуск сопрограммы общения с ИИ
            followUpLoading.value = true // Включение кругового индикатора прогресса в UI
            followUpResponse.value = null // Очистка предыдущего ответа
            val lang = if (selectedLanguage.value == AppLanguage.RUS) "RU" else "EN" // Определение языка общения
            val resp = repository.askFollowUpQuestion(analysisText, question, lang) // Вызов API Gemini через репозиторий
            followUpResponse.value = resp // Запись полученного ИИ-ответа в поток данных для Compose-интерфейса
            followUpLoading.value = false // Снятие индикатора ожидания ответа
            onComplete(resp) // Вызов лямбды завершения
        }
    }
    
    // Метод очистки диалога уточняющих вопросов
    fun clearFollowUp() {
        followUpResponse.value = null // Сброс текстового ответа ИИ в значение null
        followUpLoading.value = false // Сброс состояния индикации загрузки в false
    }
}

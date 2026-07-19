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

    private val repository = (application as PalmistApplication).repository

    // Language state
    private val _selectedLanguage = MutableStateFlow(AppLanguage.RUS)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage

    // Font scale state
    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale

    // TTS configurations state
    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled

    private val _ttsGender = MutableStateFlow("Female")
    val ttsGender: StateFlow<String> = _ttsGender

    private val _ttsVoiceIndex = MutableStateFlow(0)
    val ttsVoiceIndex: StateFlow<Int> = _ttsVoiceIndex

    private val _ttsSpeechRate = MutableStateFlow(1.0f)
    val ttsSpeechRate: StateFlow<Float> = _ttsSpeechRate

    private val _ttsPitch = MutableStateFlow(1.0f)
    val ttsPitch: StateFlow<Float> = _ttsPitch

    // Subscribed state and readings list
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val billingState: StateFlow<BillingStateEntity?> = repository.billingState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allReadings: StateFlow<List<ReadingEntity>> = repository.allReadings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Поток всех платежей пользователя из базы данных для отображения на экране "Кабинет"
    val allPayments: StateFlow<List<com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity>> = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Analysis states
    val isAnalyzing = MutableStateFlow(false)
    val analysisProgress = MutableStateFlow(0)
    val analysisStatus = MutableStateFlow("")

    val currentReading = MutableStateFlow<ReadingEntity?>(null)
    val currentCompatibilityReading = MutableStateFlow<ReadingEntity?>(null)

    // Persisted states for UploadScreen
    val bitmapLeftPalm = MutableStateFlow<Bitmap?>(null)
    val bitmapLeftBack = MutableStateFlow<Bitmap?>(null)
    val bitmapRightPalm = MutableStateFlow<Bitmap?>(null)
    val bitmapRightBack = MutableStateFlow<Bitmap?>(null)

    val leftPalmPath = MutableStateFlow<String?>(null)
    val leftBackPath = MutableStateFlow<String?>(null)
    val rightPalmPath = MutableStateFlow<String?>(null)
    val rightBackPath = MutableStateFlow<String?>(null)

    val bitmapThumb = MutableStateFlow<Bitmap?>(null)
    val bitmapEdge = MutableStateFlow<Bitmap?>(null)
    val thumbPath = MutableStateFlow<String?>(null)
    val edgePath = MutableStateFlow<String?>(null)

    val videoUri = MutableStateFlow<android.net.Uri?>(null)
    val leftVideoUri = MutableStateFlow<android.net.Uri?>(null)
    val rightVideoUri = MutableStateFlow<android.net.Uri?>(null)
    val currentAnalysisTypeState = MutableStateFlow("brief_char")
    val showInterpretationScreen = MutableStateFlow(false)

    // Active navigation tab state
    val activeTab = MutableStateFlow("upload")

    // Dynamic billing amount preselection state
    val paymentAmountToPreselect = MutableStateFlow("250")

    init {
        // Load initially selected language
        val code = repository.getSelectedLanguage()
        val lang = AppLanguage.values().find { it.code == code } ?: AppLanguage.RUS
        _selectedLanguage.value = lang

        // Load initially stored font scale
        _fontScale.value = repository.getFontScale()

        // Load initially stored TTS configurations
        _ttsEnabled.value = repository.getTtsEnabled()
        _ttsGender.value = repository.getTtsGender()
        _ttsVoiceIndex.value = repository.getTtsVoiceIndex()
        _ttsSpeechRate.value = repository.getTtsSpeechRate()
        _ttsPitch.value = repository.getTtsPitch()
    }

    fun changeFontScale(scale: Float) {
        val clamped = scale.coerceIn(0.8f, 1.6f)
        _fontScale.value = clamped
        repository.setFontScale(clamped)
    }

    fun changeLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        repository.setSelectedLanguage(lang.code)
    }

    fun isLanguageSelected(): Boolean {
        return repository.isLanguageSelected()
    }

    fun markLanguageSelected() {
        repository.setLanguageSelected(true)
    }

    // --- TTS settings actions ---

    fun changeTtsEnabled(enabled: Boolean) {
        _ttsEnabled.value = enabled
        repository.setTtsEnabled(enabled)
    }

    fun changeTtsGender(gender: String) {
        _ttsGender.value = gender
        repository.setTtsGender(gender)
    }

    fun changeTtsVoiceIndex(index: Int) {
        _ttsVoiceIndex.value = index
        repository.setTtsVoiceIndex(index)
    }

    fun changeTtsSpeechRate(rate: Float) {
        _ttsSpeechRate.value = rate
        repository.setTtsSpeechRate(rate)
    }

    fun changeTtsPitch(pitch: Float) {
        _ttsPitch.value = pitch
        repository.setTtsPitch(pitch)
    }

    // --- Profile actions ---

    fun saveProfile(
        name: String,
        gender: String,
        age: Int,
        height: Int,
        dominantHand: String,
        email: String? = null,
        phone: String? = null,
        isRegistered: Boolean = false
    ) {
        viewModelScope.launch {
            repository.saveUserProfile(name, gender, age, height, dominantHand, email, phone, isRegistered)
        }
    }

    // --- Billing actions (Simulated for testing, fully updates the local database state!) ---

    fun simulateBuySubscription(amount: Int = 999, method: String = "Google Play (Встроенная)") {
        viewModelScope.launch {
            repository.addAnalyses(10)
            // Сохраняем информацию о покупке пакета анализов в единой базе данных
            val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = amount,
                paymentSystem = method,
                status = "Успешно",
                readingType = "Премиум подписка (10 сеансов)"
            )
            repository.insertPayment(payment)
        }
    }

    fun simulateBuySingleAnalysis(analysisType: String, amount: Int = 199, method: String = "ЮKassa (Банковская карта)") {
        viewModelScope.launch {
            repository.unlockFeature(analysisType)
            repository.addAnalyses(1)
            // Сохраняем информацию об оплате одного анализа в единой базе данных
            val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = amount,
                paymentSystem = method,
                status = "Успешно",
                readingType = "Одиночный анализ: $analysisType"
            )
            repository.insertPayment(payment)
        }
    }

    fun simulateBuyCompatibility() {
        viewModelScope.launch {
            repository.unlockFeature("compatibility")
        }
    }

    fun checkFeatureUnlocked(analysisType: String, onCheckComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isUnlocked = repository.hasUnlocked(analysisType)
            onCheckComplete(isUnlocked)
        }
    }

    // --- History actions ---

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun resetApplicationData() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.setLanguageSelected(false)
            try {
                repository.saveUserProfile("", "", 25, 175, "Right", null, null, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteReading(id: Long) {
        viewModelScope.launch {
            repository.deleteReading(id)
        }
    }

    // --- Методы для работы с историей оплат и реферальными начислениями (сохранение в БД) ---

    // Добавление новой записи о платеже в единую базу данных и начисление оплаченных анализов
    fun addPayment(amount: Int, paymentSystem: String, readingType: String, status: String = "Успешно") {
        viewModelScope.launch {
            // Создаем сущность платежа для записи в базу данных
            val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = amount,
                paymentSystem = paymentSystem,
                status = status,
                readingType = readingType
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
            repository.addAnalyses(count)
        }
    }

    // Полная очистка истории платежей в базе данных
    fun clearPaymentHistory() {
        viewModelScope.launch {
            repository.clearPaymentHistory()
        }
    }

    // Начисление вознаграждения (+3 полных анализа) за успешный шеринг и установку приложения
    fun rewardUserForSharing() {
        viewModelScope.launch {
            // Записываем информацию о бонусе в таблицу истории платежей БД для прозрачности
            val promoReward = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                amountRub = 0, // Бонусное начисление (0 рублей)
                paymentSystem = "Бонус за установку (Поделиться)",
                status = "Успешно начислено +3",
                readingType = "sharing_bonus"
            )
            // Сохраняем промо-начисление в единую базу данных
            repository.insertPayment(promoReward)
            // Добавляем 3 анализа пользователю в профиль в БД
            repository.addAnalyses(3)
        }
    }

    // --- Gemini Analysis trigger ---

    fun runPalmAnalysis(
        bitmaps: List<Bitmap>,
        videoUri: String?,
        analysisType: String, // "brief_char", "full_char", "brief_path", "full_path"
        leftPalmPath: String? = null,
        leftBackPath: String? = null,
        rightPalmPath: String? = null,
        rightBackPath: String? = null,
        onCompleted: () -> Unit
    ) {
        currentCompatibilityReading.value = null
        isAnalyzing.value = true
        analysisProgress.value = 0
        
        val isRussian = _selectedLanguage.value == AppLanguage.RUS
        analysisStatus.value = if (isRussian) "Запуск мистических сил..." else "Summoning cosmic currents..."

        viewModelScope.launch {
            try {
                // Step 1: Smoothly animate progress from 0% to 50% to represent preparing and uploading images.
                // 0 to 25: Compress/Prepare
                // 26 to 50: Send
                for (p in 1..50) {
                    analysisProgress.value = p
                    if (p <= 25) {
                        analysisStatus.value = if (isRussian) "Подготовка и сжатие снимков ладоней..." else "Preparing and compressing palm images..."
                        delay(40)
                    } else {
                        analysisStatus.value = if (isRussian) "Отправка данных на сервер..." else "Sending data to server..."
                        delay(40)
                    }
                }
                
                analysisStatus.value = if (isRussian) "Данные отправлены. Ожидание ответа..." else "Data sent. Waiting for response..."
                
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
                
                currentReading.value = reading
                
                // Decrement analysis count locally
                repository.decrementAnalyses()

                // Step 2: Upon response, smoothly increase progress from 51% to 100%
                for (p in 51..100) {
                    analysisProgress.value = p
                    if (p <= 75) {
                        analysisStatus.value = if (isRussian) "Получен ответ. Интерпретация линий..." else "Response received. Interpreting lines..."
                    } else {
                        analysisStatus.value = if (isRussian) "Сохранение результатов и построение карты..." else "Saving results and building map..."
                    }
                    delay(30)
                }

                delay(300)
                isAnalyzing.value = false
                onCompleted()
            } catch (e: Exception) {
                e.printStackTrace()
                isAnalyzing.value = false
            }
        }
    }

    // --- Gemini Compatibility trigger ---

    fun runCompatibilityAnalysis(
        selfBitmap: Bitmap?,
        partnerBitmap: Bitmap?,
        partnerName: String,
        onCompleted: () -> Unit
    ) {
        currentReading.value = null
        isAnalyzing.value = true
        analysisProgress.value = 0
        
        val isRussian = _selectedLanguage.value == AppLanguage.RUS
        analysisStatus.value = if (isRussian) "Слияние аур..." else "Sensing energetic affinity..."

        viewModelScope.launch {
            try {
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

                val stepSize = 100 / steps.size
                for (i in 0 until steps.size) {
                    analysisStatus.value = steps[i]
                    val startProgress = i * stepSize
                    val endProgress = (i + 1) * stepSize
                    for (p in startProgress..endProgress) {
                        analysisProgress.value = p
                        delay(30)
                    }
                    delay(300)
                }

                val reading = repository.analyzeCompatibility(
                    selfBitmap = selfBitmap,
                    partnerBitmap = partnerBitmap,
                    partnerName = partnerName,
                    langCode = _selectedLanguage.value.code
                )
                
                currentCompatibilityReading.value = reading
                analysisProgress.value = 100
                delay(300)
                isAnalyzing.value = false
                onCompleted()
            } catch (e: Exception) {
                e.printStackTrace()
                isAnalyzing.value = false
            }
        }
    }

    fun unlockPaidReading(readingId: Long, amount: Int = 150, method: String = "ЮKassa (СБП)", onUnlocked: () -> Unit) {
        viewModelScope.launch {
            repository.unlockPaidReading(readingId)
            val updated = repository.getReadingById(readingId)
            if (updated != null) {
                if (updated.analysisType == "compatibility") {
                    currentCompatibilityReading.value = updated
                } else {
                    currentReading.value = updated
                }
                
                // Автоматически регистрируем платеж в единой базе данных в истории платежей
                val payment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                    amountRub = amount,
                    paymentSystem = method,
                    status = "Успешно",
                    readingType = "Разблокировка: " + (if (updated.analysisType == "compatibility") "Совместимость" else "Анализ ладони")
                )
                repository.insertPayment(payment)
            }
            onUnlocked()
        }
    }

    fun addSupportPayment(amountRub: Int, paymentSystem: String) {
        viewModelScope.launch {
            try {
                val currentRead = currentReading.value ?: currentCompatibilityReading.value
                val profile = repository.getUserProfileSync()
                
                val name = currentRead?.name ?: profile?.name ?: "Искатель"
                val age = currentRead?.age ?: profile?.age ?: 25
                val lp = currentRead?.leftPalmPath
                val lb = currentRead?.leftBackPath
                val rp = currentRead?.rightPalmPath
                val rb = currentRead?.rightBackPath
                
                val granted = amountRub / 100
                
                val currentBilling = repository.getBillingStateSync()
                val currentRemaining = currentBilling?.remainingAnalyses ?: 0
                val newRemaining = currentRemaining + granted
                
                val supportPayment = com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity(
                    amountRub = amountRub,
                    paymentSystem = paymentSystem,
                    status = "Успешно",
                    readingType = "Поддержка проекта (+$granted анализов)",
                    userName = name,
                    userAge = age,
                    leftPalmPath = lp,
                    leftBackPath = lb,
                    rightPalmPath = rp,
                    rightBackPath = rb,
                    grantedAnalyses = granted,
                    remainingAnalysesAfterPayment = newRemaining
                )
                
                repository.insertPayment(supportPayment)
                
                if (granted > 0) {
                    repository.addAnalyses(granted)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

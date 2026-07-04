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

    // Analysis states
    val isAnalyzing = MutableStateFlow(false)
    val analysisProgress = MutableStateFlow(0)
    val analysisStatus = MutableStateFlow("")

    val currentReading = MutableStateFlow<ReadingEntity?>(null)
    val currentCompatibilityReading = MutableStateFlow<ReadingEntity?>(null)

    init {
        // Load initially selected language
        val code = repository.getSelectedLanguage()
        val lang = AppLanguage.values().find { it.code == code } ?: AppLanguage.RUS
        _selectedLanguage.value = lang
    }

    fun changeLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        repository.setSelectedLanguage(lang.code)
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

    fun simulateBuySubscription() {
        viewModelScope.launch {
            repository.addAnalyses(10)
        }
    }

    fun simulateBuySingleAnalysis(analysisType: String) {
        viewModelScope.launch {
            repository.unlockFeature(analysisType)
            repository.addAnalyses(1)
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

    fun deleteReading(id: Long) {
        viewModelScope.launch {
            repository.deleteReading(id)
        }
    }

    // --- Gemini Analysis trigger ---

    fun runPalmAnalysis(
        bitmaps: List<Bitmap>,
        videoUri: String?,
        analysisType: String, // "brief_char", "full_char", "brief_path", "full_path"
        onCompleted: () -> Unit
    ) {
        isAnalyzing.value = true
        analysisProgress.value = 0
        
        val isRussian = _selectedLanguage.value == AppLanguage.RUS
        analysisStatus.value = if (isRussian) "Запуск мистических сил..." else "Summoning cosmic currents..."

        viewModelScope.launch {
            try {
                // Beautiful simulated incremental loader steps for the absolute ultimate mystical UX
                val steps = if (isRussian) listOf(
                    "Настройка связи со звёздами...",
                    "Анализ переплетения линий ладони...",
                    "Изучение силы планетных холмов...",
                    "Чтение тайных рун и особых знаков...",
                    "Формирование пророчества..."
                ) else listOf(
                    "Aligning with celestial bodies...",
                    "Tracing palm line intersections...",
                    "Measuring planetary mount energy...",
                    "Scanning for sacred geometric symbols...",
                    "Writing cosmic predictions..."
                )

                for (i in 0 until steps.size) {
                    analysisStatus.value = steps[i]
                    val startProgress = i * 20
                    for (p in startProgress..(startProgress + 20)) {
                        analysisProgress.value = p
                        delay(25) // Smooth scroll
                    }
                    delay(300)
                }

                val reading = repository.analyzePalm(
                    bitmaps = bitmaps,
                    videoUri = videoUri,
                    analysisType = analysisType,
                    langCode = _selectedLanguage.value.code
                )
                
                currentReading.value = reading
                
                // Decrement analysis count locally
                repository.decrementAnalyses()

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

    // --- Gemini Compatibility trigger ---

    fun runCompatibilityAnalysis(
        selfBitmap: Bitmap?,
        partnerBitmap: Bitmap?,
        partnerName: String,
        onCompleted: () -> Unit
    ) {
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
}

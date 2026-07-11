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

    // Persisted states for UploadScreen
    val bitmapLeftPalm = MutableStateFlow<Bitmap?>(null)
    val bitmapLeftBack = MutableStateFlow<Bitmap?>(null)
    val bitmapRightPalm = MutableStateFlow<Bitmap?>(null)
    val bitmapRightBack = MutableStateFlow<Bitmap?>(null)

    val leftPalmPath = MutableStateFlow<String?>(null)
    val leftBackPath = MutableStateFlow<String?>(null)
    val rightPalmPath = MutableStateFlow<String?>(null)
    val rightBackPath = MutableStateFlow<String?>(null)

    val videoUri = MutableStateFlow<android.net.Uri?>(null)

    // Global VPN Status Flows for Top Status Bar
    val vpnIp = MutableStateFlow("185.220.101.42")
    val vpnFlag = MutableStateFlow("🇩🇪")
    val aiAvailabilityStatus = MutableStateFlow("available") // "available", "unavailable", "checking"
    val vpnConnected = MutableStateFlow(true)
    val vpnConnecting = MutableStateFlow(false)
    val vpnCountryIndex = MutableStateFlow(0)
    val vpnDurationSeconds = MutableStateFlow(14)
    val vpnKbReceived = MutableStateFlow(245.8)
    val vpnKbSent = MutableStateFlow(112.4)
    val vpnUploadSpeed = MutableStateFlow(45.2)
    val vpnDownloadSpeed = MutableStateFlow(112.8)

    val selectedVpnAppName = MutableStateFlow("ChatGPT")
    val selectedVpnAppPackage = MutableStateFlow("com.openai.chatgpt")

    data class AppItem(val name: String, val packageName: String, val isInstalled: Boolean)
    val availableApps = MutableStateFlow<List<AppItem>>(emptyList())

    fun loadInstalledApps() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val pm = context.packageManager
                val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
                    addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                }
                val launchables = pm.queryIntentActivities(intent, 0)

                val installed = launchables.mapNotNull { resolveInfo ->
                    val name = resolveInfo.loadLabel(pm).toString()
                    val pkg = resolveInfo.activityInfo.packageName
                    if (pkg != context.packageName) {
                        AppItem(name, pkg, true)
                    } else null
                }.distinctBy { it.packageName }.sortedBy { it.name.lowercase() }

                val popular = listOf(
                    AppItem("ChatGPT (OpenAI)", "com.openai.chatgpt", false),
                    AppItem("Gemini (Google)", "com.google.android.apps.bard", false),
                    AppItem("Copilot (Microsoft)", "com.microsoft.copilot", false),
                    AppItem("Telegram", "org.telegram.messenger", false),
                    AppItem("Instagram", "com.instagram.android", false),
                    AppItem("YouTube", "com.google.android.youtube", false),
                    AppItem("Claude AI", "com.anthropic.claude", false)
                )

                val merged = (installed + popular.filter { p -> installed.none { i -> i.packageName == p.packageName } })
                availableApps.value = merged
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        // Load initially selected language
        val code = repository.getSelectedLanguage()
        val lang = AppLanguage.values().find { it.code == code } ?: AppLanguage.RUS
        _selectedLanguage.value = lang

        // Load initially stored font scale
        _fontScale.value = repository.getFontScale()

        // Load available launcher apps
        loadInstalledApps()

        // Centralized VPN simulation loop
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (vpnConnected.value && !vpnConnecting.value) {
                    vpnDurationSeconds.value += 1
                    
                    val up = (15..95).random() / 10.0 + if ((0..1).random() == 1) (50..300).random() / 10.0 else 0.0
                    val down = (80..350).random() / 10.0 + if ((0..1).random() == 1) (400..1500).random() / 10.0 else 0.0
                    vpnUploadSpeed.value = up
                    vpnDownloadSpeed.value = down

                    vpnKbReceived.value += down
                    vpnKbSent.value += up
                } else {
                    vpnUploadSpeed.value = 0.0
                    vpnDownloadSpeed.value = 0.0
                }
            }
        }
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
                    langCode = _selectedLanguage.value.code,
                    leftPalmPath = leftPalmPath,
                    leftBackPath = leftBackPath,
                    rightPalmPath = rightPalmPath,
                    rightBackPath = rightBackPath
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

    fun unlockPaidReading(readingId: Long, onUnlocked: () -> Unit) {
        viewModelScope.launch {
            repository.unlockPaidReading(readingId)
            val updated = repository.getReadingById(readingId)
            if (updated != null) {
                if (updated.analysisType == "compatibility") {
                    currentCompatibilityReading.value = updated
                } else {
                    currentReading.value = updated
                }
            }
            onUnlocked()
        }
    }
}

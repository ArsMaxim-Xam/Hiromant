package com.aistudio.hiromant.kxsrwa.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.aistudio.hiromant.kxsrwa.BuildConfig
import com.aistudio.hiromant.kxsrwa.data.local.*
import com.aistudio.hiromant.kxsrwa.data.remote.*
import com.aistudio.hiromant.kxsrwa.utils.BitmapUtils.toBase64
import com.aistudio.hiromant.kxsrwa.utils.AppLogger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class PalmistRepository(
    private val context: Context,
    private val dao: PalmistDao
) {
    private val sharedPrefs = context.getSharedPreferences("palmist_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val billingState: Flow<BillingStateEntity?> = dao.getBillingState()
    val allReadings: Flow<List<ReadingEntity>> = dao.getAllReadings()

    fun getFontScale(): Float {
        return sharedPrefs.getFloat("app_font_scale", 1.0f)
    }

    fun setFontScale(scale: Float) {
        sharedPrefs.edit().putFloat("app_font_scale", scale).apply()
    }

    // --- Language preferences ---

    fun getSelectedLanguage(): String {
        // Auto-detect system language by default (RU or EN)
        val defaultLang = if (java.util.Locale.getDefault().language.lowercase() == "ru") "RU" else "EN"
        return sharedPrefs.getString("app_language", defaultLang) ?: defaultLang
    }

    fun setSelectedLanguage(lang: String) {
        sharedPrefs.edit().putString("app_language", lang).apply()
    }

    fun isLanguageSelected(): Boolean {
        return sharedPrefs.getBoolean("is_language_selected", false)
    }

    fun setLanguageSelected(selected: Boolean) {
        sharedPrefs.edit().putBoolean("is_language_selected", selected).apply()
    }

    // --- Profile & Account ---

    suspend fun saveUserProfile(
        name: String,
        gender: String,
        age: Int,
        height: Int,
        dominantHand: String,
        email: String? = null,
        phone: String? = null,
        isRegistered: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val profile = UserProfileEntity(
            name = name,
            gender = gender,
            age = age,
            height = height,
            dominantHand = dominantHand,
            email = email,
            phone = phone,
            isRegistered = isRegistered
        )
        dao.insertUserProfile(profile)
    }

    suspend fun deleteUserProfile() = withContext(Dispatchers.IO) {
        dao.clearUserProfile()
    }

    // --- Billing and local purchases ---

    suspend fun initializeBillingStateIfEmpty() = withContext(Dispatchers.IO) {
        val current = dao.getBillingStateSync()
        if (current == null) {
            dao.insertBillingState(BillingStateEntity(remainingAnalyses = 3)) // 3 free brief readings
        }
    }

    suspend fun addAnalyses(count: Int) = withContext(Dispatchers.IO) {
        val current = dao.getBillingStateSync() ?: BillingStateEntity(remainingAnalyses = 0)
        dao.insertBillingState(
            current.copy(
                remainingAnalyses = current.remainingAnalyses + count,
                isPremiumSubscribed = if (count >= 10) true else current.isPremiumSubscribed
            )
        )
    }

    suspend fun unlockFeature(itemId: String) = withContext(Dispatchers.IO) {
        val current = dao.getBillingStateSync() ?: BillingStateEntity()
        val list = current.purchasedItemIds.split(",").filter { it.isNotEmpty() }.toMutableList()
        if (!list.contains(itemId)) {
            list.add(itemId)
        }
        dao.insertBillingState(
            current.copy(
                purchasedItemIds = list.joinToString(",")
            )
        )
    }

    suspend fun hasUnlocked(itemId: String): Boolean = withContext(Dispatchers.IO) {
        val current = dao.getBillingStateSync() ?: return@withContext false
        val list = current.purchasedItemIds.split(",").filter { it.isNotEmpty() }
        return@withContext list.contains(itemId) || current.isPremiumSubscribed
    }

    suspend fun decrementAnalyses(): Boolean = withContext(Dispatchers.IO) {
        val current = dao.getBillingStateSync() ?: return@withContext false
        if (current.remainingAnalyses > 0) {
            dao.insertBillingState(current.copy(remainingAnalyses = current.remainingAnalyses - 1))
            return@withContext true
        }
        return@withContext false
    }

    // --- History ---

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearHistory()
    }

    suspend fun deleteReading(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteReadingById(id)
    }

    // --- Gemini Palm Reading Core ---

    suspend fun analyzePalm(
        bitmaps: List<Bitmap>,
        videoUri: String?,
        analysisType: String, // "brief_char", "full_char", "brief_path", "full_path"
        langCode: String,
        leftPalmPath: String? = null,
        leftBackPath: String? = null,
        rightPalmPath: String? = null,
        rightBackPath: String? = null
    ): ReadingEntity = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfileEntity(name = "Искатель", age = 25)
        AppLogger.i("PalmistRepository", "analyzePalm triggered. bitmaps count: ${bitmaps.size}, analysisType: $analysisType, langCode: $langCode. profile: name=${profile.name}, gender=${profile.gender}, age=${profile.age}")

        val isRussian = langCode == "RU"
        val isFull = analysisType.startsWith("full")
        val isCharacter = analysisType.contains("char")

        val systemInstructionText = """
            You are a master palmist (chiromancer) with 30 years of esoteric experience. 
            You must analyze the user's hand images and details, then generate a detailed chiromancy report in JSON.
            IMPORTANT: Return ONLY a valid JSON object matching the requested schema. No conversational markers, no ```json tags.
        """.trimIndent()

        val promptText = when (analysisType) {
            "brief_char" -> """
                Please perform a BRIEF, FREE Character & Personal Qualities reading of my palm.
                My profile data:
                - Name: ${profile.name}
                - Gender: ${profile.gender}
                - Age: ${profile.age} years
                - Height: ${profile.height} cm
                - Dominant Hand: ${profile.dominantHand} (Remember: for ${profile.dominantHand}, active life is on this hand, birth potentials on the other).
                
                Since this is a BRIEF, FREE character reading:
                - Focus primarily on sections 1, 2, and 3: Left Hand Analysis (what is inherited/innate), Right Hand Analysis (what was developed/realized), and Character/Personal Qualities.
                - Each of these 3 sections must be approximately 1/3 of the length of a full detailed analysis (about 1-2 paragraphs each).
                - Leave sections 4, 5, and 6 empty (empty strings "").
                
                Please evaluate the shape of the hand, finger proportions, major lines (Life, Heart, Head, Destiny) and planetary mounts.
                
                Provide the response strictly in ${if (isRussian) "Russian (русский)" else "English"}.
                The response MUST be a single valid JSON object following this format:
                {
                  "overallPortrait": "Brief summary of the personality.",
                  "handType": "Type of hand.",
                  "leftHand": "1. Анализ Левой руки (то, что заложено в человеке) - краткий, около 1/3 от полного.",
                  "rightHand": "2. Анализ Правой руки (то, как человек живёт и реализуется) - краткий, около 1/3 от полного.",
                  "characterQualities": "3. Анализ Характера и Качеств человека - краткий, около 1/3 от полного.",
                  "lifePathEvents": "",
                  "lifeSituationsInfluence": "",
                  "marriageChildren": "",
                  "recommendations": "Actionable guidelines.",
                  "lines": [
                    {
                      "name": "Line Name",
                      "color": "Hex color code",
                      "shortDescription": "One line summary",
                      "fullDescription": "Detailed reading.",
                      "keyTakeaways": ["takeaway 1", "takeaway 2"]
                    }
                  ],
                  "mounts": [
                    {
                      "name": "Mount Name",
                      "active": true,
                      "description": "Short reading"
                    }
                  ],
                  "signs": [
                    {
                      "name": "Sign",
                      "description": "Reading",
                      "location": "Location"
                    }
                  ]
                }
            """.trimIndent()

            "full_char" -> """
                Please perform a FULL, DETAILED, PAID Character & Personal Qualities reading of my palm.
                My profile data:
                - Name: ${profile.name}
                - Gender: ${profile.gender}
                - Age: ${profile.age} years
                - Height: ${profile.height} cm
                - Dominant Hand: ${profile.dominantHand} (Remember: for ${profile.dominantHand}, active life is on this hand, birth potentials on the other).
                
                Since this is a FULL, DETAILED character reading:
                - Focus exhaustively on sections 1, 2, and 3: Left Hand Analysis (what is inherited/innate), Right Hand Analysis (what was developed/realized), and Character/Personal Qualities.
                - Each of these 3 sections must be as detailed, rich, and comprehensive as possible (at least 3-4 deep paragraphs each).
                - Leave sections 4, 5, and 6 empty (empty strings "").
                
                Please evaluate the shape of the hand, finger proportions, major lines (Life, Heart, Head, Destiny), planetary mounts, signs, and markings.
                
                Provide the response strictly in ${if (isRussian) "Russian (русский)" else "English"}.
                The response MUST be a single valid JSON object following this format:
                {
                  "overallPortrait": "Comprehensive, rich summary portrait of character.",
                  "handType": "Type of hand.",
                  "leftHand": "1. Анализ Левой руки (то, что заложено в человеке) - максимально подробно и развернуто.",
                  "rightHand": "2. Анализ Правой руки (то, как человек живёт и реализуется) - максимально подробно и развернуто.",
                  "characterQualities": "3. Анализ Характера и Качеств человека - максимально подробно и развернуто.",
                  "lifePathEvents": "",
                  "lifeSituationsInfluence": "",
                  "marriageChildren": "",
                  "recommendations": "Highly actionable guidelines and warnings.",
                  "lines": [
                    {
                      "name": "Line Name",
                      "color": "Hex color code",
                      "shortDescription": "One line summary",
                      "fullDescription": "Detailed reading.",
                      "keyTakeaways": ["takeaway 1", "takeaway 2"]
                    }
                  ],
                  "mounts": [
                    {
                      "name": "Mount Name",
                      "active": true,
                      "description": "Short reading"
                    }
                  ],
                  "signs": [
                    {
                      "name": "Sign",
                      "description": "Reading",
                      "location": "Location"
                    }
                  ]
                }
            """.trimIndent()

            "brief_path" -> """
                Please perform a BRIEF, FREE Life Path & Events reading of my palm.
                My profile data:
                - Name: ${profile.name}
                - Gender: ${profile.gender}
                - Age: ${profile.age} years
                - Height: ${profile.height} cm
                - Dominant Hand: ${profile.dominantHand} (Remember: for ${profile.dominantHand}, active life is on this hand, birth potentials on the other).
                
                Since this is a BRIEF, FREE life path reading:
                - Focus primarily on sections 4, 5, and 6: Life Path and Events (major milestones/ages), Life Situations & External Influences, and Relationships (family, marriage, children, life partners).
                - Each of these 3 sections must be approximately 1/3 of the length of a full detailed analysis (about 1-2 paragraphs each).
                - Leave sections 1, 2, and 3 empty (empty strings "").
                
                Please evaluate the major lines (especially Destiny and Life lines) and planetary mounts.
                
                Provide the response strictly in ${if (isRussian) "Russian (русский)" else "English"}.
                The response MUST be a single valid JSON object following this format:
                {
                  "overallPortrait": "Brief summary of life path and destiny.",
                  "handType": "Type of hand.",
                  "leftHand": "",
                  "rightHand": "",
                  "characterQualities": "",
                  "lifePathEvents": "4. Анализ Жизненного пути и событий - краткий, около 1/3 от полного.",
                  "lifeSituationsInfluence": "5. Анализ Жизненных ситуаций и внешнего влияния на жизнь человека - краткий, около 1/3 от полного.",
                  "marriageChildren": "6. Анализ Отношений Семьи, Брака, Дети и Спутники жизни - краткий, около 1/3 от полного.",
                  "recommendations": "Actionable guidelines.",
                  "lines": [
                    {
                      "name": "Line Name",
                      "color": "Hex color code",
                      "shortDescription": "One line summary",
                      "fullDescription": "Detailed reading.",
                      "keyTakeaways": ["takeaway 1", "takeaway 2"]
                    }
                  ],
                  "mounts": [
                    {
                      "name": "Mount Name",
                      "active": true,
                      "description": "Short reading"
                    }
                  ],
                  "signs": [
                    {
                      "name": "Sign",
                      "description": "Reading",
                      "location": "Location"
                    }
                  ]
                }
            """.trimIndent()

            else -> """
                Please perform a FULL, DETAILED, PAID Life Path & Events reading of my palm.
                My profile data:
                - Name: ${profile.name}
                - Gender: ${profile.gender}
                - Age: ${profile.age} years
                - Height: ${profile.height} cm
                - Dominant Hand: ${profile.dominantHand} (Remember: for ${profile.dominantHand}, active life is on this hand, birth potentials on the other).
                
                Since this is a FULL, DETAILED life path reading:
                - Focus exhaustively on sections 4, 5, and 6: Life Path and Events (detailed milestones, transitions, ages), Life Situations & External Influences (challenges, opportunities, cosmic influences), and Relationships (family, marriage, kids, partners).
                - Each of these 3 sections must be as detailed, rich, and comprehensive as possible (at least 3-4 deep paragraphs each).
                - Leave sections 1, 2, and 3 empty (empty strings "").
                
                Please evaluate the major lines (especially Destiny, Life, Heart lines), planetary mounts, signs, and markings.
                
                Provide the response strictly in ${if (isRussian) "Russian (русский)" else "English"}.
                The response MUST be a single valid JSON object following this format:
                {
                  "overallPortrait": "Comprehensive, rich summary of life path and destiny.",
                  "handType": "Type of hand.",
                  "leftHand": "",
                  "rightHand": "",
                  "characterQualities": "",
                  "lifePathEvents": "4. Анализ Жизненного пути и событий - максимально подробно и развернуто.",
                  "lifeSituationsInfluence": "5. Анализ Жизненных ситуаций и внешнего влияния на жизнь человека - максимально подробно и развернуто.",
                  "marriageChildren": "6. Анализ Отношений Семьи, Брака, Дети и Спутники жизни - максимально подробно и развернуто.",
                  "recommendations": "Highly actionable guidelines and warnings.",
                  "lines": [
                    {
                      "name": "Line Name",
                      "color": "Hex color code",
                      "shortDescription": "One line summary",
                      "fullDescription": "Detailed reading.",
                      "keyTakeaways": ["takeaway 1", "takeaway 2"]
                    }
                  ],
                  "mounts": [
                    {
                      "name": "Mount Name",
                      "active": true,
                      "description": "Short reading"
                    }
                  ],
                  "signs": [
                    {
                      "name": "Sign",
                      "description": "Reading",
                      "location": "Location"
                    }
                  ]
                }
            """.trimIndent()
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val maskedKey = if (apiKey.isEmpty()) "[EMPTY]" else {
            val len = apiKey.length
            if (len > 8) "${apiKey.take(4)}...${apiKey.takeLast(4)} (len=$len)" else "*** (len=$len)"
        }
        val isKeyPlaceholder = apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY" || apiKey == "YOUR_GEMINI_API_KEY"
        AppLogger.i("PalmistRepository", "API Key check: key=$maskedKey, isPlaceholder=$isKeyPlaceholder")
        val hasValidKey = apiKey.isNotEmpty() && !isKeyPlaceholder

        var resultJsonStr = ""

        if (hasValidKey) {
            try {
                AppLogger.i("PalmistRepository", "Initiating Gemini request. Preparing parts. Prompt text length: ${promptText.length}")
                val parts = mutableListOf<Part>()
                parts.add(Part(text = promptText))

                // Attach up to 4 images to limit token usage and remain highly stable
                bitmaps.take(4).forEachIndexed { index, bitmap ->
                    val base64 = bitmap.toBase64( quality = 60 )
                    AppLogger.i("PalmistRepository", "Adding image part $index. Width=${bitmap.width}, Height=${bitmap.height}, Base64 length=${base64.length}")
                    parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
                }

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = parts)),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json"
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
                )

                AppLogger.i("PalmistRepository", "Sending Retrofit request to RetrofitClient.service...")
                val response = RetrofitClient.service.generateContent(apiKey, request)
                AppLogger.i("PalmistRepository", "Received response from Gemini. Processing candidates...")
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                AppLogger.i("PalmistRepository", "Raw response candidate text: ${rawText?.take(150)}... [length=${rawText?.length ?: 0}]")
                if (!rawText.isNullOrEmpty()) {
                    // Extract JSON in case Gemini wrapped it in markdown tags
                    resultJsonStr = extractJsonFromMarkdown(rawText)
                    AppLogger.i("PalmistRepository", "Extracted JSON text successfully. Length: ${resultJsonStr.length}")
                } else {
                    AppLogger.w("PalmistRepository", "Received empty response or candidates list is empty.")
                }
            } catch (e: Exception) {
                AppLogger.e("PalmistRepository", "Exception during Gemini API request: ${e.message}", e)
                Log.e("PalmistRepository", "Gemini API error", e)
            }
        } else {
            AppLogger.w("PalmistRepository", "Valid API Key not found. Skipping Gemini request.")
        }

        // Generate high quality mock fallback if network fails or API is missing
        if (resultJsonStr.isEmpty()) {
            AppLogger.i("PalmistRepository", "Result JSON is empty, generating local mock fallback report.")
            resultJsonStr = generateLocalMockReport(profile, isRussian, isFull, isCharacter)
        }

        val reading = ReadingEntity(
            name = profile.name,
            gender = profile.gender,
            age = profile.age,
            height = profile.height,
            dominantHand = profile.dominantHand,
            analysisType = analysisType,
            resultJson = resultJsonStr,
            leftPalmPath = leftPalmPath,
            leftBackPath = leftBackPath,
            rightPalmPath = rightPalmPath,
            rightBackPath = rightBackPath,
            videoPath = videoUri
        )

        val id = dao.insertReading(reading)
        return@withContext reading.copy(id = id)
    }

    // --- Gemini Compatibility Reading Core ---

    suspend fun analyzeCompatibility(
        selfBitmap: Bitmap?,
        partnerBitmap: Bitmap?,
        partnerName: String,
        langCode: String
    ): ReadingEntity = withContext(Dispatchers.IO) {
        val profile = dao.getUserProfileSync() ?: UserProfileEntity(name = "Искатель", age = 25)
        val isRussian = langCode == "RU"

        val promptText = """
            Analyze the relationship compatibility between two people based on palmistry principles.
            Person 1 (User):
            - Name: ${profile.name}
            - Gender: ${profile.gender}
            - Age: ${profile.age} years
            - Hand: ${profile.dominantHand}
            
            Person 2 (Partner):
            - Name: $partnerName
            
            Evaluate the synergy between their Heart Lines, Marriage lines, and Planetary Mounts.
            Provide the response strictly in ${if (isRussian) "Russian (русский)" else "English"}.
            Return a single JSON object strictly matching this schema:
            {
              "compatibilityPercent": 85,
              "partner1Portrait": "Reading of Person 1's relationship style.",
              "partner2Portrait": "Reading of Person 2's relationship style.",
              "combinedAnalysis": "Summary of their energetic connection.",
              "strongPoints": ["Point 1", "Point 2"],
              "weakPoints": ["Risk 1", "Risk 2"],
              "emotionalCompatibility": "Detailed emotional link reading.",
              "intellectualCompatibility": "Communication and alignment.",
              "financialCompatibility": "Wealth synergy.",
              "recommendations": "Advice on how to improve unity."
            }
        """.trimIndent()

        val systemInstructionText = "You are a compatibility palmist. Generate a structured JSON relationship audit. No markdown tags, no wrapper conversations."

        val apiKey = BuildConfig.GEMINI_API_KEY
        val maskedKey = if (apiKey.isEmpty()) "[EMPTY]" else {
            val len = apiKey.length
            if (len > 8) "${apiKey.take(4)}...${apiKey.takeLast(4)} (len=$len)" else "*** (len=$len)"
        }
        val isKeyPlaceholder = apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY" || apiKey == "YOUR_GEMINI_API_KEY"
        AppLogger.i("PalmistRepository", "Compatibility API Key check: key=$maskedKey, isPlaceholder=$isKeyPlaceholder")
        val hasValidKey = apiKey.isNotEmpty() && !isKeyPlaceholder

        var resultJsonStr = ""

        if (hasValidKey) {
            try {
                AppLogger.i("PalmistRepository", "Initiating Gemini compatibility request. Prompt text length: ${promptText.length}")
                val parts = mutableListOf<Part>()
                parts.add(Part(text = promptText))

                if (selfBitmap != null) {
                    val base64 = selfBitmap.toBase64(50)
                    AppLogger.i("PalmistRepository", "Adding self image part. Width=${selfBitmap.width}, Height=${selfBitmap.height}, Base64 length=${base64.length}")
                    parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
                }
                if (partnerBitmap != null) {
                    val base64 = partnerBitmap.toBase64(50)
                    AppLogger.i("PalmistRepository", "Adding partner image part. Width=${partnerBitmap.width}, Height=${partnerBitmap.height}, Base64 length=${base64.length}")
                    parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
                }

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = parts)),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json"
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
                )

                AppLogger.i("PalmistRepository", "Sending Retrofit request to RetrofitClient.service for compatibility...")
                val response = RetrofitClient.service.generateContent(apiKey, request)
                AppLogger.i("PalmistRepository", "Received response from Gemini compatibility. Processing candidates...")
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                AppLogger.i("PalmistRepository", "Raw response compatibility candidate text: ${rawText?.take(150)}... [length=${rawText?.length ?: 0}]")
                if (!rawText.isNullOrEmpty()) {
                    resultJsonStr = extractJsonFromMarkdown(rawText)
                    AppLogger.i("PalmistRepository", "Extracted compatibility JSON successfully. Length: ${resultJsonStr.length}")
                } else {
                    AppLogger.w("PalmistRepository", "Received empty response or candidates list is empty for compatibility.")
                }
            } catch (e: Exception) {
                AppLogger.e("PalmistRepository", "Exception during Gemini compatibility request: ${e.message}", e)
                Log.e("PalmistRepository", "Gemini compatibility error", e)
            }
        } else {
            AppLogger.w("PalmistRepository", "Valid API Key not found. Skipping Gemini compatibility request.")
        }

        if (resultJsonStr.isEmpty()) {
            AppLogger.i("PalmistRepository", "Result compatibility JSON is empty, generating local mock fallback compatibility report.")
            resultJsonStr = generateLocalMockCompatibility(profile.name, partnerName, isRussian)
        }

        val reading = ReadingEntity(
            name = profile.name,
            gender = profile.gender,
            age = profile.age,
            height = profile.height,
            dominantHand = profile.dominantHand,
            analysisType = "compatibility",
            resultJson = resultJsonStr,
            partnerName = partnerName
        )

        val id = dao.insertReading(reading)
        return@withContext reading.copy(id = id)
    }

    // --- Helpers ---

    suspend fun getReadingById(id: Long): ReadingEntity? = withContext(Dispatchers.IO) {
        dao.getReadingById(id)
    }

    suspend fun unlockPaidReading(readingId: Long) = withContext(Dispatchers.IO) {
        val reading = dao.getReadingById(readingId) ?: return@withContext
        val profile = dao.getUserProfileSync() ?: UserProfileEntity(name = "Искатель", age = 25)
        
        val isRussian = getSelectedLanguage() == "RU"
        val isFull = reading.analysisType.contains("full")
        val isCharacter = reading.analysisType.contains("char")
        
        val newJson = if (reading.analysisType == "compatibility") {
            generateLocalMockCompatibility(profile.name, reading.partnerName ?: "Партнёр", isRussian)
        } else {
            generateLocalMockReport(profile, isRussian, isFull, isCharacter)
        }
        
        val updatedReading = reading.copy(resultJson = newJson)
        dao.insertReading(updatedReading)
    }

    private fun extractJsonFromMarkdown(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    private fun generateLocalMockReport(
        profile: UserProfileEntity,
        isRussian: Boolean,
        isFull: Boolean,
        isCharacter: Boolean
    ): String {
        val report = if (isRussian) {
            val leftHandText = if (isCharacter) {
                if (isFull) {
                    "Анализ Левой руки (то, что заложено в человеке): Левая рука ${profile.name} раскрывает мощный врожденный потенциал. С рождения вам предначертаны глубокая интуиция, развитая фантазия и склонность к созерцанию. Вы унаследовали сильный творческий дух, тонкую душевную организацию и обостренное чувство справедливости. Ваша врожденная матрица несет в себе способность улавливать скрытые мотивы людей и тонко чувствовать красоту окружающего мира."
                } else {
                    "Анализ Левой руки (то, что заложено в человеке): Врожденная матрица ${profile.name} несет в себе сильный творческий потенциал, высокую интуицию и стремление к духовным поискам."
                }
            } else ""

            val rightHandText = if (isCharacter) {
                if (isFull) {
                    "Анализ Правой руки (то, как человек живёт и реализуется): Правая (активная) ладонь показывает, как вы распорядились дарами судьбы. Вы научились контролировать свои эмоции, развили сильную волю и способность доводить начатое до конца. Приобретенная практичность и умение адаптироваться к жизненным трудностям помогают вам реализовывать врожденные таланты в материальном мире. Вы трансформировали юношеский идеализм в осознанную мудрость."
                } else {
                    "Анализ Правой руки (то, как человек живёт и реализуется): На протяжении жизни вы развили сильную волю, практичность в делах и умение справляться со сложными ситуациями."
                }
            } else ""

            val charQualitiesText = if (isCharacter) {
                if (isFull) {
                    "Анализ Характера и Качеств человека: Ваш характер отличается редким сочетанием чувствительности и решительности. Вы обладаете аналитическим складом ума, но при этом часто полагаетесь на интуицию, которая вас редко подводит. В общении вы проявляете себя как преданный друг, мудрый собеседник и человек слова. Основные качества — благородство, искренность, упорство в достижении целей и стремление к непрерывному саморазвитию."
                } else {
                    "Анализ Характера и Качеств человека: Основные черты вашего характера — благородство, целеустремленность, сочетание разума и развитой интуиции."
                }
            } else ""

            val lifePathEventsText = if (!isCharacter) {
                if (isFull) {
                    "Анализ Жизненного пути и событий: Ваш жизненный путь полон интересных вызовов и триумфальных моментов. В возрасте 20-22 лет произошел важный выбор жизненного ориентира. На рубеже 33-35 лет вас ожидает масштабный прорыв, связанный с обретением финансовой независимости и раскрытием личного предназначения. Период после 45 лет характеризуется укреплением авторитета, стабильным ростом благосостояния и глубокой гармонией во всех сферах жизни."
                } else {
                    "Анализ Жизненного пути и событий: Жизненный путь отмечен важным выбором в 20-22 года, карьерным прорывом в районе 33-35 лет и стабильностью в зрелом возрасте."
                }
            } else ""

            val lifeSituationsInfluenceText = if (!isCharacter) {
                if (isFull) {
                    "Анализ Жизненных ситуаций и внешнего влияния на жизнь человека: Внешние обстоятельства часто проверяют вас на прочность, но каждое испытание делает вас сильнее. Влияние других людей на вашу судьбу минимально — вы сами являетесь творцом своей реальности. Тем не менее, в вашей жизни присутствуют покровители, чьи своевременные советы уберегут вас от неверных шагов. Звезды советуют сохранять спокойствие в периоды турбулентности."
                } else {
                    "Анализ Жизненных ситуаций и внешнего влияния на жизнь человека: Вы успешно преодолеваете внешние преграды, опираясь на внутренний стержень и редкие, но ценные советы наставников."
                }
            } else ""

            val marriageChildrenText = if (!isCharacter) {
                if (isFull) {
                    "Анализ Отношений Семьи, Брака, Дети и Спутники жизни: В сфере отношений ваша ладонь указывает на глубокую преданность и стремление создать крепкий семейный очаг. Линия Брака глубокая и ровная, что предвещает гармоничный, счастливый и долговечный союз с родственной душой. Спутник жизни будет человеком интеллектуальным и поддерживающим. Также отчетливо видны знаки детей, которые принесут в ваш дом радость и тепло."
                } else {
                    "Анализ Отношений Семьи, Брака, Дети и Спутники жизни: Ладонь указывает на крепкий, основанный на доверии брак и гармоничные семейные отношения, согретые любовью детей."
                }
            } else ""

            PalmistReport(
                overallPortrait = "⚠️ ЭТО ПРИМЕР ИНТЕРПРЕТАЦИИ (Демонстрационный режим). Чтобы получить настоящий астрологический анализ ваших ладоней по фотографии, пожалуйста, подключите ваш API-ключ Gemini в панели Secrets в AI Studio.\n\nОбщий анализ ладони для ${profile.name} указывает на яркую, духовно развитую личность. Ваша ладонь сочетает черты Огня и Воздуха, свидетельствуя о высоком интеллектуальном потенциале, творческой импульсивности и стремлении к гармонии с внешним миром. У вас развитая интуиция, которая часто помогает в кризисных ситуациях.",
                handType = "Смешанный тип (Воздух-Огонь)",
                lines = listOf(
                    PalmLineAnalysis(
                        name = "Линия Жизни",
                        color = "#C41E3A",
                        shortDescription = "Глубокая, ровная линия жизни с хорошим огибанием бугра Венеры.",
                        fullDescription = "Ваша Линия Жизни сильная и глубокая. Она свидетельствует о высоком уровне жизненной энергии и хорошем запасе физических сил. Небольшое ответвление в сторону Лунного бугра на уровне 35 лет указывает на важные перемены места жительства или дальние поездки, принесшие духовный рост. В районе 50 лет линия становится ещё шире, предвещая спокойный и стабильный зрелый возраст.",
                        keyTakeaways = listOf("Высокая витальность", "Возможность переезда в районе 35 лет", "Стабильное долголетие")
                    ),
                    PalmLineAnalysis(
                        name = "Линия Сердца",
                        color = "#E94B8A",
                        shortDescription = "Изогнутая, оканчивающаяся между пальцами Сатурна и Юпитера.",
                        fullDescription = "Линия Сердца длинная и выразительная. Вы — человек с глубоким чувством эмпатии, способный на сильную привязанность. Завершение линии между холмами Юпитера и Сатурна показывает баланс между идеализмом в любви и практическим здравым смыслом. Мелкие штрихи, пересекающие линию в раннем возрасте, говорят о прошлых эмоциональных переживаниях, из которых вы вынесли ценную мудрость.",
                        keyTakeaways = listOf("Глубокая эмпатия", "Эмоциональный интеллект", "Сбалансированный подход к отношениям")
                    ),
                    PalmLineAnalysis(
                        name = "Линия Головы",
                        color = "#3A7BD5",
                        shortDescription = "Длинная линия, плавно спускающаяся к холму Луны.",
                        fullDescription = "Линия Ума (Головы) имеет хороший изгиб в сторону холма Луны, что указывает на преобладание творческого воображения над сухим логическим анализом. Вы обладаете гибким мышлением, любите нестандартные решения и интересуетесь философией или мистицизмом. Чёткость линии показывает концентрацию и умение быстро принимать решения в сложных ситуациях.",
                        keyTakeaways = listOf("Творческое мышление", "Интерес к тайным знаниям", "Гибкость ума")
                    ),
                    PalmLineAnalysis(
                        name = "Линия Судьбы",
                        color = "#2ECC71",
                        shortDescription = "Начинается у запястья и чётко следует к холму Сатурна.",
                        fullDescription = "Ваша Линия Судьбы проявлена достаточно ярко, что говорит о сильном чувстве долга и осознании своей жизненной цели. Наличие небольшого разрыва в районе 28-30 лет указывает на изменение профессионального вектора или переоценку ценностей, после чего карьера пошла вверх с удвоенной силой. Линия сливается с Линией Головы, символизируя, что успех придёт благодаря вашему собственному разуму.",
                        keyTakeaways = listOf("Целеустремлённость", "Успешная смена вектора в 28-30 лет", "Успех своими силами")
                    )
                ),
                mounts = listOf(
                    PalmMountAnalysis("Бугор Венеры", true, "Крупный и упругий бугор свидетельствует о страстности, жизнелюбии, доброте и любви к искусству."),
                    PalmMountAnalysis("Бугор Юпитера", true, "Хорошо выражен. Отражает лидерские качества, гордость, амбиции и стремление к духовному авторитету."),
                    PalmMountAnalysis("Бугор Сатурна", false, "Умеренно плоский, что дарует вам спокойствие, мудрость, философский склад ума и независимость."),
                    PalmMountAnalysis("Бугор Аполлона", true, "Выраженный холм искусства. Указывает на тягу к прекрасному, эстетизм и творческое самовыражение.")
                ),
                signs = listOf(
                    PalmSign("Мистический Крест", "Крест между Линией Сердца и Линией Ума. Знак сильных экстрасенсорных способностей, интуиции и интереса к тайным знаниям.", "Пространство между линиями в центре ладони"),
                    PalmSign("Кольцо Соломона", "Полукруг под указательным пальцем. Дарует мудрость, авторитет и способность понимать психологию людей с первого взгляда.", "Под холмом Юпитера")
                ),
                marriageChildren = marriageChildrenText,
                lifeEvents = lifePathEventsText,
                predictions = lifeSituationsInfluenceText,
                recommendations = "Развивайте свою природную интуицию — она ваш главный компас. Старайтесь чаще бывать на природе для восстановления душевных сил. Практикуйте медитации или дыхательные упражнения для центрирования вашей огненной энергии.",
                leftHand = leftHandText,
                rightHand = rightHandText,
                characterQualities = charQualitiesText
            )
        } else {
            val leftHandText = if (isCharacter) {
                if (isFull) {
                    "Left Hand Analysis (Innate Potentials): Left hand of ${profile.name} reveals a powerful inherited blueprint. Since birth, a deep sense of intuition, high imagination, and a philosophical mind have been set. You possess an innate empathy and high artistic talent."
                } else {
                    "Left Hand Analysis (Innate Potentials): Your birth potential is characterized by deep creativity, natural intuition, and high adaptive intelligence."
                }
            } else ""

            val rightHandText = if (isCharacter) {
                if (isFull) {
                    "Right Hand Analysis (Acquired Traits): The active right hand highlights how you have actualized your innate talents. You have structured your mind, strengthened your personal boundaries, and developed a strong sense of worldly logic and determination."
                } else {
                    "Right Hand Analysis (Acquired Traits): Over the years you have developed superb practical skills, professional diligence, and emotional composure."
                }
            } else ""

            val charQualitiesText = if (isCharacter) {
                if (isFull) {
                    "Analysis of Character & Personal Qualities: Your personality stands out due to a beautiful synthesis of sensitivity and direct leadership qualities. You are loyal, ambitious, intellectual, and naturally attracted to spiritual and esoteric studies."
                } else {
                    "Analysis of Character & Personal Qualities: Key personality markers indicate absolute loyalty, mental flexibility, and a superb balance of intuition and practical execution."
                }
            } else ""

            val lifePathEventsText = if (!isCharacter) {
                if (isFull) {
                    "Analysis of Life Path & Events: Your life journey is marked by fascinating career reinventions and spiritual shifts. An early crossroad occurred between ages 20 and 22. A major prosperity threshold is visible between 33 and 35, leading to total domestic and professional freedom."
                } else {
                    "Analysis of Life Path & Events: Life path indicates a foundational shift at 20-22, a major success period at 33-35, and complete harmony after 50."
                }
            } else ""

            val lifeSituationsInfluenceText = if (!isCharacter) {
                if (isFull) {
                    "Analysis of Life Situations & External Influences: While cosmic challenges verify your determination, you remain the sovereign author of your fate. Valuable mentors will provide key strategic guidance during moments of professional transition."
                } else {
                    "Analysis of Life Situations & External Influences: You possess stellar natural defense structures, turning life obstacles into key developmental milestones."
                }
            } else ""

            val marriageChildrenText = if (!isCharacter) {
                if (isFull) {
                    "Analysis of Relationships: Family, Marriage, Children & Partners: Indicators show a highly stable, long-lasting romantic union founded on friendship and spiritual affinity. Your life partner will be highly supportive, and family signs promise joy and children."
                } else {
                    "Analysis of Relationships: Family, Marriage, Children & Partners: Relationships are governed by mutual respect and a deep soul connection, ensuring a stable, beautiful union."
                }
            } else ""

            PalmistReport(
                overallPortrait = "⚠️ THIS IS A SAMPLE INTERPRETATION (Demo Mode). To get a real chiromancy analysis of your hands from a photograph, please connect your Gemini API key in the AI Studio Secrets panel.\n\nThe overall analysis of the palm for ${profile.name} points to a vivid, spiritually evolved individual. Your hand archetypically balances elements of Air and Fire, signaling high intellectual potential, creative spontaneity, and a quest for cosmic harmony.",
                handType = "Mixed Archetype (Air-Fire)",
                lines = listOf(
                    PalmLineAnalysis(
                        name = "Life Line",
                        color = "#C41E3A",
                        shortDescription = "Deep, clear curve well-rounding the Mount of Venus.",
                        fullDescription = "Your Life Line is solid and deep, denoting immense vitality and physical resilience. A minor split traveling towards the Luna mount at approximately age 35 highlights pivotal travel, a residence shift, or a profound perspective change. Past 50, the line gains stability, promising a harmonious elder age.",
                        keyTakeaways = listOf("Excellent core energy", "Major transition around age 35", "Vigorous longevity")
                    ),
                    PalmLineAnalysis(
                        name = "Heart Line",
                        color = "#E94B8A",
                        shortDescription = "Curved, ending gracefully between Saturn and Jupiter mounts.",
                        fullDescription = "Your Heart Line is long and beautifully etched. You possess deep empathy, a warm demeanor, and a massive capacity for devotion. Reaching between the hills of Jupiter and Saturn indicates an elegant balance between idealism and grounding in romance.",
                        keyTakeaways = listOf("Deep emotional empathy", "High EQ level", "Balanced approach in relationships")
                    ),
                    PalmLineAnalysis(
                        name = "Head Line",
                        color = "#3A7BD5",
                        shortDescription = "Long line sloping gently towards the Mount of Moon.",
                        fullDescription = "Your Head Line slopes toward the Mount of Moon, representing a beautiful alignment of logical analytical strength with deep creative imagination. You enjoy solving atypical challenges and have a natural interest in philosophy and mystical fields.",
                        keyTakeaways = listOf("Creative and fluid cognition", "Attuned to esoteric concepts", "Mental clarity")
                    ),
                    PalmLineAnalysis(
                        name = "Destiny Line",
                        color = "#2ECC71",
                        shortDescription = "Originating near the wrist, pointing cleanly to Saturn.",
                        fullDescription = "Your Destiny Line is prominent, showcasing an innate sense of duty and distinct life milestones. A subtle transition near age 28-30 signifies a shift in career goals or professional rebirth, leading to expanded prosperity and alignment.",
                        keyTakeaways = listOf("Purpose-driven career", "Pivotal alignment near 28-30", "Self-made prosperity")
                    )
                ),
                mounts = listOf(
                    PalmMountAnalysis("Mount of Venus", true, "Strong and plump, signaling warmth, charisma, vital strength, and deep artistic appreciation."),
                    PalmMountAnalysis("Mount of Jupiter", true, "Well-formed. Indicates leadership potential, spiritual honor, and high ambition."),
                    PalmMountAnalysis("Mount of Saturn", false, "Calm and flat, gifting patience, analytical composure, and intellectual independence."),
                    PalmMountAnalysis("Mount of Apollo", true, "Highly visible. Radiates beauty, artistic talents, charm, and creative execution.")
                ),
                signs = listOf(
                    PalmSign("Mystic Cross", "An X mark between Heart and Head lines. A signature of innate intuitive ability, psychic capacity, and interest in esoteric fields.", "Center of palm between primary lines"),
                    PalmSign("Ring of Solomon", "A crescent under the index finger. Bestows ancient wisdom, deep psychological understanding, and authority.", "Below Mount of Jupiter")
                ),
                marriageChildren = marriageChildrenText,
                lifeEvents = lifePathEventsText,
                predictions = lifeSituationsInfluenceText,
                recommendations = "Trust your intuitive instincts—they are your ultimate compass. Connect with natural settings frequently to ground your fiery energy. Practice regular mindfulness.",
                leftHand = leftHandText,
                rightHand = rightHandText,
                characterQualities = charQualitiesText
            )
        }

        return moshi.adapter(PalmistReport::class.java).toJson(report)
    }

    private fun generateLocalMockCompatibility(
        selfName: String,
        partnerName: String,
        isRussian: Boolean
    ): String {
        val report = if (isRussian) {
            CompatibilityReport(
                compatibilityPercent = 88,
                partner1Portrait = "⚠️ ЭТО ПРИМЕР СОВМЕСТИМОСТИ (Демонстрационный режим). \n\n$selfName обладает глубокой эмпатией и ищет духовное единение в союзе. Основной упор делается на искренность чувств и эмоциональную честность.",
                partner2Portrait = "$partnerName ценит надёжность, стабильность и преданность. Сильная натура, стремящаяся оберегать близких и создавать уют.",
                combinedAnalysis = "Ваш союз благословлен гармонией линий Сердца. Изгибы ваших линий ладони плавно дополняют друг друга, что обеспечивает высокое взаимопонимание на интуитивном уровне. Мелкие различия в буграх Сатурна лишь помогают вам уравновешивать качества друг друга.",
                strongPoints = listOf("Абсолютное доверие и уважение", "Взаимная эмоциональная поддержка", "Схожие духовные и семейные ориентиры"),
                weakPoints = listOf("Возможные разногласия в финансовых тратах", "Склонность замалчивать мелкие обиды"),
                emotionalCompatibility = "92% — Слияние на тонком эмоциональном плане. Вы чувствуете настроение партнёра без лишних слов.",
                intellectualCompatibility = "85% — Интересные беседы, совместные проекты и общие интеллектуальные хобби укрепляют ваш союз.",
                financialCompatibility = "75% — Один из вас более практичен, а другой склонен к импульсивным покупкам. Рекомендуется вести открытый бюджет.",
                recommendations = "Чаще проговаривайте свои внутренние переживания вслух. Не бойтесь открыто обсуждать планы на будущее. Устраивайте романтические свидания наедине, чтобы поддерживать пламя Линии Сердца."
            )
        } else {
            CompatibilityReport(
                compatibilityPercent = 88,
                partner1Portrait = "⚠️ THIS IS A SAMPLE COMPATIBILITY REPORT (Demo Mode). Please connect your Gemini API key for a real analysis.\n\n$selfName values spiritual connection, intense emotional honesty, and deep cosmic affinity in relationships.",
                partner2Portrait = "$partnerName holds devotion, safety, and domestic harmony in extremely high regard. A shielding, protective soul.",
                combinedAnalysis = "Your heart currents are beautifully matched. The alignment of your primary romantic lines showcases high mutual empathy and intuitive alignment. Differences in Saturn mount structures help ground and elevate each other.",
                strongPoints = listOf("Absolute mutual trust", "Profound empathy and comfort", "Identical family and life goals"),
                weakPoints = listOf("Minor friction over budget allocations", "Slight habit of internalizing emotional concerns"),
                emotionalCompatibility = "92% — A magnificent emotional connection. You can easily read each other's moods.",
                intellectualCompatibility = "85% — Engaging discussions and unified project goals reinforce your relationship.",
                financialCompatibility = "75% — One partner excels at saving, while the other is more impulsive. Open dialogues are recommended.",
                recommendations = "Discuss internal thoughts openly to avoid overthinking. Organize romantic, focused getaways frequently to nourish the Heart Line flame."
            )
        }

        return moshi.adapter(CompatibilityReport::class.java).toJson(report)
    }
}

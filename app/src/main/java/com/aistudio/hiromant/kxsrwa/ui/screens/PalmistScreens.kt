package com.aistudio.hiromant.kxsrwa.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.aistudio.hiromant.kxsrwa.BuildConfig
import com.aistudio.hiromant.kxsrwa.utils.AppLogger
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.aistudio.hiromant.kxsrwa.data.local.ReadingEntity
import com.aistudio.hiromant.kxsrwa.ui.PalmistViewModel
import com.aistudio.hiromant.kxsrwa.ui.components.*
import com.aistudio.hiromant.kxsrwa.ui.language.AppLanguage
import com.aistudio.hiromant.kxsrwa.ui.language.LocalizedStrings
import com.aistudio.hiromant.kxsrwa.ui.language.PalmistStrings
import com.aistudio.hiromant.kxsrwa.ui.theme.*
import com.aistudio.hiromant.kxsrwa.utils.BitmapUtils
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

const val PALMIST_PROJECT_SUPPORT_TEXT = "Программа Хиромант, анализирует снимки Ваших рук, с помощью ИИ/AI Gemeni... Анализ может быть не совсем верным... Gemeni имеет гораздо более широкий спектр применения. Мы работаем, над созданием специализированной Нейросети (ИИ) Хиромант. Поддержите проект и качество анализа, значительно улучшится в будующих версиях программы Хоромант."

fun configureTtsVoice(
    tts: TextToSpeech?,
    currentLang: AppLanguage,
    voiceGender: String,
    voiceIndex: Int,
    speechRate: Float,
    speechPitch: Float
) {
    if (tts == null) return
    try {
        tts.setSpeechRate(speechRate)
        val allVoices = tts.voices?.toList() ?: emptyList()
        val langCode = if (currentLang == AppLanguage.RUS) "ru" else "en"
        val matchingVoices = allVoices.filter { 
            it.locale.language.equals(langCode, ignoreCase = true) 
        }
        
        val femaleVoices = matchingVoices.filter { voice ->
            val nameLower = voice.name.lowercase(java.util.Locale.US)
            nameLower.contains("female") || 
            nameLower.contains("f-local") || 
            nameLower.contains("ruf") || 
            nameLower.contains("dfc") || 
            nameLower.contains("dfh") || 
            nameLower.contains("rua") || 
            nameLower.contains("ruc") || 
            nameLower.contains("rue") ||
            nameLower.contains("ru-ru-a") ||
            nameLower.contains("ru-ru-c") ||
            nameLower.contains("ru-ru-e") ||
            nameLower.contains("-f-") ||
            nameLower.contains("-f_") ||
            nameLower.contains("_f_")
        }

        val maleVoices = matchingVoices.filter { voice ->
            val nameLower = voice.name.lowercase(java.util.Locale.US)
            nameLower.contains("male") || 
            nameLower.contains("m-local") || 
            nameLower.contains("rum") || 
            nameLower.contains("dfd") || 
            nameLower.contains("dfg") || 
            nameLower.contains("rub") || 
            nameLower.contains("rud") ||
            nameLower.contains("ru-ru-b") ||
            nameLower.contains("ru-ru-d") ||
            nameLower.contains("-m-") ||
            nameLower.contains("-m_") ||
            nameLower.contains("_m_")
        }
        
        val preferredVoice = if (voiceGender == "Female") {
            if (femaleVoices.isNotEmpty()) femaleVoices[voiceIndex % femaleVoices.size] else null
        } else {
            if (maleVoices.isNotEmpty()) maleVoices[voiceIndex % maleVoices.size] else null
        }
        
        if (preferredVoice != null) {
            tts.voice = preferredVoice
        }
        
        val basePitch = if (voiceGender == "Female") 1.35f else 0.75f
        val pitchMultiplier = when (voiceIndex) {
            0 -> 1.00f
            1 -> 0.88f
            2 -> 1.15f
            else -> 1.00f
        }
        tts.setPitch(basePitch * pitchMultiplier * speechPitch)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// --- SCREEN 0: LANGUAGE SELECTION ---

@Composable
fun LanguageSelectionScreen(
    viewModel: PalmistViewModel,
    onNavigateToSplash: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            
            Spacer(modifier = Modifier.weight(0.5f))

            // Mystical Logo
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MysticGold,
                modifier = Modifier
                    .size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MysticHeader(strings.langSelectTitle)
            MysticSubtitle(strings.langSelectSubtitle)

            Spacer(modifier = Modifier.weight(0.8f))

            // Russia (RUS) flag selection card
            LanguageCard(
                langName = "Русский (RUS)",
                flagEmoji = "🇷🇺",
                isSelected = currentLang == AppLanguage.RUS,
                onClick = { viewModel.changeLanguage(AppLanguage.RUS) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // United Kingdom (ENG) flag selection card
            LanguageCard(
                langName = "English (ENG)",
                flagEmoji = "🇬🇧",
                isSelected = currentLang == AppLanguage.ENG,
                onClick = { viewModel.changeLanguage(AppLanguage.ENG) }
            )

            Spacer(modifier = Modifier.weight(1.2f))

            MysticButton(
                text = strings.langContinue,
                onClick = onNavigateToSplash,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun LanguageCard(
    langName: String,
    flagEmoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderStroke = if (isSelected) {
        BorderStroke(2.dp, MysticGold)
    } else {
        BorderStroke(1.dp, MysticBronze.copy(0.4f))
    }

    val backgroundColor = if (isSelected) Color(0x33D4AF37) else Color(0x99141420)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = borderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = flagEmoji,
                fontSize = 36.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = langName,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (isSelected) MysticGold else Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MysticGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


// --- SCREEN 1: SPLASH SCREEN (ANCIENT SCROLL ANIMATION WITH GLOWING LINES) ---

enum class HandElementType { LINE, MOUNT }

@Composable
fun MysticSplashScreen(
    viewModel: PalmistViewModel,
    onNavigateNext: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)
    val coroutineScope = rememberCoroutineScope()

    // Animation states
    var scrollOpened by remember { mutableStateOf(false) }
    var showSymbols by remember { mutableStateOf(false) }
    var pulseLines by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }

    class AnimatedElementState(
        val id: String,
        val type: HandElementType,
        val name: String,
        val color: Color,
        val symbol: String = "",
        val points: List<Pair<Float, Float>> = emptyList(),
        val position: Pair<Float, Float> = Pair(0f, 0f)
    ) {
        val opacity = mutableStateOf(0f)
        val flash = mutableStateOf(1f)
    }

    val elements = remember { // Запоминаем список анимированных элементов между рекомпозициями
        listOf( // Создаем список, содержащий линии ладони и планетарные холмы
            // --- ЛИНИИ ЛАДОНИ (LINE) ---
            AnimatedElementState( // Определяем состояние для Линии Жизни
                id = "life_line", // Уникальный идентификатор Линии Жизни
                type = HandElementType.LINE, // Тип элемента - Линия
                name = "Life Line", // Английское наименование для внутренней логики
                color = Color(0xFFFF4D4D), // Насыщенный кораллово-красный цвет для Линии Жизни
                points = listOf( // Координаты точек (X, Y) от 0.0 до 1.0, идеально описывающие анатомический изгиб холма Венеры
                    Pair(0.58f, 0.46f), // Начальная точка между большим и указательным пальцами
                    Pair(0.54f, 0.54f), // Точка плавного спуска вдоль внутренней стороны ладони
                    Pair(0.50f, 0.64f), // Середина дуги, огибающей холм Венеры
                    Pair(0.48f, 0.77f), // Нижняя часть дуги, уходящая к запястью
                    Pair(0.48f, 0.90f)  // Финальная точка у самого основания запястья
                ) // Завершаем список точек для Линии Жизни
            ), // Завершаем описание Линии Жизни
            AnimatedElementState( // Определяем состояние для Линии Головы (Ума)
                id = "head_line", // Уникальный идентификатор Линии Головы
                type = HandElementType.LINE, // Тип элемента - Линия
                name = "Head Line", // Английское наименование для внутренней логики
                color = Color(0xFF00BFFF), // Яркий глубокий голубой цвет для Линии Головы
                points = listOf( // Координаты точек (X, Y) от 0.0 до 1.0, идеально описывающие диагональный спуск Линии Ума
                    Pair(0.58f, 0.46f), // Исток Линии Головы у ребра большого пальца
                    Pair(0.50f, 0.50f), // Прохождение под средним пальцем
                    Pair(0.44f, 0.54f), // Прохождение под безымянным пальцем
                    Pair(0.36f, 0.58f), // Плавный наклон к холму Луны
                    Pair(0.28f, 0.62f)  // Финальная точка Линии Головы на холме Луны
                ) // Завершаем список точек для Линии Головы
            ), // Завершаем описание Линии Головы
            AnimatedElementState( // Определяем состояние для Линии Сердца
                id = "heart_line", // Уникальный идентификатор Линии Сердца
                type = HandElementType.LINE, // Тип элемента - Линия
                name = "Heart Line", // Английское наименование для внутренней логики
                color = Color(0xFFFF1493), // Яркий розовый цвет для Линии Сердца
                points = listOf( // Координаты точек (X, Y) от 0.0 до 1.0, описывающие красивую дугу под пальцами
                    Pair(0.23f, 0.49f), // Начало под мизинцем на ребре ладони
                    Pair(0.32f, 0.47f), // Плавный прогиб под безымянным пальцем
                    Pair(0.42f, 0.46f), // Прохождение под средним пальцем
                    Pair(0.47f, 0.41f)  // Изгиб вверх к холму Юпитера у указательного пальца
                ) // Завершаем список точек для Линии Сердца
            ), // Завершаем описание Линии Сердца
            AnimatedElementState( // Определяем состояние для Линии Судьбы
                id = "destiny_line", // Уникальный идентификатор Линии Судьбы
                type = HandElementType.LINE, // Тип элемента - Линия
                name = "Destiny Line", // Английское наименование для внутренней логики
                color = Color(0xFFDA70D6), // Нежный орхидеево-фиолетовый цвет для Линии Судьбы
                points = listOf( // Координаты точек (X, Y) от 0.0 до 1.0, описывающие вертикальное восхождение судьбы
                    Pair(0.48f, 0.91f), // Исток у самого основания запястья
                    Pair(0.47f, 0.78f), // Подъем по центральной нижней части ладони
                    Pair(0.47f, 0.65f), // Прохождение через середину ладони
                    Pair(0.45f, 0.52f), // Пересечение Линии Головы
                    Pair(0.44f, 0.40f)  // Устремление к холму Сатурна под средним пальцем
                ) // Завершаем список точек для Линии Судьбы
            ), // Завершаем описание Линии Судьбы
            // --- ПЛАНЕТАРНЫЕ ХОЛМЫ И ИХ СИМВОЛЫ (MOUNT) ---
            AnimatedElementState( // Холм Юпитера (под указательным пальцем)
                id = "mount_jupiter", // Идентификатор холма Юпитера
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Mount of Jupiter", // Наименование для внутренней логики
                color = Color(0xFF9370DB), // Благородный фиолетовый цвет Юпитера
                symbol = "♃", // Древний астрологический символ Юпитера
                position = Pair(0.54f, 0.38f) // Анатомическое положение холма под указательным пальцем
            ), // Завершаем описание холма Юпитера
            AnimatedElementState( // Холм Сатурна (под средним пальцем)
                id = "mount_saturn", // Идентификатор холма Сатурна
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Mount of Saturn", // Наименование для внутренней логики
                color = Color(0xFFFFD700), // Золотой королевский цвет Сатурна
                symbol = "♄", // Древний астрологический символ Сатурна
                position = Pair(0.425f, 0.36f) // Анатомическое положение холма под средним пальцем
            ), // Завершаем описание холма Сатурна
            AnimatedElementState( // Холм Аполлона / Солнца (под безымянным пальцем)
                id = "mount_apollo", // Идентификатор холма Аполлона
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Mount of Apollo", // Наименование для внутренней логики
                color = Color(0xFFFF8C00), // Солнечный оранжевый цвет Аполлона
                symbol = "☉", // Древний астрологический символ Солнца
                position = Pair(0.32f, 0.38f) // Анатомическое положение холма под безымянным пальцем
            ), // Завершаем описание холма Аполлона
            AnimatedElementState( // Холм Меркурия (под мизинцем)
                id = "mount_mercury", // Идентификатор холма Меркурия
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Mount of Mercury", // Наименование для внутренней логики
                color = Color(0xFF00FA9A), // Изумрудно-зеленый цвет Меркурия
                symbol = "☿", // Древний астрологический символ Меркурия
                position = Pair(0.235f, 0.44f) // Анатомическое положение холма под мизинцем
            ), // Завершаем описание холма Меркурия
            AnimatedElementState( // Холм Венеры (крупное основание большого пальца)
                id = "mount_venus", // Идентификатор холма Венеры
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Mount of Venus", // Наименование для внутренней логики
                color = Color(0xFFFF69B4), // Нежно-розовый чувственный цвет Венеры
                symbol = "♀", // Древний астрологический символ Венеры
                position = Pair(0.68f, 0.74f) // Анатомическое положение холма у основания большого пальца
            ), // Завершаем описание холма Венеры
            AnimatedElementState( // Нижний Марс (внутренний холм войны, между большим и указательным)
                id = "mount_mars_lower", // Идентификатор Нижнего Марса
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Lower Mars", // Наименование для внутренней логики
                color = Color(0xFFFF0000), // Огненно-красный воинственный цвет Марса
                symbol = "♂", // Древний астрологический символ Марса (щит и копье)
                position = Pair(0.59f, 0.51f) // Анатомическое положение у основания складки большого пальца
            ), // Завершаем описание Нижнего Марса
            AnimatedElementState( //  Верхний Марс (внешний холм мужества и пассивной обороны)
                id = "mount_mars_upper", // Идентификатор Верхнего Марса
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Upper Mars", // Наименование для внутренней логики
                color = Color(0xFFFF4500), // Красно-оранжевый цвет Верхнего Марса
                symbol = "♂", // Древний астрологический символ Марса
                position = Pair(0.27f, 0.55f) // Анатомическое положение на внешнем ребре ладони под Линией Сердца
            ), // Завершаем описание Верхнего Марса
            AnimatedElementState( // Холм Луны (нижняя внешняя часть ладони)
                id = "mount_moon", // Идентификатор холма Луны
                type = HandElementType.MOUNT, // Тип элемента - Холм
                name = "Mount of Moon", // Наименование для внутренней логики
                color = Color(0xFFE6E6FA), // Мистический лавандовый цвет Луны
                symbol = "☽", // Древний астрологический символ Луны
                position = Pair(0.30f, 0.75f) // Анатомическое положение на внешнем нижнем ребре ладони
            ) // Завершаем описание холма Луны
        ) // Завершаем создание списка элементов
    }

    val imageAlpha by animateFloatAsState(
        targetValue = if (showSymbols) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "ImageAlpha"
    )

    var scaleTarget by remember { mutableStateOf(0.75f) }
    val handScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(durationMillis = 7500, easing = androidx.compose.animation.core.EaseOutCubic),
        label = "HandScale"
    )

    var triggerFlash by remember { mutableStateOf(false) }
    val titleFlashProgress by animateFloatAsState(
        targetValue = if (triggerFlash) 0f else 1f, // starts at 1f (white flash) and decays to 0f (gold)
        animationSpec = tween(durationMillis = 1500, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
        label = "TitleFlash"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(1200),
        label = "TitleAlpha"
    )

    val titleScale by animateFloatAsState(
        targetValue = if (titleVisible) 1.05f else 0.82f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "TitleScale"
    )

    val titleGlowRadius by animateFloatAsState(
        targetValue = if (titleVisible) 40f else 0f,
        animationSpec = tween(2000, delayMillis = 500),
        label = "TitleGlowRadius"
    )

    fun lerpColor(start: Color, end: Color, fraction: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * fraction,
            green = start.green + (end.green - start.green) * fraction,
            blue = start.blue + (end.blue - start.blue) * fraction,
            alpha = start.alpha + (end.alpha - start.alpha) * fraction
        )
    }

    LaunchedEffect(Unit) {
        // Zoom-in hand slowly
        delay(200)
        scaleTarget = 1.03f

        // Unfold ancient scroll
        delay(400)
        scrollOpened = true
        delay(600)
        showSymbols = true
        
        // Let elements light up sequentially
        elements.forEachIndexed { index, element ->
            delay(400)
            coroutineScope.launch {
                val steps = 15
                for (i in 1..steps) {
                    val progress = i.toFloat() / steps
                    element.opacity.value = progress
                    if (progress < 0.5f) {
                        element.flash.value = 1f + (progress * 2f) * 1.5f
                    } else {
                        element.flash.value = 2.5f - ((progress - 0.5f) * 2f) * 1.5f
                    }
                    delay(16)
                }
                element.opacity.value = 1f
                element.flash.value = 1f
            }
        }
        
        delay(800)
        pulseLines = true
        titleVisible = true
        triggerFlash = true
        
        // Final presentation before automatic skip
        delay(2500)
        onNavigateNext()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        // Full screen container with 8.dp margin (fits "5-10 pixels")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, MysticGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Color(0xFF07070F))
        ) {
            // Background Layer: Hand representation scaled to fill the entire box!
            androidx.compose.animation.AnimatedVisibility(
                visible = scrollOpened,
                enter = scaleIn(animationSpec = tween(1200)) + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                // Контейнер с анимацией масштабирования для всей композиции заставки
                Box(
                    modifier = Modifier
                        .fillMaxSize() // Заполняем весь экран
                        .scale(handScale), // Применяем плавное масштабирование
                    contentAlignment = Alignment.Center // Центрируем содержимое
                ) {
                    // Вложенный контейнер с фиксированными пропорциями 9:16 (соответствует картинке руки)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight() // Заполняем максимальную высоту
                            .aspectRatio(9f / 16f) // Сохраняем пропорции 9:16 для предотвращения искажений руки
                    ) {
                        // Отрисовываем реалистичное изображение ладони на заставке
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = coil.request.ImageRequest.Builder(LocalContext.current)
                                    .data(com.aistudio.hiromant.kxsrwa.R.drawable.img_splash_hand) // Подгружаем картинку ладони
                                    .crossfade(true) // Включаем мягкое перетекание при загрузке
                                    .build()
                            ),
                            contentDescription = "Realistic Mystic Hand", // Описание элемента для доступности
                            contentScale = ContentScale.FillBounds, // Заполняем область контейнера 9:16 без растяжения и сжатия
                            modifier = Modifier.fillMaxSize(), // Занимает всё пространство пропорционального контейнера
                            alpha = imageAlpha // Анимируем появление прозрачности
                        )
                        
                        // Холст для точного рисования вспыхивающих линий и знаков поверх ладони
                        Canvas(
                            modifier = Modifier.fillMaxSize() // Холст ложится в точности поверх картинки 1-в-1
                        ) {
                        val w = size.width
                        val h = size.height
                        val toAndroidColor = { c: Color ->
                            android.graphics.Color.argb(
                                (c.alpha * 255).toInt(),
                                (c.red * 255).toInt(),
                                (c.green * 255).toInt(),
                                (c.blue * 255).toInt()
                            )
                        }

                        elements.forEach { element ->
                            val op = element.opacity.value
                            val fl = element.flash.value
                            if (op > 0f) {
                                val baseColor = element.color
                                if (element.type == HandElementType.LINE && element.points.isNotEmpty()) {
                                    val path = Path().apply {
                                        val first = element.points.first()
                                        moveTo(first.first * w, first.second * h)
                                        for (i in 1 until element.points.size) {
                                            val pt = element.points[i]
                                            lineTo(pt.first * w, pt.second * h)
                                        }
                                    }
                                    
                                    // 1. Outer glow
                                    drawPath(
                                        path = path,
                                        color = baseColor.copy(alpha = op * 0.15f * fl),
                                        style = Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                    
                                    // 2. Medium glow
                                    drawPath(
                                        path = path,
                                        color = baseColor.copy(alpha = op * 0.4f * fl),
                                        style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                    
                                    // 3. Bright core
                                    drawPath(
                                        path = path,
                                        color = Color.White.copy(alpha = op * 0.9f),
                                        style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                } else if (element.type == HandElementType.MOUNT) {
                                    val px = element.position.first * w
                                    val py = element.position.second * h
                                    
                                    // Draw outer halo
                                    drawContext.canvas.nativeCanvas.drawText(
                                        element.symbol,
                                        px,
                                        py,
                                        android.graphics.Paint().apply {
                                            color = toAndroidColor(baseColor.copy(alpha = op * 0.3f * fl))
                                            textSize = 36.dp.toPx()
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isAntiAlias = true
                                            style = android.graphics.Paint.Style.FILL_AND_STROKE
                                            strokeWidth = 6.dp.toPx()
                                        }
                                    )
                                    
                                    // Draw core text with shadow
                                    drawContext.canvas.nativeCanvas.drawText(
                                        element.symbol,
                                        px,
                                        py,
                                        android.graphics.Paint().apply {
                                            color = toAndroidColor(Color.White.copy(alpha = op))
                                            textSize = 24.dp.toPx()
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isAntiAlias = true
                                            style = android.graphics.Paint.Style.FILL
                                            setShadowLayer(8.dp.toPx(), 0f, 0f, toAndroidColor(baseColor))
                                        }
                                    )
                                }
                            }
                        }
                    } // Конец Canvas
                    } // Конец пропорционального контейнера 9:16
                } // Конец внешнего анимированного контейнера заставки
            } // Конец AnimatedVisibility

            // Foreground Layer: Content on top of background hand
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Application Logo / Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = titleVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -30 })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .graphicsLayer(
                                        alpha = titleAlpha,
                                        scaleX = titleScale,
                                        scaleY = titleScale
                                    )
                            ) {
                                val uppercaseTitle = strings.appName.uppercase()
                                
                                // 1. Glow underlay (Creates soft halo glow)
                                Text(
                                    text = uppercaseTitle,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        color = Color.Transparent,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 4.sp,
                                        shadow = Shadow(
                                            color = MysticGold.copy(alpha = titleAlpha * 0.9f),
                                            offset = Offset(0f, 0f),
                                            blurRadius = titleGlowRadius
                                        )
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                                
                                // 2. Outer contour / outline layer (Dark backing)
                                Text(
                                    text = uppercaseTitle,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        color = Color.Black,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 4.sp,
                                        drawStyle = Stroke(
                                            width = 12f, // thick backing outline
                                            join = StrokeJoin.Round
                                        )
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                                
                                // 3. Golden contour / outline layer
                                Text(
                                    text = uppercaseTitle,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        color = MysticGold.copy(alpha = 0.85f),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 4.sp,
                                        drawStyle = Stroke(
                                            width = 4f, // fine gold outline
                                            join = StrokeJoin.Round
                                        )
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                                
                                // 4. Main inner text layer with brightness flash
                                val flashColor = lerpColor(MysticGold, Color.White, titleFlashProgress)
                                Text(
                                    text = uppercaseTitle,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        color = flashColor,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 4.sp
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Highlighted, high-legibility subtitle
                            Text(
                                text = strings.splashLogoSubtitle.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MysticGold,
                                    letterSpacing = 2.5.sp,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(0f, 2f),
                                        blurRadius = 8f
                                    )
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                    .border(1.dp, MysticGold.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Middle: spacer to push the button to the bottom
                Spacer(modifier = Modifier.weight(1f))

                // Bottom: "Пропустить заставку" button
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(0.6f)) // Semi-transparent black behind button
                        .border(1.5.dp, MysticGold.copy(0.6f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateNext() }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = strings.splashTapToSkip,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MysticGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}


// --- SCREEN 2: OPTIONAL AUTHENTICATION / REGISTRATION ---

@Composable
fun ShrinkableText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    val initialSize = if (style.fontSize.isSp) style.fontSize.value else 14f
    var fontSizeValue by remember(text) { mutableStateOf(initialSize) }
    Text(
        text = text,
        color = color,
        fontWeight = fontWeight,
        style = style.copy(fontSize = fontSizeValue.sp),
        maxLines = 1,
        softWrap = false,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth || textLayoutResult.didOverflowHeight) {
                if (fontSizeValue > 8f) {
                    fontSizeValue -= 0.5f
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun AuthScreen(
    viewModel: PalmistViewModel,
    onNavigateNext: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }

    var emailOrPhoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    
    var storedVerificationId by remember { mutableStateOf("") }
    var resendingToken by remember { mutableStateOf<com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var activeError by remember { mutableStateOf<String?>(null) }

    val isEmailMode = emailOrPhone.trim().contains("@")

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    isLoading = true
                    val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null as String?)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            isLoading = false
                            if (authTask.isSuccessful) {
                                val user = auth.currentUser
                                viewModel.saveProfile(
                                    name = user?.displayName ?: "Google User",
                                    gender = "Other",
                                    age = 25,
                                    height = 175,
                                    dominantHand = "Right",
                                    email = user?.email,
                                    phone = null,
                                    isRegistered = true
                                )
                                Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Вход через Google успешен!" else "Google login successful!", Toast.LENGTH_SHORT).show()
                                onNavigateNext()
                            } else {
                                activeError = "${if (currentLang == AppLanguage.RUS) "Ошибка авторизации Google в Firebase:" else "Firebase Google login failed:"} ${authTask.exception?.message}"
                            }
                        }
                } else {
                    activeError = "Google Sign-In failed: ID Token is null. Please configure SHA-1 and Web Client ID in Firebase Console."
                }
            } catch (e: com.google.android.gms.common.api.ApiException) {
                val errorMsg = when (e.statusCode) {
                    10 -> "Developer Error (API 10). This usually means your SHA-1 fingerprint or Web Client ID is mismatching in Firebase Console / Google Cloud Platform."
                    12501 -> "Sign-In cancelled by user (12501)."
                    else -> "Google Sign-In API error code: ${e.statusCode}. ${e.message}"
                }
                activeError = errorMsg
            }
        } else {
            activeError = "Google Sign-In result code was: ${result.resultCode}. (Activity cancelled)"
        }
    }

    val callbacks = remember {
        object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                isLoading = false
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            viewModel.saveProfile(
                                name = "",
                                gender = "",
                                age = 25,
                                height = 175,
                                dominantHand = "Right",
                                email = user?.email,
                                phone = user?.phoneNumber,
                                isRegistered = true
                            )
                            Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Вход выполнен успешно!" else "Logged in successfully!", Toast.LENGTH_SHORT).show()
                            onNavigateNext()
                        } else {
                            activeError = "${if (currentLang == AppLanguage.RUS) "Ошибка входа:" else "Login error:"} ${task.exception?.message}"
                        }
                    }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                isLoading = false
                activeError = "${if (currentLang == AppLanguage.RUS) "Ошибка верификации телефона:" else "Phone verification failed:"} ${e.message}"
            }

            override fun onCodeSent(
                verificationId: String,
                token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
            ) {
                isLoading = false
                storedVerificationId = verificationId
                resendingToken = token
                codeSent = true
                Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Код отправлен!" else "Verification code sent!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun formatPhoneNumber(input: String): String {
        val digits = input.filter { it.isDigit() }
        return if (digits.startsWith("8") && digits.length == 11) {
            "+7" + digits.substring(1)
        } else if (digits.startsWith("7") && digits.length == 11) {
            "+" + digits
        } else if (!input.startsWith("+") && digits.length >= 10) {
            "+" + digits
        } else {
            input
        }
    }

    val isValidInput = {
        var valid = true
        val trimmedInput = emailOrPhone.trim()
        if (trimmedInput.isEmpty()) {
            emailOrPhoneError = if (currentLang == AppLanguage.RUS) "Введите E-mail или телефон" else "Enter E-mail or phone"
            valid = false
        }
        if (isEmailMode && password.length < 6) {
            passwordError = strings.authPasswordError
            valid = false
        }
        valid
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Text(
                text = strings.authTitle,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MysticGold,
                    fontSize = 26.sp // Smaller to fit on one line
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            MysticCard {
                Spacer(modifier = Modifier.height(16.dp))

                MysticTextField(
                    value = emailOrPhone,
                    onValueChange = { 
                        emailOrPhone = it
                        emailOrPhoneError = null
                        if (it.contains("@")) {
                            codeSent = false
                        }
                    },
                    label = strings.authEmailPhonePlaceholder,
                    placeholder = "example@domain.com / +79991234567",
                    error = emailOrPhoneError
                )

                MysticTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = null
                    },
                    label = strings.authPasswordPlaceholder,
                    placeholder = "••••••••",
                    error = passwordError
                )

                if (!isEmailMode && codeSent) {
                    MysticTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = strings.authSmsEmailCodePlaceholder,
                        placeholder = "123456"
                    )
                }

                if (isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MysticGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (currentLang == AppLanguage.RUS) 
                                "Инициализация... Если процесс завис (не приходит SMS или нет ответа), это может быть связано с блокировкой или ограничениями сервисов Google в вашем регионе. Вы можете войти без регистрации в полнофункциональном Демо-режиме!"
                            else 
                                "Initializing... If this process hangs (no SMS arrives or no response), it may be due to restrictions on Google services in your region. You can log in without registration using the fully-featured Demo Mode!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                isLoading = false
                                viewModel.saveProfile(
                                    name = "Mystic Seeker",
                                    gender = "Other",
                                    age = 28,
                                    height = 175,
                                    dominantHand = "Right",
                                    email = "demo@hiromant.app",
                                    phone = "+79991112233",
                                    isRegistered = true
                                )
                                Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Вход выполнен в тестовом режиме!" else "Logged in as Test Profile!", Toast.LENGTH_SHORT).show()
                                onNavigateNext()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MysticGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MysticGold)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MysticGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Войти в Демо-режиме" else "Enter in Demo Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEmailMode) {
                        MysticButton(
                            text = if (currentLang == AppLanguage.RUS) "Войти" else "Sign In",
                            onClick = {
                                if (isValidInput()) {
                                    isLoading = true
                                    auth.signInWithEmailAndPassword(emailOrPhone.trim(), password)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                val user = auth.currentUser
                                                if (user != null) {
                                                    if (user.isEmailVerified) {
                                                        viewModel.saveProfile(
                                                            name = "",
                                                            gender = "",
                                                            age = 25,
                                                            height = 175,
                                                            dominantHand = "Right",
                                                            email = user.email,
                                                            phone = null,
                                                            isRegistered = true
                                                        )
                                                        Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Успешный вход!" else "Successfully logged in!", Toast.LENGTH_SHORT).show()
                                                        onNavigateNext()
                                                    } else {
                                                        activeError = if (currentLang == AppLanguage.RUS) 
                                                            "Пожалуйста, подтвердите ваш E-mail! Мы отправили вам ссылку на почту." 
                                                        else 
                                                            "Please verify your E-mail! We have sent you a link."
                                                    }
                                                }
                                            } else {
                                                activeError = "${if (currentLang == AppLanguage.RUS) "Ошибка входа:" else "Login error:"} ${task.exception?.message}"
                                            }
                                        }
                                }
                            },
                            isSecondary = true,
                            modifier = Modifier.weight(1f)
                        )

                        MysticButton(
                            text = strings.authRegisterBtn,
                            onClick = {
                                if (isValidInput()) {
                                    isLoading = true
                                    auth.createUserWithEmailAndPassword(emailOrPhone.trim(), password)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                val user = auth.currentUser
                                                user?.sendEmailVerification()
                                                    ?.addOnCompleteListener { emailTask ->
                                                        if (emailTask.isSuccessful) {
                                                            Toast.makeText(
                                                                context,
                                                                if (currentLang == AppLanguage.RUS)
                                                                    "Регистрация успешна! Письмо с подтверждением отправлено на вашу почту. Пожалуйста, подтвердите почту для входа."
                                                                else
                                                                    "Registration successful! Verification email sent to your inbox. Please confirm it to sign in.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            activeError = "${if (currentLang == AppLanguage.RUS) "Письмо не отправлено:" else "Failed to send email:"} ${emailTask.exception?.message}"
                                                        }
                                                    }
                                            } else {
                                                activeError = "${if (currentLang == AppLanguage.RUS) "Ошибка регистрации:" else "Registration error:"} ${task.exception?.message}"
                                            }
                                        }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        if (!codeSent) {
                            MysticButton(
                                text = strings.authSendCodeBtn,
                                onClick = {
                                    if (isValidInput() && activity != null) {
                                        isLoading = true
                                        val formattedPhone = formatPhoneNumber(emailOrPhone.trim())
                                        val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
                                            .setPhoneNumber(formattedPhone)
                                            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                                            .setActivity(activity)
                                            .setCallbacks(callbacks)
                                            .build()
                                        com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber(options)
                                    } else if (activity == null) {
                                        Toast.makeText(context, "Activity Context is not available", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                isSecondary = true,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            MysticButton(
                                text = strings.authRegisterBtn,
                                onClick = {
                                    if (verificationCode.length >= 4) {
                                        isLoading = true
                                        val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(storedVerificationId, verificationCode)
                                        auth.signInWithCredential(credential)
                                            .addOnCompleteListener { task ->
                                                isLoading = false
                                                if (task.isSuccessful) {
                                                    val user = auth.currentUser
                                                    viewModel.saveProfile(
                                                        name = "",
                                                        gender = "",
                                                        age = 25,
                                                        height = 175,
                                                        dominantHand = "Right",
                                                        email = null,
                                                        phone = user?.phoneNumber,
                                                        isRegistered = true
                                                    )
                                                    Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Успешный вход!" else "Successfully logged in!", Toast.LENGTH_SHORT).show()
                                                    onNavigateNext()
                                                } else {
                                                    activeError = "${if (currentLang == AppLanguage.RUS) "Ошибка:" else "Error:"} ${task.exception?.message}"
                                                }
                                            }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign-In Button
            OutlinedButton(
                onClick = {
                    isLoading = true
                    val webClientId = context.getString(com.aistudio.hiromant.kxsrwa.R.string.default_web_client_id)
                    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                    )
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()

                    val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "G ",
                        color = Color(0xFF4285F4),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Войти через Google" else "Sign in with Google",
                        color = Color(0xFF5F6368),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Demo Bypass Button
            OutlinedButton(
                onClick = {
                    viewModel.saveProfile(
                        name = "Mystic Seeker",
                        gender = "Other",
                        age = 28,
                        height = 175,
                        dominantHand = "Right",
                        email = "demo@hiromant.app",
                        phone = "+79991112233",
                        isRegistered = true
                    )
                    Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Вход выполнен в тестовом режиме!" else "Logged in as Test Profile!", Toast.LENGTH_SHORT).show()
                    onNavigateNext()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MysticGold
                ),
                border = BorderStroke(1.dp, MysticGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MysticGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Войти в Демо-режиме (Тест)" else "Enter in Demo Mode (Test)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MysticGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateNext,
                colors = ButtonDefaults.textButtonColors(contentColor = MysticGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.authSkipBtn,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (activeError != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { activeError = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                border = BorderStroke(1.5.dp, MysticGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error icon",
                        tint = Color(0xFFCF6679),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Произошла ошибка" else "An error occurred",
                        style = MaterialTheme.typography.titleMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = activeError ?: "",
                            style = androidx.compose.ui.text.TextStyle(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFFE0E0E0)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (currentLang == AppLanguage.RUS) 
                            "Для пользователей из РФ и регионов с ограничениями Google рекомендуется использовать полнофункциональный Демо-режим без регистрации."
                        else 
                            "For users in regions with Google limitations, we recommend using the fully-featured Demo Mode without registration.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            activeError = null
                            viewModel.saveProfile(
                                name = "Mystic Seeker",
                                gender = "Other",
                                age = 28,
                                height = 175,
                                dominantHand = "Right",
                                email = "demo@hiromant.app",
                                phone = "+79991112233",
                                isRegistered = true
                            )
                            Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Вход выполнен в тестовом режиме!" else "Logged in as Test Profile!", Toast.LENGTH_SHORT).show()
                            onNavigateNext()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MysticGold,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Войти в Демо-режиме" else "Enter in Demo Mode",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeError = null },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            ShrinkableText(
                                text = if (currentLang == AppLanguage.RUS) "Закрыть" else "Close",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:")
                                    putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("arsmaxim@gmail.com"))
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Hiromant App Error Report")
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Hi developer,\n\nI encountered the following error in the Hiromant app:\n\n${activeError}\n\nDevice: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Почтовое приложение не найдено" else "No email app found", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            ShrinkableText(
                                text = if (currentLang == AppLanguage.RUS) "Отчёт" else "Report",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- SCREEN 3: USER PROFILE DETAILS ---

@Composable
fun ProfileScreen(
    viewModel: PalmistViewModel,
    onNavigateNext: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val existingProfile by viewModel.userProfile.collectAsState()

    var name by remember { mutableStateOf("Максим") }
    var gender by remember { mutableStateOf("Male") }
    var birthYearText by remember { mutableStateOf("1982") }
    var heightText by remember { mutableStateOf("175") }
    var dominantHand by remember { mutableStateOf("Right") }

    var nameError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existingProfile) {
        existingProfile?.let {
            if (it.name.isNotEmpty()) {
                name = it.name
                gender = it.gender
                birthYearText = (2026 - it.age).toString()
                heightText = it.height.toString()
                dominantHand = it.dominantHand
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            MysticHeader(strings.profileTitle)
            MysticSubtitle(strings.profileSubtitle)

            Spacer(modifier = Modifier.height(10.dp))

            MysticCard(modifier = Modifier.weight(1f, fill = false)) {
                Spacer(modifier = Modifier.height(16.dp))

                // Name and Gender Row
                val isRu = currentLang == com.aistudio.hiromant.kxsrwa.ui.language.AppLanguage.RUS
                val maleLabel = if (isRu) "М" else "M"
                val femaleLabel = if (isRu) "Ж" else "F"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Name Field
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.profileNameLabel,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = if (it.trim().length < 2) strings.profileNameError else null
                            },
                            placeholder = { Text("Максим", color = Color.Gray) },
                            singleLine = true,
                            isError = nameError != null,
                            keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MysticGold,
                                unfocusedBorderColor = MysticBronze.copy(0.6f),
                                cursorColor = MysticGold,
                                errorBorderColor = Color(0xFFCF6679)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError!!,
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCF6679)),
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // Right Column: Gender Buttons (М/Ж)
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = strings.profileGenderLabel,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                maleLabel to "Male",
                                femaleLabel to "Female"
                            ).forEach { (label, value) ->
                                val selected = gender == value
                                OutlinedButton(
                                    onClick = { gender = value },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, if (selected) MysticGold else MysticBronze.copy(0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) Color(0x22D4AF37) else Color.Transparent,
                                        contentColor = if (selected) MysticGold else Color.White
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(width = 50.dp, height = 54.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Birth Year and Height Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Birth Year Input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.profileAgeLabel,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = birthYearText,
                            onValueChange = { newValue ->
                                if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                                    birthYearText = newValue
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MysticGold,
                                focusedBorderColor = MysticGold,
                                unfocusedBorderColor = MysticBronze.copy(0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("1982", color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Height Input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.profileHeightLabel,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { newValue ->
                                if (newValue.length <= 3 && newValue.all { it.isDigit() }) {
                                    heightText = newValue
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MysticGold,
                                focusedBorderColor = MysticGold,
                                unfocusedBorderColor = MysticBronze.copy(0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("172", color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Dominant hand selector (ESSENTIAL)
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                    Text(
                        text = strings.profileHandLabel,
                        style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            strings.profileHandLeft to "Left",
                            strings.profileHandRight to "Right"
                        ).forEach { (label, value) ->
                            val selected = dominantHand == value
                            OutlinedButton(
                                onClick = { dominantHand = value },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, if (selected) MysticGold else MysticBronze.copy(0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) Color(0x22D4AF37) else Color.Transparent,
                                    contentColor = if (selected) MysticGold else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (dominantHand == "Left") strings.profileHandDescLeft else strings.profileHandDescRight,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MysticBronze,
                            fontSize = 11.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            MysticButton(
                text = strings.next,
                onClick = {
                    if (name.trim().length >= 2 && gender.isNotEmpty()) {
                        val parsedBirthYear = birthYearText.toIntOrNull() ?: 1995
                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                        val parsedAge = (currentYear - parsedBirthYear).coerceIn(18, 100)
                        val parsedHeight = heightText.toIntOrNull() ?: 172

                        viewModel.saveProfile(name, gender, parsedAge, parsedHeight, dominantHand)
                        onNavigateNext()
                    } else {
                        if (name.trim().length < 2) nameError = strings.profileNameError
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


// --- SCREEN 4: MEDIA UPLOAD & RUN ANALYSES ---

fun createGalleryImageUri(context: Context, title: String): Uri? {
    try {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "${title}_${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Palmist")
        }
        return context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun createGalleryVideoUri(context: Context, title: String): Uri? {
    try {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, "${title}_${System.currentTimeMillis()}.mp4")
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/Palmist")
        }
        return context.contentResolver.insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
fun HandStencilCanvas(slotName: String, modifier: Modifier = Modifier) {
    // Рисуем реалистичный анатомически правильный контур ладони или тыльной стороны руки на Canvas
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width // Получаем ширину области рисования в пикселях
        val height = size.height // Получаем высоту области рисования в пикселях
        
        // Определяем тип руки: ладонь (содержит линии судьбы) или тыльная сторона (содержит ногти)
        val isPalm = slotName == "left_palm" || slotName == "right_palm" // Булево значение для ладони
        
        // Определяем расположение большого пальца в зависимости от ракурса камеры:
        // - Для правой ладони (right_palm) и тыльной стороны левой руки (left_back) большой палец находится справа на фото.
        // - Для левой ладони (left_palm) и тыльной стороны правой руки (right_back) большой палец находится слева на фото.
        val thumbOnRight = (slotName == "right_palm" || slotName == "left_back") // Флаг расположения большого пальца справа

        
        // Вспомогательные функции для перевода относительных координат (0.0 - 1.0) в реальные пиксели экрана
        fun getX(f: Float): Float {
            // Если большой палец должен быть справа, рисуем как есть, иначе зеркально отражаем по горизонтали
            return if (thumbOnRight) width * f else width * (1.0f - f)
        }
        fun getY(f: Float): Float {
            // Возвращаем координату Y, умноженную на общую высоту холста
            return height * f
        }
        
        // Создаем путь для рисования реалистичного и изящного внешнего контура руки
        val handPath = androidx.compose.ui.graphics.Path().apply {
            // Начинаем рисование с левой части запястья
            moveTo(getX(0.36f), getY(0.92f))
            
            // Плавная и реалистичная кривая по внешнему ребру ладони (холм Луны)
            cubicTo(
                getX(0.30f), getY(0.82f), // Первая контрольная точка для скругления ребра ладони внизу
                getX(0.23f), getY(0.70f), // Вторая контрольная точка для сужения к пальцам
                getX(0.23f), getY(0.50f)  // Конечная точка у основания мизинца
            )
            
            // --- МИЗИНЕЦ ---
            // Левая грань мизинца (плавное сужение кверху)
            quadraticTo(getX(0.21f), getY(0.42f), getX(0.21f), getY(0.32f))
            // Анатомически округлая верхушка мизинца
            cubicTo(
                getX(0.21f), getY(0.29f), // Левый изгиб подушечки пальца
                getX(0.26f), getY(0.29f), // Правый изгиб подушечки пальца
                getX(0.26f), getY(0.32f)  // Точка перехода на правую грань мизинца
            )
            // Правая грань мизинца до межпальцевой складки
            quadraticTo(getX(0.26f), getY(0.42f), getX(0.27f), getY(0.46f))
            
            // --- БЕЗЫМЯННЫЙ ПАЛЕЦ ---
            // Левая грань безымянного пальца
            quadraticTo(getX(0.28f), getY(0.30f), getX(0.29f), getY(0.20f))
            // Мягкая округлая подушечка безымянного пальца
            cubicTo(
                getX(0.29f), getY(0.16f), // Левое скругление верхушки
                getX(0.35f), getY(0.16f), // Правое скругление верхушки
                getX(0.35f), getY(0.20f)  // Переход к правой грани
            )
            // Правая грань безымянного пальца до межпальцевой впадины
            quadraticTo(getX(0.35f), getY(0.30f), getX(0.36f), getY(0.42f))
            
            // --- СРЕДНИЙ ПАЛЕЦ (самый длинный палец руки) ---
            // Левая грань среднего пальца
            quadraticTo(getX(0.37f), getY(0.24f), getX(0.39f), getY(0.12f))
            // Красивая округлая верхушка среднего пальца
            cubicTo(
                getX(0.39f), getY(0.08f), // Левый изгиб высшей точки пальца
                getX(0.46f), getY(0.08f), // Правый изгиб высшей точки пальца
                getX(0.46f), getY(0.12f)  // Переход к правой грани
            )
            // Правая грань среднего пальца до межпальцевой складки
            quadraticTo(getX(0.47f), getY(0.24f), getX(0.48f), getY(0.41f))
            
            // --- УКАЗАТЕЛЬНЫЙ ПАЛЕЦ ---
            // Левая грань указательного пальца
            quadraticTo(getX(0.49f), getY(0.28f), getX(0.51f), getY(0.18f))
            // Аккуратная округлая подушечка указательного пальца
            cubicTo(
                getX(0.51f), getY(0.14f), // Левый изгиб верхушки
                getX(0.57f), getY(0.14f), // Правый изгиб верхушки
                getX(0.57f), getY(0.18f)  // Переход к правой грани
            )
            // Правая грань указательного пальца до глубокой межпальцевой впадины
            quadraticTo(getX(0.57f), getY(0.28f), getX(0.58f), getY(0.44f))
            
            // --- МЕЖПАЛЬЦЕВАЯ ВПАДИНА И БОЛЬШОЙ ПАЛЕЦ ---
            // Плавный реалистичный изгиб кожной складки между указательным и большим пальцем
            quadraticTo(getX(0.60f), getY(0.52f), getX(0.65f), getY(0.55f))
            
            // Верхняя грань большого пальца (направленная в сторону)
            quadraticTo(getX(0.74f), getY(0.56f), getX(0.83f), getY(0.58f))
            // Анатомически правильный закругленный кончик большого пальца
            cubicTo(
                getX(0.86f), getY(0.59f), // Левая контрольная точка скругления
                getX(0.87f), getY(0.64f), // Правая контрольная точка скругления
                getX(0.83f), getY(0.66f)  // Переход на внутреннюю грань пальца
            )
            // Внутренняя грань большого пальца до сустава основания
            quadraticTo(getX(0.74f), getY(0.71f), getX(0.67f), getY(0.78f))
            
            // Нижнее ребро ладони (область холма Венеры) до основания запястья
            quadraticTo(getX(0.62f), getY(0.85f), getX(0.60f), getY(0.92f))
            
            // Соединяем края запястья аккуратной горизонтальной линией
            lineTo(getX(0.36f), getY(0.92f))
            
            // Закрываем векторный контур
            close()
        }
        
        // Заливка силуэта руки мягким полупрозрачным золотистым тоном для эстетичности
        drawPath(
            path = handPath, // Используем созданный контур руки
            color = MysticGold.copy(0.04f), // Нежный золотистый оттенок
            style = androidx.compose.ui.graphics.drawscope.Fill // Тип отрисовки - сплошная заливка
        )
        
        // Отрисовка пунктирной золотой линии по контуру для создания технологичного эффекта биометрического сканера
        drawPath(
            path = handPath, // Наш детальный контур руки
            color = MysticGold.copy(0.55f), // Свечение золотистого цвета
            style = androidx.compose.ui.graphics.drawscope.Stroke( // Рисуем только линию контура
                width = 2.5.dp.toPx(), // Оптимальная толщина линии контура в пикселях
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect( // Пунктирный паттерн линии
                    floatArrayOf(15f, 10f), // Длина штриха 15 пикселей, длина пробела 10 пикселей
                    0f // Без смещения начала линии
                )
            )
        )
        
        // Отрисовка трех изящных параллельных складок запястья ("браслеты" или пояса Ориона)
        for (i in 0..2) {
            val offset = i * 0.025f // Относительное смещение для каждой линии вниз
            val wristLinePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.36f), getY(0.92f + offset)) // Левая стартовая точка браслета
                quadraticTo( // Дугообразная линия запястья
                    getX(0.48f), getY(0.945f + offset), // Контрольная точка изгиба дуги вверх
                    getX(0.60f), getY(0.92f + offset)   // Правая финишная точка у ребра
                )
            }
            drawPath(
                path = wristLinePath, // Путь дуги запястья
                color = MysticGold.copy(0.4f), // Золотистый полупрозрачный цвет
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()) // Толщина линии в пикселях
            )
        }
        
        // Отрисовка межфаланговых складок (суставов) на длинных пальцах для максимальной реалистичности рисунка
        val knucklesColor = MysticGold.copy(0.35f) // Индивидуальный мягкий цвет для тонких складок кожи
        val jointStroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()) // Толщина линий суставов
        
        // Координаты центров и ширины суставов пальцев: (X, Y, Ширина складки)
        val jointCreases = listOf(
            // Мизинец: нижний и верхний суставы
            Triple(0.235f, 0.40f, 0.035f), Triple(0.235f, 0.35f, 0.035f),
            // Безымянный палец: нижний и верхний суставы
            Triple(0.32f, 0.34f, 0.045f), Triple(0.32f, 0.26f, 0.045f),
            // Средний палец: нижний и верхний суставы
            Triple(0.425f, 0.30f, 0.050f), Triple(0.425f, 0.20f, 0.050f),
            // Указательный палец: нижний и верхний суставы
            Triple(0.54f, 0.34f, 0.045f), Triple(0.54f, 0.25f, 0.045f)
        )
        
        // Цикл прорисовки двух тонких параллельных линий для каждого межфалангового сустава
        for (joint in jointCreases) {
            val cx = getX(joint.first) // Рассчитываем пиксельную координату X
            val cy = getY(joint.second) // Рассчитываем пиксельную координату Y
            val w = joint.third * width // Рассчитываем реальную ширину складки
            
            val creasePath = androidx.compose.ui.graphics.Path().apply {
                // Нижняя линия складки сустава
                moveTo(cx - w / 2, cy)
                quadraticTo(cx, cy + 2f, cx + w / 2, cy)
                // Верхняя линия складки сустава для объемного эффекта кожи
                moveTo(cx - w / 2, cy - 3f)
                quadraticTo(cx, cy - 1f, cx + w / 2, cy - 3f)
            }
            drawPath(path = creasePath, color = knucklesColor, style = jointStroke)
        }
        
        // Прорисовка наклонной кожной складки сустава на большом пальце
        val thumbCreasePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(getX(0.72f), getY(0.69f)) // Начальная точка сбоку пальца
            quadraticTo(getX(0.745f), getY(0.655f), getX(0.77f), getY(0.62f)) // Диагональная дуга
            moveTo(getX(0.71f), getY(0.68f)) // Вторая параллельная линия
            quadraticTo(getX(0.735f), getY(0.645f), getX(0.76f), getY(0.61f))
        }
        drawPath(path = thumbCreasePath, color = knucklesColor, style = jointStroke)
        
        if (isPalm) {
            // --- РЕЖИМ ЛАДОНИ: РИСУЕМ РЕАЛИСТИЧНЫЕ ПАПИЛЛЯРНЫЕ И ХИРОМАНТИЧЕСКИЕ ЛИНИИ ---
            
            // 1. ЛИНИЯ ЖИЗНИ (огибает холм Венеры у большого пальца, символизирует витальность)
            val lifeLine = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.58f), getY(0.46f)) // Исток между большим и указательным пальцами
                quadraticTo( // Основной полукруглый контур вокруг большого пальца
                    getX(0.50f), getY(0.64f), // Контрольная точка максимального изгиба
                    getX(0.48f), getY(0.90f)  // Окончание у запястья
                )
            }
            drawPath(
                path = lifeLine,
                color = MysticGold.copy(0.70f), // Хорошо заметная золотая линия
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx()) // Утолщенный контур
            )
            
            // 2. ВНУТРЕННЯЯ ЛИНИЯ МАРСА (Тонкая линия Ангела-Хранителя - дублирует линию жизни изнутри)
            val marsLine = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.62f), getY(0.54f)) // Исток чуть глубже под большим пальцем
                quadraticTo(
                    getX(0.55f), getY(0.68f), // Идет строго параллельно линии жизни
                    getX(0.53f), getY(0.83f)  // Оканчивается у основания холма
                )
            }
            drawPath(
                path = marsLine,
                color = MysticGold.copy(0.40f), // Деликатное тонкое свечение
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx()) // Изящная толщина
            )
            
            // 3. ЛИНИЯ ГОЛОВЫ / УМА (идет поперек ладони к холму Луны, символизирует интеллект)
            val headLine = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.58f), getY(0.46f)) // Сливается у истока с линией жизни
                quadraticTo(
                    getX(0.44f), getY(0.54f), // Пересекает ладонь по диагонали
                    getX(0.28f), getY(0.62f)  // Оканчивается на холме Луны с красивым спуском вниз
                )
            }
            drawPath(
                path = headLine,
                color = MysticGold.copy(0.70f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
            )
            
            // 4. ЛИНИЯ СЕРДЦА (идет от ребра ладони к указательному пальцу, имеет вилку писателя на конце)
            val heartLine = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.23f), getY(0.49f)) // Берет начало под мизинцем на ребре ладони
                quadraticTo(
                    getX(0.42f), getY(0.46f), // Дугообразный изгиб к верхним пальцам
                    getX(0.47f), getY(0.41f)  // Точка развилки у холма Юпитера
                )
            }
            drawPath(
                path = heartLine,
                color = MysticGold.copy(0.70f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
            )
            
            // Левая ветвь вилки сердца (направлена вверх к указательному и среднему пальцам)
            val heartFork1 = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.47f), getY(0.41f))
                quadraticTo(getX(0.49f), getY(0.39f), getX(0.52f), getY(0.38f))
            }
            // Правая ветвь вилки сердца (направлена мягко вниз к холму ума)
            val heartFork2 = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.47f), getY(0.41f))
                quadraticTo(getX(0.45f), getY(0.42f), getX(0.43f), getY(0.43f))
            }
            drawPath(path = heartFork1, color = MysticGold.copy(0.60f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx()))
            drawPath(path = heartFork2, color = MysticGold.copy(0.60f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx()))
            
            // 5. ЛИНИЯ СУДЬБЫ / РОКА (вертикальная линия по центру ладони, символ жизненного пути)
            val destinyLine = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.48f), getY(0.91f)) // Исток у запястья по центру
                quadraticTo(
                    getX(0.47f), getY(0.65f), // Поднимается вертикально вверх сквозь всю ладонь
                    getX(0.44f), getY(0.40f)  // Упирается в холм Сатурна под средним пальцем
                )
            }
            drawPath(
                path = destinyLine,
                color = MysticGold.copy(0.50f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.8.dp.toPx())
            )
            
            // 6. ЛИНИЯ ЗДОРОВЬЯ / МЕРКУРИЯ (идет по диагонали снизу к холму под мизинцем)
            val healthLine = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.46f), getY(0.88f)) // Исток около низа линии жизни
                quadraticTo(
                    getX(0.36f), getY(0.70f), // Пересекает ладонь по диагонали к внешнему краю
                    getX(0.26f), getY(0.52f)  // Оканчивается под мизинцем
                )
            }
            drawPath(
                path = healthLine,
                color = MysticGold.copy(0.45f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
            
            // 7. ЛИНИЯ СОЛНЦА / АПОЛЛОНА (Линия удачи и творческого успеха, идет к безымянному пальцу)
            val sunLine = androidx.compose.ui.graphics.Path().apply {
                moveTo(getX(0.36f), getY(0.65f)) // Исток на равнине Марса
                quadraticTo(
                    getX(0.34f), getY(0.53f), // Подъем параллельно линии судьбы
                    getX(0.33f), getY(0.41f)  // Окончание у безымянного пальца
                )
            }
            drawPath(
                path = sunLine,
                color = MysticGold.copy(0.45f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
            
            // 8. ЛИНИИ БРАКА / ОТНОШЕНИЙ (короткие горизонтальные отметки на ребре под мизинцем)
            for (j in 0..1) {
                val yOffset = j * 0.015f // Вертикальный интервал между линиями брака
                val marriageLine = androidx.compose.ui.graphics.Path().apply {
                    moveTo(getX(0.22f), getY(0.45f + yOffset)) // Точка на ребре ладони
                    quadraticTo(
                        getX(0.235f), getY(0.45f + yOffset), // Простирается горизонтально на холм Меркурия
                        getX(0.25f), getY(0.45f + yOffset)   // Завершение черты брака
                    )
                }
                drawPath(
                    path = marriageLine,
                    color = MysticGold.copy(0.50f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                )
            }
            
        } else {
            // --- РЕЖИМ ТЫЛЬНОЙ СТОРОНЫ: РИСУЕМ РЕАЛИСТИЧНЫЕ И ОЧЕРЧЕННЫЕ НОГТИ НА ПАЛЬЦАХ ---
            
            // Локальная функция для детального рисования анатомически правильного реалистичного ногтя
            fun drawRealisticNail(cx: Float, cy: Float, widthFract: Float, heightFract: Float) {
                val nailWidth = widthFract * width   // Переводим относительную ширину ногтя в пиксели
                val nailHeight = heightFract * height // Переводим относительную высоту ногтя в пиксели
                
                // Сохраняем состояние холста Canvas перед вращением и смещением системы координат
                drawContext.canvas.save()
                
                // Смещаем начало координат холста в центр верхушки пальца
                val nailX = getX(cx)
                val nailY = getY(cy)
                drawContext.canvas.translate(nailX, nailY)
                
                // Угол поворота ногтевой пластины. Большой палец и мизинец имеют наклон для реалистичности.
                val rotDegrees = if (cx > 0.7f) {
                    if (thumbOnRight) -35f else 35f // Угол разворота для большого пальца
                } else if (cx < 0.28f) {
                    if (thumbOnRight) 12f else -12f  // Небольшой наклон для мизинца наружу
                } else 0f
                drawContext.canvas.rotate(rotDegrees)
                
                // Создаем форму ногтя (вертикально-вытянутая округлая миндалевидная форма из hiro.su)
                val nailPath = androidx.compose.ui.graphics.Path().apply {
                    val halfW = nailWidth / 2f
                    val halfH = nailHeight / 2f
                    
                    // Левая нижняя точка у кутикулы
                    moveTo(-halfW, halfH)
                    // Левый край ногтя с легким сужением к свободному краю
                    quadraticTo(-halfW * 1.05f, -halfH * 0.4f, -halfW * 0.9f, -halfH)
                    // Верхний свободный край (аккуратный полукруглый изгиб ногтевой кромки)
                    quadraticTo(0f, -halfH * 1.35f, halfW * 0.9f, -halfH)
                    // Правый край ногтя с изгибом книзу
                    quadraticTo(halfW * 1.05f, -halfH * 0.4f, halfW, halfH)
                    // Нижний дугообразный край (линия улыбки кутикулы)
                    quadraticTo(0f, halfH * 1.25f, -halfW, halfH)
                    close()
                }
                
                // 1. Заполняем тело ногтя нежным полупрозрачным золотым градиентом
                drawPath(
                    path = nailPath,
                    color = MysticGold.copy(0.12f),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
                
                // 2. Очерчиваем четкие границы ногтевой пластины тонким золотистым контуром
                drawPath(
                    path = nailPath,
                    color = MysticGold.copy(0.65f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
                
                // 3. Прорисовываем Лунулу (матово-белое полукружие у основания кутикулы)
                val lunulaPath = androidx.compose.ui.graphics.Path().apply {
                    val halfW = nailWidth / 2f
                    val halfH = nailHeight / 2f
                    moveTo(-halfW * 0.6f, halfH) // Нижняя левая точка кутикулы
                    quadraticTo(0f, halfH * 0.4f, halfW * 0.6f, halfH) // Изгиб дуги вверх
                    quadraticTo(0f, halfH * 1.15f, -halfW * 0.6f, halfH) // Замыкание по дуге кутикулы
                    close()
                }
                drawPath(
                    path = lunulaPath,
                    color = Color.White.copy(0.40f), // Нежный полупрозрачный белый цвет
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )
                
                // 4. Дополнительная складка кожи (кутикула) у основания ногтевой пластины
                val cuticleCrease = androidx.compose.ui.graphics.Path().apply {
                    val halfW = nailWidth / 2f
                    val halfH = nailHeight / 2f
                    moveTo(-halfW * 1.3f, halfH * 1.28f)
                    quadraticTo(0f, halfH * 1.55f, halfW * 1.3f, halfH * 1.28f)
                }
                drawPath(
                    path = cuticleCrease,
                    color = MysticGold.copy(0.35f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
                
                // Восстанавливаем сохраненное состояние Canvas для рисования остальных ногтей
                drawContext.canvas.restore()
            }
            
            // Размещаем анатомически выверенные ногти на каждом из пяти пальцев тыльной стороны руки
            drawRealisticNail(0.235f, 0.32f, 0.016f, 0.024f) // Ноготь мизинца
            drawRealisticNail(0.32f, 0.20f, 0.021f, 0.031f)  // Ноготь безымянного пальца
            drawRealisticNail(0.425f, 0.12f, 0.023f, 0.033f) // Ноготь среднего пальца
            drawRealisticNail(0.54f, 0.20f, 0.021f, 0.031f)  // Ноготь указательного пальца
            drawRealisticNail(0.83f, 0.61f, 0.025f, 0.024f)  // Ноготь большого пальца
        }
    }
}

@Composable
fun HandSlotCard(
    title: String,
    bitmap: Bitmap?,
    slotName: String,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onClear: () -> Unit,
    btnCameraText: String,
    btnGalleryText: String,
    modifier: Modifier = Modifier,
    onSaveToGallery: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 1. Label above the preview window
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (bitmap != null) MysticGold else Color.White,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 2. Preview Window
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x1F1E1E2C))
                .border(2.dp, if (bitmap != null) MysticGold else MysticBronze.copy(0.3f), RoundedCornerShape(16.dp))
                .clickable { onTakePhoto() } // Tapping triggers camera
        ) {
            if (bitmap != null) {
                Image(
                    painter = rememberAsyncImagePainter(bitmap),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Clear button in the corner
                IconButton(
                    onClick = { onClear() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = Color.Red,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Кнопка сохранения в галерею в левом углу
                if (onSaveToGallery != null) {
                    IconButton(
                        onClick = { onSaveToGallery() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Сохранить в галерею",
                            tint = MysticGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Large placeholder in the middle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MysticBronze.copy(0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Тапните для фото",
                        style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze, fontSize = 11.sp)
                    )
                }
            }

            // Контуры на превью удалены по запросу пользователя для улучшения внешнего вида

        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Upload buttons UNDER the preview window
        Row(
            modifier = Modifier.width(220.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Take Photo Button
            Button(
                onClick = onTakePhoto,
                colors = ButtonDefaults.buttonColors(containerColor = MysticBronze.copy(0.2f)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Take Photo",
                    tint = MysticGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = btnCameraText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MysticGold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Pick Gallery Button
            Button(
                onClick = onPickPhoto,
                colors = ButtonDefaults.buttonColors(containerColor = MysticBronze.copy(0.2f)),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Gallery",
                    tint = MysticGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = btnGalleryText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MysticGold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}


fun getVideoThumbnail(context: Context, uri: Uri?): Bitmap? {
    if (uri == null) return null
    return try {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val bitmap = retriever.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        retriever.release()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun InAppCameraDialog(
    slotName: String,
    currentLang: AppLanguage,
    onPhotoCaptured: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val isRussian = currentLang == AppLanguage.RUS
    
    var hasCameraHardware by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val imageCapture = remember { androidx.camera.core.ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context) }
    
    val cameraExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
            border = BorderStroke(2.dp, MysticGold),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (hasCameraHardware) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                            val previewView = androidx.camera.view.PreviewView(ctx).apply {
                                scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                            }
                            
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = androidx.camera.core.Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    
                                    val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                                    
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    hasCameraHardware = false
                                    errorMessage = e.message
                                }
                            }, androidx.core.content.ContextCompat.getMainExecutor(ctx))
                            
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback visual simulation placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0D0B18)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MysticGold,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isRussian) "Режим эмуляции камеры" else "Camera Emulation Active",
                                style = MaterialTheme.typography.titleMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isRussian) 
                                    "В эмуляторе физическая камера недоступна. Будет автоматически сгенерировано высококачественное мистическое изображение ладони для анализа."
                                else 
                                    "No hardware camera detected in this emulator. A high-quality mystical palm print will be procedurally generated for your analysis.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Beautiful Hand Guide Overlay (for alignment)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HandStencilCanvas(
                            slotName = slotName,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            val guideTxt = when (slotName) {
                                "left_palm" -> if (isRussian) "Левая ладонь (линии вверх)" else "Left Palm (lines up)"
                                "left_back" -> if (isRussian) "Тыл левой руки (ногти)" else "Left Back (nails)"
                                "right_palm" -> if (isRussian) "Правая ладонь (+запястье)" else "Right Palm (+wrist)"
                                "right_back" -> if (isRussian) "Тыл правой руки (ногти)" else "Right Back (nails)"
                                else -> if (isRussian) "Поместите руку сюда" else "Place hand here"
                            }
                            Text(
                                text = guideTxt,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MysticGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Top control: Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                
                // Top control: Title indicating which hand to position
                val titleString = when (slotName) {
                    "left_palm" -> if (isRussian) "Левая ладонь" else "Left Palm"
                    "left_back" -> if (isRussian) "Тыльная сторона левой руки" else "Left Hand Back"
                    "right_palm" -> if (isRussian) "Правая ладонь" else "Right Palm"
                    "right_back" -> if (isRussian) "Тыльная сторона правой руки" else "Right Hand Back"
                    else -> if (isRussian) "Фото ладони" else "Palm Photo"
                }
                Text(
                    text = titleString,
                    style = MaterialTheme.typography.titleMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 22.dp)
                        .background(Color.Black.copy(0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                
                // Bottom control: Capture Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.4f))
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (hasCameraHardware) {
                                // Take actual photo
                                val photoFile = java.io.File(
                                    context.cacheDir,
                                    "captured_palm_${System.currentTimeMillis()}.jpg"
                                )
                                val outputOptions = androidx.camera.core.ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                
                                imageCapture.takePicture(
                                    outputOptions,
                                    androidx.core.content.ContextCompat.getMainExecutor(context),
                                    object : androidx.camera.core.ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: androidx.camera.core.ImageCapture.OutputFileResults) {
                                            val bitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)
                                            if (bitmap != null) {
                                                onPhotoCaptured(bitmap)
                                            } else {
                                                // Fallback to procedural generator if decoding fails
                                                val mockBitmap = BitmapUtils.generateMysticHandBitmap(context, slotName, isRussian)
                                                onPhotoCaptured(mockBitmap)
                                            }
                                        }
                                        
                                        override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                                            exception.printStackTrace()
                                            // Fallback to procedural generator on error
                                            val mockBitmap = BitmapUtils.generateMysticHandBitmap(context, slotName, isRussian)
                                            onPhotoCaptured(mockBitmap)
                                        }
                                    }
                                )
                            } else {
                                // Camera emulation capture -> procedural golden palm print image
                                val mockBitmap = BitmapUtils.generateMysticHandBitmap(context, slotName, isRussian)
                                onPhotoCaptured(mockBitmap)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MysticGold),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .border(4.dp, Color.White, CircleShape),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: MEDIA UPLOAD & RUN ANALYSES ---

@Composable
fun UploadScreen(
    viewModel: PalmistViewModel,
    onNavigateToLoading: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToVideoScan: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val billingState by viewModel.billingState.collectAsState()

    var activeSlot by remember { mutableStateOf<String?>(null) }
    val bitmapLeftPalm by viewModel.bitmapLeftPalm.collectAsState()
    val bitmapLeftBack by viewModel.bitmapLeftBack.collectAsState()
    val bitmapRightPalm by viewModel.bitmapRightPalm.collectAsState()
    val bitmapRightBack by viewModel.bitmapRightBack.collectAsState()

    val leftPalmPath by viewModel.leftPalmPath.collectAsState()
    val leftBackPath by viewModel.leftBackPath.collectAsState()
    val rightPalmPath by viewModel.rightPalmPath.collectAsState()
    val rightBackPath by viewModel.rightBackPath.collectAsState()

    val showInterpretationScreen by viewModel.showInterpretationScreen.collectAsState()

    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val progress by viewModel.analysisProgress.collectAsState()
    val status by viewModel.analysisStatus.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "BlinkTransition")
    val textBlinkColor by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlinkTextColor"
    )
    val textShadow = remember(textBlinkColor) {
        Shadow(
            color = textBlinkColor.copy(alpha = 0.8f),
            offset = Offset(0f, 0f),
            blurRadius = 12f
        )
    }

    // Текстовое озвучивание (TTS) для инструкции
    var isGuideExpanded by remember { mutableStateOf(false) }
    var isPlayingTts by remember { mutableStateOf(false) }
    var ttsVolume by remember { mutableStateOf(1f) }
    var isMuted by remember { mutableStateOf(false) }
    var currentWordRange by remember { mutableStateOf<IntRange?>(null) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    val ttsGenderState by viewModel.ttsGender.collectAsState()
    val ttsVoiceIndex by viewModel.ttsVoiceIndex.collectAsState()
    val ttsRateState by viewModel.ttsSpeechRate.collectAsState()
    val ttsPitchState by viewModel.ttsPitch.collectAsState()

    val instructionText = "Для точного анализа важно, чтобы снимок был сделан при хорошем освещении.\n" +
            "1. Расположите ладонь ровно перед камерой, без наклона.\n" +
            "2. Пальцы должны быть слегка разведены.\n" +
            "3. Избегайте размытия и теней, падающих на линии руки.\n" +
            "4. Сфотографируйте поочерёдно ладонь и тыльную сторону обеих рук."

    val annotatedInstructionText = remember(currentWordRange) {
        buildAnnotatedString {
            val range = currentWordRange
            if (range != null && range.first in instructionText.indices && range.last <= instructionText.length) {
                append(instructionText.substring(0, range.first))
                withStyle(style = SpanStyle(background = MysticGold.copy(0.4f), color = Color.White, fontWeight = FontWeight.Bold)) {
                    append(instructionText.substring(range.first, range.last))
                }
                append(instructionText.substring(range.last))
            } else {
                append(instructionText)
            }
        }
    }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = if (currentLang == AppLanguage.RUS) java.util.Locale("ru") else java.util.Locale.US
                tts?.language = locale
            }
        }
        
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isPlayingTts = true
            }

            override fun onDone(utteranceId: String?) {
                isPlayingTts = false
                currentWordRange = null
            }

            override fun onError(utteranceId: String?) {
                isPlayingTts = false
                currentWordRange = null
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                currentWordRange = start..end
            }
        })
        
        ttsInstance = tts
        
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Media Store URI values for system-native cameras
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showInAppCamera by remember { mutableStateOf(false) }

    // Launchers for media
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val bitmap = BitmapUtils.uriToBitmap(context, it)
                if (bitmap != null) {
                    when (activeSlot) {
                        "left_palm" -> {
                            viewModel.bitmapLeftPalm.value = bitmap
                            viewModel.leftPalmPath.value = it.toString()
                        }
                        "left_back" -> {
                            viewModel.bitmapLeftBack.value = bitmap
                            viewModel.leftBackPath.value = it.toString()
                        }
                        "right_palm" -> {
                            viewModel.bitmapRightPalm.value = bitmap
                            viewModel.rightPalmPath.value = it.toString()
                        }
                        "right_back" -> {
                            viewModel.bitmapRightBack.value = bitmap
                            viewModel.rightBackPath.value = it.toString()
                        }
                    }
                }
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.videoUri.value = it
                Toast.makeText(context, strings.uploadPreviewVideo, Toast.LENGTH_SHORT).show()
            }
        }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    val bitmap = BitmapUtils.uriToBitmap(context, uri)
                    if (bitmap != null) {
                        when (activeSlot) {
                            "left_palm" -> {
                                viewModel.bitmapLeftPalm.value = bitmap
                                viewModel.leftPalmPath.value = uri.toString()
                            }
                            "left_back" -> {
                                viewModel.bitmapLeftBack.value = bitmap
                                viewModel.leftBackPath.value = uri.toString()
                            }
                            "right_palm" -> {
                                viewModel.bitmapRightPalm.value = bitmap
                                viewModel.rightPalmPath.value = uri.toString()
                            }
                            "right_back" -> {
                                viewModel.bitmapRightBack.value = bitmap
                                viewModel.rightBackPath.value = uri.toString()
                            }
                        }
                    }
                }
            }
        }
    )

    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success) {
                tempVideoUri?.let { uri ->
                    viewModel.videoUri.value = uri
                    Toast.makeText(context, strings.uploadPreviewVideo, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Camera permission launchers
    val cameraPermissionLauncherForPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showInAppCamera = true
            } else {
                Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val cameraPermissionLauncherForVideo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createGalleryVideoUri(context, "hand_video")
                if (uri != null) {
                    tempVideoUri = uri
                    videoCaptureLauncher.launch(uri)
                }
            } else {
                Toast.makeText(context, "Camera permission is required to record video", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!showInterpretationScreen) {
                // STEP 1: Upload media screen
                MysticHeader(strings.uploadTitle)

                // Инструкция «Как правильно фотографировать ладонь» с TTS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1A000000)),
                    border = BorderStroke(1.dp, MysticBronze.copy(0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isGuideExpanded = !isGuideExpanded }
                        ) {
                            Icon(
                                imageVector = if (isGuideExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MysticGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Как правильно фотографировать ладонь",
                                style = MaterialTheme.typography.labelLarge.copy(color = MysticGold, fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (isGuideExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Панель управления TTS
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x1F1E1E2C), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                // Кнопка Воспроизведения / Остановки
                                IconButton(
                                    onClick = {
                                        if (isPlayingTts) {
                                            ttsInstance?.stop()
                                            isPlayingTts = false
                                            currentWordRange = null
                                        } else {
                                            configureTtsVoice(
                                                tts = ttsInstance,
                                                currentLang = currentLang,
                                                voiceGender = ttsGenderState,
                                                voiceIndex = ttsVoiceIndex,
                                                speechRate = ttsRateState,
                                                speechPitch = ttsPitchState
                                            )
                                            val speakParams = android.os.Bundle().apply {
                                                val volume = if (isMuted) 0f else ttsVolume
                                                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
                                                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "guideline_tts")
                                            }
                                            val result = ttsInstance?.speak(instructionText, TextToSpeech.QUEUE_FLUSH, speakParams, "guideline_tts")
                                            if (result == TextToSpeech.SUCCESS) {
                                                isPlayingTts = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingTts) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Озвучить текст",
                                        tint = MysticGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Кнопка Mute
                                IconButton(
                                    onClick = {
                                        isMuted = !isMuted
                                        if (isPlayingTts) {
                                            configureTtsVoice(
                                                tts = ttsInstance,
                                                currentLang = currentLang,
                                                voiceGender = ttsGenderState,
                                                voiceIndex = ttsVoiceIndex,
                                                speechRate = ttsRateState,
                                                speechPitch = ttsPitchState
                                            )
                                            // Обновляем громкость на лету, если играет
                                            val speakParams = android.os.Bundle().apply {
                                                val volume = if (isMuted) 0f else ttsVolume
                                                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
                                                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "guideline_tts")
                                            }
                                            ttsInstance?.speak(instructionText, TextToSpeech.QUEUE_FLUSH, speakParams, "guideline_tts")
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Mute",
                                        tint = if (isMuted) Color.Red else MysticGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Слайдер громкости
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Громкость",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MysticBronze, fontSize = 10.sp)
                                        )
                                        Text(
                                            text = "${(ttsVolume * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MysticGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Slider(
                                        value = ttsVolume,
                                        onValueChange = {
                                            ttsVolume = it
                                            if (isPlayingTts && !isMuted) {
                                                configureTtsVoice(
                                                    tts = ttsInstance,
                                                    currentLang = currentLang,
                                                    voiceGender = ttsGenderState,
                                                    voiceIndex = ttsVoiceIndex,
                                                    speechRate = ttsRateState,
                                                    speechPitch = ttsPitchState
                                                )
                                                val speakParams = android.os.Bundle().apply {
                                                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, it)
                                                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "guideline_tts")
                                                }
                                                ttsInstance?.speak(instructionText, TextToSpeech.QUEUE_FLUSH, speakParams, "guideline_tts")
                                            }
                                        },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = MysticGold,
                                            activeTrackColor = MysticGold,
                                            inactiveTrackColor = MysticBronze.copy(0.3f)
                                        ),
                                        modifier = Modifier.height(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Текст инструкции с подсветкой слов
                            Text(
                                text = annotatedInstructionText,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFC0C0D0), lineHeight = 20.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            )
                        }
                    }
                }

                // Photo upload block (Stack of 4 beautiful vertical slots)
                MysticCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = strings.uploadPhotoSection,
                            style = MaterialTheme.typography.titleMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Left Palm Slot
                        HandSlotCard(
                            title = strings.slotLeftPalm,
                            bitmap = bitmapLeftPalm,
                            slotName = "left_palm",
                            onTakePhoto = {
                                activeSlot = "left_palm"
                                val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    showInAppCamera = true
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "left_palm"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                viewModel.bitmapLeftPalm.value = null
                                viewModel.leftPalmPath.value = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery",
                            onSaveToGallery = {
                                bitmapLeftPalm?.let { bmp ->
                                    val saved = com.aistudio.hiromant.kxsrwa.utils.BitmapUtils.saveBitmapToGallery(context, bmp, "LeftPalm")
                                    val msg = if (saved) "Изображение левой ладони сохранено в галерею" else "Не удалось сохранить изображение"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Left Back Slot
                        HandSlotCard(
                            title = strings.slotLeftBack,
                            bitmap = bitmapLeftBack,
                            slotName = "left_back",
                            onTakePhoto = {
                                activeSlot = "left_back"
                                val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    showInAppCamera = true
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "left_back"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                viewModel.bitmapLeftBack.value = null
                                viewModel.leftBackPath.value = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery",
                            onSaveToGallery = {
                                bitmapLeftBack?.let { bmp ->
                                    val saved = com.aistudio.hiromant.kxsrwa.utils.BitmapUtils.saveBitmapToGallery(context, bmp, "LeftBack")
                                    val msg = if (saved) "Изображение тыла левой руки сохранено в галерею" else "Не удалось сохранить изображение"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Right Palm Slot
                        HandSlotCard(
                            title = strings.slotRightPalm,
                            bitmap = bitmapRightPalm,
                            slotName = "right_palm",
                            onTakePhoto = {
                                activeSlot = "right_palm"
                                val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    showInAppCamera = true
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "right_palm"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                viewModel.bitmapRightPalm.value = null
                                viewModel.rightPalmPath.value = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery",
                            onSaveToGallery = {
                                bitmapRightPalm?.let { bmp ->
                                    val saved = com.aistudio.hiromant.kxsrwa.utils.BitmapUtils.saveBitmapToGallery(context, bmp, "RightPalm")
                                    val msg = if (saved) "Изображение правой ладони сохранено в галерею" else "Не удалось сохранить изображение"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Right Back Slot
                        HandSlotCard(
                            title = strings.slotRightBack,
                            bitmap = bitmapRightBack,
                            slotName = "right_back",
                            onTakePhoto = {
                                activeSlot = "right_back"
                                val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    showInAppCamera = true
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "right_back"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                viewModel.bitmapRightBack.value = null
                                viewModel.rightBackPath.value = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery",
                            onSaveToGallery = {
                                bitmapRightBack?.let { bmp ->
                                    val saved = com.aistudio.hiromant.kxsrwa.utils.BitmapUtils.saveBitmapToGallery(context, bmp, "RightBack")
                                    val msg = if (saved) "Изображение тыла правой руки сохранено в галерею" else "Не удалось сохранить изображение"
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Below all, the "Next" (Далее) Button
                MysticButton(
                    text = if (currentLang == AppLanguage.RUS) "Далее" else "Next",
                    onClick = {
                        val hasMedia = bitmapLeftPalm != null || bitmapLeftBack != null || bitmapRightPalm != null || bitmapRightBack != null
                        if (hasMedia) {
                            viewModel.showInterpretationScreen.value = true
                        } else {
                            Toast.makeText(
                                context,
                                if (currentLang == AppLanguage.RUS) "Пожалуйста, загрузите хотя бы одно фото ладони!" else "Please upload at least one hand photo!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))

            } else {
                // STEP 2: Interpretation / Selection screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = { viewModel.showInterpretationScreen.value = false }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MysticGold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Интерпритация",
                            style = MaterialTheme.typography.titleLarge.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Выберите тип анализа",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val bitmaps = listOfNotNull(bitmapLeftPalm, bitmapLeftBack, bitmapRightPalm, bitmapRightBack)

                // Button 1: Краткий анализ (БЕСПЛАТНО!)
                TriggerAnalysisButton(
                    label = "Краткий анализ",
                    priceText = "(БЕСПЛАТНО!)",
                    onClick = {
                        viewModel.currentAnalysisTypeState.value = "brief"
                        viewModel.runPalmAnalysis(
                            bitmaps = bitmaps,
                            videoUri = null,
                            analysisType = "brief",
                            leftPalmPath = leftPalmPath,
                            leftBackPath = leftBackPath,
                            rightPalmPath = rightPalmPath,
                            rightBackPath = rightBackPath,
                            onCompleted = {
                                viewModel.showInterpretationScreen.value = false
                                onNavigateToLoading()
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка 2: Полный анализ (Обновленная стоимость - 250 р. по требованию пользователя)
                TriggerAnalysisButton(
                    label = "Полный анализ", // Текст метки кнопки полного анализа
                    priceText = "Стоимость - 250 р.", // Обновленная стоимость (было 200 р.)
                    onClick = { // Обработчик нажатия на кнопку запуска полного анализа
                        viewModel.checkFeatureUnlocked("full") { unlocked -> // Проверка доступности функции
                            viewModel.currentAnalysisTypeState.value = "full" // Установка типа анализа в "full"
                            if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) { // Если разблокировано или есть доступные анализы
                                viewModel.showInterpretationScreen.value = false // Скрываем экран интерпретации
                                onNavigateToVideoScan() // Навигация к видео-сканированию
                            } else {
                                viewModel.paymentAmountToPreselect.value = "250"
                                onNavigateToBilling() // Иначе переходим на страницу оплаты
                            }
                        }
                    }
                )

                // Кнопка 3: Анализ Совместимости
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MysticGold
                    ),
                    border = BorderStroke(1.2.dp, MysticGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            viewModel.activeTab.value = "compatibility"
                            viewModel.showInterpretationScreen.value = false
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ShrinkableText(
                            text = "Анализ Совместимости",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isAnalyzing) 100.dp else 40.dp))
            }
        }



        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MysticGold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(1.5.dp, MysticGold, RoundedCornerShape(14.dp))
                ) {
                    val progressFraction = (progress.toFloat() / 100f).coerceIn(0f, 1f)
                    if (progressFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = progressFraction)
                                .background(MysticGold)
                        )
                    }

                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (progressFraction >= 0.5f) Color.Black else MysticGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            shadow = if (progressFraction >= 0.5f) null else textShadow
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showInAppCamera) {
        InAppCameraDialog(
            slotName = activeSlot ?: "palm_photo",
            currentLang = currentLang,
            onPhotoCaptured = { bitmap ->
                when (activeSlot) {
                    "left_palm" -> {
                        viewModel.bitmapLeftPalm.value = bitmap
                        viewModel.leftPalmPath.value = "in_app_camera"
                    }
                    "left_back" -> {
                        viewModel.bitmapLeftBack.value = bitmap
                        viewModel.leftBackPath.value = "in_app_camera"
                    }
                    "right_palm" -> {
                        viewModel.bitmapRightPalm.value = bitmap
                        viewModel.rightPalmPath.value = "in_app_camera"
                    }
                    "right_back" -> {
                        viewModel.bitmapRightBack.value = bitmap
                        viewModel.rightBackPath.value = "in_app_camera"
                    }
                }
                showInAppCamera = false
            },
            onDismiss = {
                showInAppCamera = false
            }
        )
    }
}

@Composable
fun TriggerAnalysisButton(
    label: String,
    priceText: String,
    onClick: () -> Unit
) {
    val isFree = priceText.contains("БЕСПЛАТНО", ignoreCase = true) || 
                 priceText.contains("Free", ignoreCase = true) ||
                 priceText.contains("Бесплатно", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFree) Color(0xFF1E1C18) else MysticGold
        ),
        border = BorderStroke(1.2.dp, if (isFree) MysticGold.copy(0.4f) else MysticGold),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (isFree) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = priceText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isFree) MysticGold else Color.Black.copy(0.8f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}


// --- SCREEN 5: PROGRESSIVE MYSTIC LOADING ---

@Composable
fun MysticLoadingScreen(
    viewModel: PalmistViewModel
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val progress by viewModel.analysisProgress.collectAsState()
    val status by viewModel.analysisStatus.collectAsState()

    // Rotation animation for symbols
    val infiniteTransition = rememberInfiniteTransition(label = "SymbolRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            MysticHeader(strings.loadMysticTitle)
            Spacer(modifier = Modifier.height(20.dp))

            // Glowing central hand with rotating symbols
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Rotating runes / stars circle
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = null,
                    tint = MysticGold.copy(0.4f),
                    modifier = Modifier
                        .size(220.dp)
                        .rotate(rotationAngle)
                )

                // Central glowing hand
                Icon(
                    imageVector = Icons.Default.BackHand,
                    contentDescription = null,
                    tint = MysticGold,
                    modifier = Modifier
                        .size(110.dp)
                        .scale(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MysticGold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = strings.loadProgressText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MysticBronze,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Percentage
            Text(
                text = "$progress%",
                style = MaterialTheme.typography.displayLarge.copy(color = Color.White),
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress.toFloat() / 100f },
                color = MysticGold,
                trackColor = MysticBronze.copy(0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}


// --- SCREEN 6: ANALYSIS RESULTS + INTERACTIVE LINES OVERLAY ---

fun buildReportAnnotatedString(
    report: com.aistudio.hiromant.kxsrwa.data.remote.PalmistReport,
    strings: com.aistudio.hiromant.kxsrwa.ui.language.PalmistStrings,
    spokenWordRange: Pair<Int, Int>?
): AnnotatedString {
    return buildAnnotatedString {
        fun appendHeader(text: String) {
            withStyle(SpanStyle(color = MysticGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                append(text)
            }
            append("\n")
        }
        
        fun appendBody(text: String) {
            withStyle(SpanStyle(color = Color.White, fontSize = 16.sp)) {
                append(text)
            }
            append("\n\n")
        }
        
        appendHeader(strings.resOverallPortrait)
        appendBody(report.overallPortrait)
        
        appendHeader(strings.resHandType)
        appendBody(report.handType)
        
        appendHeader(strings.resMountsHeader)
        report.mounts.forEach { mount ->
            withStyle(SpanStyle(color = MysticGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("${mount.name}: ")
            }
            withStyle(SpanStyle(color = Color.White, fontSize = 16.sp)) {
                append(mount.description)
            }
            append("\n")
        }
        append("\n")
        
        appendHeader(strings.resSignsHeader)
        report.signs.forEach { sign ->
            withStyle(SpanStyle(color = MysticGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                append("${sign.name} (${sign.location}): ")
            }
            withStyle(SpanStyle(color = Color.White, fontSize = 16.sp)) {
                append(sign.description)
            }
            append("\n")
        }
        append("\n")
        
        if (report.leftHand.isNotBlank()) {
            appendHeader(strings.resInheritedPotentials)
            appendBody(report.leftHand)
        }

        if (report.rightHand.isNotBlank()) {
            appendHeader(strings.resAcquiredTraits)
            appendBody(report.rightHand)
        }

        if (report.characterQualities.isNotBlank()) {
            appendHeader(strings.resCharacterQualities)
            appendBody(report.characterQualities)
        }

        val lifePathEventsVal = if (report.lifePathEvents?.isNotBlank() == true) report.lifePathEvents else report.lifeEvents
        if (!lifePathEventsVal.isNullOrBlank()) {
            appendHeader(strings.resLifeEvents)
            appendBody(lifePathEventsVal)
        }

        val predictionsVal = if (report.lifeSituationsInfluence?.isNotBlank() == true) report.lifeSituationsInfluence else report.predictions
        if (!predictionsVal.isNullOrBlank()) {
            appendHeader(strings.resPredictions)
            appendBody(predictionsVal)
        }

        if (report.marriageChildren.isNotBlank()) {
            appendHeader(strings.resMarriageChildren)
            appendBody(report.marriageChildren)
        }

        if (report.recommendations.isNotBlank()) {
            appendHeader(strings.resRecommendations)
            appendBody(report.recommendations)
        }
        
        // Apply highlight on spokenWordRange if present
        spokenWordRange?.let { (start, end) ->
            if (start in 0..length && end in start..length) {
                addStyle(
                    style = SpanStyle(
                        background = MysticGold.copy(alpha = 0.4f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ),
                    start = start,
                    end = end
                )
            }
        }
    }
}

fun buildLinesAnnotatedString(
    lines: List<com.aistudio.hiromant.kxsrwa.data.remote.PalmLineAnalysis>,
    spokenWordRange: Pair<Int, Int>?,
    onLineRangesCalculated: (Map<String, IntRange>) -> Unit
): AnnotatedString {
    val ranges = mutableMapOf<String, IntRange>()
    val annotated = buildAnnotatedString {
        lines.forEach { line ->
            val startIdx = length
            
            withStyle(SpanStyle(color = MysticGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                append(line.name)
            }
            append("\n")
            
            withStyle(SpanStyle(color = Color.White, fontSize = 16.sp)) {
                append(line.fullDescription)
            }
            append("\n")
            
            line.keyTakeaways.forEach { takeaway ->
                withStyle(SpanStyle(color = MysticBronze, fontSize = 14.sp)) {
                    append("• ")
                }
                withStyle(SpanStyle(color = Color(0xFFC0C0D0), fontSize = 14.sp)) {
                    append(takeaway)
                }
                append("\n")
            }
            append("\n")
            
            val endIdx = length
            ranges[line.name] = startIdx until endIdx
        }
        
        // Apply highlight on spokenWordRange if present
        spokenWordRange?.let { (start, end) ->
            if (start in 0..length && end in start..length) {
                addStyle(
                    style = SpanStyle(
                        background = MysticGold.copy(alpha = 0.4f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ),
                    start = start,
                    end = end
                )
            }
        }
    }
    onLineRangesCalculated(ranges)
    return annotated
}

@Composable
fun SelectableInterpretationText(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(), // Передаем ScrollState для синхронизации плавающего заголовка
    spokenWordRange: Pair<Int, Int>? = null,
    onSpeakSelected: (String) -> Unit,
    onReadFromCursor: (Int) -> Unit,
    headerContent: @Composable (() -> Unit)? = null, // Добавлен параметр для гибкого плавающего заголовка
    bottomContent: @Composable () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    // Используем переданный в параметрах scrollState для обеспечения плавной прокрутки заголовка
    var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
    
    val startOffset = spokenWordRange?.first
    LaunchedEffect(startOffset) {
        if (startOffset != null && textLayoutResult != null) {
            try {
                val layout = textLayoutResult!!
                val line = layout.getLineForOffset(startOffset)
                val lineTop = layout.getLineTop(line)
                val lineBottom = layout.getLineBottom(line)
                val viewportHeight = scrollState.viewportSize
                if (viewportHeight > 0) {
                    val targetScroll = (lineTop + lineBottom) / 2f - viewportHeight / 2f
                    val clampedScroll = targetScroll.coerceIn(0f, scrollState.maxValue.toFloat()).toInt()
                    scrollState.animateScrollTo(clampedScroll)
                } else {
                    scrollState.animateScrollTo(lineTop.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    var isFocused by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp) // Фиксированный top padding убран; он регулируется через headerContent
        ) {
            if (headerContent != null) {
                headerContent()
            } else {
                Spacer(modifier = Modifier.height(128.dp)) // На случай, если заголовок не передан
            }
            
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Default,
                    lineHeight = 24.sp
                ),
                onTextLayout = { textLayoutResult = it },
                cursorBrush = SolidColor(MysticGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
            )
            
            bottomContent()
        }
        
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        onReadFromCursor(value.selection.start)
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MysticGold, CircleShape)
                        .border(1.dp, Color.White.copy(0.3f), CircleShape)
                        .shadow(6.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Read from cursor",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TtsVoiceController(
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    rate: Float,
    onRateChange: (Float) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    maleVoices: List<android.speech.tts.Voice>,
    femaleVoices: List<android.speech.tts.Voice>,
    selectedVoice: android.speech.tts.Voice?,
    onVoiceSelected: (android.speech.tts.Voice) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var maleMenuExpanded by remember { mutableStateOf(false) }
    var femaleMenuExpanded by remember { mutableStateOf(false) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color(0xBB000000), RoundedCornerShape(22.dp))
            .border(1.dp, MysticBronze.copy(0.4f), RoundedCornerShape(22.dp))
            .padding(4.dp)
            .animateContentSize()
    ) {
        IconButton(
            onClick = {
                expanded = !expanded
                onPlayToggle()
            },
            modifier = Modifier
                .size(36.dp)
                .background(MysticGold, CircleShape)
        ) {
            Text(
                text = if (isPlaying) "||" else ">",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Male voice button & dropdown
                Box {
                    IconButton(
                        onClick = {
                            onGenderChange("Male")
                            maleMenuExpanded = true
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (gender == "Male") MysticGold else Color.White.copy(0.1f),
                                CircleShape
                            )
                            .border(1.dp, if (gender == "Male") MysticGold else Color.Gray, CircleShape)
                    ) {
                        Text(
                            text = "м",
                            color = if (gender == "Male") Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    DropdownMenu(
                        expanded = maleMenuExpanded,
                        onDismissRequest = { maleMenuExpanded = false },
                        modifier = Modifier.background(MysticDarkSurface)
                    ) {
                        if (maleVoices.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Мужской стандартный", color = Color.White) },
                                onClick = {
                                    onGenderChange("Male")
                                    maleMenuExpanded = false
                                }
                            )
                        } else {
                            maleVoices.forEachIndexed { index, voice ->
                                val isSelected = selectedVoice?.name == voice.name
                                val cleanName = voice.name.substringAfterLast(".").substringBefore("-local")
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Голос М${index + 1} ($cleanName)",
                                            color = if (isSelected) MysticGold else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onVoiceSelected(voice)
                                        onGenderChange("Male")
                                        maleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Slider(
                    value = rate,
                    onValueChange = onRateChange,
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = MysticGold,
                        inactiveTrackColor = Color.Gray,
                        thumbColor = MysticGold
                    ),
                    modifier = Modifier
                        .width(120.dp)
                        .height(32.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Female voice button & dropdown
                Box {
                    IconButton(
                        onClick = {
                            onGenderChange("Female")
                            femaleMenuExpanded = true
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (gender == "Female") MysticGold else Color.White.copy(0.1f),
                                CircleShape
                            )
                            .border(1.dp, if (gender == "Female") MysticGold else Color.Gray, CircleShape)
                    ) {
                        Text(
                            text = "ж",
                            color = if (gender == "Female") Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    DropdownMenu(
                        expanded = femaleMenuExpanded,
                        onDismissRequest = { femaleMenuExpanded = false },
                        modifier = Modifier.background(MysticDarkSurface)
                    ) {
                        if (femaleVoices.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Женский стандартный", color = Color.White) },
                                onClick = {
                                    onGenderChange("Female")
                                    femaleMenuExpanded = false
                                }
                            )
                        } else {
                            femaleVoices.forEachIndexed { index, voice ->
                                val isSelected = selectedVoice?.name == voice.name
                                val cleanName = voice.name.substringAfterLast(".").substringBefore("-local")
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Голос Ж${index + 1} ($cleanName)",
                                            color = if (isSelected) MysticGold else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onVoiceSelected(voice)
                                        onGenderChange("Female")
                                        femaleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun buildLeftHandAnnotatedString(
    report: com.aistudio.hiromant.kxsrwa.data.remote.PalmistReport,
    spokenWordRange: Pair<Int, Int>?
): AnnotatedString {
    val annotated = buildAnnotatedString {
        fun appendHeader(text: String) {
            withStyle(SpanStyle(color = MysticGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                append(text)
            }
            append("\n")
        }
        
        fun appendBody(text: String) {
            withStyle(SpanStyle(color = Color.White, fontSize = 16.sp)) {
                append(text)
            }
            append("\n\n")
        }
        
        appendHeader("Левая рука (Врожденный потенциал)")
        appendBody(if (report.leftHand.isNotBlank()) report.leftHand else "Анализ левой ладони недоступен.")
        
        if (report.overallPortrait.isNotBlank()) {
            appendHeader("Общий портрет личности")
            appendBody(report.overallPortrait)
        }
        
        if (report.handType.isNotBlank()) {
            appendHeader("Тип ладони")
            appendBody(report.handType)
        }
    }

    // Применяем выделение серым цветом для текущего произносимого слова при чтении
    return if (spokenWordRange != null) {
        val (start, end) = spokenWordRange
        if (start in 0..annotated.length && end in start..annotated.length) {
            buildAnnotatedString {
                append(annotated)
                addStyle(
                    style = SpanStyle(
                        background = Color.Gray.copy(alpha = 0.4f), // Полупрозрачный серый фон для читаемого слова
                        color = Color.White, // Белый цвет текста для контраста
                        fontWeight = FontWeight.Bold // Выделяем слово жирным шрифтом
                    ),
                    start = start,
                    end = end
                )
            }
        } else {
            annotated
        }
    } else {
        annotated
    }
}

fun buildRightHandAnnotatedString(
    report: com.aistudio.hiromant.kxsrwa.data.remote.PalmistReport,
    spokenWordRange: Pair<Int, Int>?
): AnnotatedString {
    val annotated = buildAnnotatedString {
        fun appendHeader(text: String) {
            withStyle(SpanStyle(color = MysticGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                append(text)
            }
            append("\n")
        }
        
        fun appendBody(text: String) {
            withStyle(SpanStyle(color = Color.White, fontSize = 16.sp)) {
                append(text)
            }
            append("\n\n")
        }
        
        appendHeader("Правая рука (Приобретенные качества)")
        appendBody(if (report.rightHand.isNotBlank()) report.rightHand else "Анализ правой ладони недоступен.")
        
        if (report.recommendations.isNotBlank()) {
            appendHeader("Рекомендации и предостережения")
            appendBody(report.recommendations)
        }
    }

    // Применяем выделение серым цветом для текущего произносимого слова при чтении
    return if (spokenWordRange != null) {
        val (start, end) = spokenWordRange
        if (start in 0..annotated.length && end in start..annotated.length) {
            buildAnnotatedString {
                append(annotated)
                addStyle(
                    style = SpanStyle(
                        background = Color.Gray.copy(alpha = 0.4f), // Полупрозрачный серый фон для читаемого слова
                        color = Color.White, // Белый цвет текста для контраста
                        fontWeight = FontWeight.Bold // Выделяем слово жирным шрифтом
                    ),
                    start = start,
                    end = end
                )
            }
        } else {
            annotated
        }
    } else {
        annotated
    }
}

@Composable
fun ProjectSupportSection(
    viewModel: PalmistViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showSupportDialog by remember { mutableStateOf(false) }
    var supportAmount by remember { mutableStateOf("250") }
    var selectedMethod by remember { mutableStateOf("yoomoney") } // "yoomoney", "ozon", "wb"
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var confirmAttempts by remember { mutableStateOf(0) }
    var isCheckingPayment by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MysticBronze.copy(0.2f))
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = PALMIST_PROJECT_SUPPORT_TEXT,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.LightGray.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        MysticButton(
            text = if (currentLang == AppLanguage.RUS) "Поддержать проект Хиромант" else "Support the Palmist Project",
            onClick = { showSupportDialog = true },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showSupportDialog) {
        Dialog(onDismissRequest = { showSupportDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                border = BorderStroke(1.5.dp, MysticGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Поддержать проект Хиромант" else "Support the Project",
                        style = MaterialTheme.typography.titleLarge,
                        color = MysticGold,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Выбор платежной системы
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Выберите платежную систему:" else "Select payment system:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "yoomoney" to "ЮMoney / Карты",
                        "ozon" to "Ozon Банк (СБП)",
                        "wb" to "WB Банк (СБП)"
                    ).forEach { (methodId, label) ->
                        val isSelected = selectedMethod == methodId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    if (isSelected) MysticGold.copy(0.12f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MysticGold else Color.Gray.copy(0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMethod = methodId }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedMethod = methodId },
                                colors = RadioButtonDefaults.colors(selectedColor = MysticGold, unselectedColor = Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ввод суммы с текстом "Сколько не жалко!)"
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Сумма поддержки:" else "Support Amount:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    MysticTextField(
                        value = supportAmount,
                        onValueChange = { newVal ->
                            supportAmount = newVal.filter { it.isDigit() }
                        },
                        label = if (currentLang == AppLanguage.RUS) "Сколько не жалко!)" else "As much as you wish!)",
                        placeholder = if (currentLang == AppLanguage.RUS) "Сколько не жалко!)" else "As much as you wish!)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MysticButton(
                            text = if (currentLang == AppLanguage.RUS) "Отмена" else "Cancel",
                            onClick = { showSupportDialog = false },
                            isSecondary = true,
                            modifier = Modifier.weight(1f)
                        )
                        
                        MysticButton(
                            text = if (currentLang == AppLanguage.RUS) "Отправить" else "Send",
                            onClick = {
                                val amountVal = supportAmount.trim()
                                if (amountVal.isEmpty() || amountVal.toIntOrNull() == null || amountVal.toInt() <= 0) {
                                    val errorMsg = if (currentLang == AppLanguage.RUS) "Пожалуйста, введите корректную сумму" else "Please enter a valid amount"
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    return@MysticButton
                                }
                                
                                try {
                                    val cleanWallet = "410013630971157"
                                    val targets = "Hiromant Project Support: $amountVal RUB"
                                    val encodedTargets = java.net.URLEncoder.encode(targets, "UTF-8")
                                    val url = "https://yoomoney.ru/quickpay/confirm.xml?" +
                                            "receiver=$cleanWallet&" +
                                            "quickpay-form=button&" +
                                            "targets=$encodedTargets&" +
                                            "paymentType=AC&" +
                                            "sum=$amountVal"
                                    
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                    showConfirmationDialog = true
                                } catch (e: Exception) {
                                    val errorMsg = if (currentLang == AppLanguage.RUS) "Ошибка запуска оплаты: ${e.localizedMessage}" else "Payment launch error: ${e.localizedMessage}"
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showConfirmationDialog) {
        val dialogTitle = when (selectedMethod) {
            "ozon" -> if (currentLang == AppLanguage.RUS) "Ожидание оплаты Ozon Банк (СБП)" else "Pending Ozon Bank Payment (SBP)"
            "wb" -> if (currentLang == AppLanguage.RUS) "Ожидание оплаты WB Банк (СБП)" else "Pending WB Bank Payment (SBP)"
            else -> if (currentLang == AppLanguage.RUS) "Ожидание оплаты ЮMoney" else "Pending YooMoney Payment"
        }
        val dialogText = when (selectedMethod) {
            "ozon" -> if (currentLang == AppLanguage.RUS) {
                "Была инициализирована оплата через Ozon Банк (СБП) на сумму $supportAmount ₽.\n\nПожалуйста, совершите перевод и нажмите кнопку 'Подтвердить' для зачисления поддержки и анализов."
            } else {
                "Payment of $supportAmount RUB via Ozon Bank (SBP) was initialized.\n\nPlease complete the transfer and click 'Confirm' to claim your support credits."
            }
            "wb" -> if (currentLang == AppLanguage.RUS) {
                "Была инициализирована оплата через WB Банк (СБП) на сумму $supportAmount ₽.\n\nПожалуйста, совершите перевод и нажмите кнопку 'Подтвердить' для зачисления поддержки и анализов."
            } else {
                "Payment of $supportAmount RUB via WB Bank (SBP) was initialized.\n\nPlease complete the transfer and click 'Confirm' to claim your support credits."
            }
            else -> if (currentLang == AppLanguage.RUS) {
                "Страница перевода была открыта.\n\nПосле завершения перевода вернитесь сюда и нажмите кнопку 'Подтвердить', чтобы получить заслуженные анализы (+1 анализ за каждые 100 рублей)!"
            } else {
                "The transfer page has been opened.\n\nAfter completing the transaction, return here and tap 'Confirm' to activate your bonus analyses (+1 analysis for every 100 rubles)!"
            }
        }

        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = dialogTitle,
                    color = MysticGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = dialogText,
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isCheckingPayment = true
                            delay(1500)
                            isCheckingPayment = false
                            if (confirmAttempts == 0) {
                                confirmAttempts++
                                val tryAgainMsg = if (currentLang == AppLanguage.RUS) "Оплата не подтверждена... Попробуйте ещё раз!" else "Payment not confirmed. Try again!"
                                Toast.makeText(context, tryAgainMsg, Toast.LENGTH_LONG).show()
                            } else {
                                showConfirmationDialog = false
                                showSupportDialog = false
                                val amountVal = supportAmount.toIntOrNull() ?: 0
                                viewModel.addSupportPayment(amountVal, "Поддержка: $selectedMethod")
                                
                                val granted = amountVal / 100
                                val successMsg = if (currentLang == AppLanguage.RUS) {
                                    "Спасибо огромное за поддержку проекта! Вам начислено +$granted анализов."
                                } else {
                                    "Thank you so much for supporting the project! You've been credited with +$granted analyses."
                                }
                                Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold),
                    enabled = !isCheckingPayment
                ) {
                    if (isCheckingPayment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Подтвердить" else "Confirm",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text(text = if (currentLang == AppLanguage.RUS) "Отмена" else "Cancel", color = Color.Gray)
                }
            },
            containerColor = MysticDarkSurface,
            textContentColor = Color.White
        )
    }
}

@Composable
fun ResultsScreen(
    viewModel: PalmistViewModel,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val reading by viewModel.currentReading.collectAsState()
    val billingState by viewModel.billingState.collectAsState()

    var activeTab by remember { mutableStateOf("left") } // "left" or "right"

    // Parse JSON
    val palmistReport = remember(reading) {
        reading?.let {
            try {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                moshi.adapter(com.aistudio.hiromant.kxsrwa.data.remote.PalmistReport::class.java).fromJson(it.resultJson ?: "")
            } catch (e: Exception) {
                null
            }
        }
    }

    // TTS configurations from central ViewModel state
    val ttsGenderState by viewModel.ttsGender.collectAsState()
    val ttsVoiceIndex by viewModel.ttsVoiceIndex.collectAsState()
    val ttsRateState by viewModel.ttsSpeechRate.collectAsState()
    val ttsPitchState by viewModel.ttsPitch.collectAsState()

    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isPlayingTts by remember { mutableStateOf(false) }
    var spokenWordRange by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var ttsOffset by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Selectable Text Field states
    var leftHandTextState by remember { mutableStateOf(TextFieldValue()) }
    var rightHandTextState by remember { mutableStateOf(TextFieldValue()) }

    var isTtsReady by remember { mutableStateOf(false) }

    // Initialize Android TTS
    DisposableEffect(Unit) {
        var ttsInstanceObj: TextToSpeech? = null
        ttsInstanceObj = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstanceObj?.language = if (currentLang == AppLanguage.RUS) Locale("ru") else Locale.US
                isTtsReady = true
            }
        }
        tts = ttsInstanceObj
        onDispose {
            ttsInstanceObj.stop()
            ttsInstanceObj.shutdown()
        }
    }

    val allAvailableVoices = remember(tts, isTtsReady, currentLang) {
        try {
            val currentLocale = if (currentLang == AppLanguage.RUS) Locale("ru") else Locale.US
            val voices = tts?.voices?.toList() ?: emptyList()
            voices.filter { it.locale.language == currentLocale.language }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val femaleVoicesList = remember(allAvailableVoices) {
        allAvailableVoices.filter { voice ->
            val nameLower = voice.name.lowercase(Locale.US)
            nameLower.contains("female") || 
            nameLower.contains("f-local") || 
            nameLower.contains("ruf") || 
            nameLower.contains("dfc") || 
            nameLower.contains("dfh") || 
            nameLower.contains("rua") || 
            nameLower.contains("ruc") || 
            nameLower.contains("rue") ||
            nameLower.contains("ru-ru-a") ||
            nameLower.contains("ru-ru-c") ||
            nameLower.contains("ru-ru-e") ||
            nameLower.contains("-f-") ||
            nameLower.contains("-f_") ||
            nameLower.contains("_f_")
        }
    }

    val maleVoicesList = remember(allAvailableVoices) {
        allAvailableVoices.filter { voice ->
            val nameLower = voice.name.lowercase(Locale.US)
            nameLower.contains("male") || 
            nameLower.contains("m-local") || 
            nameLower.contains("rum") || 
            nameLower.contains("dfd") || 
            nameLower.contains("dfg") || 
            nameLower.contains("rub") || 
            nameLower.contains("rud") ||
            nameLower.contains("ru-ru-b") ||
            nameLower.contains("ru-ru-d") ||
            nameLower.contains("-m-") ||
            nameLower.contains("-m_") ||
            nameLower.contains("_m_")
        }
    }

    val selectedVoice = remember(femaleVoicesList, maleVoicesList, ttsGenderState, ttsVoiceIndex) {
        if (ttsGenderState == "Female") {
            if (femaleVoicesList.isNotEmpty()) femaleVoicesList[ttsVoiceIndex % femaleVoicesList.size] else null
        } else {
            if (maleVoicesList.isNotEmpty()) maleVoicesList[ttsVoiceIndex % maleVoicesList.size] else null
        }
    }

    fun applyTtsSettings() {
        configureTtsVoice(
            tts = tts,
            currentLang = currentLang,
            voiceGender = ttsGenderState,
            voiceIndex = ttsVoiceIndex,
            speechRate = ttsRateState,
            speechPitch = ttsPitchState
        )
    }

    val leftHandAnnotatedString = remember(palmistReport, spokenWordRange) {
        if (palmistReport != null) {
            buildLeftHandAnnotatedString(palmistReport, spokenWordRange)
        } else {
            AnnotatedString("")
        }
    }

    val rightHandAnnotatedString = remember(palmistReport, spokenWordRange) {
        if (palmistReport != null) {
            buildRightHandAnnotatedString(palmistReport, spokenWordRange)
        } else {
            AnnotatedString("")
        }
    }

    // Состояние для хранения последнего прочитанного индекса символа для возобновления после паузы
    var lastPlaybackIndex by remember { mutableStateOf(0) }

    LaunchedEffect(activeTab) {
        tts?.stop()
        isPlayingTts = false
        spokenWordRange = null
        lastPlaybackIndex = 0 // Сбрасываем позицию прочтения при переключении ладоней
    }

    LaunchedEffect(leftHandAnnotatedString) {
        leftHandTextState = leftHandTextState.copy(annotatedString = leftHandAnnotatedString)
    }

    LaunchedEffect(rightHandAnnotatedString) {
        rightHandTextState = rightHandTextState.copy(annotatedString = rightHandAnnotatedString)
    }

    DisposableEffect(tts) {
        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    isPlayingTts = true
                }
            }
            override fun onDone(utteranceId: String?) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    isPlayingTts = false
                    spokenWordRange = null
                    lastPlaybackIndex = 0 // Сбрасываем индекс, когда чтение завершено полностью
                }
            }
            override fun onError(utteranceId: String?) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    isPlayingTts = false
                }
            }
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    val absStart = start + ttsOffset
                    val absEnd = end + ttsOffset
                    spokenWordRange = Pair(absStart, absEnd)
                    lastPlaybackIndex = absStart // Сохраняем текущую позицию чтения в реальном времени
                }
            }
        })
        onDispose {
            tts?.setOnUtteranceProgressListener(null)
        }
    }

    fun speakTextFromIndex(text: String, startIndex: Int) {
        if (text.isEmpty()) return
        tts?.stop()
        applyTtsSettings()
        
        val textToSpeak = text.substring(startIndex)
        val params = android.os.Bundle().apply {
            putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "reading_text")
        }
        
        ttsOffset = startIndex
        tts?.speak(textToSpeak, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "reading_text")
        isPlayingTts = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            val currentReadingVal = reading
            val isPaymentRequired = remember(currentReadingVal) {
                currentReadingVal?.resultJson?.contains("payment_required") == true
            }

            if (isPaymentRequired && currentReadingVal != null) {
                // Если требуется оплата, показываем фиксированную шапку "Результат Анализа"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MysticGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Результат Анализа",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MysticGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        val isFullReading = currentReadingVal.analysisType.contains("full")
                        Text(
                            text = if (isFullReading) "Полный Анализ" else "Краткий бесплатный Анализ",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    YookassaPaymentForm(
                        readingId = currentReadingVal.id,
                        analysisType = currentReadingVal.analysisType,
                        viewModel = viewModel,
                        onSuccess = {
                            // Automatically reloaded by state flow
                        },
                        onClose = onClose
                    )
                }
            } else if (palmistReport != null) {
                val currentTextState = if (activeTab == "left") leftHandTextState else rightHandTextState
                val onTextStateChange: (TextFieldValue) -> Unit = { newValue ->
                    if (activeTab == "left") {
                        leftHandTextState = newValue
                    } else {
                        rightHandTextState = newValue
                    }
                }

                // Вычисляем смещение плавающей панели вкладок (TabRow) на основе scrollState
                val density = LocalDensity.current
                val headerHeight = 72.dp
                val headerHeightPx = with(density) { headerHeight.toPx() }
                val tabRowOffset = with(density) {
                    val offsetPx = (headerHeightPx - scrollState.value).coerceAtLeast(0f)
                    offsetPx.toDp()
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    SelectableInterpretationText(
                        value = currentTextState,
                        onValueChange = onTextStateChange,
                        modifier = Modifier.fillMaxSize(),
                        scrollState = scrollState, // Передаем scrollState
                        spokenWordRange = spokenWordRange,
                        onSpeakSelected = { selectedText ->
                            tts?.stop()
                            applyTtsSettings()
                            ttsOffset = currentTextState.selection.start
                            val params = android.os.Bundle().apply {
                                putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "selection")
                            }
                            tts?.speak(selectedText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "selection")
                            isPlayingTts = true
                        },
                        onReadFromCursor = { cursorIndex ->
                            speakTextFromIndex(currentTextState.text, cursorIndex)
                        },
                        headerContent = {
                            // Плавающий заголовок, который скроллится вместе с контентом
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = onClose) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MysticGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Результат Анализа",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                color = MysticGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp
                                            )
                                        )
                                        val isFullReading = reading?.analysisType?.startsWith("full") == true
                                        Text(
                                            text = if (isFullReading) "Полный Анализ" else "Краткий бесплатный Анализ",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                }
                            }
                            
                            // Резервируем место под фиксированный TabRow на уровне scrollState = 0
                            Spacer(modifier = Modifier.height(56.dp))
                        },
                        bottomContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProjectSupportSection(viewModel = viewModel)
                                
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "ДОПОЛНИТЕЛЬНО:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze, letterSpacing = 2.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                MysticButton(
                                    text = "Закрыть отчет",
                                    onClick = onClose,
                                    modifier = Modifier.fillMaxWidth(),
                                    isSecondary = false
                                )

                                MysticButton(
                                    text = strings.resExportPdf,
                                    onClick = {
                                        Toast.makeText(context, strings.resExportSuccess, Toast.LENGTH_LONG).show()
                                    },
                                    isSecondary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                MysticButton(
                                    text = "Проверить совместимость",
                                    onClick = onNavigateToCompatibility,
                                    modifier = Modifier.fillMaxWidth(),
                                    isSecondary = true
                                )
                            }
                        }
                    )
                    
                    // Фиксированный / Прилипающий заголовок вкладок (TabRow), прилипает к верху экрана при скролле
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = tabRowOffset)
                            .background(MysticDarkBackground) // Сплошной фон скрывает прокручивающийся под ним текст
                    ) {
                        TabRow(
                            selectedTabIndex = if (activeTab == "left") 0 else 1,
                            containerColor = Color.Transparent,
                            contentColor = MysticGold,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "left") 0 else 1]),
                                    color = MysticGold
                                )
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Tab(
                                selected = activeTab == "left",
                                onClick = { activeTab = "left" },
                                text = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text("Левая рука", style = MaterialTheme.typography.labelLarge)
                                        Text("Данность от рождения", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            )
                            Tab(
                                selected = activeTab == "right",
                                onClick = { activeTab = "right" },
                                text = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text("Правая рука", style = MaterialTheme.typography.labelLarge)
                                        Text("Приобретенная судьба", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            )
                        }
                    }

                    TtsVoiceController(
                        isPlaying = isPlayingTts,
                        onPlayToggle = {
                            if (isPlayingTts) {
                                tts?.stop()
                                isPlayingTts = false
                                // Обратите внимание: при паузе мы НЕ зануляем lastPlaybackIndex, чтобы продолжить с этого же слова!
                            } else {
                                speakTextFromIndex(currentTextState.text, lastPlaybackIndex)
                            }
                        },
                        rate = ttsRateState,
                        onRateChange = { newRate ->
                            viewModel.changeTtsSpeechRate(newRate)
                            tts?.setSpeechRate(newRate)
                            if (isPlayingTts) {
                                val currentWordStart = spokenWordRange?.first ?: 0
                                speakTextFromIndex(currentTextState.text, currentWordStart)
                            }
                        },
                        gender = ttsGenderState,
                        onGenderChange = { newGender ->
                            viewModel.changeTtsGender(newGender)
                            applyTtsSettings()
                            if (isPlayingTts) {
                                val currentWordStart = spokenWordRange?.first ?: 0
                                speakTextFromIndex(currentTextState.text, currentWordStart)
                            }
                        },
                        maleVoices = maleVoicesList,
                        femaleVoices = femaleVoicesList,
                        selectedVoice = selectedVoice,
                        onVoiceSelected = { voice ->
                            val index = if (ttsGenderState == "Female") {
                                femaleVoicesList.indexOf(voice)
                            } else {
                                maleVoicesList.indexOf(voice)
                            }
                            if (index >= 0) {
                                viewModel.changeTtsVoiceIndex(index)
                            }
                            applyTtsSettings()
                            if (isPlayingTts) {
                                val currentWordStart = spokenWordRange?.first ?: 0
                                speakTextFromIndex(currentTextState.text, currentWordStart)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LineReportCard(
    line: com.aistudio.hiromant.kxsrwa.data.remote.PalmLineAnalysis,
    isActive: Boolean,
    onPlayClick: () -> Unit
) {
    val parsedColor = remember(line.color) {
        try {
            Color(android.graphics.Color.parseColor(line.color))
        } catch (e: Exception) {
            MysticGold
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0x44B87333) else Color(0x22141420)
        ),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) MysticGold else MysticBronze.copy(0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onPlayClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(parsedColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = line.name,
                        style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                    )
                }

                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speaking",
                        tint = MysticGold,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = "Read",
                        tint = MysticBronze,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = line.fullDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E2EC)
            )
            Spacer(modifier = Modifier.height(10.dp))

            line.keyTakeaways.forEach { takeaway ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MysticGold,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = takeaway,
                        fontSize = 12.sp,
                        color = MysticBronze,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MountReportRow(mount: com.aistudio.hiromant.kxsrwa.data.remote.PalmMountAnalysis) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (mount.active) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (mount.active) MysticGold else Color.Gray,
            modifier = Modifier
                .padding(top = 2.dp, end = 12.dp)
                .size(18.dp)
        )
        Column {
            Text(
                text = mount.name,
                style = MaterialTheme.typography.labelLarge.copy(color = if (mount.active) MysticGold else Color.Gray)
            )
            Text(
                text = mount.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC0C0D0)
            )
        }
    }
}

@Composable
fun SignReportCard(sign: com.aistudio.hiromant.kxsrwa.data.remote.PalmSign) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x11D4AF37)),
        border = BorderStroke(0.5.dp, MysticGold.copy(0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${sign.name} (${sign.location})",
                style = MaterialTheme.typography.labelLarge.copy(color = MysticGold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = sign.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}


// --- SCREEN 7: PARTNER COMPATIBILITY READING ---

@Composable
fun CompatibilityScreen(
    viewModel: PalmistViewModel,
    onNavigateToLoading: () -> Unit,
    onNavigateToBilling: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val billingState by viewModel.billingState.collectAsState()
    val compatibilityReading by viewModel.currentCompatibilityReading.collectAsState()

    var partnerName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val ttsGenderState by viewModel.ttsGender.collectAsState()
    val ttsVoiceIndex by viewModel.ttsVoiceIndex.collectAsState()
    val ttsRateState by viewModel.ttsSpeechRate.collectAsState()
    val ttsPitchState by viewModel.ttsPitch.collectAsState()

    var isPlayingTts by remember { mutableStateOf(false) }
    var spokenWordRange by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var ttsOffset by remember { mutableStateOf(0) }
    var lastPlaybackIndex by remember { mutableStateOf(0) }
    var ttsByLocalRef by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Initialize Android TTS
    DisposableEffect(Unit) {
        var ttsInstanceObj: android.speech.tts.TextToSpeech? = null
        ttsInstanceObj = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                ttsInstanceObj?.language = if (currentLang == AppLanguage.RUS) java.util.Locale("ru") else java.util.Locale.US
                isTtsReady = true
            }
        }
        ttsByLocalRef = ttsInstanceObj
        onDispose {
            ttsInstanceObj?.stop()
            ttsInstanceObj?.shutdown()
        }
    }

    val allAvailableVoices = remember(ttsByLocalRef, isTtsReady, currentLang) {
        try {
            val currentLocale = if (currentLang == AppLanguage.RUS) java.util.Locale("ru") else java.util.Locale.US
            val voices = ttsByLocalRef?.voices?.toList() ?: emptyList()
            voices.filter { it.locale.language == currentLocale.language }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val femaleVoicesList = remember(allAvailableVoices) {
        allAvailableVoices.filter { voice ->
            val nameLower = voice.name.lowercase(java.util.Locale.US)
            nameLower.contains("female") || 
            nameLower.contains("f-local") || 
            nameLower.contains("ruf") || 
            nameLower.contains("dfc") || 
            nameLower.contains("dfh") || 
            nameLower.contains("rua") || 
            nameLower.contains("ruc") || 
            nameLower.contains("rue") ||
            nameLower.contains("ru-ru-a") ||
            nameLower.contains("ru-ru-c") ||
            nameLower.contains("ru-ru-e") ||
            nameLower.contains("-f-") ||
            nameLower.contains("-f_") ||
            nameLower.contains("_f_")
        }
    }

    val maleVoicesList = remember(allAvailableVoices) {
        allAvailableVoices.filter { voice ->
            val nameLower = voice.name.lowercase(java.util.Locale.US)
            nameLower.contains("male") || 
            nameLower.contains("m-local") || 
            nameLower.contains("rum") || 
            nameLower.contains("dfd") || 
            nameLower.contains("dfg") || 
            nameLower.contains("rub") || 
            nameLower.contains("rud") ||
            nameLower.contains("ru-ru-b") ||
            nameLower.contains("ru-ru-d") ||
            nameLower.contains("-m-") ||
            nameLower.contains("-m_") ||
            nameLower.contains("_m_")
        }
    }

    val selectedVoice = remember(femaleVoicesList, maleVoicesList, ttsGenderState, ttsVoiceIndex) {
        if (ttsGenderState == "Female") {
            if (femaleVoicesList.isNotEmpty()) femaleVoicesList[ttsVoiceIndex % femaleVoicesList.size] else null
        } else {
            if (maleVoicesList.isNotEmpty()) maleVoicesList[ttsVoiceIndex % maleVoicesList.size] else null
        }
    }

    fun applyTtsSettings() {
        configureTtsVoice(
            tts = ttsByLocalRef,
            currentLang = currentLang,
            voiceGender = ttsGenderState,
            voiceIndex = ttsVoiceIndex,
            speechRate = ttsRateState,
            speechPitch = ttsPitchState
        )
    }

    DisposableEffect(ttsByLocalRef) {
        ttsByLocalRef?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    isPlayingTts = true
                }
            }
            override fun onDone(utteranceId: String?) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    isPlayingTts = false
                    spokenWordRange = null
                    lastPlaybackIndex = 0
                }
            }
            override fun onError(utteranceId: String?) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    isPlayingTts = false
                }
            }
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    val absStart = start + ttsOffset
                    val absEnd = end + ttsOffset
                    spokenWordRange = Pair(absStart, absEnd)
                    lastPlaybackIndex = absStart
                }
            }
        })
        onDispose {
            ttsByLocalRef?.setOnUtteranceProgressListener(null)
        }
    }

    fun speakTextFromIndex(text: String, startIndex: Int) {
        if (text.isEmpty()) return
        ttsByLocalRef?.stop()
        applyTtsSettings()
        
        val textToSpeak = text.substring(startIndex)
        val params = android.os.Bundle().apply {
            putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "reading_text")
        }
        
        ttsOffset = startIndex
        ttsByLocalRef?.speak(textToSpeak, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "reading_text")
        isPlayingTts = true
    }
    
    var selectedPartner1 by remember { mutableStateOf<ReadingEntity?>(null) }
    var selectedPartner2 by remember { mutableStateOf<ReadingEntity?>(null) }
    var activeSelectionSlot by remember { mutableStateOf(1) } // 1 or 2

    val readings by viewModel.allReadings.collectAsState()
    
    val interpretations = remember(readings) {
        readings.filter { it.analysisType != "compatibility" }
    }
    
    val filteredInterpretations = remember(interpretations, searchQuery) {
        interpretations.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }
    
    val availableChoices = remember(filteredInterpretations, selectedPartner1, activeSelectionSlot) {
        if (activeSelectionSlot == 2 && selectedPartner1 != null) {
            filteredInterpretations.filter { record ->
                val norm1 = selectedPartner1!!.gender.lowercase().trim()
                val norm2 = record.gender.lowercase().trim()
                val isMale1 = norm1.startsWith("м") || norm1.startsWith("m")
                val isMale2 = norm2.startsWith("м") || norm2.startsWith("m")
                isMale1 != isMale2
            }
        } else {
            filteredInterpretations
        }
    }

    // Parse output JSON
    val compatReport = remember(compatibilityReading) {
        compatibilityReading?.let {
            try {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                moshi.adapter(com.aistudio.hiromant.kxsrwa.data.remote.CompatibilityReport::class.java).fromJson(it.resultJson)
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Custom 2-line title for "Совместимость с партнером" to prevent ugly wordwrap
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Text(
                    text = if (currentLang == AppLanguage.RUS) "Совместимость" else "Compatibility",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = MysticGold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (currentLang == AppLanguage.RUS) "с партнёром" else "with partner",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = MysticGold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            MysticSubtitle(strings.compatSubtitle)

            Spacer(modifier = Modifier.height(20.dp))

            val currentCompatibilityReadingVal = compatibilityReading
            val isPaymentRequired = remember(currentCompatibilityReadingVal) {
                currentCompatibilityReadingVal?.resultJson?.contains("payment_required") == true
            }

            if (isPaymentRequired && currentCompatibilityReadingVal != null) {
                YookassaPaymentForm(
                    readingId = currentCompatibilityReadingVal.id,
                    analysisType = "compatibility",
                    viewModel = viewModel,
                    onSuccess = {
                        // Automatically reloaded by state flow
                    },
                    onClose = {
                        viewModel.currentCompatibilityReading.value = null
                    }
                )
            } else if (compatReport == null) {
                // --- ENTRY AND SELECTION FROM HISTORY ---
                
                // Search Field
                MysticTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = if (currentLang == AppLanguage.RUS) "Поиск по имени" else "Search by name",
                    placeholder = if (currentLang == AppLanguage.RUS) "Введите имя..." else "Enter name...",
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Active Slots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Slot 1 card
                    val slot1Selected = selectedPartner1 != null
                    val isSlot1Active = activeSelectionSlot == 1
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSlot1Active) MysticGold.copy(0.1f) else Color.White.copy(0.1f)
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (isSlot1Active) MysticGold else if (slot1Selected) MysticGold.copy(0.4f) else Color.White.copy(0.1f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSelectionSlot = 1 }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Партнёр 1" else "Partner 1",
                                style = MaterialTheme.typography.labelMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (selectedPartner1 != null) {
                                val p1 = selectedPartner1!!
                                val genderLetter = when(p1.gender.lowercase()) {
                                    "male", "мужской", "м" -> if (currentLang == AppLanguage.RUS) "М" else "M"
                                    "female", "женский", "ж" -> if (currentLang == AppLanguage.RUS) "Ж" else "F"
                                    else -> if (currentLang == AppLanguage.RUS) "Д" else "O"
                                }
                                val birthYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - p1.age
                                Text(
                                    text = p1.name,
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$birthYear г.р. • ${p1.height}см • $genderLetter",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { selectedPartner1 = null },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFCF6679))
                                ) {
                                    Text(if (currentLang == AppLanguage.RUS) "Удалить" else "Remove", fontSize = 11.sp)
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Выбрать" else "Select",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }
                        }
                    }

                    // Slot 2 card
                    val slot2Selected = selectedPartner2 != null
                    val isSlot2Active = activeSelectionSlot == 2
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSlot2Active) MysticGold.copy(0.1f) else Color.White.copy(0.1f)
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            color = if (isSlot2Active) MysticGold else if (slot2Selected) MysticGold.copy(0.4f) else Color.White.copy(0.1f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                if (selectedPartner1 == null) {
                                    Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Сначала выберите Партнёра 1" else "Select Partner 1 first", Toast.LENGTH_SHORT).show()
                                } else {
                                    activeSelectionSlot = 2 
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Партнёр 2" else "Partner 2",
                                style = MaterialTheme.typography.labelMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (selectedPartner2 != null) {
                                val p2 = selectedPartner2!!
                                val genderLetter = when(p2.gender.lowercase()) {
                                    "male", "мужской", "м" -> if (currentLang == AppLanguage.RUS) "М" else "M"
                                    "female", "женский", "ж" -> if (currentLang == AppLanguage.RUS) "Ж" else "F"
                                    else -> if (currentLang == AppLanguage.RUS) "Д" else "O"
                                }
                                val birthYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - p2.age
                                Text(
                                    text = p2.name,
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$birthYear г.р. • ${p2.height}см • $genderLetter",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { selectedPartner2 = null },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFCF6679))
                                ) {
                                    Text(if (currentLang == AppLanguage.RUS) "Удалить" else "Remove", fontSize = 11.sp)
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (selectedPartner1 == null) Color.DarkGray else Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Выбрать" else "Select",
                                    style = MaterialTheme.typography.labelSmall.copy(color = if (selectedPartner1 == null) Color.DarkGray else Color.Gray)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // List of profiles from history
                Text(
                    text = if (activeSelectionSlot == 1) {
                        if (currentLang == AppLanguage.RUS) "Выберите профиль для Партнёра 1:" else "Choose profile for Partner 1:"
                    } else {
                        if (currentLang == AppLanguage.RUS) "Выберите профиль другого пола для Партнёра 2:" else "Choose opposite gender profile for Partner 2:"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(color = MysticBronze, fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (availableChoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.White.copy(0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.RUS) {
                                if (activeSelectionSlot == 2 && selectedPartner1 != null) "Нет профилей противоположного пола" else "История интерпретации пуста"
                            } else {
                                if (activeSelectionSlot == 2 && selectedPartner1 != null) "No opposite gender profiles available" else "No profiles in interpretation history"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableChoices.forEach { record ->
                            val isSelected = (record == selectedPartner1 || record == selectedPartner2)
                            val birthYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - record.age
                            val genderLetter = when(record.gender.lowercase()) {
                                "male", "мужской", "м" -> if (currentLang == AppLanguage.RUS) "М" else "M"
                                "female", "женский", "ж" -> if (currentLang == AppLanguage.RUS) "Ж" else "F"
                                else -> if (currentLang == AppLanguage.RUS) "Д" else "O"
                            }
                            val yrUnit = if (currentLang == AppLanguage.RUS) "г.р." else "y.o.b."
                            val heightUnit = if (currentLang == AppLanguage.RUS) "см" else "cm"
                            val infoText = "${record.name}, $birthYear $yrUnit, ${record.height} $heightUnit, $genderLetter"

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MysticGold.copy(0.15f) else Color(0x22141420)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) MysticGold else Color.White.copy(0.08f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (activeSelectionSlot == 1) {
                                            selectedPartner1 = record
                                            // If same gender as selectedPartner2, clear selectedPartner2
                                            if (selectedPartner2 != null) {
                                                val norm1 = record.gender.lowercase().trim()
                                                val norm2 = selectedPartner2!!.gender.lowercase().trim()
                                                val isMale1 = norm1.startsWith("м") || norm1.startsWith("m")
                                                val isMale2 = norm2.startsWith("м") || norm2.startsWith("m")
                                                if (isMale1 == isMale2) {
                                                    selectedPartner2 = null
                                                }
                                            }
                                            // Auto-advance slot if slot 2 is empty
                                            if (selectedPartner2 == null) {
                                                activeSelectionSlot = 2
                                            }
                                        } else {
                                            if (selectedPartner1 == null) {
                                                Toast.makeText(context, "Select Partner 1 first", Toast.LENGTH_SHORT).show()
                                            } else {
                                                val norm1 = selectedPartner1!!.gender.lowercase().trim()
                                                val norm2 = record.gender.lowercase().trim()
                                                val isMale1 = norm1.startsWith("м") || norm1.startsWith("m")
                                                val isMale2 = norm2.startsWith("м") || norm2.startsWith("m")
                                                if (isMale1 == isMale2) {
                                                    Toast.makeText(context, "Partners must be of opposite genders!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    selectedPartner2 = record
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val imagePath = record.leftPalmPath ?: record.rightPalmPath ?: record.leftBackPath ?: record.rightBackPath
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color.Black.copy(0.4f), CircleShape)
                                            .border(1.dp, MysticGold.copy(0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!imagePath.isNullOrEmpty()) {
                                            Image(
                                                painter = rememberAsyncImagePainter(imagePath),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.PanTool,
                                                contentDescription = null,
                                                tint = MysticGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = infoText,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = if (record.analysisType.contains("char")) {
                                                if (currentLang == AppLanguage.RUS) "Анализ характера" else "Character reading"
                                            } else {
                                                if (currentLang == AppLanguage.RUS) "Анализ судьбы" else "Destiny reading"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                        )
                                    }
                                    
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MysticGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                MysticButton(
                    text = strings.compatAnalyzeBtn,
                    onClick = {
                        val p1 = selectedPartner1
                        val p2 = selectedPartner2
                        if (p1 != null && p2 != null) {
                            val norm1 = p1.gender.lowercase().trim()
                            val norm2 = p2.gender.lowercase().trim()
                            val isMale1 = norm1.startsWith("м") || norm1.startsWith("m")
                            val isMale2 = norm2.startsWith("м") || norm2.startsWith("m")
                            if (isMale1 == isMale2) {
                                Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Партнёры должны быть разнополыми!" else "Partners must be of opposite genders!", Toast.LENGTH_SHORT).show()
                                return@MysticButton
                            }
                            
                            // Check compatibility price (250 rubles item)
                            viewModel.checkFeatureUnlocked("compatibility") { unlocked ->
                                if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                                    val b1 = if (!p1.leftPalmPath.isNullOrEmpty()) BitmapUtils.uriToBitmap(context, Uri.parse(p1.leftPalmPath)) else null
                                    val b2 = if (!p2.leftPalmPath.isNullOrEmpty()) BitmapUtils.uriToBitmap(context, Uri.parse(p2.leftPalmPath)) else null
                                    partnerName = p2.name
                                    viewModel.runCompatibilityAnalysis(b1, b2, p2.name, onNavigateToLoading)
                                } else {
                                    onNavigateToBilling()
                                }
                            }
                        } else {
                            Toast.makeText(context, if (currentLang == AppLanguage.RUS) "Пожалуйста, выберите обоих партнёров" else "Please select both partners", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = (selectedPartner1 != null && selectedPartner2 != null),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // --- RESULTS COMPATIBILITY PRESENTATION ---
                Spacer(modifier = Modifier.height(16.dp))

                val plainTextOfReport = remember(compatReport, partnerName, currentLang) {
                    buildCompatibilityPlainText(compatReport, partnerName, currentLang)
                }

                val annotatedReportText = buildCompatibilityAnnotatedString(
                    plainText = plainTextOfReport,
                    spokenWordRange = spokenWordRange
                )

                // Ring percentage affinity display
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { compatReport.compatibilityPercent.toFloat() / 100f },
                        modifier = Modifier.size(160.dp),
                        color = MysticGold,
                        strokeWidth = 10.dp,
                        trackColor = MysticBronze.copy(0.2f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${compatReport.compatibilityPercent}%",
                            style = MaterialTheme.typography.displayLarge.copy(color = Color.White),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.compatPercentLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(color = MysticGold, letterSpacing = 1.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Copy to Clipboard and Voice Controller Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Compatibility Report", plainTextOfReport)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Текст скопирован в буфер обмена!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Copy", tint = MysticGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Скопировать", color = Color.White, fontSize = 12.sp)
                    }

                    TtsVoiceController(
                        isPlaying = isPlayingTts,
                        onPlayToggle = {
                            if (isPlayingTts) {
                                ttsByLocalRef?.stop()
                                isPlayingTts = false
                            } else {
                                speakTextFromIndex(plainTextOfReport, lastPlaybackIndex)
                            }
                        },
                        rate = ttsRateState,
                        onRateChange = { newRate ->
                            viewModel.changeTtsSpeechRate(newRate)
                            ttsByLocalRef?.setSpeechRate(newRate)
                            if (isPlayingTts) {
                                val currentWordStart = spokenWordRange?.first ?: 0
                                speakTextFromIndex(plainTextOfReport, currentWordStart)
                            }
                        },
                        gender = ttsGenderState,
                        onGenderChange = { newGender ->
                            viewModel.changeTtsGender(newGender)
                            applyTtsSettings()
                            if (isPlayingTts) {
                                val currentWordStart = spokenWordRange?.first ?: 0
                                speakTextFromIndex(plainTextOfReport, currentWordStart)
                            }
                        },
                        maleVoices = maleVoicesList,
                        femaleVoices = femaleVoicesList,
                        selectedVoice = selectedVoice,
                        onVoiceSelected = { voice ->
                            val index = if (ttsGenderState == "Female") {
                                femaleVoicesList.indexOf(voice)
                            } else {
                                maleVoicesList.indexOf(voice)
                            }
                            if (index >= 0) {
                                viewModel.changeTtsVoiceIndex(index)
                            }
                            applyTtsSettings()
                            if (isPlayingTts) {
                                val currentWordStart = spokenWordRange?.first ?: 0
                                speakTextFromIndex(plainTextOfReport, currentWordStart)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Unified beautiful reading card with text highlighting
                MysticCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = annotatedReportText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }

                ProjectSupportSection(viewModel = viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                MysticButton(
                    text = "Заново / Reset",
                    onClick = { 
                        ttsByLocalRef?.stop()
                        isPlayingTts = false
                        spokenWordRange = null
                        lastPlaybackIndex = 0
                        viewModel.currentCompatibilityReading.value = null 
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


// --- SCREEN 7.5: USER CABINET VIEW ---

@Composable
fun UserCabinetScreen(
    viewModel: PalmistViewModel,
    onNavigateToResult: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val profile by viewModel.userProfile.collectAsState()
    val billingStateVal by viewModel.billingState.collectAsState()
    val readings by viewModel.allReadings.collectAsState()
    val payments by viewModel.allPayments.collectAsState()

    val count = billingStateVal?.remainingAnalyses ?: 0
    val userName = profile?.name?.trim()?.ifBlank { null } ?: (if (currentLang == AppLanguage.RUS) "Искатель" else "Seeker")

    var selectedTab by remember { mutableStateOf(0) } // 0 - История, 1 - Оплата, 2 - Поделиться

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Карточка профиля пользователя
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
                border = BorderStroke(1.dp, MysticGold.copy(0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "User Avatar",
                        tint = MysticGold,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Stars,
                                contentDescription = "Analyses Count",
                                tint = MysticBronze,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS) 
                                    "Доступно анализов: $count" 
                                else "Analyses available: $count",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MysticGold,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Переключатель вкладок (Кастомный TabRow в мистическом стиле)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MysticGold,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MysticGold,
                        height = 2.dp
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { 
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "История" else "History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ) 
                    },
                    selectedContentColor = MysticGold,
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Оплаты" else "Payments",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ) 
                    },
                    selectedContentColor = MysticGold,
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { 
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Поделиться" else "Share",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ) 
                    },
                    selectedContentColor = MysticGold,
                    unselectedContentColor = Color.Gray
                )
            }

            // Содержимое вкладок
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> {
                        // Вкладка: История анализов
                        if (readings.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = strings.histNoRecords,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(readings) { record ->
                                        ReadingHistoryItem(
                                            record = record,
                                            currentLang = currentLang,
                                            onOpen = {
                                                viewModel.currentReading.value = record
                                                onNavigateToResult()
                                            },
                                            onDelete = {
                                                viewModel.deleteReading(record.id)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                MysticButton(
                                    text = strings.histClearHistory,
                                    onClick = { viewModel.clearHistory() },
                                    isSecondary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    1 -> {
                        // Вкладка: История оплаты
                        if (payments.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CreditCard,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.RUS)
                                            "История платежей пуста.\nПоддержите проект в разделе оплаты!"
                                        else "Payment history is empty.\nSupport us in the billing section!",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(payments) { payment ->
                                        PaymentHistoryItem(payment = payment, currentLang = currentLang)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                MysticButton(
                                    text = if (currentLang == AppLanguage.RUS) "Очистить историю оплат" else "Clear payment history",
                                    onClick = { viewModel.clearPaymentHistory() },
                                    isSecondary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    2 -> {
                        // Вкладка: Поделиться приложением
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Bonus icon",
                                tint = MysticGold,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Подарите друзьям Хироманта!" else "Gift Palmist to your friends!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = MysticGold,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS)
                                    "Поделитесь ссылкой на приложение. После успешной установки другом вы получите +3 дополнительных полных анализа судьбы!"
                                else "Share the app. You will receive +3 free full palm analyses once your friend successfully installs and launches the application!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(30.dp))

                            // Кнопка: Поделиться (Отправить ссылку)
                            MysticButton(
                                text = if (currentLang == AppLanguage.RUS) "ОТПРАВИТЬ ССЫЛКУ / APK" else "SHARE APP / APK",
                                onClick = {
                                    try {
                                        val shareText = if (currentLang == AppLanguage.RUS) {
                                            "Привет! Сканируй ладонь и раскрой свои тайны в приложении 'Хиромант'! Вот ссылка на установку APK: https://hiromant-app.ru/download/palmist.apk"
                                        } else {
                                            "Hi! Scan your palm and uncover your destiny with the Palmist app! Install APK from here: https://hiromant-app.ru/download/palmist.apk"
                                        }
                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Поделиться"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Кнопка: Симуляция установки другом (ДЛЯ ТЕСТИРОВАНИЯ)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x33D4AF37)),
                                border = BorderStroke(1.2.dp, MysticGold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (currentLang == AppLanguage.RUS) "РЕЖИМ ПРОВЕРКИ / ТЕСТИРОВАНИЯ" else "TESTING MODE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MysticGold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.RUS)
                                            "Нажмите кнопку ниже, чтобы сымитировать, будто ваш друг скачал и успешно установил приложение по вашей ссылке."
                                        else "Click the button below to simulate that your friend successfully downloaded and installed the app.",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.rewardUserForSharing()
                                            Toast.makeText(
                                                context,
                                                if (currentLang == AppLanguage.RUS)
                                                    "Ура! Друг установил приложение. Начислено +3 анализа в БД!"
                                                else "Success! Friend installed the app. +3 analyses awarded!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MysticGold)
                                    ) {
                                        Text(
                                            text = if (currentLang == AppLanguage.RUS) "Сымитировать установку" else "Simulate Installation",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentHistoryItem(
    payment: com.aistudio.hiromant.kxsrwa.data.local.PaymentHistoryEntity,
    currentLang: AppLanguage
) {
    val df = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(payment.timestamp) { df.format(Date(payment.timestamp)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33141420)),
        border = BorderStroke(1.dp, MysticBronze.copy(0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = MysticGold,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.readingType,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$formattedDate • ${payment.paymentSystem}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${payment.amountRub} ₽",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MysticGold,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}


// --- SCREEN 8: HISTORY VIEW ---

@Composable
fun HistoryScreen(
    viewModel: PalmistViewModel,
    onNavigateToResult: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val readings by viewModel.allReadings.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val maxWidthDp = maxWidth
                val calculatedSize = (maxWidthDp.value * 0.065f).coerceIn(18f, 26f).sp
                Text(
                    text = strings.histTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MysticGold,
                        fontSize = calculatedSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (readings.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    Text(
                        text = strings.histNoRecords,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(readings) { record ->
                        ReadingHistoryItem(
                            record = record,
                            currentLang = currentLang,
                            onOpen = {
                                viewModel.currentReading.value = record
                                onNavigateToResult()
                            },
                            onDelete = {
                                viewModel.deleteReading(record.id)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MysticButton(
                    text = strings.histClearHistory,
                    onClick = { viewModel.clearHistory() },
                    isSecondary = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ReadingHistoryItem(
    record: ReadingEntity,
    currentLang: AppLanguage,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val df = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(record.timestamp) { df.format(Date(record.timestamp)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33141420)),
        border = BorderStroke(1.dp, MysticGold.copy(0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (record.analysisType == "compatibility") Icons.Default.Favorite else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MysticGold,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val birthYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - record.age
                val genderLetter = when(record.gender.lowercase()) {
                    "male", "мужской", "м" -> if (currentLang == AppLanguage.RUS) "М" else "M"
                    "female", "женский", "ж" -> if (currentLang == AppLanguage.RUS) "Ж" else "F"
                    else -> if (currentLang == AppLanguage.RUS) "Д" else "O"
                }
                val infoText = if (record.analysisType == "compatibility") {
                    if (currentLang == AppLanguage.RUS) "Совместимость c ${record.partnerName}" else "Compatibility with ${record.partnerName}"
                } else {
                    val yrUnit = if (currentLang == AppLanguage.RUS) "г.р." else "y.o.b."
                    val heightUnit = if (currentLang == AppLanguage.RUS) "см" else "cm"
                    "${record.name}, $birthYear $yrUnit, ${record.height} $heightUnit, $genderLetter"
                }
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFCF6679)
                )
            }
        }
    }
}


// --- SCREEN 9: ABOUT & EDUCATIONAL FAQ SCREEN ---

@Composable
fun AboutScreen(
    viewModel: PalmistViewModel
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val appVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "1.0"
        }
    }

    var activeSubTab by remember { mutableStateOf("theory") } // "theory", "faq", "contacts"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            MysticHeader(strings.aboutTitle)

            // Sub Tabs
            TabRow(
                selectedTabIndex = when (activeSubTab) {
                    "theory" -> 0
                    "faq" -> 1
                    else -> 2
                },
                containerColor = Color.Transparent,
                contentColor = MysticGold,
                indicator = { tabPositions ->
                    val idx = when (activeSubTab) {
                        "theory" -> 0
                        "faq" -> 1
                        else -> 2
                    }
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[idx]),
                        color = MysticGold
                    )
                }
            ) {
                Tab(
                    selected = activeSubTab == "theory",
                    onClick = { activeSubTab = "theory" },
                    text = { Text(strings.aboutTabInfo, style = MaterialTheme.typography.labelLarge) }
                )
                Tab(
                    selected = activeSubTab == "faq",
                    onClick = { activeSubTab = "faq" },
                    text = { Text(strings.aboutTabFaq, style = MaterialTheme.typography.labelLarge) }
                )
                Tab(
                    selected = activeSubTab == "contacts",
                    onClick = { activeSubTab = "contacts" },
                    text = { Text(strings.aboutTabContacts, style = MaterialTheme.typography.labelLarge) }
                )
            }

            // Scrollable contents
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                when (activeSubTab) {
                    "theory" -> {
                        Text(
                            text = strings.aboutHistoryPalmist,
                            style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.aboutHistoryText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = strings.aboutTheoryLines,
                            style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.aboutTheoryText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    "faq" -> {
                        listOf(
                            "Как сделать качественный снимок?" to "Положите ладонь ровно, раздвинув пальцы на плоском однотонном фоне при ярком естественном или искусственном освещении. Рядом можно положить кредитную карту для точной калибровки размеров.",
                            "Почему анализ занимает время?" to "Мистические алгоритмы Gemini прочитывают десятки параметров руки, включая форму ногтей, длину пальцев и холмы, формируя глубокий персонализированный отчёт.",
                            "Чем отличается краткий от полного анализа?" to "Краткий даёт сжатые выводы по четырём ключевым линиям. Полный включает подробнейшую трактовку бугров, фаланг, знаков судьбы, будущих прогнозов и любовной сферы.",
                            "Насколько точны прогнозы?" to "Хиромантия — это зеркало вашей души. Линии меняются в зависимости от ваших решений, поэтому приложение предоставляет руководство и духовные ориентиры."
                        ).forEach { (q, a) ->
                            FaqItem(q, a)
                        }
                    }
                    "contacts" -> {
                        Text(
                            text = strings.aboutEmailSupport,
                            style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Support donation card with simulated YooKassa/SberPay
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x22D4AF37)),
                            border = BorderStroke(1.dp, MysticGold.copy(0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = strings.aboutDonateTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.aboutDonateDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                MysticButton(
                                    text = strings.aboutSupportBtn,
                                    onClick = {
                                        Toast.makeText(context, strings.aboutSupportSuccess, Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = strings.aboutPrivacyPolicy,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Privacy Policy: All photos are analyzed securely and deleted instantly.", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                val appFullDisplayName = if (currentLang == AppLanguage.RUS) {
                    "Хиромант $appVersionName"
                } else {
                    "Hiromant $appVersionName"
                }
                Text(
                    text = appFullDisplayName,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray.copy(alpha = 0.6f)),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
        border = BorderStroke(0.5.dp, MysticBronze.copy(0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.labelLarge.copy(color = MysticGold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MysticGold
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFC0C0D0)
                )
            }
        }
    }
}


// --- SCREEN 10: SETTINGS ---

@Composable
fun SettingsScreen(
    viewModel: PalmistViewModel,
    onNavigateToLanguage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val billingState by viewModel.billingState.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()

    val ttsGenderState by viewModel.ttsGender.collectAsState()
    val ttsVoiceIndex by viewModel.ttsVoiceIndex.collectAsState()
    val ttsRateState by viewModel.ttsSpeechRate.collectAsState()
    val ttsPitchState by viewModel.ttsPitch.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()

    var aboutProgramExpanded by remember { mutableStateOf(false) }
    var aboutDevExpanded by remember { mutableStateOf(false) }
    var voiceSettingsExpanded by remember { mutableStateOf(false) }

    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isPlayingTts by remember { mutableStateOf(false) }
    var isSpeakingTest by remember { mutableStateOf(false) }
    var currentWordRange by remember { mutableStateOf<IntRange?>(null) }
    var activeSpeakingText by remember { mutableStateOf("") } // "program" or "dev"

    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    val aboutProgramText = if (currentLang == AppLanguage.RUS) {
        "Программа «Хиромант» — это ваш персональный проводник в мир древних знаний о ладонях. С помощью современных алгоритмов искусственного интеллекта и нейросетей Gemini, приложение анализирует форму рук, пальцев и переплетение линий на ладони, сопоставляя их с канонами классической ведической и западной хиромантии. Программа считывает холмы планет, особые знаки (такие как Мистический Крест или Кольцо Соломона) и линии сердца, головы, жизни и судьбы, чтобы раскрыть ваш врождённый потенциал и дать практические советы на жизненном пути.\n\nВерсия 1.002"
    } else {
        "The 'Palmist' app is your personal guide to the ancient wisdom of palm reading. Powered by modern Gemini AI algorithms, the app analyzes your hand shape, finger proportions, and palm line networks, mapping them to the canons of classic Vedic and Western palmistry. It reads planetary mounts, sacred markings (like the Mystic Cross or Ring of Solomon), and the primary lines of Heart, Head, Life, and Destiny to unlock your innate potential and deliver actionable life guidelines.\n\nVersion 1.002"
    }

    val aboutDevText = if (currentLang == AppLanguage.RUS) {
        "Разработчик: Максим Арс. (ArsMaxim)\nКонтакты: arsmaxim@gmail.com\n\nЯ увлечён созданием интеллектуальных, красивых и полезных мобильных приложений, которые объединяют современные технологии ИИ и классическое наследие человечества. Спасибо, что выбрали моё приложение!"
    } else {
        "Developer: Maxim Ars. (ArsMaxim)\nContact: arsmaxim@gmail.com\n\nI am passionate about creating smart, beautiful, and helpful mobile applications that merge cutting-edge AI technologies with classical human heritage. Thank you for choosing my app!"
    }

    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = if (currentLang == AppLanguage.RUS) java.util.Locale("ru") else java.util.Locale.US
            }
        }
        
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isPlayingTts = true
                if (utteranceId == "voice_test_utt") {
                    isSpeakingTest = true
                }
            }

            override fun onDone(utteranceId: String?) {
                isPlayingTts = false
                currentWordRange = null
                activeSpeakingText = ""
                isSpeakingTest = false
            }

            override fun onError(utteranceId: String?) {
                isPlayingTts = false
                currentWordRange = null
                activeSpeakingText = ""
                isSpeakingTest = false
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                if (utteranceId != "voice_test_utt") {
                    currentWordRange = start..end
                }
            }
        })
        
        ttsInstance = tts
        
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val annotatedAboutProgram = remember(currentWordRange, activeSpeakingText, currentLang) {
        buildAnnotatedString {
            append(aboutProgramText)
            
            if (activeSpeakingText == "program" && currentWordRange != null) {
                val start = currentWordRange!!.first
                val end = currentWordRange!!.last
                if (start in 0..length && end in start..length) {
                    addStyle(
                        style = SpanStyle(
                            background = MysticGold.copy(alpha = 0.4f),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        ),
                        start = start,
                        end = end
                    )
                }
            }
        }
    }

    val annotatedAboutDev = remember(currentWordRange, activeSpeakingText, currentLang) {
        buildAnnotatedString {
            append(aboutDevText)
            
            val emailStr = "arsmaxim@gmail.com"
            val emailIndex = aboutDevText.indexOf(emailStr)
            if (emailIndex != -1) {
                addStyle(
                    style = SpanStyle(
                        color = MysticGold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        fontWeight = FontWeight.Bold
                    ),
                    start = emailIndex,
                    end = emailIndex + emailStr.length
                )
            }
            
            if (activeSpeakingText == "dev" && currentWordRange != null) {
                val start = currentWordRange!!.first
                val end = currentWordRange!!.last
                if (start in 0..length && end in start..length) {
                    addStyle(
                        style = SpanStyle(
                            background = MysticGold.copy(alpha = 0.4f),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        ),
                        start = start,
                        end = end
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MysticGold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.settTitle,
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = MysticGold,
                        fontSize = 32.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Choose language row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                border = BorderStroke(1.dp, MysticBronze.copy(0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToLanguage)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = strings.settLanguage, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    val flagEmoji = if (currentLang == AppLanguage.RUS) "🇷🇺" else "🇬🇧"
                    val langText = if (currentLang == AppLanguage.RUS) "Русский" else "English"
                    
                    Text(text = flagEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = langText, color = MysticGold, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MysticGold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Font scale row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                border = BorderStroke(1.dp, MysticBronze.copy(0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Шрифт" else "Font",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (fontScale > 0.85f) viewModel.changeFontScale(fontScale - 0.1f) }
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = MysticGold)
                        }
                        Text(
                            text = "${(fontScale * 100).toInt()}%",
                            color = MysticGold,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { if (fontScale < 1.55f) viewModel.changeFontScale(fontScale + 0.1f) }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = MysticGold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable card: "Голос"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                border = BorderStroke(1.dp, MysticBronze.copy(0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { voiceSettingsExpanded = !voiceSettingsExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MysticGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Голос" else "Voice",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = if (voiceSettingsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MysticGold
                        )
                    }
                    if (voiceSettingsExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Global toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Автоматическое чтение" else "Auto-reading",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Озвучивать результаты анализа при открытии" else "Read analysis results aloud on open",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = ttsEnabled,
                                onCheckedChange = { viewModel.changeTtsEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MysticGold,
                                    checkedTrackColor = MysticGold.copy(0.4f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Gender
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Пол голоса" else "Voice Gender",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Female
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (ttsGenderState == "Female") MysticGold else Color.White.copy(0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (ttsGenderState == "Female") MysticGold else MysticBronze.copy(0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.changeTtsGender("Female") }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Женский" else "Female",
                                    color = if (ttsGenderState == "Female") Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Male
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (ttsGenderState == "Male") MysticGold else Color.White.copy(0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (ttsGenderState == "Male") MysticGold else MysticBronze.copy(0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.changeTtsGender("Male") }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Мужской" else "Male",
                                    color = if (ttsGenderState == "Male") Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Select Voice Interpreter
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Выберите исполнителя (Голос)" else "Select Interpreter (Voice)",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val voiceOptions = if (ttsGenderState == "Female") {
                            if (currentLang == AppLanguage.RUS) listOf(
                                "Голос 1: Стандартный (Чистый тембр)",
                                "Голос 2: Нежный (Успокаивающий)",
                                "Голос 3: Звонкий (Энергичный)"
                            ) else listOf(
                                "Voice 1: Standard (Clear timbre)",
                                "Voice 2: Gentle (Soothing)",
                                "Voice 3: Vibrant (Energetic)"
                            )
                        } else {
                            if (currentLang == AppLanguage.RUS) listOf(
                                "Голос 1: Стандартный (Авторитетный)",
                                "Голос 2: Бархатный (Глубокий)",
                                "Голос 3: Четкий (Уверенный)"
                            ) else listOf(
                                "Voice 1: Standard (Authoritative)",
                                "Voice 2: Velvet (Deep)",
                                "Voice 3: Crisp (Confident)"
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            voiceOptions.forEachIndexed { idx, optionName ->
                                val isSelected = ttsVoiceIndex == idx
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MysticGold.copy(0.12f) else Color.White.copy(0.03f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MysticGold else Color.White.copy(0.08f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.changeTtsVoiceIndex(idx) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .border(2.dp, if (isSelected) MysticGold else Color.Gray, androidx.compose.foundation.shape.CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(MysticGold, androidx.compose.foundation.shape.CircleShape)
                                            )
                                        }
                                    }

                                    Text(
                                        text = optionName,
                                        color = if (isSelected) MysticGold else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Speech Rate
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Скорость речи" else "Speech Rate",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = String.format("%.1fx", ttsRateState),
                                style = MaterialTheme.typography.titleSmall,
                                color = MysticGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = ttsRateState,
                            onValueChange = { viewModel.changeTtsSpeechRate(it) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MysticGold,
                                activeTrackColor = MysticGold,
                                inactiveTrackColor = MysticBronze.copy(0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. Voice Pitch
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Тон голоса" else "Voice Pitch",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = String.format("%.1fx", ttsPitchState),
                                style = MaterialTheme.typography.titleSmall,
                                color = MysticGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = ttsPitchState,
                            onValueChange = { viewModel.changeTtsPitch(it) },
                            valueRange = 0.5f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = MysticGold,
                                activeTrackColor = MysticGold,
                                inactiveTrackColor = MysticBronze.copy(0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 6. Voice Test Button
                        MysticButton(
                            text = if (isSpeakingTest) {
                                if (currentLang == AppLanguage.RUS) "ОСТАНОВИТЬ ТЕСТ" else "STOP TEST"
                            } else {
                                if (currentLang == AppLanguage.RUS) "ПРОВЕРИТЬ ОЗВУЧИВАНИЕ" else "TEST VOICE SYNTHESIS"
                            },
                            onClick = {
                                if (isSpeakingTest) {
                                    ttsInstance?.stop()
                                    isSpeakingTest = false
                                } else {
                                    ttsInstance?.stop()
                                    configureTtsVoice(
                                        tts = ttsInstance,
                                        currentLang = currentLang,
                                        voiceGender = ttsGenderState,
                                        voiceIndex = ttsVoiceIndex,
                                        speechRate = ttsRateState,
                                        speechPitch = ttsPitchState
                                    )

                                    val phrase = if (currentLang == AppLanguage.RUS) {
                                        "Здравствуйте! Я ваш персональный хиромант. Озвучивание настроено и готово к работе."
                                    } else {
                                        "Hello! I am your personal palmist. Voice synthesis is configured and ready to go."
                                    }
                                    val params = android.os.Bundle().apply {
                                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "voice_test_utt")
                                    }
                                    ttsInstance?.speak(phrase, TextToSpeech.QUEUE_FLUSH, params, "voice_test_utt")
                                    isSpeakingTest = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable card: "О программе"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                border = BorderStroke(1.dp, MysticBronze.copy(0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aboutProgramExpanded = !aboutProgramExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = MysticGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "О программе" else "About Program",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = if (aboutProgramExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MysticGold
                        )
                    }
                    if (aboutProgramExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // TTS audio panel
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x1F1E1E2C), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isPlayingTts && activeSpeakingText == "program") {
                                        ttsInstance?.stop()
                                        isPlayingTts = false
                                        currentWordRange = null
                                        activeSpeakingText = ""
                                    } else {
                                        ttsInstance?.stop()
                                        activeSpeakingText = "program"
                                        configureTtsVoice(
                                            tts = ttsInstance,
                                            currentLang = currentLang,
                                            voiceGender = ttsGenderState,
                                            voiceIndex = ttsVoiceIndex,
                                            speechRate = ttsRateState,
                                            speechPitch = ttsPitchState
                                        )
                                        val speakParams = android.os.Bundle().apply {
                                            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "settings_program")
                                        }
                                        ttsInstance?.speak(aboutProgramText, TextToSpeech.QUEUE_FLUSH, speakParams, "settings_program")
                                        isPlayingTts = true
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingTts && activeSpeakingText == "program") Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = MysticGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = if (isPlayingTts && activeSpeakingText == "program") {
                                    if (currentLang == AppLanguage.RUS) "Читает..." else "Reading..."
                                } else {
                                    if (currentLang == AppLanguage.RUS) "Прослушать" else "Listen"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        androidx.compose.foundation.text.selection.SelectionContainer {
                            Text(
                                text = annotatedAboutProgram,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC0C0D0),
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable card: "О разработчике"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                border = BorderStroke(1.dp, MysticBronze.copy(0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aboutDevExpanded = !aboutDevExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MysticGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "О разработчике" else "About Developer",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = if (aboutDevExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MysticGold
                        )
                    }
                    if (aboutDevExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // TTS audio panel with direct click Email fallback icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x1F1E1E2C), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isPlayingTts && activeSpeakingText == "dev") {
                                        ttsInstance?.stop()
                                        isPlayingTts = false
                                        currentWordRange = null
                                        activeSpeakingText = ""
                                    } else {
                                        ttsInstance?.stop()
                                        activeSpeakingText = "dev"
                                        configureTtsVoice(
                                            tts = ttsInstance,
                                            currentLang = currentLang,
                                            voiceGender = ttsGenderState,
                                            voiceIndex = ttsVoiceIndex,
                                            speechRate = ttsRateState,
                                            speechPitch = ttsPitchState
                                        )
                                        val speakParams = android.os.Bundle().apply {
                                            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "settings_dev")
                                        }
                                        ttsInstance?.speak(aboutDevText, TextToSpeech.QUEUE_FLUSH, speakParams, "settings_dev")
                                        isPlayingTts = true
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingTts && activeSpeakingText == "dev") Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = MysticGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = if (isPlayingTts && activeSpeakingText == "dev") {
                                    if (currentLang == AppLanguage.RUS) "Читает..." else "Reading..."
                                } else {
                                    if (currentLang == AppLanguage.RUS) "Прослушать" else "Listen"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = MysticGold, fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Email action button
                            IconButton(
                                onClick = {
                                    try {
                                        uriHandler.openUri("mailto:arsmaxim@gmail.com")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Send Email",
                                    tint = MysticGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                        
                        androidx.compose.foundation.text.selection.SelectionContainer {
                            Text(
                                text = annotatedAboutDev,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC0C0D0),
                                lineHeight = 20.sp,
                                onTextLayout = { textLayoutResult = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(aboutDevText) {
                                        detectTapGestures { offset ->
                                            textLayoutResult?.let { layout ->
                                                val charIndex = layout.getOffsetForPosition(offset)
                                                val emailStr = "arsmaxim@gmail.com"
                                                val emailStart = aboutDevText.indexOf(emailStr)
                                                if (emailStart != -1) {
                                                    val emailEnd = emailStart + emailStr.length
                                                    if (charIndex in emailStart until emailEnd) {
                                                        try {
                                                            uriHandler.openUri("mailto:arsmaxim@gmail.com")
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable card: "Лог операций"
            var operationsLogExpanded by remember { mutableStateOf(false) }
            val logsList by AppLogger.logs.collectAsState()
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                border = BorderStroke(1.dp, MysticBronze.copy(0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { operationsLogExpanded = !operationsLogExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = MysticGold)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Лог операций" else "Operations Log",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Icon(
                            imageVector = if (operationsLogExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MysticGold
                        )
                    }
                    if (operationsLogExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Scrollable console-like logs container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color(0xFF0F0F18), RoundedCornerShape(8.dp))
                                .border(1.dp, MysticBronze.copy(0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val scrollState = rememberScrollState()
                            
                            // Automatically scroll to the bottom when new logs are added
                            LaunchedEffect(logsList.size) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            ) {
                                if (logsList.isEmpty()) {
                                    Text(
                                        text = if (currentLang == AppLanguage.RUS) "Лог пуст..." else "Log is empty...",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    logsList.forEach { entry ->
                                        val color = when (entry.level) {
                                            "E" -> Color(0xFFCF6679) // Soft red for errors
                                            "W" -> Color(0xFFFFB300) // Soft orange/amber for warnings
                                            "D" -> Color(0xFF4CAF50) // Green for debug
                                            else -> Color(0xFFD4AF37) // Mystic Gold for info
                                        }
                                        Text(
                                            text = "[${entry.timeStr}] [${entry.level}/${entry.tag}] ${entry.message}",
                                            color = color,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 14.sp
                                        )
                                        entry.throwable?.let { th ->
                                            val sw = java.io.StringWriter()
                                            th.printStackTrace(java.io.PrintWriter(sw))
                                            Text(
                                                text = sw.toString(),
                                                color = Color(0xFFCF6679).copy(alpha = 0.8f),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // "Send to Developer" button
                            Button(
                                onClick = {
                                    val logText = AppLogger.getLogText()
                                    val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = android.net.Uri.parse("mailto:")
                                        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("arsmaxim@gmail.com"))
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Palmist App Operations Log")
                                        putExtra(android.content.Intent.EXTRA_TEXT, logText)
                                    }
                                    try {
                                        context.startActivity(android.content.Intent.createChooser(emailIntent, "Send Log via Email"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No email client found!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MysticGold),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Отправить" else "Send Email",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // "Copy Log" button
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Palmist Logs", AppLogger.getLogText())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(
                                        context,
                                        if (currentLang == AppLanguage.RUS) "Лог скопирован в буфер!" else "Log copied to clipboard!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33C0C0D0)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Копировать" else "Copy",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // "Clear" button
                            Button(
                                onClick = {
                                    AppLogger.clear()
                                    Toast.makeText(
                                        context,
                                        if (currentLang == AppLanguage.RUS) "Лог очищен!" else "Log cleared!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33CF6679)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFCF6679),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Очистить" else "Clear",
                                    color = Color(0xFFCF6679),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x11D4AF37)),
                border = BorderStroke(1.dp, MysticGold.copy(0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = strings.settSubStatus, style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (billingState?.isPremiumSubscribed == true) strings.settSubActive else strings.settSubInactive,
                        style = MaterialTheme.typography.titleLarge.copy(color = MysticGold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Доступные полные анализы: ${billingState?.remainingAnalyses ?: 0}" else "Available full readings: ${billingState?.remainingAnalyses ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset cache
            MysticButton(
                text = strings.settResetApp,
                onClick = {
                    viewModel.resetApplicationData()
                    Toast.makeText(
                        context,
                        if (currentLang == AppLanguage.RUS) "Данные приложения успешно сброшены!" else "App data reset successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                isSecondary = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delete Account
            TextButton(
                onClick = {
                    viewModel.clearHistory()
                    viewModel.saveProfile("", "", 25, 175, "Right")
                    Toast.makeText(context, "Account reset successfully.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFCF6679)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = strings.settDeleteAcc, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// --- ADDITIONAL SCREEN: BILLING CHECKOUT PANEL (PLAY BILLING / YOOKASSA SIMULATED) ---

@Composable
fun BillingScreen(
    viewModel: PalmistViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val scope = rememberCoroutineScope()
    var isCheckingPayment by remember { mutableStateOf(false) }
    var confirmAttempts by remember { mutableStateOf(0) }

    val initialAmountFromVM by viewModel.paymentAmountToPreselect.collectAsState()
    var walletNum by remember { mutableStateOf("410013630971157") } // User's custom wallet number
    var paymentAmount by remember(initialAmountFromVM) { mutableStateOf(initialAmountFromVM) }
    var chosenMethod by remember { mutableStateOf<String?>(null) } // "yookassa", "google"
    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Ожидание оплаты ЮMoney",
                    color = MysticGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Официальная страница оплаты была открыта в вашем браузере.\n\n" +
                           "После успешного завершения перевода в системе ЮMoney вернитесь сюда и нажмите кнопку 'Подтвердить', чтобы разблокировать сеансы.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isCheckingPayment = true
                            delay(1500)
                            isCheckingPayment = false
                            if (confirmAttempts == 0) {
                                confirmAttempts++
                                Toast.makeText(
                                    context,
                                    if (currentLang == AppLanguage.RUS) "Оплата не подтверждена... Попробуйте ещё раз!" else "Payment not confirmed. Try again!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                showConfirmationDialog = false
                                viewModel.simulateBuySubscription()
                                Toast.makeText(context, strings.billDialogSuccess, Toast.LENGTH_LONG).show()
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold),
                    enabled = !isCheckingPayment
                ) {
                    if (isCheckingPayment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Подтвердить" else "Confirm",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Отмена", color = Color.Gray)
                }
            },
            containerColor = MysticDarkSurface,
            textContentColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Выбор способа оплаты",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MysticGold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            Text(
                text = "(Полный Анализ)",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Google play billing simulated
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (chosenMethod == "google") Color(0x33D4AF37) else Color(0x22141420)),
                border = BorderStroke(1.2.dp, if (chosenMethod == "google") MysticGold else MysticBronze.copy(0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { chosenMethod = "google" }
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = MysticGold, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(strings.billDialogGooglePlay, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // YooKassa / YooMoney
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (chosenMethod == "yookassa") Color(0x33D4AF37) else Color(0x22141420)),
                border = BorderStroke(1.2.dp, if (chosenMethod == "yookassa") MysticGold else MysticBronze.copy(0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { chosenMethod = "yookassa" }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = MysticGold, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "ЮMoney / ЮKassa (Карты/СБП)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    if (chosenMethod == "yookassa") {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Введите номер кошелька ЮMoney получателя:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        MysticTextField(
                            value = walletNum,
                            onValueChange = { },
                            label = "Номер кошелька ЮMoney",
                            placeholder = "41001xxxxxxxxxx",
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val annotatedString = androidx.compose.ui.text.buildAnnotatedString { append(walletNum) }
                                        clipboardManager.setText(annotatedString)
                                        Toast.makeText(context, "Номер кошелька скопирован!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Wallet ID",
                                        tint = MysticGold
                                    )
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Выберите сумму перевода (рубли):",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 3 кнопки суммы: 250 р.  500 р. 1000 р. согласно требованию пользователя
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("250", "500", "1000").forEach { valAmount ->
                                val isSelected = paymentAmount == valAmount
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) MysticGold else Color.White.copy(0.05f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) MysticGold else MysticBronze.copy(0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { 
                                            paymentAmount = valAmount // Выбираем фиксированную сумму при клике
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$valAmount р.", // Отображаем текст суммы в формате "X р." по заданию
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Надпись "Поддержать проект" под кнопками сумм
                        Text(
                            text = "Поддержать проект",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MysticGold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Поле ввода под надписью "Поддержать проект" с текстом "Введите любую сумму..." внутри
                        MysticTextField(
                            value = paymentAmount,
                            onValueChange = { newVal ->
                                // Оставляем только числовые символы для ввода суммы
                                val digits = newVal.filter { it.isDigit() }
                                paymentAmount = digits
                            },
                            label = "Сумма поддержки", // Название поля для доступности
                            placeholder = "Введите любую сумму...", // Текст подсказки внутри поля ввода по заданию
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MysticButton(
                    text = strings.cancel,
                    onClick = onNavigateBack,
                    isSecondary = true,
                    modifier = Modifier.weight(1f)
                )

                MysticButton(
                    text = "ОПЛАТИТЬ",
                    onClick = {
                        if (chosenMethod == "google") {
                            viewModel.simulateBuySubscription()
                            Toast.makeText(context, strings.billDialogSuccess, Toast.LENGTH_LONG).show()
                            onNavigateBack()
                        } else if (chosenMethod == "yookassa") {
                            try {
                                val cleanWallet = walletNum.replace(" ", "").trim()
                                if (cleanWallet.length < 10) {
                                    Toast.makeText(context, "Пожалуйста, введите номер кошелька", Toast.LENGTH_LONG).show()
                                    return@MysticButton
                                }
                                val amountVal = paymentAmount.trim()
                                if (amountVal.isEmpty() || amountVal.toIntOrNull() == null || amountVal.toInt() <= 0) {
                                    Toast.makeText(context, "Пожалуйста, введите корректную сумму", Toast.LENGTH_LONG).show()
                                    return@MysticButton
                                }

                                val targets = "Hiromant App Premium Subscription"
                                val encodedTargets = java.net.URLEncoder.encode(targets, "UTF-8")
                                val url = "https://yoomoney.ru/quickpay/confirm.xml?" +
                                        "receiver=$cleanWallet&" +
                                        "quickpay-form=button&" +
                                        "targets=$encodedTargets&" +
                                        "paymentType=AC&" +
                                        "sum=$amountVal"
                                
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                context.startActivity(intent)
                                showConfirmationDialog = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Ошибка открытия: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = chosenMethod != null
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun YookassaPaymentForm(
    readingId: Long,
    analysisType: String,
    viewModel: PalmistViewModel,
    onSuccess: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)
    val scope = rememberCoroutineScope()

    var walletNum by remember { mutableStateOf("410013630971157") }
    var selectedMethod by remember { mutableStateOf("yoomoney") } // "yoomoney", "ozon", "wb"
    var paymentAmount by remember { mutableStateOf("250") } // Default amount 250 RUB

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isCheckingPayment by remember { mutableStateOf(false) }
    var confirmAttempts by remember { mutableStateOf(0) }

    val actualAmount = paymentAmount

    if (showConfirmationDialog) {
        val dialogTitle = when (selectedMethod) {
            "ozon" -> if (currentLang == AppLanguage.RUS) "Ожидание оплаты Ozon Банк (СБП)" else "Pending Ozon Bank Payment (SBP)"
            "wb" -> if (currentLang == AppLanguage.RUS) "Ожидание оплаты WB Банк (СБП)" else "Pending WB Bank Payment (SBP)"
            else -> if (currentLang == AppLanguage.RUS) "Ожидание оплаты ЮMoney" else "Pending YooMoney Payment"
        }
        val dialogText = when (selectedMethod) {
            "ozon" -> if (currentLang == AppLanguage.RUS) {
                "Была инициализирована оплата через Ozon Банк (СБП) на сумму $actualAmount ₽.\n\nПожалуйста, совершите перевод и нажмите кнопку 'Подтвердить' для активации расшифровки."
            } else {
                "Payment of $actualAmount RUB via Ozon Bank (SBP) was initialized.\n\nPlease complete the transfer and click 'Confirm' to activate decoding."
            }
            "wb" -> if (currentLang == AppLanguage.RUS) {
                "Была инициализирована оплата через WB Банк (СБП) на сумму $actualAmount ₽.\n\nПожалуйста, совершите перевод и нажмите кнопку 'Подтвердить' для активации расшифровки."
            } else {
                "Payment of $actualAmount RUB via WB Bank (SBP) was initialized.\n\nPlease complete the transfer and click 'Confirm' to activate decoding."
            }
            else -> if (currentLang == AppLanguage.RUS) {
                "Официальная страница перевода ЮMoney была открыта в вашем браузере.\n\n" +
                "После успешного завершения перевода в системе ЮMoney вернитесь сюда и нажмите кнопку 'Подтвердить', чтобы получить точнейший анализ вашей судьбы!"
            } else {
                "The official YooMoney page has been opened in your browser.\n\n" +
                "After completing the transaction, return here and tap 'Confirm' to unlock your cosmic destiny analysis!"
            }
        }

        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = dialogTitle,
                    color = MysticGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = dialogText,
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isCheckingPayment = true
                            delay(1500)
                            isCheckingPayment = false
                            if (confirmAttempts == 0) {
                                confirmAttempts++
                                Toast.makeText(
                                    context,
                                    if (currentLang == AppLanguage.RUS) "Оплата не подтверждена... Попробуйте ещё раз!" else "Payment not confirmed. Try again!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                showConfirmationDialog = false
                                viewModel.unlockPaidReading(readingId) {
                                    Toast.makeText(
                                        context,
                                        if (currentLang == AppLanguage.RUS) "Оплата успешно подтверждена! Анализ разблокирован." else "Payment confirmed! Reading unlocked.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onSuccess()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold),
                    enabled = !isCheckingPayment
                ) {
                    if (isCheckingPayment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Подтвердить" else "Confirm",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text(text = strings.cancel, color = Color.Gray)
                }
            },
            containerColor = MysticDarkSurface,
            textContentColor = Color.White
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
        border = BorderStroke(1.5.dp, MysticGold),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MysticGold,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (currentLang == AppLanguage.RUS) {
                    "Активация AI-анализа ладони"
                } else {
                    "AI Palm Analysis Activation"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MysticGold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (currentLang == AppLanguage.RUS) {
                    "Ключ API Gemini временно перегружен или неактивен. Вы можете напрямую оплатить сеанс для моментальной активации пророчества."
                } else {
                    "The Gemini API is currently unavailable. You can pay for this premium session to instantly trigger manual decoding."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            // Main unified payment method label
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MysticGold.copy(0.12f)),
                border = BorderStroke(1.5.dp, MysticGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💳", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "ЮKassa/СПБ" else "YooKassa/SBP",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Прямая безопасная оплата услуг" else "Direct secure gateway payment",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Recipient Wallet
            Text(
                text = if (currentLang == AppLanguage.RUS) {
                    "Введите номер кошелька ЮMoney получателя:"
                } else {
                    "Recipient's YooMoney Wallet ID:"
                },
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            MysticTextField(
                value = walletNum,
                onValueChange = { },
                label = if (currentLang == AppLanguage.RUS) "Кошелёк получателя" else "Receiver Wallet",
                placeholder = "41001xxxxxxxxxx",
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val annotatedString = androidx.compose.ui.text.buildAnnotatedString { append(walletNum) }
                            clipboardManager.setText(annotatedString)
                            Toast.makeText(
                                context,
                                if (currentLang == AppLanguage.RUS) "Номер кошелька скопирован!" else "Wallet number copied!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Wallet ID",
                            tint = MysticGold
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amount selector
            Text(
                text = if (currentLang == AppLanguage.RUS) {
                    "Выберите сумму перевода (рубли):"
                } else {
                    "Select payment amount (RUB):"
                },
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Выбор суммы платежа: 250 р., 500 р., 1000 р.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("250", "500", "1000").forEach { valAmount ->
                    val isSelected = paymentAmount == valAmount
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) MysticGold else Color.White.copy(0.05f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) MysticGold else MysticBronze.copy(0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                paymentAmount = valAmount // Задаем выбранную сумму
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$valAmount р.", // Текст суммы в формате "X р."
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Надпись "Поддержать проект" под кнопками сумм
            Text(
                text = if (currentLang == AppLanguage.RUS) {
                    "Поддержать проект"
                } else {
                    "Support the Project"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MysticGold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Поле ввода с текстом "Введите любую сумму..." внутри
            MysticTextField(
                value = paymentAmount,
                onValueChange = { newVal ->
                    val digits = newVal.filter { it.isDigit() }
                    paymentAmount = digits
                },
                label = if (currentLang == AppLanguage.RUS) "Сумма поддержки" else "Support Amount",
                placeholder = if (currentLang == AppLanguage.RUS) "Введите любую сумму..." else "Enter any amount...",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MysticButton(
                    text = if (currentLang == AppLanguage.RUS) "ОТМЕНА" else "CANCEL",
                    onClick = onClose,
                    isSecondary = true,
                    modifier = Modifier.weight(1f)
                )
                
                MysticButton(
                    text = if (currentLang == AppLanguage.RUS) "ОПЛАТИТЬ" else "PAY NOW",
                    onClick = {
                        try {
                            val cleanWallet = walletNum.replace(" ", "").trim()
                            if (cleanWallet.length < 10) {
                                Toast.makeText(context, "Пожалуйста, введите корректный номер кошелька ЮMoney", Toast.LENGTH_LONG).show()
                                return@MysticButton
                            }
                            val amountVal = actualAmount.trim()
                            if (amountVal.isEmpty() || amountVal.toIntOrNull() == null || amountVal.toInt() <= 0) {
                                Toast.makeText(context, "Пожалуйста, введите корректную сумму (минимум 1 ₽)", Toast.LENGTH_LONG).show()
                                return@MysticButton
                            }
                            
                            if (selectedMethod == "ozon") {
                                Toast.makeText(context, "Перенаправление в Ozon Банк по СБП...", Toast.LENGTH_LONG).show()
                            } else if (selectedMethod == "wb") {
                                Toast.makeText(context, "Перенаправление в WB Банк по СБП...", Toast.LENGTH_LONG).show()
                            }

                            val targets = "Hiromant App Analysis Decoding: $analysisType"
                            val encodedTargets = java.net.URLEncoder.encode(targets, "UTF-8")
                            val url = "https://yoomoney.ru/quickpay/confirm.xml?" +
                                    "receiver=$cleanWallet&" +
                                    "quickpay-form=button&" +
                                    "targets=$encodedTargets&" +
                                    "paymentType=AC&" +
                                    "sum=$amountVal"
                            
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            context.startActivity(intent)
                            showConfirmationDialog = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Не удалось открыть оплату: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
fun PostPaymentVideoScreen(
    viewModel: PalmistViewModel,
    onNavigateToLoading: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val billingState by viewModel.billingState.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var leftVideoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var rightVideoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var activeSlot by remember { mutableStateOf<String?>(null) } // "left" or "right"
    var tempVideoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && activeSlot != null) {
            if (activeSlot == "left") {
                leftVideoUri = tempVideoUri
            } else if (activeSlot == "right") {
                rightVideoUri = tempVideoUri
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && activeSlot != null) {
            if (activeSlot == "left") {
                leftVideoUri = uri
            } else if (activeSlot == "right") {
                rightVideoUri = uri
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createGalleryVideoUri(context, "hand_${activeSlot}_video")
            if (uri != null) {
                tempVideoUri = uri
                videoCaptureLauncher.launch(uri)
            }
        } else {
            val msg = if (currentLang == AppLanguage.RUS) 
                "Требуется разрешение на использование камеры" 
            else "Camera permission required"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MysticGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLang == AppLanguage.RUS) "Запись видео ладоней" else "Record Hand Videos",
                    style = MaterialTheme.typography.titleLarge.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                )
            }
        },
        containerColor = MysticDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful Instruction Banner Card
            MysticCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        contentDescription = null,
                        tint = MysticGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLang == AppLanguage.RUS) 
                            "Для точного полного анализа необходимо загрузить или записать два видео по 1 минуте (для левой и правой руки)."
                        else 
                            "For an accurate full analysis, please record or upload two 1-minute videos (one for each hand).",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, lineHeight = 20.sp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentLang == AppLanguage.RUS)
                            "Плавно ведите камерой по всей ладони, показывая все линии крупным планом."
                        else
                            "Slowly guide the camera across the palm, showing all lines close up.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MysticBronze, lineHeight = 16.sp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LEFT PALM VIDEO CARD
            VideoSlotCard(
                title = if (currentLang == AppLanguage.RUS) "Видео ЛЕВОЙ руки" else "LEFT Hand Video",
                videoUri = leftVideoUri,
                onRecord = {
                    activeSlot = "left"
                    val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (hasCameraPermission) {
                        val uri = createGalleryVideoUri(context, "hand_left_video")
                        if (uri != null) {
                            tempVideoUri = uri
                            videoCaptureLauncher.launch(uri)
                        }
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                onPick = {
                    activeSlot = "left"
                    videoPickerLauncher.launch("video/*")
                },
                onClear = { leftVideoUri = null },
                currentLang = currentLang
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RIGHT PALM VIDEO CARD
            VideoSlotCard(
                title = if (currentLang == AppLanguage.RUS) "Видео ПРАВОЙ руки" else "RIGHT Hand Video",
                videoUri = rightVideoUri,
                onRecord = {
                    activeSlot = "right"
                    val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (hasCameraPermission) {
                        val uri = createGalleryVideoUri(context, "hand_right_video")
                        if (uri != null) {
                            tempVideoUri = uri
                            videoCaptureLauncher.launch(uri)
                        }
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                onPick = {
                    activeSlot = "right"
                    videoPickerLauncher.launch("video/*")
                },
                onClear = { rightVideoUri = null },
                currentLang = currentLang
            )

            Spacer(modifier = Modifier.height(32.dp))

            // "Run Full Analysis" Button
            val bothReady = leftVideoUri != null && rightVideoUri != null
            MysticButton(
                text = if (currentLang == AppLanguage.RUS) "Запустить полный анализ" else "Launch Full Analysis",
                onClick = {
                    if (bothReady) {
                        val leftBmp = viewModel.bitmapLeftPalm.value
                        val leftBackBmp = viewModel.bitmapLeftBack.value
                        val rightBmp = viewModel.bitmapRightPalm.value
                        val rightBackBmp = viewModel.bitmapRightBack.value
                        val bitmaps = listOfNotNull(leftBmp, leftBackBmp, rightBmp, rightBackBmp)

                        viewModel.runPalmAnalysis(
                            bitmaps = bitmaps,
                            videoUri = leftVideoUri?.toString(),
                            analysisType = viewModel.currentAnalysisTypeState.value,
                            leftPalmPath = viewModel.leftPalmPath.value,
                            leftBackPath = viewModel.leftBackPath.value,
                            rightPalmPath = viewModel.rightPalmPath.value,
                            rightBackPath = viewModel.rightBackPath.value,
                            onCompleted = onNavigateToLoading
                        )
                    } else {
                        val msg = if (currentLang == AppLanguage.RUS)
                            "Пожалуйста, запишите или загрузите видео для обеих рук!"
                        else
                            "Please record or upload videos for both hands!"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bothReady
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun VideoSlotCard(
    title: String,
    videoUri: android.net.Uri?,
    onRecord: () -> Unit,
    onPick: () -> Unit,
    onClear: () -> Unit,
    currentLang: AppLanguage
) {
    MysticCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (videoUri != null) MysticGold else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1F1E1E2C))
                    .border(2.dp, if (videoUri != null) MysticGold else MysticBronze.copy(0.3f), RoundedCornerShape(16.dp))
                    .clickable { onRecord() }
            ) {
                if (videoUri != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = null,
                            tint = MysticGold,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Видео успешно добавлено" else "Video added successfully",
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = Color.Red,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = null,
                            tint = MysticBronze.copy(0.6f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Записать видео (1 мин)" else "Record Video (1 min)",
                            style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze, fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MysticButton(
                text = if (currentLang == AppLanguage.RUS) "Загрузить из галереи" else "Upload from gallery",
                onClick = onPick,
                isSecondary = true,
                modifier = Modifier.width(220.dp)
            )
        }
    }
}

fun buildCompatibilityPlainText(
    compatReport: com.aistudio.hiromant.kxsrwa.data.remote.CompatibilityReport,
    partnerName: String,
    currentLang: AppLanguage
): String {
    val sb = java.lang.StringBuilder()
    
    sb.append(if (currentLang == AppLanguage.RUS) "ОТЧЕТ О СОВМЕСТИМОСТИ\n\n" else "COMPATIBILITY REPORT\n\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Личность 1:\n" else "Self Style:\n")
    sb.append(compatReport.partner1Portrait).append("\n\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Личность 2 ($partnerName):\n" else "Partner Style ($partnerName):\n")
    sb.append(compatReport.partner2Portrait).append("\n\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Общий анализ:\n" else "Synergy Analysis:\n")
    sb.append(compatReport.combinedAnalysis).append("\n\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Сильные стороны:\n" else "Strong Points:\n")
    compatReport.strongPoints.forEach { sb.append("- ").append(it).append("\n") }
    sb.append("\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Слабые стороны:\n" else "Weak Points:\n")
    compatReport.weakPoints.forEach { sb.append("- ").append(it).append("\n") }
    sb.append("\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Эмоциональная сфера:\n" else "Emotional sphere:\n")
    sb.append(compatReport.emotionalCompatibility).append("\n\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Интеллектуальная сфера:\n" else "Intellectual sphere:\n")
    sb.append(compatReport.intellectualCompatibility).append("\n\n")
    
    sb.append(if (currentLang == AppLanguage.RUS) "Финансовая сфера:\n" else "Financial sphere:\n")
    sb.append(compatReport.financialCompatibility)
    
    return sb.toString()
}

@Composable
fun buildCompatibilityAnnotatedString(
    plainText: String,
    spokenWordRange: Pair<Int, Int>?
): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        append(plainText)
        if (spokenWordRange != null) {
            val start = spokenWordRange.first.coerceIn(0, plainText.length)
            val end = spokenWordRange.second.coerceIn(0, plainText.length)
            if (start < end) {
                addStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        background = Color.DarkGray,
                        color = Color.White
                    ),
                    start = start,
                    end = end
                )
            }
        }
    }
}



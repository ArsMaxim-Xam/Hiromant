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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Mystical Logo
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MysticGold,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            MysticHeader(strings.langSelectTitle)
            MysticSubtitle(strings.langSelectSubtitle)

            Spacer(modifier = Modifier.height(40.dp))

            // Russia (RUS) flag selection card
            LanguageCard(
                langName = "Русский (RUS)",
                flagEmoji = "🇷🇺",
                isSelected = currentLang == AppLanguage.RUS,
                onClick = { viewModel.changeLanguage(AppLanguage.RUS) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // United Kingdom (ENG) flag selection card
            LanguageCard(
                langName = "English (ENG)",
                flagEmoji = "🇬🇧",
                isSelected = currentLang == AppLanguage.ENG,
                onClick = { viewModel.changeLanguage(AppLanguage.ENG) }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(30.dp))

            MysticButton(
                text = strings.langContinue,
                onClick = onNavigateToSplash,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
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

    val elements = remember {
        listOf(
            // Lines
            AnimatedElementState(
                id = "life_line",
                type = HandElementType.LINE,
                name = "Life Line",
                color = Color(0xFFFF4D4D), // Coral Red
                points = listOf(Pair(0.33f, 0.52f), Pair(0.36f, 0.60f), Pair(0.40f, 0.70f), Pair(0.45f, 0.78f), Pair(0.50f, 0.84f))
            ),
            AnimatedElementState(
                id = "head_line",
                type = HandElementType.LINE,
                name = "Head Line",
                color = Color(0xFF00BFFF), // Cyan Blue
                points = listOf(Pair(0.33f, 0.52f), Pair(0.46f, 0.54f), Pair(0.64f, 0.57f))
            ),
            AnimatedElementState(
                id = "heart_line",
                type = HandElementType.LINE,
                name = "Heart Line",
                color = Color(0xFFFF1493), // Deep Pink
                points = listOf(Pair(0.70f, 0.50f), Pair(0.54f, 0.48f), Pair(0.36f, 0.45f))
            ),
            AnimatedElementState(
                id = "destiny_line",
                type = HandElementType.LINE,
                name = "Destiny Line",
                color = Color(0xFFDA70D6), // Orchid / Light Purple
                points = listOf(Pair(0.50f, 0.85f), Pair(0.50f, 0.65f), Pair(0.49f, 0.41f))
            ),
            // Mounts
            AnimatedElementState(
                id = "mount_jupiter",
                type = HandElementType.MOUNT,
                name = "Mount of Jupiter",
                color = Color(0xFF9370DB), // Medium Purple
                symbol = "♃",
                position = Pair(0.38f, 0.36f)
            ),
            AnimatedElementState(
                id = "mount_saturn",
                type = HandElementType.MOUNT,
                name = "Mount of Saturn",
                color = Color(0xFFFFD700), // Gold
                symbol = "♄",
                position = Pair(0.49f, 0.34f)
            ),
            AnimatedElementState(
                id = "mount_apollo",
                type = HandElementType.MOUNT,
                name = "Mount of Apollo",
                color = Color(0xFFFF8C00), // Dark Orange
                symbol = "☉",
                position = Pair(0.61f, 0.35f)
            ),
            AnimatedElementState(
                id = "mount_mercury",
                type = HandElementType.MOUNT,
                name = "Mount of Mercury",
                color = Color(0xFF00FA9A), // Medium Spring Green
                symbol = "☿",
                position = Pair(0.72f, 0.37f)
            ),
            AnimatedElementState(
                id = "mount_venus",
                type = HandElementType.MOUNT,
                name = "Mount of Venus",
                color = Color(0xFFFF69B4), // Hot Pink
                symbol = "♀",
                position = Pair(0.34f, 0.72f)
            ),
            AnimatedElementState(
                id = "mount_mars_lower",
                type = HandElementType.MOUNT,
                name = "Lower Mars",
                color = Color(0xFFFF0000), // Pure Red
                symbol = "♂",
                position = Pair(0.33f, 0.48f)
            ),
            AnimatedElementState(
                id = "mount_mars_upper",
                type = HandElementType.MOUNT,
                name = "Upper Mars",
                color = Color(0xFFFF4500), // Orange Red
                symbol = "♂",
                position = Pair(0.71f, 0.53f)
            ),
            AnimatedElementState(
                id = "mount_moon",
                type = HandElementType.MOUNT,
                name = "Mount of Moon",
                color = Color(0xFFE6E6FA), // Lavender
                symbol = "☽",
                position = Pair(0.71f, 0.73f)
            )
        )
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = strings.appName.uppercase(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color = MysticGold,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.splashLogoSubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MysticBronze,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 10.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Center: Hand representation (1:1 square ratio centered horizontally/vertically)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = scrollOpened,
                        enter = scaleIn(animationSpec = tween(1200)) + fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(handScale)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(com.aistudio.hiromant.kxsrwa.R.drawable.img_splash_hand)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Realistic Mystic Hand",
                                contentScale = ContentScale.Fit, // Visible COMPLETELY
                                modifier = Modifier.fillMaxSize(),
                                alpha = imageAlpha
                            )
                            
                            // Canvas overlays exactly in 1:1 square coordinates
                            Canvas(
                                modifier = Modifier.fillMaxSize()
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
                            }
                        }
                    }
                }

                // Bottom: "Пропустить заставку" button
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MysticGold.copy(0.4f), RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(0.4f))
                        .clickable { onNavigateNext() }
                        .padding(horizontal = 24.dp, vertical = 10.dp)
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

    val isValidInput = {
        var valid = true
        val trimmedInput = emailOrPhone.trim()
        if (trimmedInput.isEmpty()) {
            emailOrPhoneError = strings.authEmailPhoneError
            valid = false
        } else {
            val isEmail = trimmedInput.contains("@")
            if (isEmail) {
                val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
                if (!emailRegex.matches(trimmedInput)) {
                    emailOrPhoneError = strings.authEmailPhoneError
                    valid = false
                }
            } else {
                val digitsOnly = trimmedInput.replace("[\\s()+-]".toRegex(), "")
                if (digitsOnly.length < 10 || !digitsOnly.all { it.isDigit() }) {
                    emailOrPhoneError = strings.authEmailPhoneError
                    valid = false
                }
            }
        }

        if (password.length < 6) {
            passwordError = strings.authPasswordError
            valid = false
        }

        valid
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            MysticHeader(
                text = strings.authTitle,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            MysticCard {
                Spacer(modifier = Modifier.height(16.dp))

                MysticTextField(
                    value = emailOrPhone,
                    onValueChange = { 
                        emailOrPhone = it
                        emailOrPhoneError = null
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

                if (codeSent) {
                    MysticTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = strings.authSmsEmailCodePlaceholder,
                        placeholder = "123456"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!codeSent) {
                        MysticButton(
                            text = strings.authSendCodeBtn,
                            onClick = {
                                if (isValidInput()) {
                                    codeSent = true
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
                                    // Simulated login
                                    viewModel.saveProfile(
                                        name = "",
                                        gender = "",
                                        age = 25,
                                        height = 175,
                                        dominantHand = "Right",
                                        email = if (emailOrPhone.contains("@")) emailOrPhone else null,
                                        phone = if (!emailOrPhone.contains("@")) emailOrPhone else null,
                                        isRegistered = true
                                    )
                                    onNavigateNext()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
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
            Spacer(modifier = Modifier.height(40.dp))
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

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthYearText by remember { mutableStateOf("1995") }
    var heightText by remember { mutableStateOf("172") }
    var dominantHand by remember { mutableStateOf("Right") }

    var nameError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existingProfile) {
        existingProfile?.let {
            if (it.name.isNotEmpty()) name = it.name
            if (it.gender.isNotEmpty()) gender = it.gender
            birthYearText = (2026 - it.age).toString()
            heightText = it.height.toString()
            dominantHand = it.dominantHand
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            MysticHeader(strings.profileTitle)
            MysticSubtitle(strings.profileSubtitle)

            Spacer(modifier = Modifier.height(20.dp))

            MysticCard {
                Spacer(modifier = Modifier.height(16.dp))

                // Name input
                MysticTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = if (it.trim().length < 2) strings.profileNameError else null
                    },
                    label = strings.profileNameLabel,
                    error = nameError,
                    placeholder = "Александр / Elizabeth"
                )

                // Gender choosing
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = strings.profileGenderLabel,
                        style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            strings.profileGenderMale to "Male",
                            strings.profileGenderFemale to "Female"
                        ).forEach { (label, value) ->
                            val selected = gender == value
                            OutlinedButton(
                                onClick = { gender = value },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (selected) MysticGold else MysticBronze.copy(0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) Color(0x22D4AF37) else Color.Transparent,
                                    contentColor = if (selected) MysticGold else Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }

                // Birth Year Input
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        placeholder = { Text("1995", color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Height Input
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        placeholder = { Text("172", color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Dominant hand selector (ESSENTIAL)
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = strings.profileHandLabel,
                        style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (dominantHand == "Left") strings.profileHandDescLeft else strings.profileHandDescRight,
                        style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

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
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


// --- SCREEN 4: MEDIA UPLOAD & RUN ANALYSES ---

@Composable
fun HandSlotCard(
    title: String,
    bitmap: Bitmap?,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onClear: () -> Unit,
    btnCameraText: String,
    btnGalleryText: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1F1E1E2C)),
        border = BorderStroke(1.dp, if (bitmap != null) MysticGold else MysticBronze.copy(0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Left Side: Image Preview / Icon Placeholder
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x10FFFFFF))
                    .border(1.dp, MysticBronze.copy(0.2f), RoundedCornerShape(12.dp))
            ) {
                if (bitmap != null) {
                    Image(
                        painter = rememberAsyncImagePainter(bitmap),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .background(Color.Black.copy(0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = Color.Red,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MysticBronze.copy(0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Side: Label and Actions
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (bitmap != null) MysticGold else Color.White,
                        fontSize = 15.sp
                    ),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (bitmap == null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Take Photo Button
                        Button(
                            onClick = onTakePhoto,
                            colors = ButtonDefaults.buttonColors(containerColor = MysticBronze.copy(0.2f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
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
                                style = MaterialTheme.typography.labelSmall,
                                color = MysticGold
                            )
                        }

                        // Pick Gallery Button
                        Button(
                            onClick = onPickPhoto,
                            colors = ButtonDefaults.buttonColors(containerColor = MysticBronze.copy(0.2f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
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
                                style = MaterialTheme.typography.labelSmall,
                                color = MysticGold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Готово к анализу",
                        style = MaterialTheme.typography.bodySmall.copy(color = MysticGold)
                    )
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
    onNavigateToBilling: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val billingState by viewModel.billingState.collectAsState()

    var activeSlot by remember { mutableStateOf<String?>(null) }
    var bitmapLeftPalm by remember { mutableStateOf<Bitmap?>(null) }
    var bitmapLeftBack by remember { mutableStateOf<Bitmap?>(null) }
    var bitmapRightPalm by remember { mutableStateOf<Bitmap?>(null) }
    var bitmapRightBack by remember { mutableStateOf<Bitmap?>(null) }

    var videoUri by remember { mutableStateOf<Uri?>(null) }

    // CameraX recording simulated state
    var isRecordingVideo by remember { mutableStateOf(false) }
    var recordingTimeLeft by remember { mutableStateOf(60) }

    // Launchers for media
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val bitmap = BitmapUtils.uriToBitmap(context, it)
                when (activeSlot) {
                    "left_palm" -> bitmapLeftPalm = bitmap
                    "left_back" -> bitmapLeftBack = bitmap
                    "right_palm" -> bitmapRightPalm = bitmap
                    "right_back" -> bitmapRightBack = bitmap
                }
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { videoUri = it }
        }
    )

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            bitmap?.let {
                when (activeSlot) {
                    "left_palm" -> bitmapLeftPalm = it
                    "left_back" -> bitmapLeftBack = it
                    "right_palm" -> bitmapRightPalm = it
                    "right_back" -> bitmapRightBack = it
                }
            }
        }
    )

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(null)
            } else {
                Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Timer simulation for CameraX video recording
    LaunchedEffect(isRecordingVideo, recordingTimeLeft) {
        if (isRecordingVideo && recordingTimeLeft > 0) {
            delay(1000)
            recordingTimeLeft--
            if (recordingTimeLeft == 0) {
                isRecordingVideo = false
                videoUri = Uri.parse("content://palmist/recorded_video")
                Toast.makeText(context, strings.uploadPreviewVideo, Toast.LENGTH_SHORT).show()
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
            MysticHeader(strings.uploadTitle)

            // Warning Guidelines Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x1A000000)),
                border = BorderStroke(1.dp, MysticBronze.copy(0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.uploadGuideHeader,
                        style = MaterialTheme.typography.labelLarge.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.uploadGuideText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFC0C0D0), lineHeight = 18.sp)
                    )
                }
            }

            // Photo upload block (Grid of 4 slots)
            MysticCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = strings.uploadPhotoSection,
                        style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Sequential full-width vertical layout instead of 2x2 grid
                    HandSlotCard(
                        title = strings.slotLeftPalm,
                        bitmap = bitmapLeftPalm,
                        onTakePhoto = {
                            activeSlot = "left_palm"
                            val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onPickPhoto = {
                            activeSlot = "left_palm"
                            photoPickerLauncher.launch("image/*")
                        },
                        onClear = { bitmapLeftPalm = null },
                        btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                        btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HandSlotCard(
                        title = strings.slotLeftBack,
                        bitmap = bitmapLeftBack,
                        onTakePhoto = {
                            activeSlot = "left_back"
                            val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onPickPhoto = {
                            activeSlot = "left_back"
                            photoPickerLauncher.launch("image/*")
                        },
                        onClear = { bitmapLeftBack = null },
                        btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                        btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HandSlotCard(
                        title = strings.slotRightPalm,
                        bitmap = bitmapRightPalm,
                        onTakePhoto = {
                            activeSlot = "right_palm"
                            val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onPickPhoto = {
                            activeSlot = "right_palm"
                            photoPickerLauncher.launch("image/*")
                        },
                        onClear = { bitmapRightPalm = null },
                        btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                        btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HandSlotCard(
                        title = strings.slotRightBack,
                        bitmap = bitmapRightBack,
                        onTakePhoto = {
                            activeSlot = "right_back"
                            val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        onPickPhoto = {
                            activeSlot = "right_back"
                            photoPickerLauncher.launch("image/*")
                        },
                        onClear = { bitmapRightBack = null },
                        btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                        btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                    )
                }
            }

            // Video Upload block (CameraX limited to 60s)
            MysticCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = strings.uploadVideoSection,
                        style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (isRecordingVideo) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.RadioButtonChecked,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(40.dp)
                                    .animateContentSize()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Recording: ${recordingTimeLeft}s",
                                color = Color.Red,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = strings.uploadVideoHint,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MysticBronze,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    } else if (videoUri != null) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = null,
                            tint = MysticGold,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.uploadPreviewVideo,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = null,
                            tint = MysticBronze.copy(0.5f),
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isRecordingVideo) {
                            MysticButton(
                                text = strings.uploadRecordVideo,
                                onClick = {
                                    isRecordingVideo = true
                                    recordingTimeLeft = 60
                                },
                                isSecondary = true,
                                modifier = Modifier.weight(1f)
                            )
                            MysticButton(
                                text = strings.uploadLoadVideo,
                                onClick = { videoPickerLauncher.launch("video/*") },
                                isSecondary = true,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            MysticButton(
                                text = "STOP",
                                onClick = {
                                    isRecordingVideo = false
                                    videoUri = Uri.parse("content://palmist/recorded_video")
                                },
                                isSecondary = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trigger Analysis Buttons with distinct pricing models
            Text(
                text = strings.uploadChooseAnalysisType,
                style = MaterialTheme.typography.titleMedium.copy(color = MysticGold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Trigger buttons
            TriggerAnalysisButton(
                label = strings.btnBriefChar,
                priceText = strings.freeLabel,
                onClick = {
                    val bitmaps = listOfNotNull(bitmapLeftPalm, bitmapLeftBack, bitmapRightPalm, bitmapRightBack)
                    viewModel.runPalmAnalysis(bitmaps, videoUri?.toString(), "brief_char", onNavigateToLoading)
                }
            )

            TriggerAnalysisButton(
                label = strings.btnFullChar,
                priceText = "150 ₽",
                onClick = {
                    viewModel.checkFeatureUnlocked("full_char") { unlocked ->
                        if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                            val bitmaps = listOfNotNull(bitmapLeftPalm, bitmapLeftBack, bitmapRightPalm, bitmapRightBack)
                            viewModel.runPalmAnalysis(bitmaps, videoUri?.toString(), "full_char", onNavigateToLoading)
                        } else {
                            onNavigateToBilling()
                        }
                    }
                }
            )

            TriggerAnalysisButton(
                label = strings.btnBriefPath,
                priceText = strings.freeLabel,
                onClick = {
                    val bitmaps = listOfNotNull(bitmapLeftPalm, bitmapLeftBack, bitmapRightPalm, bitmapRightBack)
                    viewModel.runPalmAnalysis(bitmaps, videoUri?.toString(), "brief_path", onNavigateToLoading)
                }
            )

            TriggerAnalysisButton(
                label = strings.btnFullPath,
                priceText = "150 ₽",
                onClick = {
                    viewModel.checkFeatureUnlocked("full_path") { unlocked ->
                        if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                            val bitmaps = listOfNotNull(bitmapLeftPalm, bitmapLeftBack, bitmapRightPalm, bitmapRightBack)
                            viewModel.runPalmAnalysis(bitmaps, videoUri?.toString(), "full_path", onNavigateToLoading)
                        } else {
                            onNavigateToBilling()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TriggerAnalysisButton(
    label: String,
    priceText: String,
    onClick: () -> Unit
) {
    val isFree = priceText.contains("Free", ignoreCase = true) || 
                 priceText.contains("Бесплатно", ignoreCase = true) ||
                 priceText.contains("Без оплаты", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFree) Color(0xFF141210) else MysticGold
        ),
        border = if (isFree) BorderStroke(1.dp, MysticGold.copy(0.2f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (isFree) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFree) MysticGold.copy(0.15f) else Color.Black.copy(0.15f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = priceText,
                    color = if (isFree) MysticGold else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
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

@Composable
fun ResultsScreen(
    viewModel: PalmistViewModel,
    onNavigateToCompatibility: () -> Unit,
    onNavigateToBilling: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val reading by viewModel.currentReading.collectAsState()
    val billingState by viewModel.billingState.collectAsState()

    var activeTab by remember { mutableStateOf("report") } // "report" or "map"

    // Parse JSON
    val palmistReport = remember(reading) {
        reading?.let {
            try {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                moshi.adapter(com.aistudio.hiromant.kxsrwa.data.remote.PalmistReport::class.java).fromJson(it.resultJson)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Interactive lines map dialog state
    var selectedLineInfo by remember { mutableStateOf<com.aistudio.hiromant.kxsrwa.data.remote.PalmLineAnalysis?>(null) }

    // TTS configurations
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isPlayingTts by remember { mutableStateOf(false) }
    var ttsGenderState by remember { mutableStateOf("Female") } // "Male" or "Female"
    var ttsRateState by remember { mutableStateOf(1.0f) }

    var activeLineBlockIndex by remember { mutableStateOf(-1) }
    val lineReadingBlocks = remember(palmistReport) {
        palmistReport?.lines ?: emptyList()
    }

    val scope = rememberCoroutineScope()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Initialize Android TTS
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = if (currentLang == AppLanguage.RUS) Locale("ru") else Locale.US
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
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
                    if (activeLineBlockIndex < lineReadingBlocks.size - 1) {
                        activeLineBlockIndex++
                    } else {
                        isPlayingTts = false
                        activeLineBlockIndex = -1
                    }
                }
            }
            override fun onError(utteranceId: String?) {
                scope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    isPlayingTts = false
                }
            }
        })
        onDispose {
            tts?.setOnUtteranceProgressListener(null)
        }
    }

    LaunchedEffect(activeLineBlockIndex, isPlayingTts) {
        if (isPlayingTts && activeLineBlockIndex in lineReadingBlocks.indices) {
            val line = lineReadingBlocks[activeLineBlockIndex]
            val textToSpeak = "${line.name}. ${line.fullDescription}"
            
            tts?.setPitch(if (ttsGenderState == "Female") 1.25f else 0.85f)
            tts?.setSpeechRate(ttsRateState)
            
            val params = android.os.Bundle().apply {
                putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "line_$activeLineBlockIndex")
            }
            tts?.speak(textToSpeak, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "line_$activeLineBlockIndex")
            
            try {
                listState.animateScrollToItem(activeLineBlockIndex + 3)
            } catch (e: Exception) {}
        }
    }

    fun playTtsOfLines() {
        if (lineReadingBlocks.isEmpty()) return
        if (isPlayingTts) {
            tts?.stop()
            isPlayingTts = false
        } else {
            if (activeLineBlockIndex !in lineReadingBlocks.indices) {
                activeLineBlockIndex = 0
            }
            isPlayingTts = true
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
                .statusBarsPadding()
        ) {
            MysticHeader(strings.resTitle)

            // Segmented Tab Controls
            TabRow(
                selectedTabIndex = if (activeTab == "report") 0 else 1,
                containerColor = Color.Transparent,
                contentColor = MysticGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "report") 0 else 1]),
                        color = MysticGold
                    )
                }
            ) {
                Tab(
                    selected = activeTab == "report",
                    onClick = { activeTab = "report" },
                    text = { Text(strings.resTabReport, style = MaterialTheme.typography.labelLarge) }
                )
                Tab(
                    selected = activeTab == "map",
                    onClick = { activeTab = "map" },
                    text = { Text(strings.resTabLinesMap, style = MaterialTheme.typography.labelLarge) }
                )
            }

            if (palmistReport != null) {
                if (activeTab == "report") {
                    // --- SCROLLABLE REPORT TEXT VIEWS ---
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Overall summary
                            Text(
                                text = strings.resOverallPortrait,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = palmistReport.overallPortrait,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Hand Type
                            Text(
                                text = strings.resHandType,
                                style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                            )
                            Text(
                                text = palmistReport.handType,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MysticBronze,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = MysticBronze.copy(0.3f))
                            Spacer(modifier = Modifier.height(20.dp))

                            // Mounts
                            Text(
                                text = strings.resMountsHeader,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Mounts list
                        items(palmistReport.mounts) { mount ->
                            MountReportRow(mount)
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = MysticBronze.copy(0.3f))
                            Spacer(modifier = Modifier.height(20.dp))

                            // Signs
                            Text(
                                text = strings.resSignsHeader,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Signs list
                        items(palmistReport.signs) { sign ->
                            SignReportCard(sign)
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = MysticBronze.copy(0.3f))
                            Spacer(modifier = Modifier.height(20.dp))

                            // Relationship marriage kids
                            Text(
                                text = strings.resMarriageChildren,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = palmistReport.marriageChildren,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Historical key transitions
                            Text(
                                text = strings.resLifeEvents,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = palmistReport.lifeEvents,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Forecasts
                            Text(
                                text = strings.resPredictions,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = palmistReport.predictions,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Practical guidelines
                            Text(
                                text = strings.resRecommendations,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = palmistReport.recommendations,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Active hands divisions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                                    border = BorderStroke(1.dp, MysticBronze.copy(0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = strings.resInheritedPotentials,
                                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = palmistReport.leftHand, fontSize = 12.sp, color = Color.White)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
                                    border = BorderStroke(1.dp, MysticBronze.copy(0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = strings.resAcquiredTraits,
                                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = palmistReport.rightHand, fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Share / Export to PDF
                            MysticButton(
                                text = strings.resExportPdf,
                                onClick = {
                                    Toast.makeText(context, strings.resExportSuccess, Toast.LENGTH_LONG).show()
                                },
                                isSecondary = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Subscriptions buy panel
                            Text(
                                text = "MONETIZATION PREMIUM UNLOCKS:",
                                style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze, letterSpacing = 2.sp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            MysticButton(
                                text = strings.resBtnBuy10,
                                onClick = {
                                    viewModel.simulateBuySubscription()
                                    Toast.makeText(context, "Purchase Successful! 10 readings credited.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            MysticButton(
                                text = strings.resBtnBuyCompat,
                                onClick = onNavigateToCompatibility,
                                modifier = Modifier.fillMaxWidth(),
                                isSecondary = true
                            )

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                } else {
                    // --- MAP VIEW TAB WITH ACTIVE TAP CLICKS ---
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // 1. Palm Map Box
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                                    .padding(vertical = 12.dp)
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = { offset ->
                                                    val w = size.width.toFloat()
                                                    val h = size.height.toFloat()
                                                    
                                                    // Elementary bounding boxes for line points
                                                    val xPct = offset.x / w
                                                    val yPct = offset.y / h
                                                    
                                                    val tappedLine = if (yPct in 0.4f..0.55f && xPct in 0.25f..0.7f) {
                                                        palmistReport.lines.find { it.name.contains("Серд") || it.name.contains("Heart") }
                                                    } else if (yPct in 0.52f..0.62f && xPct in 0.25f..0.7f) {
                                                        palmistReport.lines.find { it.name.contains("Голо") || it.name.contains("Head") }
                                                    } else if (yPct in 0.58f..0.85f && xPct in 0.2f..0.55f) {
                                                        palmistReport.lines.find { it.name.contains("Жизн") || it.name.contains("Life") }
                                                    } else if (xPct in 0.45f..0.58f && yPct in 0.35f..0.8f) {
                                                        palmistReport.lines.find { it.name.contains("Судь") || it.name.contains("Destiny") }
                                                    } else null

                                                    if (tappedLine != null) {
                                                        // When tapped on the canvas line, set active TTS line block and start speaking!
                                                        val lineIndex = palmistReport.lines.indexOf(tappedLine)
                                                        if (lineIndex != -1) {
                                                            activeLineBlockIndex = lineIndex
                                                            isPlayingTts = true
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                ) {
                                    val w = size.width
                                    val h = size.height

                                    // Draw palm base background
                                    drawCircle(
                                        color = Color(0x331E1E2D),
                                        radius = w * 0.45f
                                    )

                                    // Heart line (Pink)
                                    val heartPath = Path().apply {
                                        moveTo(w * 0.3f, h * 0.48f)
                                        quadraticTo(w * 0.5f, h * 0.44f, w * 0.75f, h * 0.42f)
                                    }
                                    drawPath(
                                        path = heartPath,
                                        color = LineHeartColor,
                                        style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )

                                    // Head line (Blue)
                                    val headPath = Path().apply {
                                        moveTo(w * 0.28f, h * 0.52f)
                                        quadraticTo(w * 0.5f, h * 0.54f, w * 0.72f, h * 0.58f)
                                    }
                                    drawPath(
                                        path = headPath,
                                        color = LineHeadColor,
                                        style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )

                                    // Life line (Red)
                                    val lifePath = Path().apply {
                                        moveTo(w * 0.28f, h * 0.52f)
                                        quadraticTo(w * 0.35f, h * 0.65f, w * 0.44f, h * 0.82f)
                                    }
                                    drawPath(
                                        path = lifePath,
                                        color = LineLifeColor,
                                        style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )

                                    // Destiny line (Green)
                                    val destinyPath = Path().apply {
                                        moveTo(w * 0.52f, h * 0.8f)
                                        quadraticTo(w * 0.51f, h * 0.58f, w * 0.5f, h * 0.38f)
                                    }
                                    drawPath(
                                        path = destinyPath,
                                        color = LineDestinyColor,
                                        style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                }
                            }
                        }

                        // 2. Click Tip
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x99000000))
                            ) {
                                Text(
                                    text = if (currentLang == AppLanguage.RUS) "Тапните по любой линии ладони или карточке ниже для прослушивания подробностей" else "Tap any line overlay on the palm or any card below to start speech description",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MysticGold,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 3. TTS Player Control Card
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x33B87333)),
                                border = BorderStroke(1.dp, MysticBronze.copy(0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = strings.resAudioTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Play/Pause button and its label
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Старт/Пауза" else "Play/Pause",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MysticGold),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            IconButton(
                                                onClick = { playTtsOfLines() },
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(MysticGold, CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlayingTts) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.Black
                                                )
                                            }
                                        }

                                        // Stop button and its label
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Стоп" else "Stop",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MysticGold),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            IconButton(
                                                onClick = {
                                                    tts?.stop()
                                                    isPlayingTts = false
                                                    activeLineBlockIndex = -1
                                                },
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(Color.White.copy(0.1f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Stop,
                                                    contentDescription = null,
                                                    tint = Color.White
                                                )
                                            }
                                        }

                                        // Voice switch and its label
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1.5f)
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Выбор голоса" else "Voice Selector",
                                                style = MaterialTheme.typography.labelSmall.copy(color = MysticGold),
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.height(44.dp)
                                            ) {
                                                Text(strings.resVoiceMale, fontSize = 11.sp, color = Color.White)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Switch(
                                                    checked = ttsGenderState == "Female",
                                                    onCheckedChange = { ttsGenderState = if (it) "Female" else "Male" },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = MysticGold,
                                                        checkedTrackColor = MysticBronze,
                                                        uncheckedThumbColor = MysticBronze,
                                                        uncheckedTrackColor = Color.DarkGray
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(strings.resVoiceFemale, fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Speed slider
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "${strings.resVoiceSpeed}: ${String.format("%.1fx", ttsRateState)}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MysticGold),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Slider(
                                            value = ttsRateState,
                                            onValueChange = { newRate ->
                                                ttsRateState = newRate
                                                if (isPlayingTts && activeLineBlockIndex in lineReadingBlocks.indices) {
                                                    val line = lineReadingBlocks[activeLineBlockIndex]
                                                    val textToSpeak = "${line.name}. ${line.fullDescription}"
                                                    tts?.setSpeechRate(newRate)
                                                    val params = android.os.Bundle().apply {
                                                        putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "line_$activeLineBlockIndex")
                                                    }
                                                    tts?.speak(textToSpeak, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "line_$activeLineBlockIndex")
                                                }
                                            },
                                            valueRange = 0.5f..2.0f,
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = MysticGold,
                                                thumbColor = MysticGold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Line report description cards
                        items(lineReadingBlocks.size) { index ->
                            val line = lineReadingBlocks[index]
                            LineReportCard(
                                line = line,
                                isActive = (index == activeLineBlockIndex && isPlayingTts),
                                onPlayClick = {
                                    if (activeLineBlockIndex == index && isPlayingTts) {
                                        tts?.stop()
                                        isPlayingTts = false
                                    } else {
                                        activeLineBlockIndex = index
                                        isPlayingTts = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Selected line information pop-up Dialog
        selectedLineInfo?.let { line ->
            Dialog(onDismissRequest = { selectedLineInfo = null }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                    border = BorderStroke(1.5.dp, MysticGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color(android.graphics.Color.parseColor(line.color)), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = line.name,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = line.fullDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        line.keyTakeaways.forEach { takeaway ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("•", color = MysticGold, modifier = Modifier.padding(end = 6.dp))
                                Text(text = takeaway, fontSize = 12.sp, color = Color(0xFFC0C0D0))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        MysticButton(
                            text = "ОК",
                            onClick = { selectedLineInfo = null },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
    var selfPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var partnerPhotoUri by remember { mutableStateOf<Uri?>(null) }

    var selfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var partnerBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val selfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                selfPhotoUri = it
                selfBitmap = BitmapUtils.uriToBitmap(context, it)
            }
        }
    )

    val partnerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                partnerPhotoUri = it
                partnerBitmap = BitmapUtils.uriToBitmap(context, it)
            }
        }
    )

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
            MysticHeader(strings.compatTitle)
            MysticSubtitle(strings.compatSubtitle)

            Spacer(modifier = Modifier.height(20.dp))

            if (compatReport == null) {
                // --- ENTRY AND UPLOADS FORM ---
                MysticCard {
                    Spacer(modifier = Modifier.height(16.dp))

                    MysticTextField(
                        value = partnerName,
                        onValueChange = { partnerName = it },
                        label = "Имя партнёра / Partner Name",
                        placeholder = "Мария / Julian"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Self photo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.compatUploadSelf, fontSize = 11.sp, color = MysticBronze)
                            Spacer(modifier = Modifier.height(8.dp))
                            GlowingBorderCircle(size = 90.dp) {
                                if (selfBitmap != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(selfBitmap),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    IconButton(onClick = { selfPicker.launch("image/*") }) {
                                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = MysticGold)
                                    }
                                }
                            }
                        }

                        // Partner photo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.compatUploadPartner, fontSize = 11.sp, color = MysticBronze)
                            Spacer(modifier = Modifier.height(8.dp))
                            GlowingBorderCircle(size = 90.dp) {
                                if (partnerBitmap != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(partnerBitmap),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    IconButton(onClick = { partnerPicker.launch("image/*") }) {
                                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = MysticGold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                MysticButton(
                    text = strings.compatAnalyzeBtn,
                    onClick = {
                        if (partnerName.isNotEmpty()) {
                            // Check compatibility price (250 rubles item)
                            viewModel.checkFeatureUnlocked("compatibility") { unlocked ->
                                if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                                    viewModel.runCompatibilityAnalysis(selfBitmap, partnerBitmap, partnerName, onNavigateToLoading)
                                } else {
                                    onNavigateToBilling()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter Partner's Name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // --- RESULTS COMPATIBILITY PRESENTATION ---
                Spacer(modifier = Modifier.height(16.dp))

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

                // Detailed portraits
                MysticCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Личность 1 / Self Style",
                            style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = compatReport.partner1Portrait, style = MaterialTheme.typography.bodyMedium, color = Color.White)

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Личность 2 ($partnerName) / Partner Style",
                            style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = compatReport.partner2Portrait, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }

                // Synergy summary
                MysticCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.compatCombinedTitle,
                            style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = compatReport.combinedAnalysis, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                }

                // Strengths
                MysticCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.compatStrongTitle,
                            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF2ECC71))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        compatReport.strongPoints.forEach { pt ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("✓", color = Color(0xFF2ECC71), modifier = Modifier.padding(end = 8.dp))
                                Text(text = pt, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
                        }
                    }
                }

                // Weaknesses
                MysticCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.compatWeakTitle,
                            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFCF6679))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        compatReport.weakPoints.forEach { pt ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("⚠", color = Color(0xFFCF6679), modifier = Modifier.padding(end = 8.dp))
                                Text(text = pt, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
                        }
                    }
                }

                // Modular metrics
                MysticCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = strings.compatEmotional, style = MaterialTheme.typography.labelLarge.copy(color = MysticGold))
                        Text(text = compatReport.emotionalCompatibility, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = strings.compatIntellectual, style = MaterialTheme.typography.labelLarge.copy(color = MysticGold))
                        Text(text = compatReport.intellectualCompatibility, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = strings.compatFinancial, style = MaterialTheme.typography.labelLarge.copy(color = MysticGold))
                        Text(text = compatReport.financialCompatibility, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MysticButton(
                    text = "Заново / Reset",
                    onClick = { viewModel.currentCompatibilityReading.value = null },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
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
            MysticHeader(strings.histTitle)
            MysticSubtitle(strings.histSubtitle)

            Spacer(modifier = Modifier.height(20.dp))

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
                Text(
                    text = if (record.analysisType == "compatibility") "Совместимость c ${record.partnerName}" else record.name,
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
                            "Как сделать качественный снимок?" to "Положите ладонь ровно, раздвинув пальцы на плоском однотонном фоне при ярком естественном или искусственном освещении. Рядом можно положить монету для точной калибровки размеров.",
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

    var aboutProgramExpanded by remember { mutableStateOf(false) }
    var aboutDevExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = strings.settLanguage, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentLang.label, color = MysticGold, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MysticGold)
                    }
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
                        text = if (currentLang == AppLanguage.RUS) "Масштаб шрифта" else "Font Scale",
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
                        Text(
                            text = if (currentLang == AppLanguage.RUS) {
                                "Программа «Хиромант» — это ваш персональный проводник в мир древних знаний о ладонях. С помощью современных алгоритмов искусственного интеллекта и нейросетей Gemini, приложение анализирует форму рук, пальцев и переплетение линий на ладони, сопоставляя их с канонами классической ведической и западной хиромантии. Программа считывает холмы планет, особые знаки (такие как Мистический Крест или Кольцо Соломона) и линии сердца, головы, жизни и судьбы, чтобы раскрыть ваш врождённый потенциал и дать практические советы на жизненном пути."
                            } else {
                                "The 'Palmist' app is your personal guide to the ancient wisdom of palm reading. Powered by modern Gemini AI algorithms, the app analyzes your hand shape, finger proportions, and palm line networks, mapping them to the canons of classic Vedic and Western palmistry. It reads planetary mounts, sacred markings (like the Mystic Cross or Ring of Solomon), and the primary lines of Heart, Head, Life, and Destiny to unlock your innate potential and deliver actionable life guidelines."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC0C0D0),
                            lineHeight = 20.sp
                        )
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
                        Text(
                            text = if (currentLang == AppLanguage.RUS) {
                                "Разработчик: Арсений (ArsMaxim)\nКонтакты: arsmaxim@gmail.com\n\nЯ увлечён созданием интеллектуальных, красивых и полезных мобильных приложений, которые объединяют современные технологии ИИ и классическое наследие человечества. Спасибо, что выбрали моё приложение!"
                            } else {
                                "Developer: Arseniy (ArsMaxim)\nContact: arsmaxim@gmail.com\n\nI am passionate about creating smart, beautiful, and helpful mobile applications that merge cutting-edge AI technologies with classical human heritage. Thank you for choosing my app!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC0C0D0),
                            lineHeight = 20.sp
                        )
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
                        style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
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
                    viewModel.clearHistory()
                    Toast.makeText(context, "Data wiped successfully.", Toast.LENGTH_SHORT).show()
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

    var cardNum by remember { mutableStateOf("") }
    var chosenMethod by remember { mutableStateOf<String?>(null) } // "yookassa", "google"

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
                .padding(24.dp)
        ) {
            MysticHeader(strings.billDialogTitle)
            MysticSubtitle(strings.billDialogChoosePay)

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

            // YooKassa simulated
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
                        Text(strings.billDialogYooKassa, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }

                    if (chosenMethod == "yookassa") {
                        Spacer(modifier = Modifier.height(16.dp))
                        MysticTextField(
                            value = cardNum,
                            onValueChange = { cardNum = it },
                            label = strings.billDialogCardNum,
                            placeholder = "1111 2222 3333 4444"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                    text = "PAY / ОПЛАТИТЬ",
                    onClick = {
                        if (chosenMethod != null) {
                            viewModel.simulateBuySubscription()
                            Toast.makeText(context, strings.billDialogSuccess, Toast.LENGTH_LONG).show()
                            onNavigateBack()
                        } else {
                            Toast.makeText(context, "Please choose a payment option", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1.5f),
                    enabled = chosenMethod != null
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

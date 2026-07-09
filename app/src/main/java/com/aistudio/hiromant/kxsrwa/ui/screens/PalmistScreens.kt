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
                .padding(horizontal = 8.dp, vertical = 12.dp)
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
            // Background Layer: Hand representation scaled to fill the entire box!
            androidx.compose.animation.AnimatedVisibility(
                visible = scrollOpened,
                enter = scaleIn(animationSpec = tween(1200)) + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
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
                        contentScale = ContentScale.FillBounds, // Stretched to fill the screen bounds so Canvas matches perfectly!
                        modifier = Modifier.fillMaxSize(),
                        alpha = imageAlpha
                    )
                    
                    // Canvas overlays exactly in coordinates covering full screen
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

    val isEmailMode = emailOrPhone.trim().contains("@")

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
                            Toast.makeText(context, "${if (currentLang == AppLanguage.RUS) "Ошибка входа:" else "Login error:"} ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                isLoading = false
                Toast.makeText(context, "${if (currentLang == AppLanguage.RUS) "Ошибка верификации телефона:" else "Phone verification failed:"} ${e.message}", Toast.LENGTH_LONG).show()
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
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MysticGold)
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
                                                        Toast.makeText(
                                                            context,
                                                            if (currentLang == AppLanguage.RUS) 
                                                                "Пожалуйста, подтвердите ваш E-mail! Мы отправили вам ссылку на почту." 
                                                            else 
                                                                "Please verify your E-mail! We have sent you a link.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(context, "${if (currentLang == AppLanguage.RUS) "Ошибка входа:" else "Login error:"} ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
                                                            Toast.makeText(
                                                                context,
                                                                "${if (currentLang == AppLanguage.RUS) "Письмо не отправлено:" else "Failed to send email:"} ${emailTask.exception?.message}",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                            } else {
                                                Toast.makeText(context, "${if (currentLang == AppLanguage.RUS) "Ошибка регистрации:" else "Registration error:"} ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
                                        val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
                                            .setPhoneNumber(emailOrPhone.trim())
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
                                                    Toast.makeText(context, "${if (currentLang == AppLanguage.RUS) "Ошибка:" else "Error:"} ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            MysticHeader(strings.profileTitle)
            MysticSubtitle(strings.profileSubtitle)

            Spacer(modifier = Modifier.height(20.dp))

            MysticCard {
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
                .size(180.dp)
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

    var leftPalmPath by remember { mutableStateOf<String?>(null) }
    var leftBackPath by remember { mutableStateOf<String?>(null) }
    var rightPalmPath by remember { mutableStateOf<String?>(null) }
    var rightBackPath by remember { mutableStateOf<String?>(null) }

    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var showInterpretationScreen by remember { mutableStateOf(false) }

    // Media Store URI values for system-native cameras
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Launchers for media
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val bitmap = BitmapUtils.uriToBitmap(context, it)
                if (bitmap != null) {
                    when (activeSlot) {
                        "left_palm" -> {
                            bitmapLeftPalm = bitmap
                            leftPalmPath = it.toString()
                        }
                        "left_back" -> {
                            bitmapLeftBack = bitmap
                            leftBackPath = it.toString()
                        }
                        "right_palm" -> {
                            bitmapRightPalm = bitmap
                            rightPalmPath = it.toString()
                        }
                        "right_back" -> {
                            bitmapRightBack = bitmap
                            rightBackPath = it.toString()
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
                videoUri = it
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
                                bitmapLeftPalm = bitmap
                                leftPalmPath = uri.toString()
                            }
                            "left_back" -> {
                                bitmapLeftBack = bitmap
                                leftBackPath = uri.toString()
                            }
                            "right_palm" -> {
                                bitmapRightPalm = bitmap
                                rightPalmPath = uri.toString()
                            }
                            "right_back" -> {
                                bitmapRightBack = bitmap
                                rightBackPath = uri.toString()
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
                    videoUri = uri
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
                val uri = createGalleryImageUri(context, "hand_photo")
                if (uri != null) {
                    tempImageUri = uri
                    takePictureLauncher.launch(uri)
                }
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
                            onTakePhoto = {
                                activeSlot = "left_palm"
                                val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    val uri = createGalleryImageUri(context, "left_palm")
                                    if (uri != null) {
                                        tempImageUri = uri
                                        takePictureLauncher.launch(uri)
                                    }
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "left_palm"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                bitmapLeftPalm = null
                                leftPalmPath = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Left Back Slot
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
                                    val uri = createGalleryImageUri(context, "left_back")
                                    if (uri != null) {
                                        tempImageUri = uri
                                        takePictureLauncher.launch(uri)
                                    }
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "left_back"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                bitmapLeftBack = null
                                leftBackPath = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Right Palm Slot
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
                                    val uri = createGalleryImageUri(context, "right_palm")
                                    if (uri != null) {
                                        tempImageUri = uri
                                        takePictureLauncher.launch(uri)
                                    }
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "right_palm"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                bitmapRightPalm = null
                                rightPalmPath = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Right Back Slot
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
                                    val uri = createGalleryImageUri(context, "right_back")
                                    if (uri != null) {
                                        tempImageUri = uri
                                        takePictureLauncher.launch(uri)
                                    }
                                } else {
                                    cameraPermissionLauncherForPhoto.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            onPickPhoto = {
                                activeSlot = "right_back"
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = {
                                bitmapRightBack = null
                                rightBackPath = null
                            },
                            btnCameraText = if (currentLang == AppLanguage.RUS) "Камера" else "Camera",
                            btnGalleryText = if (currentLang == AppLanguage.RUS) "Галерея" else "Gallery"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Video Slot Card
                MysticCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = strings.uploadVideoSection,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (videoUri != null) MysticGold else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Video preview window matching photo window styling
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x1F1E1E2C))
                                .border(2.dp, if (videoUri != null) MysticGold else MysticBronze.copy(0.3f), RoundedCornerShape(16.dp))
                                .clickable {
                                    val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CAMERA
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (hasCameraPermission) {
                                        val uri = createGalleryVideoUri(context, "hand_video")
                                        if (uri != null) {
                                            tempVideoUri = uri
                                            videoCaptureLauncher.launch(uri)
                                        }
                                    } else {
                                        cameraPermissionLauncherForVideo.launch(android.Manifest.permission.CAMERA)
                                    }
                                }
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
                                        text = strings.uploadPreviewVideo,
                                        style = MaterialTheme.typography.labelMedium.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { videoUri = null },
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
                                        text = if (currentLang == AppLanguage.RUS) "Тапните для записи видео" else "Tap to record video",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze, fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Load Video Button under video window
                        MysticButton(
                            text = if (currentLang == AppLanguage.RUS) "Загрузить видео" else "Upload video",
                            onClick = { videoPickerLauncher.launch("video/*") },
                            isSecondary = true,
                            modifier = Modifier.width(220.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Caption under load video button: "До 100 Mb/60 сек."
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "До 100 Mb/60 сек." else "Up to 100 Mb/60 sec.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MysticBronze.copy(0.8f),
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Below all, the "Next" (Далее) Button
                MysticButton(
                    text = if (currentLang == AppLanguage.RUS) "Далее" else "Next",
                    onClick = {
                        val hasMedia = bitmapLeftPalm != null || bitmapLeftBack != null || bitmapRightPalm != null || bitmapRightBack != null || videoUri != null
                        if (hasMedia) {
                            showInterpretationScreen = true
                        } else {
                            Toast.makeText(
                                context,
                                if (currentLang == AppLanguage.RUS) "Пожалуйста, загрузите хотя бы одно фото или видео ладони!" else "Please upload at least one hand photo or video!",
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
                    IconButton(onClick = { showInterpretationScreen = false }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MysticGold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Интерпретация результатов" else "Interpretation of Results",
                        style = MaterialTheme.typography.titleLarge.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = strings.uploadChooseAnalysisType,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val bitmaps = listOfNotNull(bitmapLeftPalm, bitmapLeftBack, bitmapRightPalm, bitmapRightBack)

                // Brief Character Analysis
                TriggerAnalysisButton(
                    label = strings.btnBriefChar,
                    priceText = strings.freeLabel,
                    onClick = {
                        viewModel.runPalmAnalysis(
                            bitmaps = bitmaps,
                            videoUri = videoUri?.toString(),
                            analysisType = "brief_char",
                            leftPalmPath = leftPalmPath,
                            leftBackPath = leftBackPath,
                            rightPalmPath = rightPalmPath,
                            rightBackPath = rightBackPath,
                            onCompleted = {
                                showInterpretationScreen = false
                                onNavigateToLoading()
                            }
                        )
                    }
                )

                // Full Character Analysis
                TriggerAnalysisButton(
                    label = strings.btnFullChar,
                    priceText = "150 ₽",
                    onClick = {
                        viewModel.checkFeatureUnlocked("full_char") { unlocked ->
                            if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                                viewModel.runPalmAnalysis(
                                    bitmaps = bitmaps,
                                    videoUri = videoUri?.toString(),
                                    analysisType = "full_char",
                                    leftPalmPath = leftPalmPath,
                                    leftBackPath = leftBackPath,
                                    rightPalmPath = rightPalmPath,
                                    rightBackPath = rightBackPath,
                                    onCompleted = {
                                        showInterpretationScreen = false
                                        onNavigateToLoading()
                                    }
                                )
                            } else {
                                onNavigateToBilling()
                            }
                        }
                    }
                )

                // Brief Life Path Analysis
                TriggerAnalysisButton(
                    label = strings.btnBriefPath,
                    priceText = strings.freeLabel,
                    onClick = {
                        viewModel.runPalmAnalysis(
                            bitmaps = bitmaps,
                            videoUri = videoUri?.toString(),
                            analysisType = "brief_path",
                            leftPalmPath = leftPalmPath,
                            leftBackPath = leftBackPath,
                            rightPalmPath = rightPalmPath,
                            rightBackPath = rightBackPath,
                            onCompleted = {
                                showInterpretationScreen = false
                                onNavigateToLoading()
                            }
                        )
                    }
                )

                // Full Life Path Analysis
                TriggerAnalysisButton(
                    label = strings.btnFullPath,
                    priceText = "150 ₽",
                    onClick = {
                        viewModel.checkFeatureUnlocked("full_path") { unlocked ->
                            if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                                viewModel.runPalmAnalysis(
                                    bitmaps = bitmaps,
                                    videoUri = videoUri?.toString(),
                                    analysisType = "full_path",
                                    leftPalmPath = leftPalmPath,
                                    leftBackPath = leftBackPath,
                                    rightPalmPath = rightPalmPath,
                                    rightBackPath = rightBackPath,
                                    onCompleted = {
                                        showInterpretationScreen = false
                                        onNavigateToLoading()
                                    }
                                )
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
        
        appendHeader(strings.resMarriageChildren)
        appendBody(report.marriageChildren)
        
        appendHeader(strings.resLifeEvents)
        appendBody(report.lifeEvents)
        
        appendHeader(strings.resPredictions)
        appendBody(report.predictions)
        
        appendHeader(strings.resRecommendations)
        appendBody(report.recommendations)
        
        appendHeader(strings.resInheritedPotentials)
        appendBody(report.leftHand)
        
        appendHeader(strings.resAcquiredTraits)
        appendBody(report.rightHand)
        
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
    onSpeakSelected: (String) -> Unit,
    onReadFromCursor: (Int) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    
    Box(modifier = modifier) {
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
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(top = 60.dp)
        )
        
        if (value.selection.length > 0) {
            val selectedText = value.text.substring(value.selection.start, value.selection.end)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                    border = BorderStroke(1.dp, MysticGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(selectedText))
                                onValueChange(value.copy(selection = TextRange.Zero))
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MysticGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Копировать", color = MysticGold, fontSize = 12.sp)
                        }
                        
                        TextButton(
                            onClick = {
                                onSpeakSelected(selectedText)
                            }
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = MysticGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ОЗВУЧИТЬ", color = MysticGold, fontSize = 12.sp)
                        }
                        
                        TextButton(
                            onClick = {
                                onReadFromCursor(value.selection.start)
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Read from cursor", tint = MysticGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("От курсора", color = MysticGold, fontSize = 12.sp)
                        }
                    }
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
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
                IconButton(
                    onClick = { onGenderChange("Male") },
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
                
                IconButton(
                    onClick = { onGenderChange("Female") },
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
            }
        }
    }
}

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
    var spokenWordRange by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var activeLineName by remember { mutableStateOf<String?>(null) }
    var ttsOffset by remember { mutableStateOf(0) }

    val lineReadingBlocks = remember(palmistReport) {
        palmistReport?.lines ?: emptyList()
    }

    val scope = rememberCoroutineScope()

    // Selectable Text Field states
    var reportTextState by remember { mutableStateOf(TextFieldValue()) }
    var mapTextState by remember { mutableStateOf(TextFieldValue()) }

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

    fun applyTtsSettings() {
        tts?.setSpeechRate(ttsRateState)
        tts?.setPitch(if (ttsGenderState == "Female") 1.25f else 0.85f)
        try {
            val currentLocale = if (currentLang == AppLanguage.RUS) Locale("ru") else Locale.US
            val voices = tts?.voices
            val selectedVoice = voices?.find { voice ->
                val nameLower = voice.name.lowercase(Locale.US)
                voice.locale.language == currentLocale.language &&
                if (ttsGenderState == "Female") {
                    nameLower.contains("female") || nameLower.contains("f-local") || nameLower.contains("ruf")
                } else {
                    nameLower.contains("male") || nameLower.contains("m-local") || nameLower.contains("rum")
                }
            } ?: voices?.find { voice ->
                voice.locale.language == currentLocale.language
            }
            if (selectedVoice != null) {
                tts?.voice = selectedVoice
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val lineRanges = remember { mutableStateMapOf<String, IntRange>() }

    val reportAnnotatedString = remember(palmistReport, strings, spokenWordRange) {
        if (palmistReport != null) {
            buildReportAnnotatedString(palmistReport, strings, spokenWordRange)
        } else {
            AnnotatedString("")
        }
    }

    val mapAnnotatedString = remember(palmistReport, spokenWordRange) {
        if (palmistReport != null) {
            buildLinesAnnotatedString(palmistReport.lines, spokenWordRange) { calculatedRanges ->
                lineRanges.clear()
                lineRanges.putAll(calculatedRanges)
            }
        } else {
            AnnotatedString("")
        }
    }

    LaunchedEffect(reportAnnotatedString) {
        reportTextState = reportTextState.copy(annotatedString = reportAnnotatedString)
    }

    LaunchedEffect(mapAnnotatedString) {
        mapTextState = mapTextState.copy(annotatedString = mapAnnotatedString)
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
                    activeLineName = null
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
                    
                    if (activeTab == "map") {
                        val activeLine = lineReadingBlocks.find { line ->
                            val range = lineRanges[line.name]
                            range != null && absStart in range
                        }
                        if (activeLine != null) {
                            activeLineName = activeLine.name
                        }
                    }
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
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                SelectableInterpretationText(
                                    value = reportTextState,
                                    onValueChange = { reportTextState = it },
                                    modifier = Modifier.fillMaxSize(),
                                    onSpeakSelected = { selectedText ->
                                        tts?.stop()
                                        applyTtsSettings()
                                        ttsOffset = reportTextState.selection.start
                                        val params = android.os.Bundle().apply {
                                            putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "selection")
                                        }
                                        tts?.speak(selectedText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "selection")
                                        isPlayingTts = true
                                    },
                                    onReadFromCursor = { cursorIndex ->
                                        speakTextFromIndex(reportTextState.text, cursorIndex)
                                    }
                                )
                                
                                TtsVoiceController(
                                    isPlaying = isPlayingTts,
                                    onPlayToggle = {
                                        if (isPlayingTts) {
                                            tts?.stop()
                                            isPlayingTts = false
                                            spokenWordRange = null
                                        } else {
                                            speakTextFromIndex(reportTextState.text, 0)
                                        }
                                    },
                                    rate = ttsRateState,
                                    onRateChange = { newRate ->
                                        ttsRateState = newRate
                                        tts?.setSpeechRate(newRate)
                                        if (isPlayingTts) {
                                            val currentWordStart = spokenWordRange?.first ?: 0
                                            speakTextFromIndex(reportTextState.text, currentWordStart)
                                        }
                                    },
                                    gender = ttsGenderState,
                                    onGenderChange = { newGender ->
                                        ttsGenderState = newGender
                                        applyTtsSettings()
                                        if (isPlayingTts) {
                                            val currentWordStart = spokenWordRange?.first ?: 0
                                            speakTextFromIndex(reportTextState.text, currentWordStart)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(16.dp)
                                )
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MysticButton(
                                    text = strings.resExportPdf,
                                    onClick = {
                                        Toast.makeText(context, strings.resExportSuccess, Toast.LENGTH_LONG).show()
                                    },
                                    isSecondary = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Text(
                                    text = "MONETIZATION PREMIUM UNLOCKS:",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MysticBronze, letterSpacing = 2.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                MysticButton(
                                    text = strings.resBtnBuy10,
                                    onClick = {
                                        viewModel.simulateBuySubscription()
                                        Toast.makeText(context, "Purchase Successful! 10 readings credited.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                MysticButton(
                                    text = strings.resBtnBuyCompat,
                                    onClick = onNavigateToCompatibility,
                                    modifier = Modifier.fillMaxWidth(),
                                    isSecondary = true
                                )
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            val painter = rememberAsyncImagePainter(
                                model = reading?.leftPalmPath ?: reading?.rightPalmPath ?: reading?.leftBackPath ?: reading?.rightBackPath
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Hand Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                
                                val isHeartActive = activeLineName?.let { it.contains("Серд") || it.contains("Heart") } ?: false
                                val heartPath = Path().apply {
                                    moveTo(w * 0.3f, h * 0.48f)
                                    quadraticTo(w * 0.5f, h * 0.44f, w * 0.75f, h * 0.42f)
                                }
                                drawPath(
                                    path = heartPath,
                                    color = if (isHeartActive) LineHeartColor else LineHeartColor.copy(alpha = 0.2f),
                                    style = Stroke(
                                        width = (if (isHeartActive) 14.dp else 6.dp).toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                                
                                val isHeadActive = activeLineName?.let { it.contains("Голо") || it.contains("Ум") || it.contains("Head") } ?: false
                                val headPath = Path().apply {
                                    moveTo(w * 0.28f, h * 0.52f)
                                    quadraticTo(w * 0.5f, h * 0.54f, w * 0.72f, h * 0.58f)
                                }
                                drawPath(
                                    path = headPath,
                                    color = if (isHeadActive) LineHeadColor else LineHeadColor.copy(alpha = 0.2f),
                                    style = Stroke(
                                        width = (if (isHeadActive) 14.dp else 6.dp).toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                                
                                val isLifeActive = activeLineName?.let { it.contains("Жизн") || it.contains("Life") } ?: false
                                val lifePath = Path().apply {
                                    moveTo(w * 0.28f, h * 0.52f)
                                    quadraticTo(w * 0.35f, h * 0.65f, w * 0.44f, h * 0.82f)
                                }
                                drawPath(
                                    path = lifePath,
                                    color = if (isLifeActive) LineLifeColor else LineLifeColor.copy(alpha = 0.2f),
                                    style = Stroke(
                                        width = (if (isLifeActive) 14.dp else 6.dp).toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                                
                                val isDestinyActive = activeLineName?.let { it.contains("Суд") || it.contains("Dest") } ?: false
                                val destinyPath = Path().apply {
                                    moveTo(w * 0.52f, h * 0.8f)
                                    quadraticTo(w * 0.51f, h * 0.58f, w * 0.5f, h * 0.38f)
                                }
                                drawPath(
                                    path = destinyPath,
                                    color = if (isDestinyActive) LineDestinyColor else LineDestinyColor.copy(alpha = 0.2f),
                                    style = Stroke(
                                        width = (if (isDestinyActive) 12.dp else 4.dp).toPx(),
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(MysticDarkBackground)
                        ) {
                            SelectableInterpretationText(
                                value = mapTextState,
                                onValueChange = { mapTextState = it },
                                modifier = Modifier.fillMaxSize(),
                                onSpeakSelected = { selectedText ->
                                    tts?.stop()
                                    applyTtsSettings()
                                    ttsOffset = mapTextState.selection.start
                                    val params = android.os.Bundle().apply {
                                        putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "selection")
                                    }
                                    tts?.speak(selectedText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "selection")
                                    isPlayingTts = true
                                },
                                onReadFromCursor = { cursorIndex ->
                                    speakTextFromIndex(mapTextState.text, cursorIndex)
                                }
                            )
                            
                            TtsVoiceController(
                                isPlaying = isPlayingTts,
                                onPlayToggle = {
                                    if (isPlayingTts) {
                                        tts?.stop()
                                        isPlayingTts = false
                                        spokenWordRange = null
                                        activeLineName = null
                                    } else {
                                        speakTextFromIndex(mapTextState.text, 0)
                                    }
                                },
                                rate = ttsRateState,
                                onRateChange = { newRate ->
                                    ttsRateState = newRate
                                    tts?.setSpeechRate(newRate)
                                    if (isPlayingTts) {
                                        val currentWordStart = spokenWordRange?.first ?: 0
                                        speakTextFromIndex(mapTextState.text, currentWordStart)
                                    }
                                },
                                gender = ttsGenderState,
                                onGenderChange = { newGender ->
                                    ttsGenderState = newGender
                                    applyTtsSettings()
                                    if (isPlayingTts) {
                                        val currentWordStart = spokenWordRange?.first ?: 0
                                        speakTextFromIndex(mapTextState.text, currentWordStart)
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

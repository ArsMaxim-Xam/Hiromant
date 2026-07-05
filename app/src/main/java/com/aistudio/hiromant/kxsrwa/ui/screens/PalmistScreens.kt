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


// --- SCREEN 1: SPLASH SCREEN (ANCIENT SCROLL ANIMATION) ---

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

    val infiniteTransition = rememberInfiniteTransition(label = "PulseScroll")
    val linePulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LinePulse"
    )

    LaunchedEffect(Unit) {
        // Unfold ancient scroll
        delay(800)
        scrollOpened = true
        delay(1200)
        showSymbols = true
        delay(1200)
        pulseLines = true
        delay(1000)
        titleVisible = true

        // Complete splash and navigate
        delay(2500)
        onNavigateNext()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
            .clickable {
                // Tap to skip after 3 seconds
                onNavigateNext()
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Mystical canvas scroll drawing
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                // Parchment scroll representation
                androidx.compose.animation.AnimatedVisibility(
                    visible = scrollOpened,
                    enter = scaleIn(animationSpec = tween(1200)) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .drawBehind {
                                // Draw parchment styled background
                                drawRoundRect(
                                    color = Color(0xFF1E1E28),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
                                    style = androidx.compose.ui.graphics.drawscope.Fill
                                )
                                drawRoundRect(
                                    color = MysticBronze.copy(0.6f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                    ) {
                        // Drawing lines inside scroll
                        if (showSymbols) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Hand schematic lines representation
                                val w = size.width
                                val h = size.height

                                // Draw hand outline
                                val handPath = Path().apply {
                                    moveTo(w * 0.35f, h * 0.8f)
                                    quadraticTo(w * 0.25f, h * 0.65f, w * 0.3f, h * 0.5f)
                                    quadraticTo(w * 0.3f, h * 0.35f, w * 0.35f, h * 0.25f) // Index
                                    quadraticTo(w * 0.4f, h * 0.22f, w * 0.44f, h * 0.25f)
                                    lineTo(w * 0.44f, h * 0.45f)
                                    
                                    moveTo(w * 0.46f, h * 0.45f)
                                    lineTo(w * 0.46f, h * 0.2f) // Middle
                                    quadraticTo(w * 0.5f, h * 0.17f, w * 0.54f, h * 0.2f)
                                    lineTo(w * 0.54f, h * 0.45f)

                                    moveTo(w * 0.56f, h * 0.45f)
                                    lineTo(w * 0.56f, h * 0.23f) // Ring
                                    quadraticTo(w * 0.6f, h * 0.2f, w * 0.64f, h * 0.23f)
                                    lineTo(w * 0.64f, h * 0.48f)

                                    moveTo(w * 0.66f, h * 0.48f)
                                    lineTo(w * 0.66f, h * 0.3f) // Pinky
                                    quadraticTo(w * 0.7f, h * 0.28f, w * 0.73f, h * 0.3f)
                                    quadraticTo(w * 0.75f, h * 0.45f, w * 0.72f, h * 0.6f)
                                    quadraticTo(w * 0.75f, h * 0.75f, w * 0.68f, h * 0.85f)
                                    
                                    // Thumb
                                    moveTo(w * 0.32f, h * 0.65f)
                                    quadraticTo(w * 0.2f, h * 0.62f, w * 0.18f, h * 0.55f)
                                    quadraticTo(w * 0.16f, h * 0.48f, w * 0.23f, h * 0.46f)
                                    quadraticTo(w * 0.29f, h * 0.48f, w * 0.34f, h * 0.58f)
                                }
                                drawPath(
                                    path = handPath,
                                    color = MysticBronze.copy(alpha = 0.4f),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )

                                // Heart line
                                drawLine(
                                    color = LineHeartColor.copy(alpha = if (pulseLines) linePulse else 0.3f),
                                    start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.52f),
                                    end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.46f),
                                    strokeWidth = 3.dp.toPx()
                                )
                                // Head line
                                drawLine(
                                    color = LineHeadColor.copy(alpha = if (pulseLines) linePulse else 0.3f),
                                    start = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.56f),
                                    end = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.58f),
                                    strokeWidth = 3.dp.toPx()
                                )
                                // Life line
                                drawArc(
                                    color = LineLifeColor.copy(alpha = if (pulseLines) linePulse else 0.3f),
                                    startAngle = 100f,
                                    sweepAngle = 120f,
                                    useCenter = false,
                                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.42f),
                                    size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.38f),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Titles
            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 30 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.appName.uppercase(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            color = MysticGold,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.splashLogoSubtitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MysticBronze,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = strings.splashTapToSkip,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                modifier = Modifier.padding(bottom = 30.dp)
            )
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
            Spacer(modifier = Modifier.height(30.dp))

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MysticGold,
                modifier = Modifier.size(60.dp)
            )

            MysticHeader(strings.authTitle)
            MysticSubtitle(strings.authSubtitle)

            Spacer(modifier = Modifier.height(30.dp))

            MysticCard {
                Spacer(modifier = Modifier.height(16.dp))

                MysticTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = strings.authEmailPhonePlaceholder,
                    placeholder = "example@domain.com / +79991234567"
                )

                MysticTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = strings.authPasswordPlaceholder,
                    placeholder = "••••••••"
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
                                if (emailOrPhone.length > 5) {
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = strings.authSkipBtn,
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
    var age by remember { mutableStateOf(28) }
    var height by remember { mutableStateOf(172) }
    var dominantHand by remember { mutableStateOf("Right") }

    var nameError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existingProfile) {
        existingProfile?.let {
            if (it.name.isNotEmpty()) name = it.name
            if (it.gender.isNotEmpty()) gender = it.gender
            age = it.age
            height = it.height
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
                            strings.profileGenderFemale to "Female",
                            strings.profileGenderNone to "Other"
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

                // Age slider
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.profileAgeLabel,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold)
                        )
                        Text(
                            text = "$age",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                        )
                    }
                    Slider(
                        value = age.toFloat(),
                        onValueChange = { age = it.toInt() },
                        valueRange = 18f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = MysticGold,
                            activeTrackColor = MysticGold,
                            inactiveTrackColor = MysticBronze.copy(0.3f)
                        )
                    )
                }

                // Height slider
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.profileHeightLabel,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold)
                        )
                        Text(
                            text = "$height",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                        )
                    }
                    Slider(
                        value = height.toFloat(),
                        onValueChange = { height = it.toInt() },
                        valueRange = 100f..230f,
                        colors = SliderDefaults.colors(
                            thumbColor = MysticGold,
                            activeTrackColor = MysticGold,
                            inactiveTrackColor = MysticBronze.copy(0.3f)
                        )
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
                        viewModel.saveProfile(name, gender, age, height, dominantHand)
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
fun UploadScreen(
    viewModel: PalmistViewModel,
    onNavigateToLoading: () -> Unit,
    onNavigateToBilling: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val billingState by viewModel.billingState.collectAsState()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // CameraX recording simulated state
    var isRecordingVideo by remember { mutableStateOf(false) }
    var recordingTimeLeft by remember { mutableStateOf(60) }

    // Launchers for media
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                photoUri = it
                selectedBitmap = BitmapUtils.uriToBitmap(context, it)
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
                selectedBitmap = it
                photoUri = Uri.parse("content://palmist/temp_photo") // dummy placeholder
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

            // Photo upload block
            MysticCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = strings.uploadPhotoSection,
                        style = MaterialTheme.typography.titleMedium.copy(color = MysticGold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedBitmap != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedBitmap),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, MysticGold, RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = strings.uploadPreviewPhoto,
                            style = MaterialTheme.typography.labelMedium.copy(color = MysticBronze)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
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
                        MysticButton(
                            text = strings.uploadTakePhoto,
                            onClick = {
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
                            isSecondary = true,
                            modifier = Modifier.weight(1f)
                        )
                        MysticButton(
                            text = strings.uploadGallery,
                            onClick = { photoPickerLauncher.launch("image/*") },
                            isSecondary = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                    val bitmaps = if (selectedBitmap != null) listOf(selectedBitmap!!) else emptyList()
                    viewModel.runPalmAnalysis(bitmaps, videoUri?.toString(), "brief_char", onNavigateToLoading)
                }
            )

            TriggerAnalysisButton(
                label = strings.btnFullChar,
                priceText = "150 ₽",
                onClick = {
                    viewModel.checkFeatureUnlocked("full_char") { unlocked ->
                        if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                            val bitmaps = if (selectedBitmap != null) listOf(selectedBitmap!!) else emptyList()
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
                    val bitmaps = if (selectedBitmap != null) listOf(selectedBitmap!!) else emptyList()
                    viewModel.runPalmAnalysis(bitmaps, videoUri?.toString(), "brief_path", onNavigateToLoading)
                }
            )

            TriggerAnalysisButton(
                label = strings.btnFullPath,
                priceText = "150 ₽",
                onClick = {
                    viewModel.checkFeatureUnlocked("full_path") { unlocked ->
                        if (unlocked || (billingState?.remainingAnalyses ?: 0) > 0) {
                            val bitmaps = if (selectedBitmap != null) listOf(selectedBitmap!!) else emptyList()
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

    fun playTtsOfReport() {
        if (palmistReport == null) return
        if (isPlayingTts) {
            tts?.stop()
            isPlayingTts = false
        } else {
            // Build text to read
            val textBuilder = StringBuilder()
            textBuilder.append(palmistReport.overallPortrait).append(". ")
            palmistReport.lines.forEach {
                textBuilder.append(it.name).append(": ").append(it.shortDescription).append(". ")
            }
            textBuilder.append(palmistReport.marriageChildren).append(". ")
            textBuilder.append(palmistReport.predictions)

            // Adjust voice pitch/speed to simulate gender
            tts?.setPitch(if (ttsGenderState == "Female") 1.25f else 0.85f)
            tts?.setSpeechRate(ttsRateState)
            tts?.speak(textBuilder.toString(), TextToSpeech.QUEUE_FLUSH, null, "PalmistReport")
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

                            Text(
                                text = strings.resLinesHeader,
                                style = MaterialTheme.typography.titleLarge.copy(color = MysticGold)
                            )
                        }

                        // Palm lines
                        items(palmistReport.lines) { line ->
                            LineReportCard(line)
                        }

                        item {
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

                            Spacer(modifier = Modifier.height(30.dp))

                            // --- AUDIO VOICE TTS PLAYER CONTROLLER panel ---
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0x33B87333)),
                                border = BorderStroke(1.dp, MysticBronze.copy(0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = strings.resAudioTitle,
                                        style = MaterialTheme.typography.labelLarge.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Play Button
                                        IconButton(
                                            onClick = { playTtsOfReport() },
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(MysticGold, CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingTts) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.Black
                                            )
                                        }

                                        // Stop
                                        IconButton(
                                            onClick = {
                                                tts?.stop()
                                                isPlayingTts = false
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Stop,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }

                                        // Male/Female
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Text(strings.resVoiceMale, fontSize = 11.sp)
                                            Switch(
                                                checked = ttsGenderState == "Female",
                                                onCheckedChange = { ttsGenderState = if (it) "Female" else "Male" },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MysticGold,
                                                    checkedTrackColor = MysticBronze
                                                )
                                            )
                                            Text(strings.resVoiceFemale, fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Speed slider
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(strings.resVoiceSpeed, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                                        Slider(
                                            value = ttsRateState,
                                            onValueChange = { ttsRateState = it },
                                            valueRange = 0.5f..2.0f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(activeTrackColor = MysticGold)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Drawing an interactive abstract Palm Outline layout where lines pulse on click
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

                                            // Resolve click to Line index
                                            if (yPct in 0.4f..0.55f && xPct in 0.25f..0.7f) {
                                                // Heart line
                                                selectedLineInfo = palmistReport.lines.find { it.name.contains("Серд") || it.name.contains("Heart") }
                                            } else if (yPct in 0.52f..0.62f && xPct in 0.25f..0.7f) {
                                                // Head line
                                                selectedLineInfo = palmistReport.lines.find { it.name.contains("Голо") || it.name.contains("Head") }
                                            } else if (yPct in 0.58f..0.85f && xPct in 0.2f..0.55f) {
                                                // Life line
                                                selectedLineInfo = palmistReport.lines.find { it.name.contains("Жизн") || it.name.contains("Life") }
                                            } else if (xPct in 0.45f..0.58f && yPct in 0.35f..0.8f) {
                                                // Destiny line
                                                selectedLineInfo = palmistReport.lines.find { it.name.contains("Судь") || it.name.contains("Destiny") }
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

                        // Floating Tips
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xCC000000))
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.RUS) "Тапните по любой линии ладони для просмотра подробностей" else "Tap any line overlay on the palm to reveal specifics",
                                style = MaterialTheme.typography.labelSmall,
                                color = MysticGold,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
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
fun LineReportCard(line: com.aistudio.hiromant.kxsrwa.data.remote.PalmLineAnalysis) {
    val parsedColor = remember(line.color) {
        try {
            Color(android.graphics.Color.parseColor(line.color))
        } catch (e: Exception) {
            MysticGold
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22141420)),
        border = BorderStroke(1.dp, MysticBronze.copy(0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
    onNavigateToLanguage: () -> Unit
) {
    val context = LocalContext.current
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val billingState by viewModel.billingState.collectAsState()

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
            MysticHeader(strings.settTitle)

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
                        text = "Доступные полные анализы: ${billingState?.remainingAnalyses ?: 0}",
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

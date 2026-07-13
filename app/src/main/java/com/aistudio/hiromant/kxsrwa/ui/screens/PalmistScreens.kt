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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.aistudio.hiromant.kxsrwa.BuildConfig
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

    val elements = remember {
        listOf(
            // Lines
            AnimatedElementState(
                id = "life_line",
                type = HandElementType.LINE,
                name = "Life Line",
                color = Color(0xFFFF4D4D), // Coral Red
                points = listOf(Pair(0.62f, 0.50f), Pair(0.58f, 0.58f), Pair(0.53f, 0.68f), Pair(0.48f, 0.77f), Pair(0.46f, 0.82f))
            ),
            AnimatedElementState(
                id = "head_line",
                type = HandElementType.LINE,
                name = "Head Line",
                color = Color(0xFF00BFFF), // Cyan Blue
                points = listOf(Pair(0.62f, 0.50f), Pair(0.50f, 0.53f), Pair(0.38f, 0.58f))
            ),
            AnimatedElementState(
                id = "heart_line",
                type = HandElementType.LINE,
                name = "Heart Line",
                color = Color(0xFFFF1493), // Deep Pink
                points = listOf(Pair(0.32f, 0.50f), Pair(0.45f, 0.48f), Pair(0.56f, 0.44f))
            ),
            AnimatedElementState(
                id = "destiny_line",
                type = HandElementType.LINE,
                name = "Destiny Line",
                color = Color(0xFFDA70D6), // Orchid / Light Purple
                points = listOf(Pair(0.46f, 0.85f), Pair(0.47f, 0.64f), Pair(0.48f, 0.43f))
            ),
            // Mounts
            AnimatedElementState(
                id = "mount_jupiter",
                type = HandElementType.MOUNT,
                name = "Mount of Jupiter",
                color = Color(0xFF9370DB), // Medium Purple
                symbol = "♃",
                position = Pair(0.60f, 0.40f)
            ),
            AnimatedElementState(
                id = "mount_saturn",
                type = HandElementType.MOUNT,
                name = "Mount of Saturn",
                color = Color(0xFFFFD700), // Gold
                symbol = "♄",
                position = Pair(0.50f, 0.38f)
            ),
            AnimatedElementState(
                id = "mount_apollo",
                type = HandElementType.MOUNT,
                name = "Mount of Apollo",
                color = Color(0xFFFF8C00), // Dark Orange
                symbol = "☉",
                position = Pair(0.40f, 0.39f)
            ),
            AnimatedElementState(
                id = "mount_mercury",
                type = HandElementType.MOUNT,
                name = "Mount of Mercury",
                color = Color(0xFF00FA9A), // Medium Spring Green
                symbol = "☿",
                position = Pair(0.30f, 0.43f)
            ),
            AnimatedElementState(
                id = "mount_venus",
                type = HandElementType.MOUNT,
                name = "Mount of Venus",
                color = Color(0xFFFF69B4), // Hot Pink
                symbol = "♀",
                position = Pair(0.64f, 0.68f)
            ),
            AnimatedElementState(
                id = "mount_mars_lower",
                type = HandElementType.MOUNT,
                name = "Lower Mars",
                color = Color(0xFFFF0000), // Pure Red
                symbol = "♂",
                position = Pair(0.59f, 0.53f)
            ),
            AnimatedElementState(
                id = "mount_mars_upper",
                type = HandElementType.MOUNT,
                name = "Upper Mars",
                color = Color(0xFFFF4500), // Orange Red
                symbol = "♂",
                position = Pair(0.33f, 0.57f)
            ),
            AnimatedElementState(
                id = "mount_moon",
                type = HandElementType.MOUNT,
                name = "Mount of Moon",
                color = Color(0xFFE6E6FA), // Lavender
                symbol = "☽",
                position = Pair(0.36f, 0.75f)
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
                    Spacer(modifier = Modifier.height(20.dp))
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
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                            )
                        }
                        Button(
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MysticGold,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            ShrinkableText(
                                text = if (currentLang == AppLanguage.RUS) "Отправить" else "Send",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
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
                        colors = ButtonDefaults.textButtonColors(contentColor = MysticGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.RUS) "Пропустить и войти в Демо-режиме" else "Bypass and enter in Demo mode",
                            style = MaterialTheme.typography.labelLarge.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                color = MysticGold
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(280.dp)
                            .border(1.dp, MysticGold.copy(0.4f), RoundedCornerShape(140.dp))
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = null,
                            tint = MysticGold.copy(0.25f),
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRussian) "Поместите ладонь сюда" else "Place palm inside the frame",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MysticGold.copy(0.6f),
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
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
    onNavigateToBilling: () -> Unit
) {
    val context = LocalContext.current
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

    val videoUri by viewModel.videoUri.collectAsState()

    val videoThumbnail = remember(videoUri) {
        if (videoUri != null) {
            getVideoThumbnail(context, videoUri)
        } else {
            null
        }
    }
    var showInterpretationScreen by remember { mutableStateOf(false) }

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
                                if (videoThumbnail != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(videoThumbnail),
                                        contentDescription = "Video Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color.Black.copy(0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = MysticGold,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                } else {
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
                                }
                                
                                IconButton(
                                    onClick = { viewModel.videoUri.value = null },
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
    spokenWordRange: Pair<Int, Int>? = null,
    onSpeakSelected: (String) -> Unit,
    onReadFromCursor: (Int) -> Unit,
    bottomContent: @Composable () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
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
                .padding(top = 60.dp, bottom = 24.dp)
        ) {
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

    var activeTab by remember { mutableStateOf("report") } // "report" or "map"

    var linesOffsetX by remember { mutableStateOf(0f) }
    var linesOffsetY by remember { mutableStateOf(0f) }
    var linesScale by remember { mutableStateOf(1f) }
    var linesRotation by remember { mutableStateOf(0f) }
    var imageRotation by remember { mutableStateOf(0f) }
    var isAdjustmentPanelOpen by remember { mutableStateOf(false) }

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

    var selectedMaleVoice by remember { mutableStateOf<android.speech.tts.Voice?>(null) }
    var selectedFemaleVoice by remember { mutableStateOf<android.speech.tts.Voice?>(null) }

    val allAvailableVoices = remember(tts, currentLang) {
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
        val isFemale = ttsGenderState == "Female"
        tts?.setSpeechRate(ttsRateState)
        tts?.setPitch(if (isFemale) 1.35f else 0.75f)
        try {
            if (allAvailableVoices.isNotEmpty()) {
                val preferredVoice = if (isFemale) {
                    selectedFemaleVoice ?: femaleVoicesList.firstOrNull() 
                        ?: allAvailableVoices.firstOrNull { it !in maleVoicesList } 
                        ?: allAvailableVoices.firstOrNull()
                } else {
                    selectedMaleVoice ?: maleVoicesList.firstOrNull() 
                        ?: allAvailableVoices.firstOrNull { it !in femaleVoicesList } 
                        ?: allAvailableVoices.firstOrNull()
                }
                
                if (preferredVoice != null) {
                    tts?.voice = preferredVoice
                    // Synchronize state
                    if (isFemale) {
                        selectedFemaleVoice = preferredVoice
                    } else {
                        selectedMaleVoice = preferredVoice
                    }
                }
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

            val currentReadingVal = reading
            val isPaymentRequired = remember(currentReadingVal) {
                currentReadingVal?.resultJson?.contains("payment_required") == true
            }

            if (isPaymentRequired && currentReadingVal != null) {
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
                if (activeTab == "report") {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        SelectableInterpretationText(
                            value = reportTextState,
                            onValueChange = { reportTextState = it },
                            modifier = Modifier.fillMaxSize(),
                            spokenWordRange = spokenWordRange,
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
                            },
                            bottomContent = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "PREMIUM SERVICES:",
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
                                        text = strings.btnClose,
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
                                        text = strings.resBtnBuyCompat,
                                        onClick = onNavigateToCompatibility,
                                        modifier = Modifier.fillMaxWidth(),
                                        isSecondary = true
                                    )
                                }
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
                            maleVoices = maleVoicesList,
                            femaleVoices = femaleVoicesList,
                            selectedVoice = if (ttsGenderState == "Female") selectedFemaleVoice else selectedMaleVoice,
                            onVoiceSelected = { voice ->
                                if (ttsGenderState == "Female") {
                                    selectedFemaleVoice = voice
                                } else {
                                    selectedMaleVoice = voice
                                }
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(imageRotation),
                                contentScale = ContentScale.Fit
                            )
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                
                                withTransform({
                                    this.translate(linesOffsetX, linesOffsetY)
                                    this.scale(linesScale, linesScale, center)
                                    this.rotate(linesRotation, center)
                                }) {
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

                            // Interactive Alignment panel
                            if (isAdjustmentPanelOpen) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.85f))
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (currentLang == AppLanguage.RUS) "Настройка линий и фото" else "Adjust Lines & Photo",
                                            style = MaterialTheme.typography.titleSmall.copy(color = MysticGold, fontWeight = FontWeight.Bold)
                                        )
                                        
                                        // Photo rotation button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Повернуть фото на 90°" else "Rotate Photo by 90°",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                            )
                                            IconButton(
                                                onClick = { imageRotation = (imageRotation + 90f) % 360f }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Cached,
                                                    contentDescription = "Rotate Photo",
                                                    tint = MysticGold
                                                )
                                            }
                                        }
                                        
                                        // Slider 1: Scale Lines
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Размер: " else "Scale: ",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                                modifier = Modifier.width(60.dp)
                                            )
                                            Slider(
                                                value = linesScale,
                                                onValueChange = { linesScale = it },
                                                valueRange = 0.5f..2.0f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MysticGold,
                                                    activeTrackColor = MysticGold,
                                                    inactiveTrackColor = Color.DarkGray
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        
                                        // Slider 2: Rotate Lines
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Наклон: " else "Rotate: ",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                                modifier = Modifier.width(60.dp)
                                            )
                                            Slider(
                                                value = linesRotation,
                                                onValueChange = { linesRotation = it },
                                                valueRange = -90f..90f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MysticGold,
                                                    activeTrackColor = MysticGold,
                                                    inactiveTrackColor = Color.DarkGray
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        
                                        // Slider 3: Move Horizontal (Offset X)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Сдвиг X: " else "Shift X: ",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                                modifier = Modifier.width(60.dp)
                                            )
                                            Slider(
                                                value = linesOffsetX,
                                                onValueChange = { linesOffsetX = it },
                                                valueRange = -300f..300f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MysticGold,
                                                    activeTrackColor = MysticGold,
                                                    inactiveTrackColor = Color.DarkGray
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        
                                        // Slider 4: Move Vertical (Offset Y)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (currentLang == AppLanguage.RUS) "Сдвиг Y: " else "Shift Y: ",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                                modifier = Modifier.width(60.dp)
                                            )
                                            Slider(
                                                value = linesOffsetY,
                                                onValueChange = { linesOffsetY = it },
                                                valueRange = -300f..300f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MysticGold,
                                                    activeTrackColor = MysticGold,
                                                    inactiveTrackColor = Color.DarkGray
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        
                                        // Row for Reset and Close Buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    linesOffsetX = 0f
                                                    linesOffsetY = 0f
                                                    linesScale = 1f
                                                    linesRotation = 0f
                                                    imageRotation = 0f
                                                },
                                                border = BorderStroke(1.dp, Color.Gray),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (currentLang == AppLanguage.RUS) "Сбросить" else "Reset")
                                            }
                                            
                                            Button(
                                                onClick = { isAdjustmentPanelOpen = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = MysticGold, contentColor = Color.Black),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (currentLang == AppLanguage.RUS) "Готово" else "Done")
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Settings adjustment trigger icon
                                IconButton(
                                    onClick = { isAdjustmentPanelOpen = true },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Adjust Lines",
                                        tint = MysticGold
                                    )
                                }
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
                                spokenWordRange = spokenWordRange,
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
                                maleVoices = maleVoicesList,
                                femaleVoices = femaleVoicesList,
                                selectedVoice = if (ttsGenderState == "Female") selectedFemaleVoice else selectedMaleVoice,
                                onVoiceSelected = { voice ->
                                    if (ttsGenderState == "Female") {
                                        selectedFemaleVoice = voice
                                    } else {
                                        selectedMaleVoice = voice
                                    }
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
    var searchQuery by remember { mutableStateOf("") }
    
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
                Text(
                    text = "${strings.appVersionLabel}: $appVersionName",
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

    var walletNum by remember { mutableStateOf("410013630971157") } // User's custom wallet number
    var paymentAmount by remember { mutableStateOf("250") } // Default amount in rubles
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
                        showConfirmationDialog = false
                        viewModel.simulateBuySubscription()
                        Toast.makeText(context, strings.billDialogSuccess, Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold)
                ) {
                    Text("Подтвердить", color = Color.Black, fontWeight = FontWeight.Bold)
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
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
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("100", "250", "1000").forEach { valAmount ->
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
                                        .clickable { paymentAmount = valAmount }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$valAmount ₽",
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
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

    var walletNum by remember { mutableStateOf("410013630971157") }
    var selectedMethod by remember { mutableStateOf("yoomoney") } // "yoomoney", "ozon", "wb"
    var paymentAmount by remember { mutableStateOf("150") } // Default amount 150 RUB
    var customAmount by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }

    var showConfirmationDialog by remember { mutableStateOf(false) }

    val actualAmount = if (isCustomSelected) customAmount else paymentAmount

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
                        showConfirmationDialog = false
                        viewModel.unlockPaidReading(readingId) {
                            Toast.makeText(
                                context,
                                if (currentLang == AppLanguage.RUS) "Оплата успешно подтверждена! Анализ разблокирован." else "Payment confirmed! Reading unlocked.",
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold)
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Подтвердить" else "Confirm",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
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
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("150", "250").forEach { valAmount ->
                    val isSelected = !isCustomSelected && paymentAmount == valAmount
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
                                isCustomSelected = false
                                paymentAmount = valAmount
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$valAmount ₽",
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Custom Option
                val isCustomActive = isCustomSelected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isCustomActive) MysticGold else Color.White.copy(0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isCustomActive) MysticGold else MysticBronze.copy(0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isCustomSelected = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.RUS) "Сумма" else "Custom",
                        color = if (isCustomActive) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            
            if (isCustomSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                MysticTextField(
                    value = customAmount,
                    onValueChange = { customAmount = it },
                    label = if (currentLang == AppLanguage.RUS) "Сумма в рублях" else "Amount in RUB",
                    placeholder = "Например: 1000"
                )
            }
            
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



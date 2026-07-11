package com.aistudio.hiromant.kxsrwa

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.aistudio.hiromant.kxsrwa.ui.PalmistViewModel
import com.aistudio.hiromant.kxsrwa.ui.language.AppLanguage
import com.aistudio.hiromant.kxsrwa.ui.language.LocalizedStrings
import com.aistudio.hiromant.kxsrwa.ui.screens.*
import com.aistudio.hiromant.kxsrwa.ui.theme.MyApplicationTheme
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticBronze
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticDarkBackground
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticDarkSurface
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {

    private val viewModel: PalmistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission dynamically for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }

        // Check if there was a previous crash, and display a helpful diagnostic screen if so
        val sharedPrefs = getSharedPreferences("palmist_prefs", Context.MODE_PRIVATE)
        val lastCrash = sharedPrefs.getString("last_crash_log", null)

        if (lastCrash != null) {
            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFFD4AF37),
                        background = Color(0xFF0C0C14),
                        surface = Color(0xFF141420)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0C0C14))
                            .padding(24.dp)
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFCF6679),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Произошел сбой приложения",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Скопируйте текст ошибки ниже и пришлите его нам для исправления:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Scrollable text area for the crash log
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray.copy(0.3f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = lastCrash,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCF6679)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val context = LocalContext.current
                            
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                            data = android.net.Uri.parse("mailto:")
                                            putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("ArsMaxim@gmail.com"))
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Crash Report: Palmist App")
                                            putExtra(android.content.Intent.EXTRA_TEXT, lastCrash)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Отправить отчет..."))
                                    } catch (ex: Exception) {
                                        try {
                                            val backupIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("ArsMaxim@gmail.com"))
                                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Crash Report: Palmist App")
                                                putExtra(android.content.Intent.EXTRA_TEXT, lastCrash)
                                            }
                                            context.startActivity(android.content.Intent.createChooser(backupIntent, "Отправить отчет..."))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Не удалось открыть почтовый клиент", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Отправить отчет об ошибке разработчику\n(ArsMaxim@gmail.com)",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Crash Log", lastCrash)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Лог скопирован", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MysticBronze),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Скопировать")
                                }
                                
                                Button(
                                    onClick = {
                                        sharedPrefs.edit().remove("last_crash_log").commit()
                                        // Relaunch the MainActivity clean
                                        val intent = intent
                                        finish()
                                        startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MysticGold),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Сбросить и запустить", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
            return
        }

        // Normal launch path wrapped in a robust catch block
        try {
            // Setup real-time system ongoing notification
            lifecycleScope.launch {
                viewModel.vpnConnected.collect { updateSystemNotification() }
            }
            lifecycleScope.launch {
                viewModel.vpnConnecting.collect { updateSystemNotification() }
            }
            lifecycleScope.launch {
                viewModel.vpnFlag.collect { updateSystemNotification() }
            }
            lifecycleScope.launch {
                viewModel.selectedVpnAppName.collect { updateSystemNotification() }
            }
            lifecycleScope.launch {
                viewModel.aiAvailabilityStatus.collect { updateSystemNotification() }
            }
            lifecycleScope.launch {
                viewModel.vpnUploadSpeed.collect { updateSystemNotification() }
            }
            lifecycleScope.launch {
                viewModel.vpnDownloadSpeed.collect { updateSystemNotification() }
            }
            lifecycleScope.launch {
                viewModel.selectedLanguage.collect { updateSystemNotification() }
            }

            setContent {
                MyApplicationTheme {
                    val navController = rememberNavController()
                    val fontScaleValue by viewModel.fontScale.collectAsState()
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val customDensity = remember(density, fontScaleValue) {
                        object : androidx.compose.ui.unit.Density by density {
                            override val fontScale: Float
                                get() = density.fontScale * fontScaleValue
                        }
                    }
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalDensity provides customDensity
                    ) {
                        val startDest = remember {
                            if (viewModel.isLanguageSelected()) "splash" else "language"
                        }

                        val currentBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = currentBackStackEntry?.destination?.route

                        var showVpnSheet by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MysticDarkBackground)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = startDest,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    // 1. Language selector
                                    composable("language") {
                                        LanguageSelectionScreen(
                                            viewModel = viewModel,
                                            onNavigateToSplash = {
                                                val isAlreadySelected = viewModel.isLanguageSelected()
                                                viewModel.markLanguageSelected()
                                                if (isAlreadySelected) {
                                                    navController.popBackStack()
                                                } else {
                                                    navController.navigate("splash") {
                                                        popUpTo("language") { inclusive = true }
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    // 2. Parchment splash animation
                                    composable("splash") {
                                        MysticSplashScreen(
                                            viewModel = viewModel,
                                            onNavigateNext = { navController.navigate("vpn") }
                                        )
                                    }

                                    // VPN Setup
                                    composable("vpn") {
                                        VpnScreen(
                                            viewModel = viewModel,
                                            onNavigateNext = {
                                                navController.navigate("auth") {
                                                    popUpTo("vpn") { inclusive = true }
                                                }
                                            }
                                        )
                                    }

                                    // 3. Optional authorization
                                    composable("auth") {
                                        AuthScreen(
                                            viewModel = viewModel,
                                            onNavigateNext = { navController.navigate("profile") }
                                        )
                                    }

                                    // 4. Profile Details Form
                                    composable("profile") {
                                        ProfileScreen(
                                            viewModel = viewModel,
                                            onNavigateNext = {
                                                navController.navigate("main_container") {
                                                    popUpTo("profile") { inclusive = true }
                                                }
                                            }
                                        )
                                    }

                                    // 5. Main dashboard with bottom navigation bar tabs
                                    composable("main_container") {
                                        MainContainerScreen(
                                            viewModel = viewModel,
                                            onNavigateToLoading = { navController.navigate("loading") },
                                            onNavigateToBilling = { navController.navigate("billing") },
                                            onNavigateToSettings = { navController.navigate("settings") }
                                        )
                                    }

                                    // 6. Progressive loader screen
                                    composable("loading") {
                                        val isAnalyzing by viewModel.isAnalyzing.collectAsState()
                                        
                                        LaunchedEffect(isAnalyzing) {
                                            if (!isAnalyzing) {
                                                navController.navigate("result") {
                                                    popUpTo("loading") { inclusive = true }
                                                }
                                            }
                                        }

                                        MysticLoadingScreen(viewModel = viewModel)
                                    }

                                    // 7. Analysis Report Display + interactive palm map
                                    composable("result") {
                                        ResultsScreen(
                                            viewModel = viewModel,
                                            onNavigateToCompatibility = {
                                                navController.navigate("main_container")
                                            },
                                            onNavigateToBilling = { navController.navigate("billing") },
                                            onClose = {
                                                navController.navigate("main_container") {
                                                    popUpTo("main_container") { inclusive = true }
                                                }
                                            }
                                        )
                                    }

                                    // 8. Payment panel (Yandex Pay / YooKassa / Play billing)
                                    composable("billing") {
                                        BillingScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }

                                    // 9. Settings screen
                                    composable("settings") {
                                        SettingsScreen(
                                            viewModel = viewModel,
                                            onNavigateToLanguage = { navController.navigate("language") },
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                }
                                if (currentRoute != "splash" && currentRoute != "language") {
                                    GlobalVpnBottomBar(viewModel = viewModel, onOpenSheet = { showVpnSheet = true })
                                }
                            }

                            // Sliding VPN Sheet on top of everything
                            AnimatedVisibility(
                                visible = showVpnSheet,
                                enter = slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                                ) + fadeIn(),
                                exit = slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                                ) + fadeOut()
                            ) {
                                GlobalVpnSheet(
                                    viewModel = viewModel,
                                    onClose = { showVpnSheet = false }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            // Log any early runtime crash to a file
            logException(e)
            val logFile = File(getExternalFilesDir(null), "crash.log")
            Toast.makeText(
                this,
                "Ошибка запуска: ${e.message}\nЛог записан в ${logFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
            throw e
        }
    }

    private fun updateSystemNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "cosmic_vpn_channel"

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Cosmic VPN Status",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Shows real-time Cosmic VPN speed and status"
                    setShowBadge(false)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val isConnected = viewModel.vpnConnected.value
            val isConnecting = viewModel.vpnConnecting.value
            val flag = viewModel.vpnFlag.value
            val appName = viewModel.selectedVpnAppName.value
            val aiStatus = viewModel.aiAvailabilityStatus.value
            val isRu = viewModel.selectedLanguage.value == AppLanguage.RUS

            if (!isConnected && !isConnecting) {
                notificationManager.cancel(8888)
                return
            }

            val titleText = if (isConnecting) {
                if (isRu) "$flag Установка соединения... ($appName)" else "$flag Connecting... ($appName)"
            } else {
                if (isRu) "$flag $appName: Трафик защищен (VPN)" else "$flag $appName: Traffic Secured (VPN)"
            }

            val simUpload = viewModel.vpnUploadSpeed.value
            val simDownload = viewModel.vpnDownloadSpeed.value

            fun formatSpeed(speedKb: Double): String {
                return if (speedKb >= 1000.0) {
                    val mb = speedKb / 1024.0
                    String.format(java.util.Locale.US, "%04.1f Mb", mb)
                } else {
                    String.format(java.util.Locale.US, "%04.1f Kb", speedKb)
                }
            }

            val speedText = "↑${formatSpeed(simUpload)} / ↓${formatSpeed(simDownload)}"

            val aiStatusStr = when (aiStatus) {
                "available" -> if (isRu) "AI: Доступен" else "AI: Available"
                "unavailable" -> if (isRu) "AI: Недоступен" else "AI: Unavailable"
                else -> if (isRu) "AI: Проверка" else "AI: Checking"
            }

            val vpnStatusStr = if (isConnected) (if (isRu) "VPN: АКТИВЕН" else "VPN: ACTIVE") else (if (isRu) "VPN: ПОДКЛЮЧЕНИЕ" else "VPN: CONNECTING")

            val contentText = "$flag $speedText • $aiStatusStr • $vpnStatusStr"

            val intent = android.content.Intent(this, MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                this,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle(titleText)
                .setContentText(contentText)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)

            notificationManager.notify(8888, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logException(e: Throwable) {
        try {
            val logDir = getExternalFilesDir(null)
            if (logDir != null && (logDir.exists() || logDir.mkdirs())) {
                val logFile = File(logDir, "crash.log")
                val writer = FileWriter(logFile, true)
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                e.printStackTrace(pw)
                writer.write("\n--- Crash at ${System.currentTimeMillis()} ---\n")
                writer.write(sw.toString())
                writer.write("\n--- End of crash ---\n\n")
                writer.flush()
                writer.close()
            }
        } catch (ex: Exception) {
            Log.e("MainActivity", "Не удалось записать лог", ex)
        }
    }
}

@Composable
fun GlobalVpnBottomBar(
    viewModel: PalmistViewModel,
    onOpenSheet: () -> Unit
) {
    val ip by viewModel.vpnIp.collectAsState()
    val flag by viewModel.vpnFlag.collectAsState()
    val aiStatus by viewModel.aiAvailabilityStatus.collectAsState()
    val isConnected by viewModel.vpnConnected.collectAsState()
    val currentLang by viewModel.selectedLanguage.collectAsState()

    val isRu = currentLang == AppLanguage.RUS

    val aiStatusText = when (aiStatus) {
        "available" -> if (isRu) "AI: Доступен" else "AI: Available"
        "unavailable" -> if (isRu) "AI: Недоступен" else "AI: Unavailable"
        else -> if (isRu) "AI: Проверка..." else "AI: Checking..."
    }

    val statusColor = when (aiStatus) {
        "available" -> Color(0xFF4CAF50) // Green
        "unavailable" -> Color(0xFFF44336) // Red
        else -> Color(0xFFFF9800) // Orange
    }

    val simUpload by viewModel.vpnUploadSpeed.collectAsState()
    val simDownload by viewModel.vpnDownloadSpeed.collectAsState()

    fun formatSpeed(speedKb: Double): String {
        return if (speedKb >= 1000.0) {
            val mb = speedKb / 1024.0
            String.format(java.util.Locale.US, "%04.1f Mb", mb)
        } else {
            String.format(java.util.Locale.US, "%04.1f Kb", speedKb)
        }
    }

    val speedText = "↑${formatSpeed(simUpload)} / ↓${formatSpeed(simDownload)}"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount < -15) {
                        onOpenSheet()
                    }
                }
            }
            .clickable {
                onOpenSheet()
            },
        color = Color(0xFF0C0C14),
        border = BorderStroke(1.dp, MysticGold.copy(0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Flag & Speed
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$flag ",
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = MysticGold.copy(0.7f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = speedText,
                    color = MysticGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // 2. AI Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = aiStatusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. VPN status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.VpnLock else Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = if (isConnected) Color(0xFF2ECC71) else Color.Gray,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (isConnected) (if (isRu) "ВКЛ" else "ON") else (if (isRu) "ВЫКЛ" else "OFF"),
                    color = if (isConnected) Color(0xFF2ECC71) else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun GlobalVpnSheet(
    viewModel: PalmistViewModel,
    onClose: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val isRussian = currentLang == AppLanguage.RUS

    val countries = remember {
        listOf(
            com.aistudio.hiromant.kxsrwa.ui.screens.VpnCountry("Германия (Франкфурт)", "Germany (Frankfurt)", "🇩🇪", "185.220.101.42", "38 ms"),
            com.aistudio.hiromant.kxsrwa.ui.screens.VpnCountry("Нидерланды (Амстердам)", "Netherlands (Amsterdam)", "🇳🇱", "195.206.105.18", "42 ms"),
            com.aistudio.hiromant.kxsrwa.ui.screens.VpnCountry("США (Нью-Йорк)", "United States (New York)", "🇺🇸", "104.244.75.12", "112 ms"),
            com.aistudio.hiromant.kxsrwa.ui.screens.VpnCountry("Сингапур (Чанги)", "Singapore (Changi)", "🇸🇬", "116.12.43.90", "180 ms"),
            com.aistudio.hiromant.kxsrwa.ui.screens.VpnCountry("Великобритания (Лондон)", "United Kingdom (London)", "🇬🇧", "94.46.20.111", "48 ms"),
            com.aistudio.hiromant.kxsrwa.ui.screens.VpnCountry("Финляндия (Хельсинки)", "Finland (Helsinki)", "🇫🇮", "95.175.99.3", "29 ms")
        )
    }

    val countryIndex by viewModel.vpnCountryIndex.collectAsState()
    val selectedCountry = countries[countryIndex.coerceIn(0, countries.lastIndex)]
    val isConnected by viewModel.vpnConnected.collectAsState()
    val isConnecting by viewModel.vpnConnecting.collectAsState()
    val durationSeconds by viewModel.vpnDurationSeconds.collectAsState()
    val kbReceived by viewModel.vpnKbReceived.collectAsState()
    val kbSent by viewModel.vpnKbSent.collectAsState()
    val aiStatus by viewModel.aiAvailabilityStatus.collectAsState()

    var showCountrySelector by remember { mutableStateOf(false) }
    var showAppSelector by remember { mutableStateOf(false) }
    val availableApps by viewModel.availableApps.collectAsState()
    val selectedAppName by viewModel.selectedVpnAppName.collectAsState()
    var connectionStep by remember { mutableStateOf("") }

    // Connecting steps animation
    LaunchedEffect(isConnecting) {
        if (isConnecting) {
            val steps = if (isRussian) {
                listOf(
                    "Поиск оптимальных созвездий серверов...",
                    "Авторизация в Celestial-Tunnel...",
                    "Применение сквозного шифрования AES-256...",
                    "Канал связи успешно установлен!"
                )
            } else {
                listOf(
                    "Searching for optimal server nodes...",
                    "Authenticating in Celestial-Tunnel...",
                    "Applying AES-256-GCM encryption...",
                    "Secure channel established!"
                )
            }
            for (step in steps) {
                connectionStep = step
                delay(700)
            }
            viewModel.vpnConnecting.value = false
            viewModel.vpnConnected.value = true
        }
    }

    // Capture background overlay touch to dismiss/close the sheet
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.7f))
            .clickable { onClose() }
    ) {
        // Bottom drawer content
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {} // block click propagation
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (dragAmount > 15) { // drag down to dismiss
                            onClose()
                        }
                    }
                },
            color = MysticDarkBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.5.dp, MysticGold.copy(0.3f))
        ) {
            // Starry decorative canvas
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val random = java.util.Random(42)
                for (i in 0..40) {
                    val x = random.nextFloat() * size.width
                    val y = random.nextFloat() * size.height
                    val radius = random.nextFloat() * 1.5f + 1f
                    val alpha = random.nextFloat() * 0.4f + 0.15f
                    drawCircle(
                        color = MysticGold.copy(alpha = alpha),
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .background(Color.White.copy(0.2f), RoundedCornerShape(2.5.dp))
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Title header row with close button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnLock,
                        contentDescription = null,
                        tint = MysticGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isRussian) "КОСМИЧЕСКИЙ VPN" else "COSMIC VPN GATEWAY",
                        fontSize = 18.sp,
                        color = MysticGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(Color.White.copy(0.04f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = if (isRussian) "Обход блокировок AI для точных предсказаний" else "Bypassing AI geoblocks for precise readings",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, bottom = 16.dp)
                )

                Spacer(modifier = Modifier.weight(0.1f))

                // Central Pulsing Connect Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.05f,
                        targetValue = 0.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    val glowColor = if (isConnected) Color(0xFF2ECC71) else MysticGold
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale)
                            .background(glowColor.copy(alpha = pulseAlpha), CircleShape)
                            .border(1.5.dp, glowColor.copy(alpha = pulseAlpha * 2), CircleShape)
                    )

                    if (isConnecting) {
                        val angle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .rotate(angle)
                                .border(
                                    width = 2.dp,
                                    brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                                        listOf(
                                            Color.Transparent,
                                            MysticGold.copy(0.1f),
                                            MysticGold,
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(2.dp, if (isConnected) Color(0xFF2ECC71) else MysticBronze),
                        modifier = Modifier
                            .size(110.dp)
                            .clickable {
                                if (isConnecting) return@clickable
                                if (isConnected) {
                                    viewModel.vpnConnected.value = false
                                } else {
                                    viewModel.vpnConnecting.value = true
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = if (isConnected) Color(0xFF2ECC71) else Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isConnecting) {
                                    if (isRussian) "СВЯЗЬ" else "CONNECT"
                                } else if (isConnected) {
                                    if (isRussian) "АКТИВЕН" else "ACTIVE"
                                } else {
                                    if (isRussian) "ОТКЛЮЧЕН" else "INACTIVE"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isConnected) Color(0xFF2ECC71) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Connection Step message
                AnimatedVisibility(
                    visible = isConnecting,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Text(
                        text = connectionStep,
                        color = MysticGold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Selected Server Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                    border = BorderStroke(1.dp, MysticBronze.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCountrySelector = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(0.05f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedCountry.flag, fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isRussian) "Местоположение" else "Server Location",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = if (isRussian) selectedCountry.nameRu else selectedCountry.nameEn,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MysticGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Stats Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // IP Address Box
                    Card(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MysticDarkSurface.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color.White.copy(0.04f))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "IP ADDRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isConnected) selectedCountry.ip else "---.---.---.---",
                                color = if (isConnected) MysticGold else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Ping Box
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MysticDarkSurface.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color.White.copy(0.04f))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "LATENCY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isConnected) selectedCountry.ping else "---",
                                color = if (isConnected) Color(0xFF2ECC71) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Availability Status Card (Non-clickable)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticDarkSurface.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, if (aiStatus == "available") Color(0xFF2ECC71).copy(0.3f) else Color(0xFFCF6679).copy(0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (aiStatus == "checking") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = MysticGold,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRussian) "Проверка доступности AI..." else "Checking AI availability...",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (aiStatus == "available") {
                            Text(
                                text = if (isRussian) "Для этой локации доступен AI" else "AI is available for this location",
                                color = Color(0xFF2ECC71),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = if (isRussian) "AI недоступен в данной локации..." else "AI is unavailable in this location...",
                                color = Color(0xFFCF6679),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Separate "Выбрать приложение" Button
                com.aistudio.hiromant.kxsrwa.ui.components.MysticButton(
                    text = if (isRussian) "ВЫБРАТЬ ПРИЛОЖЕНИЕ" else "CHOOSE APPLICATION",
                    onClick = {
                        showAppSelector = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isSecondary = true,
                    height = 48.dp
                )

                Spacer(modifier = Modifier.weight(0.15f))

                // Compact status row (Flag, Speed, AI Status, VPN Status) matching the bottom bar
                val isRu = currentLang == AppLanguage.RUS
                val flagVal by viewModel.vpnFlag.collectAsState()
                val simUploadVal by viewModel.vpnUploadSpeed.collectAsState()
                val simDownloadVal by viewModel.vpnDownloadSpeed.collectAsState()

                fun formatVpnSpeed(speedKb: Double): String {
                    return if (speedKb >= 1000.0) {
                        val mb = speedKb / 1024.0
                        String.format(java.util.Locale.US, "%04.1f Mb", mb)
                    } else {
                        String.format(java.util.Locale.US, "%04.1f Kb", speedKb)
                    }
                }
                val speedTextVal = "↑${formatVpnSpeed(simUploadVal)} / ↓${formatVpnSpeed(simDownloadVal)}"

                val aiStatusText = when (aiStatus) {
                    "available" -> if (isRu) "AI: Доступен" else "AI: Available"
                    "unavailable" -> if (isRu) "AI: Недоступен" else "AI: Unavailable"
                    else -> if (isRu) "AI: Проверка..." else "AI: Checking..."
                }
                val statusColor = when (aiStatus) {
                    "available" -> Color(0xFF4CAF50)
                    "unavailable" -> Color(0xFFF44336)
                    else -> Color(0xFFFF9800)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Flag & Speed
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$flagVal ",
                                fontSize = 11.sp
                            )
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = MysticGold.copy(0.7f),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = speedTextVal,
                                color = MysticGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // 2. AI Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = aiStatusText,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // 3. VPN status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.VpnLock else Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = if (isConnected) Color(0xFF2ECC71) else Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isConnected) (if (isRu) "ВКЛ" else "ON") else (if (isRu) "ВЫКЛ" else "OFF"),
                                color = if (isConnected) Color(0xFF2ECC71) else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    // App Selector Dialog inside bottom sheet
    if (showAppSelector) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAppSelector = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                border = BorderStroke(1.5.dp, MysticGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (isRussian) "Приложение для VPN" else "App for VPN",
                        style = MaterialTheme.typography.titleLarge,
                        color = MysticGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = if (isRussian) "Трафик выбранного приложения будет направлен через VPN." else "Traffic from the selected app will be routed through the secure VPN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val context = androidx.compose.ui.platform.LocalContext.current
                    val pm = remember { context.packageManager }

                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        items(availableApps) { app ->
                            val isCurrent = app.name == selectedAppName
                            val appIcon = remember(app.packageName) {
                                try {
                                    pm.getApplicationIcon(app.packageName)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrent) MysticGold.copy(0.12f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrent) MysticGold else Color.White.copy(0.05f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.selectedVpnAppName.value = app.name
                                        viewModel.selectedVpnAppPackage.value = app.packageName
                                        showAppSelector = false
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White.copy(0.05f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (appIcon != null) {
                                        androidx.compose.foundation.Image(
                                            painter = coil.compose.rememberAsyncImagePainter(appIcon),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = if (app.name.contains("ChatGPT") || app.name.contains("Gemini") || app.name.contains("Copilot") || app.name.contains("Claude")) "🤖" 
                                                   else if (app.name.contains("Telegram")) "💬" 
                                                   else if (app.name.contains("Instagram")) "📸" 
                                                   else if (app.name.contains("YouTube")) "📺" 
                                                   else "📱",
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (app.isInstalled) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF2ECC71).copy(0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isRussian) "Установлено" else "Installed",
                                            color = Color(0xFF2ECC71),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showAppSelector = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = if (isRussian) "Отмена" else "Cancel", color = MysticGold)
                    }
                }
            }
        }
    }

    // Country Selector Dialog inside bottom sheet
    if (showCountrySelector) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCountrySelector = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MysticDarkSurface),
                border = BorderStroke(1.5.dp, MysticGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (isRussian) "Выберите космический прокси" else "Choose Cosmic Proxy",
                        style = MaterialTheme.typography.titleLarge,
                        color = MysticGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(countries) { country ->
                            val isCurrent = country == selectedCountry
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrent) MysticGold.copy(0.12f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrent) MysticGold else Color.White.copy(0.05f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        val idx = countries.indexOf(country)
                                        if (idx >= 0) {
                                            viewModel.vpnCountryIndex.value = idx
                                        }
                                        showCountrySelector = false
                                        // Reconnect to new country simulation
                                        viewModel.vpnConnected.value = false
                                        viewModel.vpnConnecting.value = true
                                        viewModel.vpnDurationSeconds.value = 0
                                        viewModel.vpnKbReceived.value = 1.2
                                        viewModel.vpnKbSent.value = 0.8
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(country.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isRussian) country.nameRu else country.nameEn,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "IP: ${country.ip}",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isCurrent) MysticGold else Color.White.copy(0.05f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = country.ping,
                                        color = if (isCurrent) Color.Black else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showCountrySelector = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = if (isRussian) "Отмена" else "Cancel", color = MysticGold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainContainerScreen(
    viewModel: PalmistViewModel,
    onNavigateToLoading: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentLang by viewModel.selectedLanguage.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    var activeTab by remember { mutableStateOf("upload") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MysticDarkSurface,
                contentColor = MysticGold,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(
                        width = 1.dp,
                        color = MysticBronze.copy(0.2f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                // Upload / Scan
                NavigationBarItem(
                    selected = activeTab == "upload",
                    onClick = { activeTab = "upload" },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "upload") Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = null
                        )
                    },
                    label = { Text(strings.navScan, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MysticGold,
                        selectedTextColor = MysticGold,
                        indicatorColor = Color(0x22D4AF37),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // Partner Compatibility
                NavigationBarItem(
                    selected = activeTab == "compatibility",
                    onClick = { activeTab = "compatibility" },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "compatibility") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null
                        )
                    },
                    label = { Text(strings.navCompat, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MysticGold,
                        selectedTextColor = MysticGold,
                        indicatorColor = Color(0x22D4AF37),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // History Records
                NavigationBarItem(
                    selected = activeTab == "history",
                    onClick = { activeTab = "history" },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "history") Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = null
                        )
                    },
                    label = { Text(strings.navHistory, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MysticGold,
                        selectedTextColor = MysticGold,
                        indicatorColor = Color(0x22D4AF37),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                // About / FAQ Theory
                NavigationBarItem(
                    selected = activeTab == "about",
                    onClick = { activeTab = "about" },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "about") Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = null
                        )
                    },
                    label = { Text(strings.navAbout, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MysticGold,
                        selectedTextColor = MysticGold,
                        indicatorColor = Color(0x22D4AF37),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MysticDarkBackground)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "upload" -> UploadScreen(
                    viewModel = viewModel,
                    onNavigateToLoading = onNavigateToLoading,
                    onNavigateToBilling = onNavigateToBilling
                )
                "compatibility" -> CompatibilityScreen(
                    viewModel = viewModel,
                    onNavigateToLoading = onNavigateToLoading,
                    onNavigateToBilling = onNavigateToBilling
                )
                "history" -> HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToResult = onNavigateToLoading
                )
                "about" -> AboutScreen(viewModel = viewModel)
            }

            // Settings button in top-right corner
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MysticGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

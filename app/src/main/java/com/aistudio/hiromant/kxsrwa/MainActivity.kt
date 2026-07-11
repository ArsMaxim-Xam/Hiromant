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
import androidx.compose.foundation.clickable
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
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {

    private val viewModel: PalmistViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                                GlobalVpnBottomBar(viewModel, navController)
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
fun GlobalVpnBottomBar(viewModel: PalmistViewModel, navController: androidx.navigation.NavController) {
    val ip by viewModel.vpnIp.collectAsState()
    val flag by viewModel.vpnFlag.collectAsState()
    val aiStatus by viewModel.aiAvailabilityStatus.collectAsState()
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

    var simUpload by remember { mutableStateOf(45.2) }
    var simDownload by remember { mutableStateOf(112.8) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            simUpload = (15..95).random() / 10.0 + if ((0..1).random() == 1) (50..300).random() / 10.0 else 0.0
            simDownload = (80..350).random() / 10.0 + if ((0..1).random() == 1) (400..1500).random() / 10.0 else 0.0
        }
    }

    fun formatSpeed(speedKb: Double): String {
        return if (speedKb >= 1000.0) {
            val mb = speedKb / 1024.0
            String.format(java.util.Locale.US, "%05.1f Mb", mb)
        } else {
            String.format(java.util.Locale.US, "%05.1f Kb", speedKb)
        }
    }

    val speedText = "↑${formatSpeed(simUpload)} / ↓${formatSpeed(simDownload)}"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clickable {
                try {
                    navController.navigate("vpn") {
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        color = Color(0xFF0F0F1A),
        border = BorderStroke(1.dp, MysticGold.copy(0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MysticGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "IP: $ip $flag",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = speedText,
                color = MysticGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = aiStatusText,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
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

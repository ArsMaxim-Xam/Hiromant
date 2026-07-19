package com.aistudio.hiromant.kxsrwa

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            try {
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to show system bars on focus change: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to show system bars: ${e.message}")
        }

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

                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val showBottomBar = remember(currentRoute) {
                            currentRoute in listOf("main_container", "result", "billing", "settings")
                        }

                        val currentLang by viewModel.selectedLanguage.collectAsState()
                        val strings = LocalizedStrings.get(currentLang)
                        val activeTab by viewModel.activeTab.collectAsState()

                        Scaffold(
                            // 1. Убираем верхнюю панель с количеством анализов по требованию пользователя
                            topBar = {},
                            bottomBar = {
                                if (showBottomBar) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MysticDarkSurface)
                                            .border(
                                                width = 1.dp,
                                                color = MysticBronze.copy(0.2f),
                                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                            )
                                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                            .navigationBarsPadding()
                                            .height(60.dp)
                                            .padding(horizontal = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        val tabs = listOf(
                                            Triple("upload", strings.navScan, if (activeTab == "upload") Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome),
                                            Triple("compatibility", strings.navCompat, if (activeTab == "compatibility") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder),
                                            Triple("user_cabinet", if (currentLang == AppLanguage.RUS) "Кабинет" else "Cabinet", if (activeTab == "user_cabinet") Icons.Filled.Person else Icons.Outlined.Person),
                                            Triple("settings", if (currentLang == AppLanguage.RUS) "Настройки" else "Settings", if (activeTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings),
                                            Triple("about", strings.navAbout, if (activeTab == "about") Icons.Filled.Info else Icons.Outlined.Info)
                                        )

                                        tabs.forEach { (tabId, label, icon) ->
                                            val isSelected = if (currentRoute == "main_container") activeTab == tabId else {
                                                if (currentRoute == "settings" && tabId == "settings") true
                                                else if (currentRoute == "result" && tabId == "upload") true
                                                else false
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable(
                                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = {
                                                            viewModel.activeTab.value = tabId
                                                            if (currentRoute != "main_container") {
                                                                navController.navigate("main_container") {
                                                                    popUpTo("main_container") { inclusive = false }
                                                                }
                                                            }
                                                        }
                                                    )
                                                    .padding(vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                // Контейнер для иконки с поддержкой бейджа
                                                Box(
                                                    contentAlignment = Alignment.TopEnd
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MysticGold else Color.Gray,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    
                                                    // Рисуем цифру количества анализов на значке пользователя (Кабинет)
                                                    if (tabId == "user_cabinet") {
                                                        val billingStateVal by viewModel.billingState.collectAsState() // Получаем состояние биллинга
                                                        val count = billingStateVal?.remainingAnalyses ?: 0 // Извлекаем количество анализов
                                                        if (count > 0) { // Если доступно больше 0 анализов, рисуем бейдж
                                                            Box(
                                                                modifier = Modifier
                                                                    .offset(x = 6.dp, y = (-6).dp) // Смещаем бейдж в правый верхний угол иконки
                                                                    .background(Color.Red, shape = RoundedCornerShape(50)) // Красный скругленный фон
                                                                    .padding(horizontal = 4.dp, vertical = 1.dp) // Отступы вокруг текста цифры
                                                            ) {
                                                                Text(
                                                                    text = count.toString(), // Выводим число анализов
                                                                    color = Color.White, // Белый цвет текста
                                                                    fontSize = 7.sp, // Компактный размер шрифта для бейджа
                                                                    fontWeight = FontWeight.Bold // Жирный шрифт для читаемости
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 8.sp,
                                                    color = if (isSelected) MysticGold else Color.Gray,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MysticDarkBackground)
                                    .padding(innerPadding)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = startDest,
                                    modifier = Modifier.fillMaxSize()
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
                                            onNavigateNext = {
                                                val profile = viewModel.userProfile.value
                                                if (profile != null && profile.name.isNotBlank()) {
                                                    navController.navigate("main_container") {
                                                        popUpTo("splash") { inclusive = true }
                                                    }
                                                } else {
                                                    navController.navigate("profile") {
                                                        popUpTo("splash") { inclusive = true }
                                                    }
                                                }
                                            }
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
                                            onNavigateToLanguage = { navController.navigate("language") },
                                            onNavigateToVideoScan = { navController.navigate("video_scan") }
                                        )
                                    }

                                    // 10. Post-payment video recording screen
                                    composable("video_scan") {
                                        PostPaymentVideoScreen(
                                            viewModel = viewModel,
                                            onNavigateToLoading = {
                                                navController.navigate("loading") {
                                                    popUpTo("video_scan") { inclusive = true }
                                                }
                                            },
                                            onNavigateBack = { navController.popBackStack() }
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
fun MainContainerScreen(
    viewModel: PalmistViewModel,
    onNavigateToLoading: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToVideoScan: () -> Unit
) {
    val activeTab by viewModel.activeTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MysticDarkBackground)
    ) {
        when (activeTab) {
            "upload" -> UploadScreen(
                viewModel = viewModel,
                onNavigateToLoading = onNavigateToLoading,
                onNavigateToBilling = onNavigateToBilling,
                onNavigateToVideoScan = onNavigateToVideoScan
            )
            "compatibility" -> CompatibilityScreen(
                viewModel = viewModel,
                onNavigateToLoading = onNavigateToLoading,
                onNavigateToBilling = onNavigateToBilling
            )
            "user_cabinet" -> UserCabinetScreen(
                viewModel = viewModel,
                onNavigateToResult = onNavigateToLoading
            )
            "settings" -> SettingsScreen(
                viewModel = viewModel,
                onNavigateToLanguage = onNavigateToLanguage,
                onNavigateBack = { viewModel.activeTab.value = "upload" }
            )
            "about" -> AboutScreen(viewModel = viewModel)
        }
    }
}

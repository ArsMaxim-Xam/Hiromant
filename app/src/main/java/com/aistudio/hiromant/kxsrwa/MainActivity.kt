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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aistudio.hiromant.kxsrwa.ui.PalmistViewModel
import com.aistudio.hiromant.kxsrwa.ui.language.AppLanguage
import com.aistudio.hiromant.kxsrwa.ui.language.LocalizedStrings
import com.aistudio.hiromant.kxsrwa.ui.screens.*
import com.aistudio.hiromant.kxsrwa.ui.theme.MyApplicationTheme
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticBronze
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticDarkBackground
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticDarkSurface
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticGold
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
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val context = LocalContext.current
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Crash Log", lastCrash)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Лог скопирован", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MysticBronze),
                                    modifier = Modifier.weight(1.dp.value)
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
                                    modifier = Modifier.weight(1.dp.value)
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
                    val startDest = remember {
                        if (viewModel.isLanguageSelected()) "splash" else "language"
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDest,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MysticDarkBackground)
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
                                onNavigateNext = { navController.navigate("auth") }
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
                                onNavigateToBilling = { navController.navigate("billing") }
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
                                onNavigateToLanguage = { navController.navigate("language") }
                            )
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
                    label = { Text(strings.navScan, fontSize = 10.sp) },
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
                    label = { Text(strings.navCompat, fontSize = 10.sp) },
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
                    label = { Text(strings.navHistory, fontSize = 10.sp) },
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
                    label = { Text(strings.navAbout, fontSize = 10.sp) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSettings,
                containerColor = MysticGold,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
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
        }
    }
}

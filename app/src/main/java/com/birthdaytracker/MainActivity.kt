package com.birthdaytracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.birthdaytracker.navigation.AppNavigation
import com.birthdaytracker.navigation.Screen
import com.birthdaytracker.notification.BirthdayNotificationWorker
import com.birthdaytracker.ui.theme.BirthdayTrackerTheme
import com.birthdaytracker.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            BirthdayNotificationWorker.scheduleNotifications(this)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channel
        BirthdayNotificationWorker.createNotificationChannel(this)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    BirthdayNotificationWorker.scheduleNotifications(this)
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            BirthdayNotificationWorker.scheduleNotifications(this)
        }

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            // Get initial default view once at startup
            var initialView by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                initialView = settingsViewModel.defaultView.first()
            }

            // Don't render until we have the initial view
            if (initialView == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                return@setContent
            }

            BirthdayTrackerTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding(),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        getString(R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            if (currentRoute != Screen.Settings.route) {
                                                navController.navigate(Screen.Settings.route) {
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = getString(R.string.settings)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                ) { paddingValues ->
                    AppNavigation(
                        navController = navController,
                        startDestination = when (initialView) {
                            "calendar" -> Screen.CalendarView.route
                            else -> Screen.ListView.route
                        },
                        onThemeChange = { mode ->
                            settingsViewModel.setThemeMode(mode)
                        },
//                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
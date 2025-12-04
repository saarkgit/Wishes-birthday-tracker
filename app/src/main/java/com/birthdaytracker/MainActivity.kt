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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.birthdaytracker.navigation.AppNavigation
import com.birthdaytracker.navigation.Screen
import com.birthdaytracker.notification.BirthdayNotificationWorker
import com.birthdaytracker.ui.components.StableTopBar
import com.birthdaytracker.ui.theme.BirthdayTrackerTheme
import com.birthdaytracker.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, schedule notifications
            BirthdayNotificationWorker.scheduleNotifications(this)
        }
    }

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
                    // Show rationale if needed
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
            val defaultView by settingsViewModel.defaultView.collectAsState(initial = "list")

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val startDestination = when (defaultView) {
                "calendar" -> Screen.CalendarView.route
                else -> Screen.ListView.route
            }

            BirthdayTrackerTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        StableTopBar(
                            title = {
                                Text(
                                    getString(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        navController.navigate(Screen.Settings.route)
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = getString(R.string.settings)
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination,
                        onThemeChange = { mode ->
                            settingsViewModel.setThemeMode(mode)
                        },
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
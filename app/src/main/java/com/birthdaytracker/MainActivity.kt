package com.birthdaytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.birthdaytracker.data.BirthdayDatabase
import com.birthdaytracker.navigation.AppNavigation
import com.birthdaytracker.navigation.Screen
import com.birthdaytracker.notification.BirthdayNotificationWorker
import com.birthdaytracker.repository.BirthdayRepository
import com.birthdaytracker.ui.components.StableTopBar
import com.birthdaytracker.ui.theme.BirthdayTrackerTheme
import com.birthdaytracker.util.PreferencesManager
import com.birthdaytracker.viewmodel.BirthdayViewModel
import com.birthdaytracker.viewmodel.BirthdayViewModelFactory
import com.birthdaytracker.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize notification channel
        BirthdayNotificationWorker.createNotificationChannel(this)
        BirthdayNotificationWorker.scheduleNotifications(this)
        
        val database = BirthdayDatabase.getDatabase(this)
        val repository = BirthdayRepository(database.birthdayDao())
        val preferencesManager = PreferencesManager(this)
        
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel {
                SettingsViewModel(preferencesManager)
            }
            
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
                val birthdayViewModel: BirthdayViewModel = viewModel(
                    factory = BirthdayViewModelFactory(repository)
                )
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        StableTopBar(
                            title = { Text("Birthday Tracker", style = MaterialTheme.typography.titleLarge) },
                            actions = {
                                IconButton(onClick = {
                                    navController.navigate(Screen.Settings.route)
                                }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination,
                        birthdayViewModel = birthdayViewModel,
                        settingsViewModel = settingsViewModel,
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


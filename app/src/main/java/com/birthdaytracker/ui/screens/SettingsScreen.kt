package com.birthdaytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.birthdaytracker.ui.components.StableTopBar

@Composable
fun SettingsScreen(
    settingsViewModel: com.birthdaytracker.viewmodel.SettingsViewModel,
    onBack: () -> Unit,
    onThemeChange: (String) -> Unit
) {
    val defaultView by settingsViewModel.defaultView.collectAsState(initial = "list")
    val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")
    val notificationDayOf by settingsViewModel.notificationDayOf.collectAsState(initial = true)
    val notificationWeekBefore by settingsViewModel.notificationWeekBefore.collectAsState(initial = true)
    
    Scaffold(
        topBar = {
            StableTopBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingsSection(title = "Display") {
                SettingsItem(
                    title = "Default View",
                    subtitle = "Choose the default screen when opening the app"
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(
                                text = when (defaultView) {
                                    "list" -> "List View"
                                    "calendar" -> "Calendar View"
                                    else -> "List View"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("List View") },
                                onClick = {
                                    settingsViewModel.setDefaultView("list")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Calendar View") },
                                onClick = {
                                    settingsViewModel.setDefaultView("calendar")
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                SettingsItem(
                    title = "Theme",
                    subtitle = "Choose app theme"
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(
                                text = when (themeMode) {
                                    "light" -> "Light"
                                    "dark" -> "Dark"
                                    "system" -> "Follow System"
                                    else -> "Follow System"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Light") },
                                onClick = {
                                    settingsViewModel.setThemeMode("light")
                                    onThemeChange("light")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Dark") },
                                onClick = {
                                    settingsViewModel.setThemeMode("dark")
                                    onThemeChange("dark")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Follow System") },
                                onClick = {
                                    settingsViewModel.setThemeMode("system")
                                    onThemeChange("system")
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            SettingsSection(title = "Notifications") {
                SettingsItem(
                    title = "Day of Birthday",
                    subtitle = "Get notified on the day of the birthday"
                ) {
                    Switch(
                        checked = notificationDayOf,
                        onCheckedChange = { settingsViewModel.setNotificationDayOf(it) }
                    )
                }
                
                SettingsItem(
                    title = "Week Before",
                    subtitle = "Get notified one week before the birthday"
                ) {
                    Switch(
                        checked = notificationWeekBefore,
                        onCheckedChange = { settingsViewModel.setNotificationWeekBefore(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        action()
    }
}


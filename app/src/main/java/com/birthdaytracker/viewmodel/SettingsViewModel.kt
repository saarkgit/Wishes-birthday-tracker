package com.birthdaytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdaytracker.util.PreferencesManager
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    val defaultView = preferencesManager.defaultView
    val themeMode = preferencesManager.themeMode
    val notificationDayOf = preferencesManager.notificationDayOf
    val notificationWeekBefore = preferencesManager.notificationWeekBefore
    
    fun setDefaultView(view: String) {
        viewModelScope.launch {
            preferencesManager.setDefaultView(view)
        }
    }
    
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }
    
    fun setNotificationDayOf(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationDayOf(enabled)
        }
    }
    
    fun setNotificationWeekBefore(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationWeekBefore(enabled)
        }
    }
}


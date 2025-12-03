package com.birthdaytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdaytracker.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val defaultView = preferencesManager.defaultView
    val themeMode = preferencesManager.themeMode
    val notificationDayOf = preferencesManager.notificationDayOf
    val notificationWeekBefore = preferencesManager.notificationWeekBefore

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setDefaultView(view: String) {
        viewModelScope.launch {
            try {
                preferencesManager.setDefaultView(view)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update default view: ${e.message}"
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            try {
                preferencesManager.setThemeMode(mode)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update theme: ${e.message}"
            }
        }
    }

    fun setNotificationDayOf(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setNotificationDayOf(enabled)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update notification settings: ${e.message}"
            }
        }
    }

    fun setNotificationWeekBefore(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setNotificationWeekBefore(enabled)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update notification settings: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
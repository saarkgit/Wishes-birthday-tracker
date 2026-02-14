package com.birthdaytracker.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {


    companion object {
        private val DEFAULT_VIEW_KEY = stringPreferencesKey("default_view")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val NOTIFICATION_DAY_OF_KEY = booleanPreferencesKey("notification_day_of")
        private val NOTIFICATION_WEEK_BEFORE_KEY = booleanPreferencesKey("notification_week_before")
    }

    val defaultView: Flow<String> = dataStore.data.map { preferences ->
        preferences[DEFAULT_VIEW_KEY] ?: "list"
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "system"
    }

    val notificationDayOf: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_DAY_OF_KEY] ?: true
    }

    val notificationWeekBefore: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_WEEK_BEFORE_KEY] ?: true
    }

    suspend fun setDefaultView(view: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_VIEW_KEY] = view
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setNotificationDayOf(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_DAY_OF_KEY] = enabled
        }
    }

    suspend fun setNotificationWeekBefore(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_WEEK_BEFORE_KEY] = enabled
        }
    }
}
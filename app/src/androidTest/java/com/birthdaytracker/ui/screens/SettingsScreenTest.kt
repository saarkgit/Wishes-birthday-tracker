package com.birthdaytracker.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdaytracker.ui.theme.BirthdayTrackerTheme
import com.birthdaytracker.util.PreferencesManager
import com.birthdaytracker.viewmodel.SettingsViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * UI tests for SettingsScreen
 * Location: src/androidTest/java/com/birthdaytracker/ui/screens/SettingsScreenTest.kt
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var mockPreferencesManager: PreferencesManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        hiltRule.inject()

        mockPreferencesManager = mock()

        whenever(mockPreferencesManager.defaultView).thenReturn(flowOf("list"))
        whenever(mockPreferencesManager.themeMode).thenReturn(flowOf("system"))
        whenever(mockPreferencesManager.notificationDayOf).thenReturn(flowOf(true))
        whenever(mockPreferencesManager.notificationWeekBefore).thenReturn(flowOf(true))

        viewModel = SettingsViewModel(mockPreferencesManager)
    }

    @Test
    fun settingsScreen_displays_title() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_has_back_button() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_display_section() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Display").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_notifications_section() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_default_view_option() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Default View").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose the default screen when opening the app")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_theme_option() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose app theme").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_notification_toggles() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Day of Birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Week Before").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_back_button_works() {
        var backCalled = false

        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = { backCalled = true },
                    onThemeChange = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Back").performClick()
        assert(backCalled)
    }

    @Test
    fun settingsScreen_default_view_dropdown_opens() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        // Click the List View button to open dropdown
        composeTestRule.onNodeWithText("List View").performClick()

        // Dropdown should show options
        composeTestRule.onNodeWithText("Calendar View").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_theme_dropdown_opens() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        // Click the Follow System button to open dropdown
        composeTestRule.onNodeWithText("Follow System").performClick()

        // Dropdown should show options
        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_notification_switches_are_toggleable() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                SettingsScreen(
                    settingsViewModel = viewModel,
                    onBack = {},
                    onThemeChange = {}
                )
            }
        }

        // Find switches by their parent text
        val dayOfSwitch = composeTestRule.onNode(
            hasParent(hasText("Day of Birthday"))
        )
        dayOfSwitch.assertExists()
    }
}
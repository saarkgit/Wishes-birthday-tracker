package com.birthdaytracker.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.birthdaytracker.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

/**
 * Unit tests for SettingsViewModel
 * Location: src/test/java/com/birthdaytracker/viewmodel/SettingsViewModelTest.kt
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        preferencesManager = mock()

        // Default mock behavior
        whenever(preferencesManager.defaultView).thenReturn(flowOf("list"))
        whenever(preferencesManager.themeMode).thenReturn(flowOf("system"))
        whenever(preferencesManager.notificationDayOf).thenReturn(flowOf(true))
        whenever(preferencesManager.notificationWeekBefore).thenReturn(flowOf(true))

        viewModel = SettingsViewModel(preferencesManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setDefaultView calls preferences manager`() = runTest {
        viewModel.setDefaultView("calendar")
        advanceUntilIdle()

        verify(preferencesManager).setDefaultView("calendar")
    }

    @Test
    fun `setThemeMode calls preferences manager`() = runTest {
        viewModel.setThemeMode("dark")
        advanceUntilIdle()

        verify(preferencesManager).setThemeMode("dark")
    }

    @Test
    fun `setNotificationDayOf calls preferences manager`() = runTest {
        viewModel.setNotificationDayOf(false)
        advanceUntilIdle()

        verify(preferencesManager).setNotificationDayOf(false)
    }

    @Test
    fun `setNotificationWeekBefore calls preferences manager`() = runTest {
        viewModel.setNotificationWeekBefore(false)
        advanceUntilIdle()

        verify(preferencesManager).setNotificationWeekBefore(false)
    }

    @Test
    fun `defaultView flow emits correct value`() = runTest {
        whenever(preferencesManager.defaultView).thenReturn(flowOf("calendar"))

        val newViewModel = SettingsViewModel(preferencesManager)

        newViewModel.defaultView.test {
            assertEquals("calendar", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `themeMode flow emits correct value`() = runTest {
        whenever(preferencesManager.themeMode).thenReturn(flowOf("dark"))

        val newViewModel = SettingsViewModel(preferencesManager)

        newViewModel.themeMode.test {
            assertEquals("dark", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error message is set when preferences manager throws exception`() = runTest {
        whenever(preferencesManager.setThemeMode(any())).thenThrow(RuntimeException("Preferences error"))

        viewModel.setThemeMode("dark")
        advanceUntilIdle()

        viewModel.errorMessage.test {
            val error = awaitItem()
            assertEquals(true, error?.contains("Failed to update theme"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearError resets error message`() = runTest {
        // Trigger an error
        whenever(preferencesManager.setThemeMode(any())).thenThrow(RuntimeException("Error"))
        viewModel.setThemeMode("dark")
        advanceUntilIdle()

        // Clear the error
        viewModel.clearError()

        viewModel.errorMessage.test {
            assertEquals(null, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
package com.birthdaytracker.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdaytracker.data.BirthdayDao
import com.birthdaytracker.data.BirthdayDatabase
import com.birthdaytracker.ui.theme.BirthdayTrackerTheme
import com.birthdaytracker.viewmodel.BirthdayViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * UI tests for AddEditBirthdayScreen
 * Location: src/androidTest/java/com/birthdaytracker/ui/screens/AddEditBirthdayScreenTest.kt
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddEditBirthdayScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var database: BirthdayDatabase

    @Inject
    lateinit var dao: BirthdayDao

    @Before
    fun setup() = runTest {
        hiltRule.inject()
        database.clearAllTables()
    }

    @Test
    fun addBirthdayScreen_displays_correct_title() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Add Birthday").assertIsDisplayed()
    }

    @Test
    fun editBirthdayScreen_displays_correct_title() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = 1L,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Edit Birthday").assertIsDisplayed()
    }

    @Test
    fun addBirthdayScreen_has_name_field() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
    }

    @Test
    fun addBirthdayScreen_has_category_field() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Category (optional)").assertIsDisplayed()
    }

    @Test
    fun addBirthdayScreen_has_date_selector() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Select Date", substring = true).assertIsDisplayed()
    }

    @Test
    fun addBirthdayScreen_name_input_works() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Name")
            .performTextInput("John Doe")

        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    }

    @Test
    fun addBirthdayScreen_category_input_works() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Category (optional)")
            .performTextInput("Friend")

        composeTestRule.onNodeWithText("Friend").assertIsDisplayed()
    }

    @Test
    fun addBirthdayScreen_cancel_button_works() {
        var backCalled = false

        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = { backCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(backCalled)
    }

    @Test
    fun addBirthdayScreen_save_button_exists() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun editBirthdayScreen_shows_delete_button() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = 1L,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }

    @Test
    fun addBirthdayScreen_does_not_show_delete_button() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete").assertDoesNotExist()
    }

    @Test
    fun editBirthdayScreen_delete_shows_confirmation_dialog() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = 1L,
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        composeTestRule.onNodeWithText("Delete Birthday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this birthday?")
            .assertIsDisplayed()
    }
}
package com.birthdaytracker.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.birthdaytracker.TestUtils
import com.birthdaytracker.data.Birthday
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * UI tests for CalendarViewScreen
 * Location: src/androidTest/java/com/birthdaytracker/ui/screens/CalendarViewScreenTest.kt
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CalendarViewScreenTest {

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

        // Insert real test data
        dao.insertBirthday(
            TestUtils.createBirthdayOnDate(
                LocalDate.now().monthValue,
                15,
                "Alice Smith",
                1995,
                "Friend"
            )
        )
        dao.insertBirthday(
            TestUtils.createBirthdayOnDate(
                LocalDate.now().plusMonths(1).monthValue,
                10,
                "Bob Johnson",
                1990,
                "Family"
            )
        )
    }

    @Test
    fun calendarViewScreen_displays_current_month() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onDateClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        composeTestRule.onNodeWithText(currentMonth).assertIsDisplayed()
    }

    @Test
    fun calendarViewScreen_displays_day_headers() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onDateClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        // Check for day abbreviations
        composeTestRule.onNodeWithText("Sun").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sat").assertIsDisplayed()
    }

    @Test
    fun calendarViewScreen_add_button_works() {
        var addClicked = false

        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = { addClicked = true },
                    onDateClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Birthday").performClick()
        assert(addClicked)
    }

    @Test
    fun calendarViewScreen_previous_month_button_works() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onDateClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        val currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        val previousMonth =
            LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        // Click previous month button
        composeTestRule.onNodeWithContentDescription("Previous Month").performClick()

        // Should show previous month
        composeTestRule.onNodeWithText(previousMonth).assertIsDisplayed()
        composeTestRule.onNodeWithText(currentMonth).assertDoesNotExist()
    }

    @Test
    fun calendarViewScreen_next_month_button_works() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onDateClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        val nextMonth =
            LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("MMMM yyyy"))

        // Click next month button
        composeTestRule.onNodeWithContentDescription("Next Month").performClick()

        // Should show next month
        composeTestRule.onNodeWithText(nextMonth).assertIsDisplayed()
    }

    @Test
    fun calendarViewScreen_displays_birthdays_in_current_month() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onDateClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        // Alice's birthday is in current month
        composeTestRule.onNodeWithText("Alice Smith").assertIsDisplayed()
    }

    @Test
    fun calendarViewScreen_birthday_click_triggers_callback() {
        var clickedBirthday: Birthday? = null

        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = {},
                    onDateClick = {},
                    onBirthdayClick = { clickedBirthday = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Alice Smith").performClick()
        assert(clickedBirthday?.name == "Alice Smith")
    }
}
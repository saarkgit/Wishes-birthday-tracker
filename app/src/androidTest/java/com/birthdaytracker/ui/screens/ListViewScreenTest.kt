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
import javax.inject.Inject


//@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ListViewScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    // No more manual ViewModel or repository instances needed here

    @Inject
    lateinit var database: BirthdayDatabase

    @Inject
    lateinit var dao: BirthdayDao

    @Before
    fun setup() = runTest {
        hiltRule.inject()
        database.clearAllTables()

        // Insert real test data
        dao.insertBirthday(TestUtils.createBirthdayInDays(5, "Alice Smith", 25, "Friend"))
        dao.insertBirthday(TestUtils.createBirthdayInDays(10, "Bob Johnson", 30, "Family"))
    }

    @Test
    fun listViewScreen_displays_birthdays() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                // Let Hilt provide the ViewModel automatically.
                // hiltViewModel() will create a ViewModel using the bindings
                // from our TestAppModule, which includes the mock repository.
                val viewModel: BirthdayViewModel = hiltViewModel()
                ListViewScreen(
                    viewModel = hiltViewModel(),
                    onAddClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        // Assertions should now work against the mock data
        composeTestRule.onNodeWithText("Alice Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Johnson").assertIsDisplayed()
    }

    @Test
    fun listViewScreen_add_button_is_clickable() {
        var addClicked = false

        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                ListViewScreen(
                    viewModel = hiltViewModel(),
                    onAddClick = { addClicked = true },
                    onBirthdayClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add birthday").performClick()
        assert(addClicked)
    }

    @Test
    fun listViewScreen_birthday_cards_are_clickable() {
        var clickedBirthday: Birthday? = null

        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                ListViewScreen(
                    viewModel = hiltViewModel(),
                    onAddClick = {},
                    onBirthdayClick = { clickedBirthday = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Alice Smith").performClick()
        assert(clickedBirthday?.name == "Alice Smith")
    }

    @Test
    fun listViewScreen_sort_menu_opens() {
        composeTestRule.setContent {
            BirthdayTrackerTheme {
                val viewModel: BirthdayViewModel = hiltViewModel()
                ListViewScreen(
                    viewModel = hiltViewModel(),
                    onAddClick = {},
                    onBirthdayClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort").performClick()
        composeTestRule.onNodeWithText("Sort By").assertIsDisplayed()
    }
}

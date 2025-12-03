package com.birthdaytracker.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.birthdaytracker.ui.screens.AddEditBirthdayScreen
import com.birthdaytracker.ui.screens.CalendarViewScreen
import com.birthdaytracker.ui.screens.ListViewScreen
import com.birthdaytracker.ui.screens.SettingsScreen
import com.birthdaytracker.viewmodel.BirthdayViewModel
import com.birthdaytracker.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    object ListView : Screen("list_view")
    object CalendarView : Screen("calendar_view")
    object Settings : Screen("settings")
    object AddBirthday : Screen("add_birthday")
    object EditBirthday : Screen("edit_birthday/{birthdayId}") {
        fun createRoute(birthdayId: Long) = "edit_birthday/$birthdayId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    onThemeChange: (String) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.ListView.route) {
                val viewModel: BirthdayViewModel = hiltViewModel()
                ListViewScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Screen.AddBirthday.route) },
                    onBirthdayClick = { birthday ->
                        navController.navigate(Screen.EditBirthday.createRoute(birthday.id))
                    }
                )
            }

            composable(Screen.CalendarView.route) {
                val viewModel: BirthdayViewModel = hiltViewModel()
                CalendarViewScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Screen.AddBirthday.route) },
                    onDateClick = { date ->
                        navController.navigate(Screen.AddBirthday.route)
                    },
                    onBirthdayClick = { birthday ->
                        navController.navigate(Screen.EditBirthday.createRoute(birthday.id))
                    }
                )
            }

            composable(Screen.AddBirthday.route) {
                val viewModel: BirthdayViewModel = hiltViewModel()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = null,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EditBirthday.route) { backStackEntry ->
                val viewModel: BirthdayViewModel = hiltViewModel()
                val birthdayId = backStackEntry.arguments?.getString("birthdayId")?.toLongOrNull()
                AddEditBirthdayScreen(
                    viewModel = viewModel,
                    birthdayId = birthdayId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = { navController.popBackStack() },
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}
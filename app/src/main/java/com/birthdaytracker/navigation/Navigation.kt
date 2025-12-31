package com.birthdaytracker.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
//    isDarkTheme: Boolean,
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
                val defaultView by settingsViewModel.defaultView.collectAsState(initial = "list")

                val navigateBack: () -> Unit = {

                    val targetRoute = when (defaultView) {
                        "calendar" -> Screen.CalendarView.route
                        else -> Screen.ListView.route
                    }
                    // Optimization: check if the screen we're returning to matches our preference
                    val previousRoute = navController.previousBackStackEntry?.destination?.route

                    if (previousRoute == targetRoute) {
                        // No extra work needed: just pop back to the existing screen
                        navController.popBackStack()
                    } else {
                        // Preference changed: navigate to the new view and reset the stack
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }

                BackHandler(onBack = navigateBack)

                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBack = navigateBack,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}
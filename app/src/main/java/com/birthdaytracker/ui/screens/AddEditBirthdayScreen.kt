package com.birthdaytracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdaytracker.R
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.viewmodel.BirthdayViewModel
import java.time.Instant
//import com.vanpra.composematerialdialogs.datetime.date.datepicker
//import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.MonthDay
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBirthdayScreen(
    viewModel: BirthdayViewModel = hiltViewModel(),
    birthdayId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var birthMonthDay by remember { mutableStateOf(MonthDay.now()) }
    var birthYear by remember { mutableStateOf<Int?>(LocalDate.now().year - 25) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }

    var useManualInput by remember { mutableStateOf(false) }
    var dateText by remember { mutableStateOf("") }
//    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
//    val dateDialogState = rememberMaterialDialogState()

    var loadedBirthday by remember { mutableStateOf<Birthday?>(null) }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Show error/success messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
            onBack()
        }
    }

    LaunchedEffect(birthdayId) {
        birthdayId?.let { id ->
            val b = viewModel.getBirthdayById(id)
            b?.let {
                loadedBirthday = it
                name = it.name
                category = it.category
                birthMonthDay = it.birthMonthDay
                birthYear = it.birthYear
            }
        }
    }

    val isEditMode = birthdayId != null

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(if (isEditMode) R.string.edit_birthday else R.string.add_birthday),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    actions = {
                        if (isEditMode) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                val validationError = viewModel.validateBirthday(name, birthMonthDay, birthYear)
                                if (validationError != null) {
                                    nameError = validationError
                                    return@TextButton
                                }

                                if (isEditMode && loadedBirthday != null) {
                                    viewModel.updateBirthday(
                                        loadedBirthday!!.copy(
                                            name = name.trim(),
                                            category = category.trim(),
                                            birthDay = birthMonthDay.dayOfMonth,
                                            birthMonth = birthMonthDay.monthValue,
                                            birthYear = birthYear
                                        )
                                    )
                                } else {
                                    viewModel.insertBirthday(
                                        Birthday(
                                            name = name.trim(),
                                            category = category.trim(),
                                            birthDay = birthMonthDay.dayOfMonth,
                                            birthMonth = birthMonthDay.monthValue,
                                            birthYear = birthYear
                                        )
                                    )
                                }
                            }
                        ) {
                            Text(stringResource(R.string.save))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = {
                    nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.enter_category)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Family, Friend, Colleague") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                )
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayDate = if (birthYear != null) {
                    LocalDate.of(birthYear!!, birthMonthDay.monthValue, birthMonthDay.dayOfMonth)
                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                } else {
                    birthMonthDay.format(DateTimeFormatter.ofPattern("MMM dd"))
                }
                Text("${stringResource(R.string.select_date)}: $displayDate")
            }

            //new
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = birthYear != null,
                    onCheckedChange = { isChecked ->
                        birthYear = if (isChecked) LocalDate.now().year - 25 else null
                    }
                )
                Text("Include birth year")
            }

            if (birthYear != null) {
                OutlinedTextField(
                    value = birthYear?.toString() ?: "",
                    onValueChange = {
                        birthYear = it.toIntOrNull()
                    },
                    label = { Text("Birth Year") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_birthday)) },
            text = { Text(stringResource(R.string.delete_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        loadedBirthday?.let {
                            viewModel.deleteBirthday(it)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDatePicker) {
        val initialDate = birthMonthDay.atYear(LocalDate.now().year)

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val pickedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        birthMonthDay = MonthDay.of(
                            pickedDate.monthValue,
                            pickedDate.dayOfMonth
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
//    com.vanpra.composematerialdialogs.MaterialDialog(
//        dialogState = dateDialogState,
//        buttons = {
//            positiveButton("Ok") { dateDialogState.hide() }
//            negativeButton(stringResource(R.string.cancel)) { dateDialogState.hide() }
//        }
//    ) {
//        datepicker(
//            initialDate = birthDate,
//            title = stringResource(R.string.select_date),
//            yearRange = (LocalDate.now().year - 150)..LocalDate.now().year,
//            onDateChange = {
//                if (!it.isAfter(LocalDate.now())) {
//                    birthDate = it
//                }
//            }
//        )
//    }
}
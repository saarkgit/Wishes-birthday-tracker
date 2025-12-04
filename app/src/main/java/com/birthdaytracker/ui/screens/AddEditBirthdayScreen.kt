package com.birthdaytracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdaytracker.R
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.ui.components.StableTopBar
import com.birthdaytracker.viewmodel.BirthdayViewModel
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditBirthdayScreen(
    viewModel: BirthdayViewModel = hiltViewModel(),
    birthdayId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(LocalDate.now().minusYears(25)) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val dateDialogState = rememberMaterialDialogState()

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
                birthDate = it.birthDate
            }
        }
    }

    val isEditMode = birthdayId != null

    Scaffold(
        topBar = {
            StableTopBar(
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
                            val validationError = viewModel.validateBirthday(name, birthDate)
                            if (validationError != null) {
                                nameError = validationError
                                return@TextButton
                            }

                            if (isEditMode && loadedBirthday != null) {
                                viewModel.updateBirthday(
                                    loadedBirthday!!.copy(
                                        name = name.trim(),
                                        category = category.trim(),
                                        birthDate = birthDate
                                    )
                                )
                            } else {
                                viewModel.insertBirthday(
                                    Birthday(
                                        name = name.trim(),
                                        category = category.trim(),
                                        birthDate = birthDate
                                    )
                                )
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
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
                }
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text(stringResource(R.string.enter_category)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Family, Friend, Colleague") }
            )

            OutlinedButton(
                onClick = { dateDialogState.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${stringResource(R.string.select_date)}: ${birthDate.format(dateFormatter)}")
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

    com.vanpra.composematerialdialogs.MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton("Ok") { dateDialogState.hide() }
            negativeButton(stringResource(R.string.cancel)) { dateDialogState.hide() }
        }
    ) {
        datepicker(
            initialDate = birthDate,
            title = stringResource(R.string.select_date),
            yearRange = (LocalDate.now().year - 150)..LocalDate.now().year,
            onDateChange = {
                if (!it.isAfter(LocalDate.now())) {
                    birthDate = it
                }
            }
        )
    }
}
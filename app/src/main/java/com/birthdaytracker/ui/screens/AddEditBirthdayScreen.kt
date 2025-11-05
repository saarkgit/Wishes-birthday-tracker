package com.birthdaytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.ui.components.StableTopBar
import com.birthdaytracker.viewmodel.BirthdayViewModel
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditBirthdayScreen(
    viewModel: BirthdayViewModel,
    birthdayId: Long? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(LocalDate.now().minusYears(25)) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val dateDialogState = rememberMaterialDialogState()
    
    var loadedBirthday by remember { mutableStateOf<Birthday?>(null) }
    
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
                title = { Text(if (isEditMode) "Edit Birthday" else "Add Birthday", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Cancel")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = {
                        if (name.isNotBlank()) {
                            if (isEditMode && loadedBirthday != null) {
                                viewModel.updateBirthday(
                                    loadedBirthday!!.copy(name = name, category = category, birthDate = birthDate)
                                )
                            } else {
                                viewModel.insertBirthday(
                                    Birthday(name = name, category = category, birthDate = birthDate)
                                )
                            }
                            onBack()
                        }
                    }) {
                        Text("Save")
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
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Family, Friend, Colleague") }
            )
            
            OutlinedButton(
                onClick = { dateDialogState.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Date: ${birthDate.format(dateFormatter)}")
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Birthday") },
            text = { Text("Are you sure you want to delete this birthday?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        loadedBirthday?.let {
                            viewModel.deleteBirthday(it)
                            onBack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    com.vanpra.composematerialdialogs.MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton("Ok") { dateDialogState.hide() }
            negativeButton("Cancel") { dateDialogState.hide() }
        }
    ) {
        datepicker(
            initialDate = birthDate,
            title = "Select Birthday",
            onDateChange = { birthDate = it }
        )
    }
}


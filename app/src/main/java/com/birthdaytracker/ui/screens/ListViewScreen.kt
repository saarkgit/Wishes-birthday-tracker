package com.birthdaytracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.ui.components.StableTopBar
import com.birthdaytracker.viewmodel.BirthdayViewModel
import com.birthdaytracker.viewmodel.SortOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ListViewScreen(
    viewModel: BirthdayViewModel = hiltViewModel(),
    onAddClick: () -> Unit,
    onBirthdayClick: (Birthday) -> Unit
) {
    val birthdays by viewModel.birthdays.collectAsState(initial = emptyList())
    val sortOption by viewModel.sortOption.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    val sortedBirthdays = remember(birthdays, sortOption) {
        when (sortOption) {
            SortOption.DATE -> birthdays.sortedWith(compareBy { birthday ->
                val today = LocalDate.now()
                val thisYear = birthday.birthDate.withYear(today.year)
                val nextYear = birthday.birthDate.withYear(today.year + 1)
                val upcoming = if (thisYear >= today) thisYear else nextYear
                upcoming.toEpochDay()
            })

            SortOption.NAME -> birthdays.sortedBy { it.name }
            SortOption.CATEGORY -> birthdays.sortedBy { it.category }
        }
    }

    val nextUpcoming = viewModel.getNextUpcomingBirthday(sortedBirthdays)
    val todayBirthday = sortedBirthdays.firstOrNull { viewModel.isToday(it) }

    Scaffold(
        topBar = {
            StableTopBar(
                title = { Text("Birthday Tracker", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Birthday")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Name",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Date",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            items(
                items = sortedBirthdays,
                key = { it.id }
            ) { birthday ->
                val backgroundColor = when {
                    todayBirthday?.id == birthday.id -> Color(0xFF4CAF50) // Green for today
                    nextUpcoming?.id == birthday.id -> Color(0xFFFF9800) // Orange for upcoming
                    else -> MaterialTheme.colorScheme.surface
                }

                BirthdayRow(
                    birthday = birthday,
                    backgroundColor = backgroundColor,
                    onClick = { onBirthdayClick(birthday) }
                )
            }
        }
    }

    if (showSortMenu) {
        AlertDialog(
            onDismissRequest = { showSortMenu = false },
            title = { Text("Sort By") },
            text = {
                Column {
                    SortOption.values().forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setSortOption(option) }
                                .padding(8.dp)
                        ) {
                            RadioButton(
                                selected = sortOption == option,
                                onClick = { viewModel.setSortOption(option) }
                            )
                            Text(
                                text = when (option) {
                                    SortOption.DATE -> "Date"
                                    SortOption.NAME -> "Name"
                                    SortOption.CATEGORY -> "Category"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortMenu = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun BirthdayRow(
    birthday: Birthday,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val thisYear = birthday.birthDate.withYear(today.year)
    val nextYear = birthday.birthDate.withYear(today.year + 1)
    val displayDate =
        if (thisYear >= today) thisYear else nextYear //here is where you set the age for the birthdays
    val age = java.time.Period.between(birthday.birthDate, displayDate).years
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = birthday.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${displayDate.format(formatter)} (Age $age)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = birthday.category.ifEmpty { "-" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


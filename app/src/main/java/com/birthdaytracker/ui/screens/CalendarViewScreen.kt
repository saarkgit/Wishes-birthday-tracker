package com.birthdaytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.viewmodel.BirthdayViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarViewScreen(
    viewModel: BirthdayViewModel = hiltViewModel(),
    onAddClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onBirthdayClick: (Birthday) -> Unit
) {
    val birthdays by viewModel.birthdays.collectAsState(initial = emptyList())
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val days = (1..daysInMonth).map { currentMonth.atDay(it) }

    val nextUpcoming = viewModel.getNextUpcomingBirthday(birthdays)
    val todayBirthday = birthdays.firstOrNull { viewModel.isToday(it) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Birthday")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month navigation header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                IconButton(onClick = {
                    currentMonth = currentMonth.minusMonths(1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }
                Text(
                    text = currentMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    currentMonth = currentMonth.plusMonths(1)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }

            // Day headers
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")) { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Calendar days
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(firstDayOfWeek) {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                items(
                    items = days,
                    key = { it.toString() }
                ) { date ->
                    val dayBirthdays = birthdays.filter { birthday ->
                        birthday.birthMonthDay.month == date.month &&
                                birthday.birthMonthDay.dayOfMonth == date.dayOfMonth
                    }
                    val isToday = date == LocalDate.now()
                    val hasBirthday = dayBirthdays.isNotEmpty()
                    val isUpcoming = dayBirthdays.any { nextUpcoming?.id == it.id }
                    val isTodayBirthday = dayBirthdays.any { todayBirthday?.id == it.id }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                selectedDate = if (hasBirthday) date else null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .background(
                                        color = when {
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            )
                            if (hasBirthday) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = when {
                                                isTodayBirthday -> Color(0xFF4CAF50)
                                                isUpcoming -> Color(0xFFFF9800)
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // Birthday details for selected date or month
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedDate != null) {
                    val selectedBirthdays = birthdays.filter {
                        it.birthMonthDay.month == selectedDate!!.month &&
                                it.birthMonthDay.dayOfMonth == selectedDate!!.dayOfMonth
                    }

                    item {
                        Text(
                            text = "Birthdays on ${
                                selectedDate!!.format(
                                    DateTimeFormatter.ofPattern(
                                        "MMMM d"
                                    )
                                )
                            }:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(selectedBirthdays) { birthday ->
                        EnlargedBirthdayCard(
                            birthday = birthday,
                            onClick = { onBirthdayClick(birthday) }
                        )
                    }

                    item {
                        TextButton(onClick = { selectedDate = null }) {
                            Text("Show all birthdays this month")
                        }
                    }
                } else {
                    val monthBirthdays = birthdays
                        .filter { it.birthMonthDay.month == currentMonth.month }
                        .sortedBy { it.birthMonthDay.dayOfMonth }

                    if (monthBirthdays.isNotEmpty()) {
                        item {
                            Text(
                                text = "Birthdays this month:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(monthBirthdays) { birthday ->
                            CalendarBirthdayItem(
                                birthday = birthday,
                                isToday = viewModel.isToday(birthday),
                                isUpcoming = nextUpcoming?.id == birthday.id,
                                onClick = { onBirthdayClick(birthday) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnlargedBirthdayCard(
    birthday: Birthday,
    onClick: () -> Unit
) {
    val today = LocalDate.now()

    // Calculate current age (how old they are TODAY)
    val currentAge = if (birthday.birthYear != null) {
        val today = LocalDate.now()
        java.time.Period.between(birthday.birthDate, today).years
    } else {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = birthday.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (currentAge != null) {
                Text("Age: $currentAge", style = MaterialTheme.typography.titleLarge)
            }
//            else {
//                Text("Year unknown", style = MaterialTheme.typography.bodySmall)
//            }
            if (birthday.category.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = birthday.category,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CalendarBirthdayItem(
    birthday: Birthday,
    isToday: Boolean,
    isUpcoming: Boolean,
    onClick: () -> Unit
) {
    val today = LocalDate.now()

    // Calculate current age (how old they are TODAY)
    val currentAge = if (birthday.birthYear != null) {
        val today = LocalDate.now()
        java.time.Period.between(birthday.birthDate, today).years
    } else {
        null
    }

    val backgroundColor = when {
        isToday -> Color(0xFF4CAF50).copy(alpha = 0.3f)
        isUpcoming -> Color(0xFFFF9800).copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = birthday.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (birthday.category.isNotEmpty()) {
                    Text(
                        text = birthday.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${birthday.birthMonthDay.month.name.take(3)} ${birthday.birthMonthDay.dayOfMonth}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (currentAge != null) {
                    Text(
                        text = "Age $currentAge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
//                else {
//                    Text("Year unknown", style = MaterialTheme.typography.bodySmall)
//                }
            }
        }
    }
}
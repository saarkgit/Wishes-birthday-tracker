package com.birthdaytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.ui.components.StableTopBar
import com.birthdaytracker.viewmodel.BirthdayViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@Composable
fun CalendarViewScreen(
    viewModel: BirthdayViewModel,
    onAddClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onBirthdayClick: (Birthday) -> Unit
) {
    val birthdays by viewModel.birthdays.collectAsState(initial = emptyList())
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    
    val days = (1..daysInMonth).map { currentMonth.atDay(it) }
    
    val nextUpcoming = viewModel.getNextUpcomingBirthday(birthdays)
    val todayBirthday = birthdays.firstOrNull { viewModel.isToday(it) }
    
    Scaffold(
        topBar = {
            StableTopBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = {
                            currentMonth = currentMonth.minusMonths(1)
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
                        }
                        Text(
                            text = currentMonth.format(monthFormatter),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            currentMonth = currentMonth.plusMonths(1)
                        }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Birthday")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                // Empty cells for days before month starts
                items(firstDayOfWeek) {
                    Spacer(modifier = Modifier.size(48.dp))
                }
                
                items(
                    items = days,
                    key = { it.toString() }
                ) { date ->
                    val dayBirthdays = birthdays.filter { birthday ->
                        birthday.birthDate.month == date.month &&
                        birthday.birthDate.dayOfMonth == date.dayOfMonth
                    }
                    val isToday = date == LocalDate.now()
                    val hasBirthday = dayBirthdays.isNotEmpty()
                    val isUpcoming = dayBirthdays.any { nextUpcoming?.id == it.id }
                    val isTodayBirthday = dayBirthdays.any { todayBirthday?.id == it.id }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onDateClick(date) },
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
                                                isTodayBirthday -> Color(0xFF4CAF50) // Green
                                                isUpcoming -> Color(0xFFFF9800) // Orange
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
            
            // Birthday list for selected month
            if (days.any { day ->
                birthdays.any { it.birthDate.month == day.month && it.birthDate.dayOfMonth == day.dayOfMonth }
            }) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Birthdays this month:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                birthdays
                    .filter { it.birthDate.month == currentMonth.month }
                    .sortedBy { it.birthDate.dayOfMonth }
                    .forEach { birthday ->
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

@Composable
fun CalendarBirthdayItem(
    birthday: Birthday,
    isToday: Boolean,
    isUpcoming: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isToday -> Color(0xFF4CAF50).copy(alpha = 0.3f) // Green
        isUpcoming -> Color(0xFFFF9800).copy(alpha = 0.3f) // Orange
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
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
            Column {
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
            Text(
                text = "${birthday.birthDate.month.name.take(3)} ${birthday.birthDate.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


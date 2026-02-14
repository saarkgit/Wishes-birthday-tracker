package com.birthdaytracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.birthdaytracker.data.Birthday
import com.birthdaytracker.repository.BirthdayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DateTimeException
import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class SortOption {
    DATE, NAME, CATEGORY
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

@HiltViewModel
class BirthdayViewModel @Inject constructor(
    private val repository: BirthdayRepository
) : ViewModel() {

    val birthdays = repository.getAllBirthdays()
        .catch { e ->
            _errorMessage.value = "Failed to load birthdays: ${e.message}"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _sortOption = MutableStateFlow(SortOption.DATE)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    suspend fun getBirthdayById(id: Long): Birthday? {
        return try {
            repository.getBirthdayById(id)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load birthday: ${e.message}"
            null
        }
    }

    fun insertBirthday(birthday: Birthday) {
        viewModelScope.launch {
            try {
                repository.insertBirthday(birthday)
                _successMessage.value = "Birthday added successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add birthday: ${e.message}"
            }
        }
    }

    fun updateBirthday(birthday: Birthday) {
        viewModelScope.launch {
            try {
                repository.updateBirthday(birthday)
                _successMessage.value = "Birthday updated successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update birthday: ${e.message}"
            }
        }
    }

    fun deleteBirthday(birthday: Birthday) {
        viewModelScope.launch {
            try {
                repository.deleteBirthday(birthday)
                _successMessage.value = "Birthday deleted successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete birthday: ${e.message}"
            }
        }
    }

    fun validateBirthday(name: String, birthMonthDay: MonthDay, birthYear: Int?): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 100 -> "Name must be less than 100 characters"
            birthYear != null && birthYear > LocalDate.now().year -> "Birth year cannot be in the future"
            birthYear != null && birthYear < LocalDate.now().year - 150 -> "Birth year seems invalid"
            else -> null
        }
    }

    fun getNextUpcomingBirthday(birthdays: List<Birthday>): Birthday? {
        val today = LocalDate.now()
        return birthdays
            .filter { birthday ->
                val thisYear = try {
                    birthday.birthMonthDay.atYear(today.year)
                } catch (e: DateTimeException) {
                    LocalDate.of(today.year, 2, 28)
                }
                val nextYear = try {
                    birthday.birthMonthDay.atYear(today.year + 1)
                } catch (e: DateTimeException) {
                    LocalDate.of(today.year + 1, 2, 28)
                }
                val upcoming = if (thisYear >= today) thisYear else nextYear
                upcoming >= today
            }
            .minByOrNull { birthday ->
                val thisYear = try {
                    birthday.birthMonthDay.atYear(today.year)
                } catch (e: DateTimeException) {
                    LocalDate.of(today.year, 2, 28)
                }
                val nextYear = try {
                    birthday.birthMonthDay.atYear(today.year + 1)
                } catch (e: DateTimeException) {
                    LocalDate.of(today.year + 1, 2, 28)
                }
                val upcoming = if (thisYear >= today) thisYear else nextYear
                ChronoUnit.DAYS.between(today, upcoming)
            }
    }

    fun isToday(birthday: Birthday): Boolean {
        val today = LocalDate.now()
        return birthday.birthMonthDay.month == today.month &&
                birthday.birthMonthDay.dayOfMonth == today.dayOfMonth
    }
}
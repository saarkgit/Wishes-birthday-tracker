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
import java.time.LocalDate
import java.time.Period
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
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

    fun validateBirthday(name: String, birthDate: LocalDate): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 100 -> "Name must be less than 100 characters"
            birthDate.isAfter(LocalDate.now()) -> "Birth date cannot be in the future"
            birthDate.isBefore(LocalDate.now().minusYears(150)) -> "Birth date seems invalid"
            else -> null
        }
    }

    fun getNextUpcomingBirthday(birthdays: List<Birthday>): Birthday? {
        val today = LocalDate.now()
        return birthdays
            .filter { birthday ->
                val thisYear = birthday.birthDate.withYear(today.year)
                val nextYear = birthday.birthDate.withYear(today.year + 1)
                val upcoming = if (thisYear >= today) thisYear else nextYear
                upcoming >= today
            }
            .minByOrNull { birthday ->
                val thisYear = birthday.birthDate.withYear(today.year)
                val nextYear = birthday.birthDate.withYear(today.year + 1)
                val upcoming = if (thisYear >= today) thisYear else nextYear
                Period.between(today, upcoming).days
            }
    }

    fun isToday(birthday: Birthday): Boolean {
        val today = LocalDate.now()
        return birthday.birthDate.month == today.month &&
                birthday.birthDate.dayOfMonth == today.dayOfMonth
    }
}